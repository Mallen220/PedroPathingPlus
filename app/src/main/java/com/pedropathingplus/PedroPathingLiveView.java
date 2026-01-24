package com.pedropathingplus;

import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.Pose;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * A utility class to send real-time robot pose telemetry to the Pedro Pathing Visualizer.
 * It runs a TCP server on port 8888 and broadcasts the robot's pose as JSON.
 * <p>
 * This class uses the Singleton pattern to ensure the server persists across OpModes.
 * <p>
 * <h2>Usage Instructions:</h2>
 * <ol>
 *   <li><strong>Start:</strong> Call {@link #start()} in your OpMode's {@code init()} (it is safe to call multiple times).</li>
 *   <li><strong>Set Follower:</strong> Call {@link #setFollower(Follower)} in {@code init()} to link the current OpMode's follower.</li>
 *   <li><strong>Cleanup:</strong> Call {@link #disable()} in your OpMode's {@code stop()} to clear the reference and prevent memory leaks.</li>
 * </ol>
 * 
 * <pre>{@code
 * public class MyAuto extends LinearOpMode {
 *     private Follower follower;
 *
 *     @Override
 *     public void runOpMode() {
 *         follower = new Follower(...);
 *         
 *         // Start server (if not running) and link follower
 *         PedroPathingLiveView.getInstance().start();
 *         PedroPathingLiveView.getInstance().setFollower(follower);
 *         
 *         waitForStart();
 *         // ... run pathing ...
 *         
 *         // Clean up reference when done
 *         PedroPathingLiveView.getInstance().disable();
 *     }
 * }
 * }</pre>
 */
public class PedroPathingLiveView {
    private static final PedroPathingLiveView INSTANCE = new PedroPathingLiveView();
    
    private final AtomicReference<Supplier<Pose>> poseProvider = new AtomicReference<>();
    private Thread serverThread;
    private ServerSocket serverSocket;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private static final int PORT = 8888;
    private static final int UPDATE_INTERVAL_MS = 50;

    /**
     * Private constructor for Singleton.
     */
    private PedroPathingLiveView() {}

    /**
     * @return The singleton instance of PedroPathingLiveView.
     */
    public static PedroPathingLiveView getInstance() {
        return INSTANCE;
    }

    /**
     * Sets the Follower instance to track. 
     * call this in your OpMode init.
     * @param follower The follower to track.
     */
    public void setFollower(Follower follower) {
        if (follower != null) {
            this.poseProvider.set(follower::getPose);
        } else {
            this.poseProvider.set(null);
        }
    }

    /**
     * Sets a custom pose provider.
     * @param poseProvider A supplier for the current robot pose.
     */
    public void setPoseProvider(Supplier<Pose> poseProvider) {
        this.poseProvider.set(poseProvider);
    }

    /**
     * Disables telemetry by clearing the pose provider. 
     * Call this in your OpMode stop() to prevent accessing closed hardware.
     */
    public void disable() {
        this.poseProvider.set(null);
    }

    /**
     * Starts the telemetry server in a background thread if it is not already running.
     * This method is idempotent.
     */
    public synchronized void start() {
        if (running.get()) return;
        running.set(true);
        serverThread = new Thread(this::serverLoop);
        serverThread.setDaemon(true); // Ensure it doesn't prevent JVM shutdown
        serverThread.start();
    }

    /**
     * Stops the telemetry server and closes connections.
     * Typically not needed if you want the server to persist across OpModes.
     */
    public synchronized void stop() {
        if (!running.get()) return;
        running.set(false);
        
        // Close server socket to interrupt accept()
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        if (serverThread != null) {
            serverThread.interrupt();
        }
    }

    private void serverLoop() {
        try {
            serverSocket = new ServerSocket(PORT);
            while (running.get()) {
                try {
                    Socket client = serverSocket.accept();
                    new Thread(() -> handleClient(client)).start();
                } catch (IOException e) {
                    if (running.get()) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            if (running.get()) {
                e.printStackTrace();
            }
        }
    }

    private void handleClient(Socket client) {
        try (OutputStream out = client.getOutputStream();
             PrintWriter writer = new PrintWriter(out, true)) {
            
            while (running.get() && !client.isClosed() && client.isConnected()) {
                Supplier<Pose> provider = poseProvider.get();
                if (provider != null) {
                    try {
                        Pose pose = provider.get();
                        if (pose != null) {
                            String json = String.format(Locale.US, 
                                "{\"x\":%.4f, \"y\":%.4f, \"heading\":%.4f}", 
                                pose.getX(), pose.getY(), pose.getHeading());
                            writer.println(json);
                        }
                    } catch (Exception e) {
                        // Handle potential exceptions from accessing closed hardware in provider
                        // e.g. if user forgot to call disable()
                        writer.println("{\"error\": \"provider_error\"}");
                    }
                }
                
                if (writer.checkError()) {
                    break;
                }

                try {
                    Thread.sleep(UPDATE_INTERVAL_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        } catch (IOException e) {
            // Client connection error
        } finally {
            try { client.close(); } catch (IOException ignored) {}
        }
    }
}

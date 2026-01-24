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
import java.util.function.Supplier;

/**
 * A utility class to send real-time robot pose telemetry to the Pedro Pathing Visualizer.
 * It runs a TCP server on port 8888 and broadcasts the robot's pose as JSON.
 */
public class PedroPathingLiveView {
    private final Supplier<Pose> poseProvider;
    private Thread serverThread;
    private ServerSocket serverSocket;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private static final int PORT = 8888;
    private static final int UPDATE_INTERVAL_MS = 50;

    /**
     * Constructs the live view with a Follower instance.
     * @param follower The follower to track.
     */
    public PedroPathingLiveView(Follower follower) {
        this(follower::getPose);
    }

    /**
     * Constructs the live view with a custom pose provider.
     * @param poseProvider A supplier for the current robot pose.
     */
    public PedroPathingLiveView(Supplier<Pose> poseProvider) {
        this.poseProvider = poseProvider;
    }

    /**
     * Starts the telemetry server in a background thread.
     */
    public void start() {
        if (running.get()) return;
        running.set(true);
        serverThread = new Thread(this::serverLoop);
        serverThread.setDaemon(true); // Ensure it doesn't prevent JVM shutdown
        serverThread.start();
    }

    /**
     * Stops the telemetry server and closes connections.
     */
    public void stop() {
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
                    // Handle client in the same thread (blocking others) or separate?
                    // Usually for a visualizer, we might have one connection.
                    // Or we spawn a thread per client.
                    // Given the simple requirement, handling one client at a time in a loop
                    // or spawning a thread is better.
                    // Let's spawn a thread so we can accept reconnects or multiple visualizers.
                    new Thread(() -> handleClient(client)).start();
                } catch (IOException e) {
                    // Socket closed or error
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
                Pose pose = poseProvider.get();
                if (pose != null) {
                    // JSON format: {"x": 10.0, "y": 20.0, "heading": 1.57}
                    String json = String.format(Locale.US,
                        "{\"x\":%.4f, \"y\":%.4f, \"heading\":%.4f}",
                        pose.getX(), pose.getY(), pose.getHeading());
                    writer.println(json);
                }

                if (writer.checkError()) {
                    // Error writing (client disconnected)
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

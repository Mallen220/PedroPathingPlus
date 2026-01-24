package com.pedropathingplus;

import com.pedropathing.geometry.Pose;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.Assert.*;

public class PedroPathingLiveViewTest {

    private PedroPathingLiveView liveView;
    private AtomicReference<Pose> currentPose;

    @Before
    public void setUp() {
        currentPose = new AtomicReference<>(new Pose(0, 0, 0));
        liveView = new PedroPathingLiveView(currentPose::get);
    }

    @After
    public void tearDown() {
        if (liveView != null) {
            liveView.stop();
        }
    }

    @Test
    public void testTelemetryData() throws Exception {
        liveView.start();

        // Wait a bit for server to start
        Thread.sleep(500);

        try (Socket socket = new Socket("localhost", 8888);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Read first line
            String line = reader.readLine();
            assertNotNull("Should receive data", line);
            // Check for format {"x":0.0000, "y":0.0000, "heading":0.0000}
            assertTrue("Should contain x", line.contains("\"x\":0.0000"));
            assertTrue("Should contain y", line.contains("\"y\":0.0000"));

            // Update pose
            currentPose.set(new Pose(10.5, 20.123, 1.57));

            // Read subsequent lines until we see the new pose
            boolean found = false;
            for (int i = 0; i < 20; i++) { // Try for ~1 second (assuming 50ms interval)
                line = reader.readLine();
                if (line != null && line.contains("\"x\":10.5000") && line.contains("\"y\":20.1230")) {
                    found = true;
                    break;
                }
            }
            assertTrue("Should receive updated pose", found);
        }
    }
}

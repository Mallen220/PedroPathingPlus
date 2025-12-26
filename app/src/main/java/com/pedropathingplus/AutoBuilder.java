package com.pedropathingplus;

import android.content.Context;
import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.BezierCurve;
import com.pedropathing.geometry.BezierLine;
import com.pedropathing.geometry.Point;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.Path;
import com.pedropathing.paths.PathBuilder;
import com.pedropathing.paths.PathChain;
import com.pedropathingplus.pathing.NamedCommands;
import com.pedropathingplus.pathing.ProgressTracker;
import com.pedropathingplus.pathing.command.Command;
import com.pedropathingplus.pathing.command.FollowPathCommand;
import com.pedropathingplus.pathing.command.InstantCommand;
import com.pedropathingplus.pathing.command.ParallelDeadlineGroup;
import com.pedropathingplus.pathing.command.SequentialCommandGroup;
import com.pedropathingplus.pathing.command.WaitCommand;
import com.pedropathingplus.pathing.command.WaitUntilCommand;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.firstinspires.ftc.robotcore.external.Telemetry;

public class AutoBuilder {

    /**
     * Builds a Command group from a .pp file.
     *
     * @param filename Name of the .pp file (without extension or with).
     * @param follower The Follower instance.
     * @param telemetry Telemetry instance.
     * @param context Android Context (usually hardwareMap.appContext).
     * @return A SequentialCommandGroup containing the auto.
     */
    public static Command buildAuto(String filename, Follower follower, Telemetry telemetry, Context context) {
        try {
            PedroPathReader reader = new PedroPathReader(filename, context);
            ProgressTracker tracker = new ProgressTracker(follower, telemetry);
            return build(reader, follower, tracker);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load AutoPaths/" + filename, e);
        }
    }

    private static Command build(PedroPathReader reader, Follower follower, ProgressTracker tracker) {
        SequentialCommandGroup mainGroup = new SequentialCommandGroup();

        // 1. Set Starting Pose
        Pose startPose = reader.get("startPoint");
        if (startPose != null) {
            mainGroup.addCommands(new InstantCommand(() -> follower.setStartingPose(startPose)));
        }

        Map<String, PedroPP.Line> lineMap = new HashMap<>();
        if (reader.file.lines != null) {
            for (PedroPP.Line line : reader.file.lines) {
                if (line.id != null) {
                    lineMap.put(line.id, line);
                }
            }
        }

        Pose currentPose = startPose;
        double currentHeading = startPose.getHeading();

        PathBuilder currentChainBuilder = follower.pathBuilder();
        List<Command> currentChainListeners = new ArrayList<>();
        boolean buildingChain = false;
        int currentChainPathIndex = 0;

        if (reader.file.sequence != null) {
            for (PedroPP.SequenceItem item : reader.file.sequence) {
                if ("path".equals(item.kind)) {
                    PedroPP.Line line = lineMap.get(item.lineId);
                    if (line != null) {
                        String cleanName = line.name.replace(" ", "");
                        Pose endPose = reader.get(cleanName);

                        // Check for Wait Before
                        if (line.waitBeforeMs > 0) {
                            // Break chain
                            if (buildingChain) {
                                finishChain(mainGroup, currentChainBuilder, follower, tracker, currentChainListeners);
                                currentChainBuilder = follower.pathBuilder(); // Reset
                                currentChainListeners = new ArrayList<>();
                                buildingChain = false;
                                currentChainPathIndex = 0;
                            }
                            mainGroup.addCommands(new WaitCommand(line.waitBeforeMs));
                        }

                        // Build Path Segment
                        Path path;
                        if (line.controlPoints != null && !line.controlPoints.isEmpty()) {
                            Point[] points = new Point[line.controlPoints.size() + 2];
                            points[0] = new Point(currentPose.getX(), currentPose.getY());
                            for(int i=0; i<line.controlPoints.size(); i++) {
                                points[i+1] = PedroPathReader.toPoint(line.controlPoints.get(i).x, line.controlPoints.get(i).y);
                            }
                            points[points.length-1] = new Point(endPose.getX(), endPose.getY());
                            path = new BezierCurve(points);
                        } else {
                            path = new BezierLine(
                                new Point(currentPose.getX(), currentPose.getY()),
                                new Point(endPose.getX(), endPose.getY())
                            );
                        }
                        path.setPathEndTimeout(0);

                        // Add to Builder
                        currentChainBuilder.addPath(path);
                        buildingChain = true;

                        String headingMode = line.endPoint.heading;
                        if ("linear".equals(headingMode)) {
                            currentChainBuilder.setLinearHeadingInterpolation(currentHeading, endPose.getHeading());
                        } else if ("tangential".equals(headingMode)) {
                            currentChainBuilder.setTangentHeadingInterpolation();
                        } else {
                             currentChainBuilder.setConstantHeadingInterpolation(endPose.getHeading());
                        }

                        // Events
                        final int segmentIndex = currentChainPathIndex;

                        // Add meta-event to update tracker name
                        currentChainListeners.add(new SequentialCommandGroup(
                            new WaitUntilCommand(() -> follower.getChainIndex() == segmentIndex),
                            new InstantCommand(() -> {
                                tracker.setCurrentPathName(cleanName);
                            })
                        ));

                        if (line.eventMarkers != null) {
                            for (PedroPP.EventMarker marker : line.eventMarkers) {
                                // Trigger: Chain Index matches AND T-Value >= position
                                currentChainListeners.add(new SequentialCommandGroup(
                                    new WaitUntilCommand(() ->
                                        follower.getChainIndex() == segmentIndex &&
                                        follower.getCurrentTValue() >= marker.position
                                    ),
                                    new InstantCommand(() -> {
                                        if (NamedCommands.hasCommand(marker.name)) {
                                            NamedCommands.getCommand(marker.name).schedule();
                                        }
                                    })
                                ));
                            }
                        }

                        // Update State
                        currentPose = endPose;
                        if ("tangential".equals(headingMode)) {
                            currentHeading = endPose.getHeading();
                        } else {
                            currentHeading = endPose.getHeading();
                        }

                        currentChainPathIndex++;

                        // Check for Wait After
                        if (line.waitAfterMs > 0) {
                             if (buildingChain) {
                                finishChain(mainGroup, currentChainBuilder, follower, tracker, currentChainListeners);
                                currentChainBuilder = follower.pathBuilder();
                                currentChainListeners = new ArrayList<>();
                                buildingChain = false;
                                currentChainPathIndex = 0;
                            }
                            mainGroup.addCommands(new WaitCommand(line.waitAfterMs));
                        }
                    }
                }
            }
        }

        // Finish remaining chain
        if (buildingChain) {
             finishChain(mainGroup, currentChainBuilder, follower, tracker, currentChainListeners);
        }

        return mainGroup;
    }

    private static void finishChain(SequentialCommandGroup group, PathBuilder builder, Follower follower, ProgressTracker tracker, List<Command> listeners) {
        try {
            PathChain chain = builder.build();
            // Enable holdEnd=true to prevent drifting after chain completion
            Command pathCommand = new FollowPathCommand(follower, chain, true);

            Command initTracker = new InstantCommand(() -> {
                tracker.setCurrentChain(chain);
            });

            if (!listeners.isEmpty()) {
                group.addCommands(initTracker);

                Command[] others = listeners.toArray(new Command[0]);
                group.addCommands(new ParallelDeadlineGroup(pathCommand, others));
            } else {
                group.addCommands(initTracker, pathCommand);
            }
        } catch (Exception e) {
            // PathBuilder might throw if empty
        }
    }
}

package com.pedropathingplus;

import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.BezierCurve;
import com.pedropathing.geometry.Point;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.Path;
import com.pedropathing.paths.PathChain;
import com.pedropathingplus.command.FollowPathCommand;
import com.pedropathingplus.command.InstantCommand;
import com.pedropathingplus.command.ParallelCommandGroup;
import com.pedropathingplus.command.SequentialCommandGroup;
import com.pedropathingplus.command.WaitCommand;
import com.pedropathingplus.command.WaitUntilCommand;
import com.pedropathingplus.pathing.NamedCommands;
import com.pedropathingplus.pathing.ProgressTracker;
import com.qualcomm.robotcore.hardware.HardwareMap;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.firstinspires.ftc.robotcore.external.Telemetry;

public class PedroPathingPlusAuto extends SequentialCommandGroup {

  private final Follower follower;
  private final ProgressTracker progressTracker;
  private final PedroPathReader reader;

  public PedroPathingPlusAuto(
      Follower follower, String filename, HardwareMap hw, Telemetry telemetry) throws IOException {
    this.follower = follower;
    this.progressTracker = new ProgressTracker(follower, telemetry);
    this.reader = new PedroPathReader(filename, hw.appContext);

    buildCommands();
  }

  private void buildCommands() {
    Pose currentStart = reader.getStartPose();
    if (currentStart == null) {
        throw new RuntimeException("Start Point not found in PP file.");
    }

    follower.setStartingPose(currentStart);

    ArrayList<Path> pendingPaths = new ArrayList<>();
    List<EventRegistration> pendingEvents = new ArrayList<>();

    List<PedroPP.Line> lines = reader.getLines();
    if (lines == null) return;

    for (int i = 0; i < lines.size(); i++) {
      PedroPP.Line line = lines.get(i);

      // Handle waitBefore
      if (line.waitBeforeMs > 0 || (line.waitBeforeName != null && !line.waitBeforeName.isEmpty())) {
          flushChain(pendingPaths, pendingEvents);
          addWait(line.waitBeforeMs, line.waitBeforeName);
      }

      // Build Path
      // Get the calculated end pose from reader (which handles heading logic)
      String cleanName = line.name.replace(" ", "");
      Pose endPose = reader.get(cleanName);
      if (endPose == null) {
          // Fallback if not found (shouldn't happen if reader works)
           endPose = PedroPathReader.toPose(line.endPoint.x, line.endPoint.y, 0);
      }

      // Convert control points
      List<Point> points = new ArrayList<>();
      points.add(new Point(currentStart.getX(), currentStart.getY(), Point.CARTESIAN));
      if (line.controlPoints != null) {
          for (PedroPP.Point cp : line.controlPoints) {
              Pose cpPose = PedroPathReader.toPose(cp);
              points.add(new Point(cpPose.getX(), cpPose.getY(), Point.CARTESIAN));
          }
      }
      points.add(new Point(endPose.getX(), endPose.getY(), Point.CARTESIAN));

      BezierCurve curve = new BezierCurve(points.toArray(new Point[0]));
      Path path = new Path(curve);

      // Apply Heading Interpolation
      String headingMode = line.endPoint.heading;
      if (headingMode == null) headingMode = "tangential"; // Default

      switch (headingMode) {
          case "linear":
              path.setLinearHeadingInterpolation(currentStart.getHeading(), endPose.getHeading());
              break;
          case "constant":
              path.setConstantHeadingInterpolation(endPose.getHeading());
              break;
          case "tangential":
          default:
              path.setTangentHeadingInterpolation();
              break;
      }

      pendingPaths.add(path);

      // Collect Events
      if (line.eventMarkers != null) {
          for (PedroPP.EventMarker marker : line.eventMarkers) {
              // The lineIndex in pending chain is pendingPaths.size() - 1
              int chainLineIndex = pendingPaths.size() - 1;
              pendingEvents.add(new EventRegistration(marker.id, marker.name, marker.position, chainLineIndex));
          }
      }

      currentStart = endPose;

      // Handle waitAfter
      if (line.waitAfterMs > 0 || (line.waitAfterName != null && !line.waitAfterName.isEmpty())) {
          flushChain(pendingPaths, pendingEvents);
          addWait(line.waitAfterMs, line.waitAfterName);
      }
    }

    // Final flush
    flushChain(pendingPaths, pendingEvents);
  }

  private void flushChain(ArrayList<Path> pendingPaths, List<EventRegistration> pendingEvents) {
      if (pendingPaths.isEmpty()) return;

      // PathChain constructor requires ArrayList<Path>
      // We create a copy to ensure the chain owns its list and it's independent of the pending list clearing
      PathChain chain = new PathChain(new ArrayList<>(pendingPaths));

      // 1. Set Chain Info Command
      addCommands(new InstantCommand(() -> {
          progressTracker.setCurrentChain(chain);
          progressTracker.setCurrentPathName("AutoSegment"); // Could be better named
      }));

      // 2. Follow Command (wrapped with events if needed)
      FollowPathCommand followCmd = new FollowPathCommand(follower, chain);

      if (pendingEvents.isEmpty()) {
          addCommands(followCmd);
      } else {
          // Register events
          addCommands(new InstantCommand(() -> {
              for (EventRegistration ev : pendingEvents) {
                  progressTracker.registerEvent(ev.id, ev.commandName, ev.position, ev.lineIndex);
              }
          }));

          // Create Parallel Group
          // It runs FollowPath AND (WaitEvent -> ExecuteEvent) for each event
          List<Object> parallelCommands = new ArrayList<>();
          parallelCommands.add(followCmd);

          for (EventRegistration ev : pendingEvents) {
              parallelCommands.add(new SequentialCommandGroup(
                  // Wait until event triggers OR path follows finishes (to avoid hanging)
                  new WaitUntilCommand(() -> progressTracker.shouldTriggerEvent(ev.id) || !follower.isBusy()),
                  new InstantCommand(() -> {
                      // Only execute if the event condition is actually met
                      if (progressTracker.shouldTriggerEvent(ev.id)) {
                          progressTracker.executeEvent(ev.id);
                      }
                  })
              ));
          }

          // ParallelCommandGroup takes varargs
          com.pedropathingplus.command.Command[] cmdArray = new com.pedropathingplus.command.Command[parallelCommands.size()];
          for(int i=0; i<parallelCommands.size(); i++) {
              cmdArray[i] = (com.pedropathingplus.command.Command) parallelCommands.get(i);
          }

          addCommands(new ParallelCommandGroup(cmdArray));
      }

      pendingPaths.clear();
      pendingEvents.clear();
  }

  private void addWait(long ms, String name) {
      if (ms > 0) {
          addCommands(new WaitCommand(ms));
      }
      if (name != null && !name.isEmpty()) {
          if (NamedCommands.hasCommand(name)) {
              addCommands(NamedCommands.getCommand(name));
          }
      }
  }

  private static class EventRegistration {
      String id;
      String commandName;
      double position;
      int lineIndex;

      EventRegistration(String id, String commandName, double position, int lineIndex) {
          this.id = id;
          this.commandName = commandName;
          this.position = position;
          this.lineIndex = lineIndex;
      }
  }
}

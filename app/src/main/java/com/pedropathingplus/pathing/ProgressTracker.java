package com.pedropathingplus.pathing;

import com.pedropathing.follower.Follower;
import com.pedropathing.paths.Path;
import com.pedropathing.paths.PathChain;
import java.util.HashMap;
import java.util.Map;
import org.firstinspires.ftc.robotcore.external.Telemetry;

public class ProgressTracker {
  private final Follower follower;
  private PathChain currentChain;
  private final Map<String, Double> eventPositions = new HashMap<>();
  private final Map<String, Boolean> eventTriggered = new HashMap<>();
  private Telemetry telemetry;
  private String currentPathName = "";
  private double chainProgress = 0.0;
  private double pathProgress = 0.0;

  // Turn tracking
  private boolean isTrackingTurn = false;
  private double startHeading;
  private double targetHeading;
  private double totalTurnRadians;

  public ProgressTracker(Follower follower, Telemetry telemetry) {
    this.follower = follower;
    this.telemetry = telemetry;
  }

  public void setCurrentChain(PathChain chain) {
    this.currentChain = chain;
    clearEvents();
    if (telemetry != null) {
      telemetry.addData("ProgressTracker", "Set new chain");
      telemetry.addData("Chain Size", chain.size());
      telemetry.addData("Current Index", follower.getChainIndex());
    }
  }

  public void setCurrentPathName(String name) {
    this.currentPathName = name;
    if (telemetry != null) {
      telemetry.addData("Current Path", name);
    }
  }

  public void registerEvent(String eventName, double position) {
    eventPositions.put(eventName, position);
    eventTriggered.put(eventName, false);
    if (telemetry != null) {
      telemetry.addData("Event Registered", eventName + " @ " + position);
      telemetry.update();
    }
  }

  public void clearEvents() {
    eventPositions.clear();
    eventTriggered.clear();
    if (telemetry != null) {
      telemetry.addData("ProgressTracker", "Events cleared");
    }
  }

  public void executeEvent(String eventName) {
    if (!eventTriggered.getOrDefault(eventName, true)) {
      eventTriggered.put(eventName, true);
      if (telemetry != null) {
        telemetry.addLine("EVENT TRIGGERED: " + eventName);
        telemetry.update();
      }
      // Execute the named command if it exists
      if (NamedCommands.hasCommand(eventName)) {
        NamedCommands.getCommand(eventName).schedule();
      }
    }
  }

  public boolean isEventTriggered(String eventName) {
    return eventTriggered.getOrDefault(eventName, false);
  }

  public boolean shouldTriggerEvent(String eventName) {
    if (!eventPositions.containsKey(eventName) || isEventTriggered(eventName)) {
      return false;
    }

    updateProgress();
    double eventPosition = eventPositions.get(eventName);

    if (telemetry != null) {
      telemetry.addData("Event Check", eventName);
      telemetry.addData("Event Position", eventPosition);
      telemetry.addData("Current Progress", pathProgress);
      telemetry.addData("Should Trigger", pathProgress >= eventPosition);
      telemetry.update();
    }

    return pathProgress >= eventPosition;
  }

  /**
   * Turns to a specific angle and registers an event to be triggered at a certain percentage of the
   * turn.
   *
   * @param radians The target angle in radians
   * @param eventName The name of the event to trigger
   * @param eventThreshold The percentage (0-1) of the turn at which to trigger the event
   */
  public void turn(double radians, String eventName, double eventThreshold) {
    follower.turnTo(radians);
    startHeading = follower.getPose().getHeading();
    targetHeading = radians;
    totalTurnRadians = Math.abs(getSmallestAngleDifference(targetHeading, startHeading));
    isTrackingTurn = true;
    clearEvents();
    registerEvent(eventName, eventThreshold);
  }

  private double getSmallestAngleDifference(double angle1, double angle2) {
    double diff = angle1 - angle2;
    while (diff > Math.PI) diff -= 2 * Math.PI;
    while (diff < -Math.PI) diff += 2 * Math.PI;
    return diff;
  }

  private void updateProgress() {
    if (isTrackingTurn) {
      if (follower.isTurning()) {
        double currentHeading = follower.getPose().getHeading();
        double remainingRadians = Math.abs(getSmallestAngleDifference(targetHeading, currentHeading));

        double progress;
        if (totalTurnRadians < 1e-6) {
          progress = 1.0;
        } else {
          progress = 1.0 - (remainingRadians / totalTurnRadians);
        }

        pathProgress = Math.max(0.0, Math.min(1.0, progress));
        chainProgress = pathProgress; // For turn, chain progress mirrors turn progress

        if (telemetry != null) {
          telemetry.addData("Turn Progress", String.format("%.3f", pathProgress));
          telemetry.addData("Turn Remaining", String.format("%.3f rad", remainingRadians));
        }
      } else {
        // Turn finished
        isTrackingTurn = false;
        pathProgress = 1.0;
        chainProgress = 1.0;
      }
    } else if (currentChain != null && follower.getCurrentPath() != null) {
      // For individual path progress (0 to 1)
      pathProgress = Math.min(follower.getCurrentTValue(), 1.0);

      // For chain progress if multiple paths in chain
      int currentIndex = follower.getChainIndex();
      double totalProgress = 0;
      double currentProgress = 0;

      for (int i = 0; i < currentChain.size(); i++) {
        Path path = currentChain.getPath(i);
        if (i < currentIndex) {
          // Path completed
          currentProgress += 1.0;
        } else if (i == currentIndex) {
          // Current path
          currentProgress += pathProgress;
        }
        totalProgress += 1.0;
      }

      chainProgress = totalProgress > 0 ? currentProgress / totalProgress : 0.0;

      if (telemetry != null) {
        telemetry.addData("Path Progress", String.format("%.3f", pathProgress));
        telemetry.addData("Chain Progress", String.format("%.3f", chainProgress));
        telemetry.addData("Current T Value", follower.getCurrentTValue());
        telemetry.addData("Chain Index", currentIndex);
      }
    }
  }

  public double getPathProgress() {
    updateProgress();
    return pathProgress;
  }

  public double getChainProgress() {
    updateProgress();
    return chainProgress;
  }

  // Common delegate methods
  public boolean isBusy() {
    return follower.isBusy();
  }

  public void breakFollowing() {
    follower.breakFollowing();
  }
}

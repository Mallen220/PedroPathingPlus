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

  private void updateProgress() {
    if (currentChain != null && follower.getCurrentPath() != null) {
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

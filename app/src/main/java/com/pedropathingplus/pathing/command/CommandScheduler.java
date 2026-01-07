package com.pedropathingplus.pathing.command;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple Command Scheduler to run commands.
 * This is a singleton to allow global access, similar to WPILib's CommandScheduler.
 */
public class CommandScheduler {
    private static CommandScheduler instance;
    private final Set<Command> scheduledCommands = new LinkedHashSet<>();
    private final List<Command> toSchedule = new ArrayList<>();
    private final List<Command> toCancel = new ArrayList<>();
    private boolean inRunLoop = false;

    public static synchronized CommandScheduler getInstance() {
        if (instance == null) {
            instance = new CommandScheduler();
        }
        return instance;
    }

    // For testing
    public static synchronized void resetInstance() {
        instance = new CommandScheduler();
    }

    public void schedule(Command command) {
        if (inRunLoop) {
            toSchedule.add(command);
        } else {
            initCommand(command);
        }
    }

    public void cancel(Command command) {
        if (inRunLoop) {
            toCancel.add(command);
        } else {
            endCommand(command, true);
        }
    }

    public void run() {
        inRunLoop = true;

        // Run scheduled commands
        for (Command command : scheduledCommands) {
            command.execute();
            if (command.isFinished()) {
                toCancel.add(command); // Finished normally
            }
        }

        inRunLoop = false;

        // Process changes
        for (Command c : toCancel) {
            endCommand(c, !c.isFinished()); // If isFinished is true, not interrupted
        }
        toCancel.clear();

        for (Command c : toSchedule) {
            initCommand(c);
        }
        toSchedule.clear();
    }

    public void reset() {
        toCancel.addAll(scheduledCommands);
        for (Command c : toCancel) {
            endCommand(c, true);
        }
        scheduledCommands.clear();
        toCancel.clear();
        toSchedule.clear();
    }

    private void initCommand(Command command) {
        if (!scheduledCommands.contains(command)) {
            command.initialize();
            scheduledCommands.add(command);
        }
    }

    private void endCommand(Command command, boolean interrupted) {
        if (scheduledCommands.contains(command)) {
            command.end(interrupted);
            scheduledCommands.remove(command);
        }
    }

    public boolean isScheduled(Command command) {
        return scheduledCommands.contains(command);
    }
}

package com.pedropathingplus.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CommandScheduler {
    private static CommandScheduler instance;

    // Map of subsystems to their current command
    private final Map<Subsystem, Command> requirements = new HashMap<>();

    // Map of subsystems to their default command
    private final Map<Subsystem, Command> defaultCommands = new HashMap<>();

    // List of scheduled commands (those currently running)
    private final List<Command> scheduledCommands = new ArrayList<>();

    // List of registered subsystems
    private final List<Subsystem> registeredSubsystems = new ArrayList<>();

    // Commands to schedule in the next loop iteration
    private final List<Command> toSchedule = new ArrayList<>();

    // Commands to cancel in the next loop iteration
    private final List<Command> toCancel = new ArrayList<>();

    // Flag to check if we are currently running the scheduler loop
    private boolean inRunLoop = false;

    public static synchronized CommandScheduler getInstance() {
        if (instance == null) {
            instance = new CommandScheduler();
        }
        return instance;
    }

    private CommandScheduler() {}

    public void registerSubsystem(Subsystem subsystem) {
        if (!registeredSubsystems.contains(subsystem)) {
            registeredSubsystems.add(subsystem);
        }
    }

    public void setDefaultCommand(Subsystem subsystem, Command defaultCommand) {
        if (!defaultCommand.getRequirements().contains(subsystem)) {
            throw new IllegalArgumentException("Default command must require the subsystem");
        }
        defaultCommands.put(subsystem, defaultCommand);
    }

    public void schedule(Command... commands) {
        if (inRunLoop) {
            for (Command command : commands) {
                toSchedule.add(command);
            }
            return;
        }

        for (Command command : commands) {
            if (scheduledCommands.contains(command)) continue;

            // Check requirements
            Set<Subsystem> requirementsSet = command.getRequirements();
            for (Subsystem subsystem : requirementsSet) {
                if (requirements.containsKey(subsystem)) {
                    Command currentCommand = requirements.get(subsystem);
                    // Cancel the current command using this subsystem
                    cancel(currentCommand);
                }
            }

            // Schedule the command
            initCommand(command, requirementsSet);
        }
    }

    private void initCommand(Command command, Set<Subsystem> requirementsSet) {
        command.initialize();
        scheduledCommands.add(command);
        for (Subsystem subsystem : requirementsSet) {
            requirements.put(subsystem, command);
        }
    }

    public void cancel(Command... commands) {
        if (inRunLoop) {
            for (Command command : commands) {
                toCancel.add(command);
            }
            return;
        }

        for (Command command : commands) {
            if (!scheduledCommands.contains(command)) continue;

            command.end(true);
            scheduledCommands.remove(command);

            // Remove from requirements map
            for (Subsystem subsystem : command.getRequirements()) {
                requirements.remove(subsystem);
            }
        }
    }

    public void run() {
        inRunLoop = true;

        // Run subsystem periodic methods
        for (Subsystem subsystem : registeredSubsystems) {
            subsystem.periodic();
        }

        // Run scheduled commands
        Iterator<Command> iterator = scheduledCommands.iterator();
        while (iterator.hasNext()) {
            Command command = iterator.next();
            command.execute();
            if (command.isFinished()) {
                command.end(false);
                iterator.remove();

                // Remove from requirements map
                for (Subsystem subsystem : command.getRequirements()) {
                    requirements.remove(subsystem);
                }
            }
        }

        inRunLoop = false;

        // Process pending scheduling and cancellations
        if (!toSchedule.isEmpty()) {
            Command[] cmds = toSchedule.toArray(new Command[0]);
            toSchedule.clear();
            schedule(cmds);
        }

        if (!toCancel.isEmpty()) {
            Command[] cmds = toCancel.toArray(new Command[0]);
            toCancel.clear();
            cancel(cmds);
        }

        // Schedule default commands if needed
        for (Subsystem subsystem : registeredSubsystems) {
            if (!requirements.containsKey(subsystem) && defaultCommands.containsKey(subsystem)) {
                schedule(defaultCommands.get(subsystem));
            }
        }
    }

    /**
     * Resets the scheduler. Clears all commands and subsystems.
     * Useful for testing.
     */
    public void reset() {
        // Clear all internal state
        scheduledCommands.clear();
        requirements.clear();
        registeredSubsystems.clear();
        defaultCommands.clear();
        toSchedule.clear();
        toCancel.clear();
        inRunLoop = false;
    }
}

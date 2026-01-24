package com.pedropathingplus.command;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CommandScheduler {
    private static CommandScheduler instance;

    // Map of subsystems (Objects) to their current command
    private final Map<Object, Command> requirements = new HashMap<>();

    // Map of subsystems (Objects) to their default command
    private final Map<Object, Command> defaultCommands = new HashMap<>();

    // List of scheduled commands (those currently running)
    private final List<Command> scheduledCommands = new ArrayList<>();

    // List of registered subsystems (Objects)
    private final List<Object> registeredSubsystems = new ArrayList<>();

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

    public void registerSubsystem(Object subsystem) {
        if (!registeredSubsystems.contains(subsystem)) {
            registeredSubsystems.add(subsystem);
        }
    }

    public void setDefaultCommand(Object subsystem, Object defaultCommand) {
        Command cmd = asCommand(defaultCommand);
        if (!cmd.getRequirements().contains(subsystem)) {
            throw new IllegalArgumentException("Default command must require the subsystem");
        }
        defaultCommands.put(subsystem, cmd);
    }

    /**
     * Schedules one or more commands (or objects that act like commands).
     * @param commands Command objects, or generic objects to be wrapped.
     */
    public void schedule(Object... commands) {
        if (inRunLoop) {
            for (Object command : commands) {
                toSchedule.add(asCommand(command));
            }
            return;
        }

        for (Object commandObj : commands) {
            Command command = asCommand(commandObj);

            if (scheduledCommands.contains(command)) continue;

            // Check requirements
            Set<Object> requirementsSet = command.getRequirements();
            for (Object subsystem : requirementsSet) {
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

    private Command asCommand(Object commandObj) {
        if (commandObj instanceof Command) {
            return (Command) commandObj;
        } else if (commandObj instanceof Runnable) {
            return new InstantCommand((Runnable) commandObj);
        } else {
            return new ReflectiveCommandAdapter(commandObj);
        }
    }

    private void initCommand(Command command, Set<Object> requirementsSet) {
        command.initialize();
        scheduledCommands.add(command);
        for (Object subsystem : requirementsSet) {
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
            for (Object subsystem : command.getRequirements()) {
                requirements.remove(subsystem);
            }
        }
    }

    public void run() {
        inRunLoop = true;

        // Run subsystem periodic methods
        for (Object subsystem : registeredSubsystems) {
            runPeriodic(subsystem);
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
                for (Object subsystem : command.getRequirements()) {
                    requirements.remove(subsystem);
                }
            }
        }

        inRunLoop = false;

        // Process pending scheduling and cancellations
        if (!toSchedule.isEmpty()) {
            Command[] cmds = toSchedule.toArray(new Command[0]);
            toSchedule.clear();
            schedule((Object[])cmds);
        }

        if (!toCancel.isEmpty()) {
            Command[] cmds = toCancel.toArray(new Command[0]);
            toCancel.clear();
            cancel(cmds);
        }

        // Schedule default commands if needed
        for (Object subsystem : registeredSubsystems) {
            if (!requirements.containsKey(subsystem) && defaultCommands.containsKey(subsystem)) {
                schedule(defaultCommands.get(subsystem));
            }
        }
    }

    private void runPeriodic(Object subsystem) {
        if (subsystem instanceof Subsystem) {
            ((Subsystem) subsystem).periodic();
        } else {
            try {
                Method periodic = subsystem.getClass().getMethod("periodic");
                periodic.invoke(subsystem);
            } catch (NoSuchMethodException ignored) {
                // If it doesn't have periodic, that's fine.
            } catch (Exception e) {
                e.printStackTrace();
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

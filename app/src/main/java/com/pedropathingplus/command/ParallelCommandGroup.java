package com.pedropathingplus.command;

import java.util.HashMap;
import java.util.Map;

/**
 * A command group that runs a set of commands in parallel.
 * The group ends when all commands have finished.
 */
public class ParallelCommandGroup extends CommandGroupBase {
    private final Map<Command, Boolean> commands = new HashMap<>();
    private boolean runWhenDisabled = true;

    public ParallelCommandGroup(Command... commands) {
        addCommands(commands);
    }

    @Override
    public void addCommands(Command... commands) {
        for (Command command : commands) {
            this.commands.put(command, false);
            addRequirements(command.getRequirements());
        }
    }

    @Override
    public void initialize() {
        for (Map.Entry<Command, Boolean> entry : commands.entrySet()) {
            entry.getKey().initialize();
            entry.setValue(true); // running
        }
    }

    @Override
    public void execute() {
        for (Map.Entry<Command, Boolean> entry : commands.entrySet()) {
            if (!entry.getValue()) continue; // already finished

            Command command = entry.getKey();
            command.execute();
            if (command.isFinished()) {
                command.end(false);
                entry.setValue(false); // finished
            }
        }
    }

    @Override
    public void end(boolean interrupted) {
        if (interrupted) {
            for (Map.Entry<Command, Boolean> entry : commands.entrySet()) {
                if (entry.getValue()) {
                    entry.getKey().end(true);
                }
            }
        }
    }

    @Override
    public boolean isFinished() {
        for (Boolean running : commands.values()) {
            if (running) return false;
        }
        return true;
    }
}

package com.pedropathingplus.pathing.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A command that runs a list of commands in sequence.
 */
public class SequentialCommandGroup implements Command {
    private final List<Command> commands = new ArrayList<>();
    private int currentCommandIndex = -1;

    public SequentialCommandGroup(Command... commands) {
        this.commands.addAll(Arrays.asList(commands));
    }

    public void addCommands(Command... commands) {
        this.commands.addAll(Arrays.asList(commands));
    }

    @Override
    public void initialize() {
        currentCommandIndex = 0;
        if (!commands.isEmpty()) {
            commands.get(0).initialize();
        }
    }

    @Override
    public void execute() {
        if (commands.isEmpty()) return;

        Command currentCommand = commands.get(currentCommandIndex);
        currentCommand.execute();

        if (currentCommand.isFinished()) {
            currentCommand.end(false);
            currentCommandIndex++;
            if (currentCommandIndex < commands.size()) {
                commands.get(currentCommandIndex).initialize();
            }
        }
    }

    @Override
    public void end(boolean interrupted) {
        if (interrupted && currentCommandIndex > -1 && currentCommandIndex < commands.size()) {
            commands.get(currentCommandIndex).end(true);
        }
        currentCommandIndex = -1;
    }

    @Override
    public boolean isFinished() {
        return currentCommandIndex >= commands.size();
    }
}

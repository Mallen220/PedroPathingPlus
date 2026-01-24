package com.pedropathingplus.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A command group that runs a list of commands in sequence.
 * As each command finishes, the next one is started.
 * The group finishes when the last command finishes.
 */
public class SequentialCommandGroup extends CommandGroupBase {
    private final List<Command> commands = new ArrayList<>();
    private int currentCommandIndex = -1;
    private boolean runWhenDisabled = true;

    public SequentialCommandGroup(Command... commands) {
        addCommands(commands);
    }

    @Override
    public void addCommands(Command... commands) {
        this.commands.addAll(Arrays.asList(commands));
        for (Command command : commands) {
            addRequirements(command.getRequirements());
        }
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
        if (commands.isEmpty()) {
            return;
        }

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
        if (interrupted && !commands.isEmpty() && currentCommandIndex > -1 && currentCommandIndex < commands.size()) {
            commands.get(currentCommandIndex).end(true);
        }
        currentCommandIndex = -1;
    }

    @Override
    public boolean isFinished() {
        return currentCommandIndex >= commands.size();
    }
}

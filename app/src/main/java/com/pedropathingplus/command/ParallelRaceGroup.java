package com.pedropathingplus.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A command group that runs a set of commands in parallel.
 * The group ends when ANY command finishes.
 */
public class ParallelRaceGroup extends CommandGroupBase {
    private final List<Command> commands = new ArrayList<>();
    private boolean finished = false;

    public ParallelRaceGroup(Command... commands) {
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
        finished = false;
        for (Command command : commands) {
            command.initialize();
        }
    }

    @Override
    public void execute() {
        if (finished) return;

        for (Command command : commands) {
            command.execute();
            if (command.isFinished()) {
                finished = true;
                command.end(false);
            }
        }
    }

    @Override
    public void end(boolean interrupted) {
        for (Command command : commands) {
            // If the command is not finished (because another one won the race), interrupt it.
            // Note: We don't have an easy way to check if a specific command finished in the loop above
            // without storing state, but usually ending a finished command again is benign or handled by isFinished checks.
            // However, strictly speaking, we should only end those that are still running.
            // Since we can't easily query "isRunning" without tracking it, we rely on the fact that
            // calling end() on a finished command is generally safe if implemented idempotently,
            // OR we accept that we might call end(true) on commands that haven't finished.

            // Better approach: Since we know the group ends immediately when one finishes,
            // all others must be interrupted.
             if (!command.isFinished()) { // Primitive check, assuming isFinished is stable
                 command.end(true);
             }
        }
    }

    @Override
    public boolean isFinished() {
        return finished;
    }
}

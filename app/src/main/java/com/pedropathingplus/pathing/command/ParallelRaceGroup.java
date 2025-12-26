package com.pedropathingplus.pathing.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A command group that runs commands in parallel, ending when ANY command ends.
 * (Similar to ParallelRaceGroup in WPILib/SolversLib).
 */
public class ParallelRaceGroup implements Command {
    private final List<Command> commands = new ArrayList<>();
    private final List<Boolean> running = new ArrayList<>();
    private boolean finished = false;

    public ParallelRaceGroup(Command... commands) {
        this.commands.addAll(Arrays.asList(commands));
    }

    @Override
    public void initialize() {
        finished = false;
        running.clear();
        for (Command c : commands) {
            c.initialize();
            running.add(true);
        }
    }

    @Override
    public void execute() {
        for (int i = 0; i < commands.size(); i++) {
            if (running.get(i)) {
                Command c = commands.get(i);
                c.execute();
                if (c.isFinished()) {
                    finished = true;
                }
            }
        }
    }

    @Override
    public void end(boolean interrupted) {
        for (int i = 0; i < commands.size(); i++) {
            if (running.get(i)) {
                commands.get(i).end(true); // Always interrupt the losers
            }
        }
    }

    @Override
    public boolean isFinished() {
        return finished;
    }
}

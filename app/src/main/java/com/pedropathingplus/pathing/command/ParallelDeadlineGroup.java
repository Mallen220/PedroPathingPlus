package com.pedropathingplus.pathing.command;

import java.util.HashMap;
import java.util.Map;

/**
 * A command group that runs commands in parallel, ending when a specific command (the deadline) ends.
 * All other commands are interrupted when the deadline command finishes.
 */
public class ParallelDeadlineGroup implements Command {
    private final Command deadline;
    private final Command[] others;
    private final Map<Command, Boolean> running = new HashMap<>();

    public ParallelDeadlineGroup(Command deadline, Command... others) {
        this.deadline = deadline;
        this.others = others;
    }

    @Override
    public void initialize() {
        deadline.initialize();
        running.put(deadline, true);

        for (Command c : others) {
            c.initialize();
            running.put(c, true);
        }
    }

    @Override
    public void execute() {
        if (!running.containsKey(deadline)) return;

        // Execute deadline
        deadline.execute();

        // Execute others regardless of deadline state in this tick (to catch simultaneous events)
        for (Command c : others) {
            if (running.get(c)) {
                c.execute();
                if (c.isFinished()) {
                    c.end(false);
                    running.put(c, false);
                }
            }
        }

        // We don't check deadline.isFinished() here to return early,
        // because we want others to run this tick.
    }

    @Override
    public void end(boolean interrupted) {
        if (running.getOrDefault(deadline, false)) {
             deadline.end(interrupted);
        }

        for (Command c : others) {
            if (running.getOrDefault(c, false)) {
                c.end(true); // Always interrupted because deadline finished (or group interrupted)
            }
        }
    }

    @Override
    public boolean isFinished() {
        return deadline.isFinished();
    }
}

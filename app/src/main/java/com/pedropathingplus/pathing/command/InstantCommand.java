package com.pedropathingplus.pathing.command;

/**
 * A command that runs a Runnable immediately when scheduled.
 * This serves as a simple replacement for SolversLib's InstantCommand when the library is not present,
 * or as a lightweight local command implementation.
 */
public class InstantCommand implements Command {
    private final Runnable toRun;

    public InstantCommand(Runnable toRun) {
        this.toRun = toRun;
    }

    @Override
    public void schedule() {
        if (toRun != null) {
            toRun.run();
        }
    }
}

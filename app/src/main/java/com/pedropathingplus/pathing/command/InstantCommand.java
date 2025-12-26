package com.pedropathingplus.pathing.command;

/**
 * A command that runs a Runnable immediately when scheduled and finishes immediately.
 */
public class InstantCommand implements Command {
    private final Runnable toRun;

    public InstantCommand(Runnable toRun) {
        this.toRun = toRun;
    }

    @Override
    public void initialize() {
        if (toRun != null) {
            toRun.run();
        }
    }

    @Override
    public boolean isFinished() {
        return true;
    }
}

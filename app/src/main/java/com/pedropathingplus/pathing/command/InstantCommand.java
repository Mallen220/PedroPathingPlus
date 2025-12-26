package com.pedropathingplus.pathing.command;

public class InstantCommand extends Command {
    private final Runnable toRun;

    public InstantCommand(Runnable toRun) {
        this.toRun = toRun;
    }

    @Override
    public void run() {
        if (toRun != null) {
            toRun.run();
        }
    }
}

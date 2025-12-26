package com.pedropathingplus.pathing.command;

public abstract class Command {
    /**
     * The run method which should be implemented by subclasses.
     * This is where the command logic resides.
     */
    public abstract void run();

    /**
     * Schedules the command to be run.
     * In this implementation without SolversLib, it immediately runs the command.
     */
    public void schedule() {
        run();
    }
}

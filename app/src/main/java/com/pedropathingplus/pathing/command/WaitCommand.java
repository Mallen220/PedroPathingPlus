package com.pedropathingplus.pathing.command;

public class WaitCommand implements Command {
    private final long durationMillis;
    private long startTime;

    public WaitCommand(long durationMillis) {
        this.durationMillis = durationMillis;
    }

    public WaitCommand(double durationSeconds) {
        this.durationMillis = (long) (durationSeconds * 1000);
    }

    @Override
    public void initialize() {
        startTime = System.currentTimeMillis();
    }

    @Override
    public boolean isFinished() {
        return System.currentTimeMillis() - startTime >= durationMillis;
    }
}

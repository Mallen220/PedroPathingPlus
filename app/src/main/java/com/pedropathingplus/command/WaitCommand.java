package com.pedropathingplus.command;

/**
 * A command that waits for a specified number of milliseconds before finishing.
 */
public class WaitCommand implements Command {
    private final long durationMs;
    private long startTimeMs;

    /**
     * Create a WaitCommand that waits for the given number of milliseconds.
     * If durationMs <= 0 the command finishes immediately.
     *
     * @param durationMs milliseconds to wait
     */
    public WaitCommand(long durationMs) {
        this.durationMs = durationMs;
        this.startTimeMs = Long.MIN_VALUE;
    }

    @Override
    public void initialize() {
        // record start time when scheduled
        this.startTimeMs = System.currentTimeMillis();
    }

    @Override
    public boolean isFinished() {
        if (durationMs <= 0) {
            return true;
        }
        if (startTimeMs == Long.MIN_VALUE) {
            // haven't been initialized yet; treat as not finished until initialized
            return false;
        }
        return System.currentTimeMillis() - startTimeMs >= durationMs;
    }
}


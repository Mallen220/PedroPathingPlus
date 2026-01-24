package com.pedropathingplus.command;

/**
 * A command that runs a Runnable repeatedly.
 */
public class RunCommand implements Command {
    private final Runnable toRun;
    private final java.util.Set<Object> requirements;

    public RunCommand(Runnable toRun, Object... requirements) {
        this.toRun = toRun;
        this.requirements = new java.util.HashSet<>(java.util.Arrays.asList(requirements));
    }

    @Override
    public void execute() {
        toRun.run();
    }

    @Override
    public java.util.Set<Object> getRequirements() {
        return requirements;
    }
}

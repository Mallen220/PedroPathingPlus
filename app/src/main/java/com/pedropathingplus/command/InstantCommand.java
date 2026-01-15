package com.pedropathingplus.command;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A Command that runs a Runnable once and finishes.
 */
public class InstantCommand implements Command {
    private final Runnable toRun;
    private final Set<Subsystem> requirements;

    public InstantCommand(Runnable toRun, Subsystem... requirements) {
        this.toRun = toRun;
        this.requirements = new HashSet<>();
        Collections.addAll(this.requirements, requirements);
    }

    public InstantCommand() {
        this(() -> {});
    }

    @Override
    public void initialize() {
        toRun.run();
    }

    @Override
    public boolean isFinished() {
        return true;
    }

    @Override
    public Set<Subsystem> getRequirements() {
        return requirements;
    }
}

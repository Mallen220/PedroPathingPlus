package com.pedropathingplus.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A base class for command groups.
 * Handles requirement merging.
 */
public abstract class CommandGroupBase implements Command {
    protected final Set<Object> requirements = new HashSet<>();

    public final void addRequirements(Set<Object> requirements) {
        this.requirements.addAll(requirements);
    }

    @Override
    public Set<Object> getRequirements() {
        return requirements;
    }

    public abstract void addCommands(Command... commands);
}

package com.pedropathingplus.pathing.command;

import java.util.function.BooleanSupplier;

/**
 * A command that waits until a condition is true.
 */
public class WaitUntilCommand implements Command {
    private final BooleanSupplier condition;

    public WaitUntilCommand(BooleanSupplier condition) {
        this.condition = condition;
    }

    @Override
    public boolean isFinished() {
        return condition.getAsBoolean();
    }
}

package com.pedropathingplus.command;

import java.util.function.BooleanSupplier;

/**
 * A command that does nothing but wait until a condition is true.
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

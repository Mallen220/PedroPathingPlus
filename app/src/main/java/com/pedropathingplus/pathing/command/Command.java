package com.pedropathingplus.pathing.command;

/**
 * Interface for a Command.
 * This defines a contract for any executable action that can be scheduled.
 * By using an interface, we allow integration with other command-based libraries (like SolversLib, NextFTC)
 * via adapters or direct implementation, without enforcing a specific inheritance hierarchy.
 */
public interface Command {
    /**
     * Schedules the command for execution.
     * The implementation detail (immediate execution, adding to a scheduler, etc.) depends on the concrete class.
     */
    void schedule();
}

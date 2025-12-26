package com.pedropathingplus.pathing.command;

/**
 * Interface for a Command.
 * Modeled after WPILib and SolversLib Command interface to support composition.
 */
public interface Command {
    /**
     * The initial subroutine of a command.  Called once when the command is initially scheduled.
     */
    default void initialize() {}

    /**
     * The main body of a command.  Called repeatedly while the command is scheduled.
     */
    default void execute() {}

    /**
     * The action to take when the command ends.  Called when either the command finishes normally,
     * or when it interrupted/canceled.
     *
     * @param interrupted whether the command was interrupted/canceled
     */
    default void end(boolean interrupted) {}

    /**
     * Whether the command has finished.  Once a command finishes, the scheduler will call its
     * end() method and un-schedule it.
     *
     * @return whether the command has finished.
     */
    default boolean isFinished() {
        return false;
    }

    /**
     * Schedules this command to be run.
     * This relies on a global or context-aware scheduler.
     * For this library, we delegate to CommandScheduler.
     */
    default void schedule() {
        CommandScheduler.getInstance().schedule(this);
    }
}

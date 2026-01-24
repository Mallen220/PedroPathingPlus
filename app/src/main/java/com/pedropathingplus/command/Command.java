package com.pedropathingplus.command;

import java.util.Collections;
import java.util.Set;

/**
 * A state machine representing a complete action to be performed by the robot.
 * Commands are run by the CommandScheduler.
 */
public interface Command {
    /**
     * The initial state of a command. Called once when the command is scheduled.
     */
    default void initialize() {}

    /**
     * The execution state of a command. Called repeatedly while the command is scheduled.
     */
    default void execute() {}

    /**
     * The ending state of a command. Called once when the command ends or is interrupted.
     *
     * @param interrupted whether the command was interrupted/canceled
     */
    default void end(boolean interrupted) {}

    /**
     * Whether the command has finished. Once a command finishes, the scheduler will call its
     * end() method and un-schedule it.
     *
     * @return whether the command has finished
     */
    default boolean isFinished() {
        return false;
    }

    /**
     * Specifies the set of subsystems used by this command.
     * Two commands cannot use the same subsystem at the same time.
     *
     * @return the set of subsystems required by this command
     */
    default Set<Object> getRequirements() {
        return Collections.emptySet();
    }
}

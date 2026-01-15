package com.pedropathingplus.command;

/**
 * A subsystem is a part of the robot, such as a drivetrain, arm, or claw.
 * Subsystems are used to specify requirements for commands, ensuring that
 * multiple commands do not try to control the same subsystem at the same time.
 */
public interface Subsystem {
    /**
     * This method is called periodically by the CommandScheduler.
     * Use this for updating subsystem state, sensors, etc.
     */
    default void periodic() {}

    /**
     * Registers this subsystem with the CommandScheduler.
     * This allows the periodic() method to be called.
     */
    default void register() {
        CommandScheduler.getInstance().registerSubsystem(this);
    }

    /**
     * Sets the default command for this subsystem.
     * The default command runs when no other command requires this subsystem.
     *
     * @param defaultCommand the command to run by default
     */
    default void setDefaultCommand(Command defaultCommand) {
        CommandScheduler.getInstance().setDefaultCommand(this, defaultCommand);
    }
}

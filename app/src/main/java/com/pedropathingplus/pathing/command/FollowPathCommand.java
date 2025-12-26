package com.pedropathingplus.pathing.command;

import com.pedropathing.follower.Follower;
import com.pedropathing.paths.PathChain;

/**
 * A command that follows a PathChain using the Follower.
 */
public class FollowPathCommand implements Command {
    private final Follower follower;
    private final PathChain pathChain;
    private final boolean holdEnd;

    public FollowPathCommand(Follower follower, PathChain pathChain) {
        this(follower, pathChain, false);
    }

    public FollowPathCommand(Follower follower, PathChain pathChain, boolean holdEnd) {
        this.follower = follower;
        this.pathChain = pathChain;
        this.holdEnd = holdEnd;
    }

    @Override
    public void initialize() {
        follower.followPath(pathChain, holdEnd);
    }

    @Override
    public void execute() {
        // Follower update is typically called in loop, not here.
        // Assuming user calls follower.update() in their OpMode loop.
        // If not, we might need to call it here, but that might duplicate calls.
        // Usually Command-based OpModes call hardware.update() or similar.
        // Pedro Pathing requires follower.update() every loop.
        // We will assume the user's loop calls follower.update().
    }

    @Override
    public boolean isFinished() {
        return !follower.isBusy();
    }

    @Override
    public void end(boolean interrupted) {
        if (interrupted) {
            follower.breakFollowing();
        }
    }
}

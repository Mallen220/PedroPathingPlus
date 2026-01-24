package com.pedropathingplus.command;

import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.Curve;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.HeadingInterpolator;
import com.pedropathing.paths.Path;
import com.pedropathing.paths.PathBuilder;
import com.pedropathing.paths.PathChain;
import com.pedropathing.paths.PathConstraints;
import com.pedropathing.paths.callbacks.PathCallback;

import java.util.Collections;
import java.util.Set;

/**
 * A Command that commands the Follower to follow a Path or PathChain.
 * It supports both pre-built PathChains and building paths fluently within the command.
 * <p>
 * Example Usage:
 * <pre>
 * // Pre-built
 * new FollowPathCommand(follower, myPathChain);
 *
 * // Fluent Builder
 * new FollowPathCommand(follower)
 *     .curveThrough(0.5, new Pose(10, 10), new Pose(20, 20))
 *     .setConstantHeadingInterpolation(Math.toRadians(90));
 * </pre>
 */
public class FollowPathCommand implements Command {
    private final Follower follower;
    private PathChain pathChain;
    private PathBuilder pathBuilder;
    private boolean holdEnd = true;
    private double maxPower = 1.0;

    // --- Constructors for Pre-Built PathChain ---

    public FollowPathCommand(Follower follower, PathChain pathChain) {
        this.follower = follower;
        this.pathChain = pathChain;
    }

    public FollowPathCommand(Follower follower, PathChain pathChain, boolean holdEnd) {
        this(follower, pathChain);
        this.holdEnd = holdEnd;
    }

    public FollowPathCommand(Follower follower, PathChain pathChain, double maxPower) {
        this(follower, pathChain);
        this.maxPower = maxPower;
    }

    public FollowPathCommand(Follower follower, PathChain pathChain, boolean holdEnd, double maxPower) {
        this.follower = follower;
        this.pathChain = pathChain;
        this.holdEnd = holdEnd;
        this.maxPower = maxPower;
    }

    // --- Constructors for Single Path (auto-converts to PathChain) ---

    public FollowPathCommand(Follower follower, Path path) {
        this(follower, new PathChain(path));
    }

    public FollowPathCommand(Follower follower, Path path, boolean holdEnd) {
        this(follower, new PathChain(path), holdEnd);
    }

    public FollowPathCommand(Follower follower, Path path, double maxPower) {
        this(follower, new PathChain(path), maxPower);
    }

    public FollowPathCommand(Follower follower, Path path, boolean holdEnd, double maxPower) {
        this(follower, new PathChain(path), holdEnd, maxPower);
    }

    // --- Constructor for Fluent Building ---

    /**
     * Creates a new FollowPathCommand in builder mode.
     * Use methods like {@link #curveThrough(double, Pose...)} to build the path.
     *
     * @param follower The follower instance.
     */
    public FollowPathCommand(Follower follower) {
        this.follower = follower;
        this.pathBuilder = new PathBuilder(follower);
    }

    // --- Configuration Methods ---

    public FollowPathCommand setHoldEnd(boolean holdEnd) {
        this.holdEnd = holdEnd;
        return this;
    }

    public FollowPathCommand setMaxPower(double maxPower) {
        this.maxPower = maxPower;
        return this;
    }

    // --- PathBuilder Delegation Methods ---

    private void ensureBuilder() {
        if (pathBuilder == null) {
            throw new IllegalStateException("Cannot add path steps to a FollowPathCommand created with a pre-built PathChain.");
        }
    }

    public FollowPathCommand addPath(Path path) {
        ensureBuilder();
        pathBuilder.addPath(path);
        return this;
    }

    public FollowPathCommand addPath(Curve curve) {
        ensureBuilder();
        pathBuilder.addPath(curve);
        return this;
    }

    public FollowPathCommand addPaths(Path... paths) {
        ensureBuilder();
        pathBuilder.addPaths(paths);
        return this;
    }

    public FollowPathCommand curveThrough(double tension, Pose... points) {
        ensureBuilder();
        pathBuilder.curveThrough(tension, points);
        return this;
    }

    public FollowPathCommand curveThrough(Pose prevPoint, Pose startPoint, double tension, Pose... points) {
        ensureBuilder();
        pathBuilder.curveThrough(prevPoint, startPoint, tension, points);
        return this;
    }

    public FollowPathCommand setLinearHeadingInterpolation(double startHeading, double endHeading) {
        ensureBuilder();
        pathBuilder.setLinearHeadingInterpolation(startHeading, endHeading);
        return this;
    }

    public FollowPathCommand setLinearHeadingInterpolation(double startHeading, double endHeading, double endTime) {
        ensureBuilder();
        pathBuilder.setLinearHeadingInterpolation(startHeading, endHeading, endTime);
        return this;
    }

    public FollowPathCommand setLinearHeadingInterpolation(double startHeading, double endHeading, double endTime, double startTime) {
        ensureBuilder();
        pathBuilder.setLinearHeadingInterpolation(startHeading, endHeading, endTime, startTime);
        return this;
    }

    public FollowPathCommand setConstantHeadingInterpolation(double setHeading) {
        ensureBuilder();
        pathBuilder.setConstantHeadingInterpolation(setHeading);
        return this;
    }

    public FollowPathCommand setTangentHeadingInterpolation() {
        ensureBuilder();
        pathBuilder.setTangentHeadingInterpolation();
        return this;
    }

    public FollowPathCommand setHeadingInterpolation(HeadingInterpolator function) {
        ensureBuilder();
        pathBuilder.setHeadingInterpolation(function);
        return this;
    }

    public FollowPathCommand setConstraints(PathConstraints constraints) {
        ensureBuilder();
        pathBuilder.setConstraints(constraints);
        return this;
    }

    public FollowPathCommand addCallback(PathCallback callback) {
        ensureBuilder();
        pathBuilder.addCallback(callback);
        return this;
    }

    public FollowPathCommand addParametricCallback(double t, Runnable runnable) {
        ensureBuilder();
        pathBuilder.addParametricCallback(t, runnable);
        return this;
    }

    public FollowPathCommand addTemporalCallback(double time, Runnable runnable) {
        ensureBuilder();
        pathBuilder.addTemporalCallback(time, runnable);
        return this;
    }

    // --- Command Interface Implementation ---

    @Override
    public void initialize() {
        if (pathChain == null) {
            if (pathBuilder != null) {
                // Build the chain on first run
                pathChain = pathBuilder.build();
            } else {
                throw new IllegalStateException("No PathChain provided or built.");
            }
        }
        follower.followPath(pathChain, maxPower, holdEnd);
    }

    @Override
    public void execute() {
        // No-op: Follower update is typically handled by the OpMode loop.
        // If specific telemetry is needed, it can be added here.
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

    @Override
    public Set<Object> getRequirements() {
        return Collections.singleton(follower);
    }
}

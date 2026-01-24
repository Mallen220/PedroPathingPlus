/* ============================================================= *
 *           Pedro Pathing Visualizer — Auto-Generated           *
 *                                                               *
 *  Version: 1.6.2.                                              *
 *  Copyright (c) 2026 Matthew Allen                             *
 *                                                               *
 *  THIS FILE IS AUTO-GENERATED — DO NOT EDIT MANUALLY.          *
 *  Changes will be overwritten when regenerated.                *
 * ============================================================= */

package org.firstinspires.ftc.teamcode.Commands.AutoCommands;

import com.pedropathing.control.FilteredPIDFCoefficients;
import com.pedropathing.control.PIDFCoefficients;
import com.pedropathing.follower.Follower;
import com.pedropathing.follower.FollowerConstants;
import com.pedropathing.geometry.BezierCurve;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.PathChain;
import com.pedropathingplus.PedroPathReader;
import com.pedropathingplus.command.FollowPathCommand;
import com.pedropathingplus.command.InstantCommand;
import com.pedropathingplus.command.ParallelRaceGroup;
import com.pedropathingplus.command.SequentialCommandGroup;
import com.pedropathingplus.command.WaitUntilCommand;
import com.pedropathingplus.pathing.ProgressTracker;
import com.qualcomm.robotcore.hardware.HardwareMap;

import java.io.IOException;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.Subsystems.Drivetrain;

public class reg extends SequentialCommandGroup {

  private final Follower follower;
  private final ProgressTracker progressTracker;

  // Poses
  private Pose startPoint;
  private Pose BeforeFirstRow;
  private Pose BeforeFirstRow_line0_control1;
  private Pose AfterFirstRow;
  private Pose AfterFirstRow_line1_control1;
  private Pose BeforeSecondRow;
  private Pose BeforeSecondRow_line2_control1;
  private Pose AfterSecondRow;
  private Pose AfterSecondRow_line3_control1;
  private Pose ScoringPosition;
  private Pose ScoringPosition_line4_control1;

  // Path chains
  private PathChain startPointTOBeforeFirstRow;
  private PathChain BeforeFirstRowTOAfterFirstRow;
  private PathChain AfterFirstRowTOBeforeSecondRow;
  private PathChain BeforeSecondRowTOAfterSecondRow;
  private PathChain AfterSecondRowTOScoringPosition;

  public reg(final Drivetrain drive, HardwareMap hw, Telemetry telemetry) throws IOException {
    this.follower = drive.getFollower();
    this.progressTracker = new ProgressTracker(follower, telemetry);

    PedroPathReader pp = new PedroPathReader("reg.pp", hw.appContext);

    // Load poses
    startPoint = pp.get("startPoint");
    BeforeFirstRow = pp.get("BeforeFirstRow");
    AfterFirstRow = pp.get("AfterFirstRow");
    BeforeSecondRow = pp.get("BeforeSecondRow");
    AfterSecondRow = pp.get("AfterSecondRow");
    ScoringPosition = pp.get("ScoringPosition");

    follower.setStartingPose(startPoint);
    //    follower.setConstants(
    //        new FollowerConstants()
    //            .BEZIER_CURVE_SEARCH_LIMIT(20)
    //            .centripetalScaling(0.0015)
    //            .translationalPIDFCoefficients(new PIDFCoefficients(0.075, 0, 0, 0.01))
    //            .headingPIDFCoefficients(new PIDFCoefficients(0.6, 0, 0, 0.004))
    //            .headingPIDFSwitch(Math.PI / 8)
    //            .holdPointTranslationalScaling(0.3)
    //            .holdPointHeadingScaling(0.25));

    follower.setConstants(
        new FollowerConstants()
            .BEZIER_CURVE_SEARCH_LIMIT(20)
            // Do not let translation outrun rotation
            .drivePIDFCoefficients(new FilteredPIDFCoefficients(0.018, 0, 0.000008, 0.5, 0.01))

            // Let physics cap speed in curves
            .centripetalScaling(0.0017)
            // Softer translational pull
            .translationalPIDFCoefficients(new PIDFCoefficients(0.065, 0, 0, 0.01))

            // Earlier and stronger heading authority
            .headingPIDFSwitch(Math.PI / 14)
            .secondaryHeadingPIDFCoefficients(new PIDFCoefficients(4.0, 0, 0.06, 0.005))

            // Don’t snap at the end
            .holdPointTranslationalScaling(0.3)
            .holdPointHeadingScaling(0.25));

    buildPaths();

    addCommands(
        new InstantCommand(
            () -> {
              progressTracker.setCurrentChain(startPointTOBeforeFirstRow);
              progressTracker.setCurrentPathName("startPointTOBeforeFirstRow");
              progressTracker.registerEvent("IntakeOn", 0.320);
            }),
        new ParallelRaceGroup(
            new FollowPathCommand(follower, startPointTOBeforeFirstRow),
            new SequentialCommandGroup(
                new WaitUntilCommand(() -> progressTracker.shouldTriggerEvent("IntakeOn")),
                new InstantCommand(
                    () -> {
                      progressTracker.executeEvent("IntakeOn");
                    }))),
        new InstantCommand(
            () -> {
              progressTracker.setCurrentChain(BeforeFirstRowTOAfterFirstRow);
              progressTracker.setCurrentPathName("BeforeFirstRowTOAfterFirstRow");
            }),
        new FollowPathCommand(follower, BeforeFirstRowTOAfterFirstRow),
        new InstantCommand(
            () -> {
              progressTracker.setCurrentChain(AfterFirstRowTOBeforeSecondRow);
              progressTracker.setCurrentPathName("AfterFirstRowTOBeforeSecondRow");
            }),
        new FollowPathCommand(follower, AfterFirstRowTOBeforeSecondRow),
        new InstantCommand(
            () -> {
              progressTracker.setCurrentChain(BeforeSecondRowTOAfterSecondRow);
              progressTracker.setCurrentPathName("BeforeSecondRowTOAfterSecondRow");
            }),
        new FollowPathCommand(follower, BeforeSecondRowTOAfterSecondRow),
        new InstantCommand(
            () -> {
              progressTracker.setCurrentChain(AfterSecondRowTOScoringPosition);
              progressTracker.setCurrentPathName("AfterSecondRowTOScoringPosition");
              progressTracker.registerEvent("IntakeOff", 0.220);
            }),
        new ParallelRaceGroup(

            new SequentialCommandGroup(
                new WaitUntilCommand(() -> progressTracker.shouldTriggerEvent("IntakeOff")),
                new InstantCommand(
                    () -> {
                      progressTracker.executeEvent("IntakeOff");
                    }))));
  }

  public void buildPaths() {
    startPointTOBeforeFirstRow =
        follower
            .pathBuilder()
            .addPath(new BezierCurve(startPoint, new Pose(51.392, 25.467), BeforeFirstRow))
            .setLinearHeadingInterpolation(startPoint.getHeading(), BeforeFirstRow.getHeading())
            .build();

    BeforeFirstRowTOAfterFirstRow =
        follower
            .pathBuilder()
            .addPath(new BezierCurve(BeforeFirstRow, new Pose(39.783, 34.152), AfterFirstRow))
            .setTangentHeadingInterpolation()
            .build();

    AfterFirstRowTOBeforeSecondRow =
        follower
            .pathBuilder()
            .addPath(new BezierCurve(AfterFirstRow, new Pose(13.713, 35.197), BeforeSecondRow))
            .setTangentHeadingInterpolation()
            .build();

    BeforeSecondRowTOAfterSecondRow =
        follower
            .pathBuilder()
            .addPath(new BezierCurve(BeforeSecondRow, new Pose(25.129, 63.403), AfterSecondRow))
            .setTangentHeadingInterpolation()
            .build();

    AfterSecondRowTOScoringPosition =
        follower
            .pathBuilder()
            .addPath(new BezierCurve(AfterSecondRow, new Pose(66.697, 49.378), ScoringPosition))
            .setTangentHeadingInterpolation()
            .build();
  }
}

package com.pedropathingplus.command;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

public class CommandSchedulerTest {

    static class TestSubsystem implements Subsystem {
        int periodicCount = 0;
        @Override
        public void periodic() {
            periodicCount++;
        }
    }

    static class TestCommand implements Command {
        boolean init = false;
        int executeCount = 0;
        boolean end = false;
        boolean interrupted = false;
        boolean finished = false;
        Set<Subsystem> requirements = new HashSet<>();

        public TestCommand(Subsystem... requirements) {
            this.requirements.addAll(Arrays.asList(requirements));
        }

        @Override
        public void initialize() {
            init = true;
        }

        @Override
        public void execute() {
            executeCount++;
        }

        @Override
        public void end(boolean interrupted) {
            this.end = true;
            this.interrupted = interrupted;
        }

        @Override
        public boolean isFinished() {
            return finished;
        }

        @Override
        public Set<Subsystem> getRequirements() {
            return requirements;
        }
    }

    @Test
    public void testScheduleAndRun() {
        CommandScheduler scheduler = CommandScheduler.getInstance();
        scheduler.reset();

        TestCommand cmd = new TestCommand();
        scheduler.schedule(cmd);

        scheduler.run();

        assertTrue(cmd.init);
        assertEquals(1, cmd.executeCount);
        assertFalse(cmd.end);

        cmd.finished = true;
        scheduler.run(); // Should call end()

        assertTrue(cmd.end);
        assertFalse(cmd.interrupted);
    }

    @Test
    public void testRequirementsAndInterruption() {
        CommandScheduler scheduler = CommandScheduler.getInstance();
        scheduler.reset();

        TestSubsystem sub = new TestSubsystem();
        TestCommand cmd1 = new TestCommand(sub);
        TestCommand cmd2 = new TestCommand(sub);

        scheduler.schedule(cmd1);
        scheduler.run();
        assertTrue(cmd1.init);

        scheduler.schedule(cmd2);
        // cmd1 should be interrupted immediately
        assertTrue(cmd1.end);
        assertTrue(cmd1.interrupted);

        // cmd2 should be initialized
        assertTrue(cmd2.init);

        scheduler.run();
        assertEquals(1, cmd2.executeCount);
    }

    @Test
    public void testDefaultCommand() {
        CommandScheduler scheduler = CommandScheduler.getInstance();
        scheduler.reset();

        TestSubsystem sub = new TestSubsystem();
        sub.register();
        TestCommand defaultCmd = new TestCommand(sub);
        sub.setDefaultCommand(defaultCmd);

        scheduler.run();
        // Default command is scheduled at the end of run(), so init is called, but execute is not.
        assertTrue(defaultCmd.init);
        assertEquals(0, defaultCmd.executeCount);

        scheduler.run();
        assertEquals(1, defaultCmd.executeCount);

        TestCommand cmd = new TestCommand(sub);
        scheduler.schedule(cmd);
        // Default command should be interrupted
        assertTrue(defaultCmd.end);
        assertTrue(defaultCmd.interrupted);
        assertTrue(cmd.init);

        cmd.finished = true;
        scheduler.run(); // cmd ends

        // Default command should be rescheduled in the NEXT run loop or immediately after

        // Let's verify execution count increases in the next run
        int prevCount = defaultCmd.executeCount;
        scheduler.run();
        assertEquals(prevCount + 1, defaultCmd.executeCount);
    }
}

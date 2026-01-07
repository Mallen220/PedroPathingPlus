package com.pedropathingplus.pathing;

import com.pedropathingplus.pathing.command.Command;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

public class TestNamedCommands {

    // Simulates a SolversLib or NextFTC command that has a schedule() method
    static class ForeignCommand {
        boolean executed = false;
        public void schedule() {
            executed = true;
            System.out.println("ForeignCommand scheduled!");
        }
    }

    @Test
    public void testRunnableRegistration() {
        boolean[] runnableExecuted = {false};
        NamedCommands.registerCommand("runnable", (Runnable) () -> {
            runnableExecuted[0] = true;
            System.out.println("Runnable executed!");
        });

        Command cmd1 = NamedCommands.getCommand("runnable");
        cmd1.schedule();
        assertTrue("Runnable failed to execute", runnableExecuted[0]);
    }

    @Test
    public void testForeignCommandRegistration() {
        ForeignCommand foreign = new ForeignCommand();
        NamedCommands.registerCommand("foreign", foreign);

        Command cmd2 = NamedCommands.getCommand("foreign");
        cmd2.schedule();
        assertTrue("Foreign command failed to execute", foreign.executed);
    }
}

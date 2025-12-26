package com.pedropathingplus.pathing;

import com.pedropathingplus.pathing.command.Command;
import com.pedropathingplus.pathing.command.InstantCommand;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.firstinspires.ftc.robotcore.external.Telemetry;

/**
 * NamedCommands utility for registering and retrieving commands by name. Similar to WPILib's
 * NamedCommands, but adapted for solverslib.
 *
 * <p>Usage: 1. Register commands in RobotContainer: NamedCommands.registerCommand("IntakeOn", new
 * IntakeOnCommand()); NamedCommands.registerCommand("Shoot", new ShootCommand());
 *
 * <p>2. Use in autonomous paths: NamedCommands.getCommand("IntakeOn").schedule();
 */
public class NamedCommands {
  private static final Map<String, Command> commands = new HashMap<>();
  private static final Map<String, String> commandDescriptions = new HashMap<>();

  /**
   * Register a command with a specific name.
   *
   * @param name The name to register the command under
   * @param command The command to register (can be Command, Runnable, or external command object)
   */
  public static void registerCommand(String name, Object command) {
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Command name cannot be null or empty");
    }

    if (command == null) {
      throw new IllegalArgumentException("Command cannot be null");
    }

    String trimmedName = name.trim();
    Command adaptedCommand = adapt(command);
    commands.put(trimmedName, adaptedCommand);

    // Store the class name of the original object, unless description is updated later
    if (!commandDescriptions.containsKey(trimmedName)) {
        commandDescriptions.put(trimmedName, command.getClass().getSimpleName());
    }
  }

  /**
   * Register a command with a specific name and description.
   *
   * @param name The name to register the command under
   * @param command The command to register
   * @param description Description of what the command does
   */
  public static void registerCommand(String name, Object command, String description) {
    registerCommand(name, command);
    commandDescriptions.put(name.trim(), description);
  }

  /**
   * Adapts a generic object to a Command interface.
   * Supports:
   * 1. com.pedropathingplus.pathing.command.Command (direct)
   * 2. Runnable (wrapped in InstantCommand)
   * 3. Objects with a schedule() method (wrapped via reflection)
   * 4. Objects with a run() method (wrapped via reflection)
   */
  private static Command adapt(Object command) {
    if (command instanceof Command) {
      return (Command) command;
    }

    if (command instanceof Runnable) {
      return new InstantCommand((Runnable) command);
    }

    // Check for schedule() method (common in command-based libraries like SolversLib/WPILib/NextFTC)
    try {
        Method scheduleMethod = command.getClass().getMethod("schedule");
        return new Command() {
            @Override
            public void schedule() {
                try {
                    scheduleMethod.invoke(command);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to invoke schedule() on command: " + command, e);
                }
            }
        };
    } catch (NoSuchMethodException e) {
        // Fallthrough
    }

    // Check for run() method (if not Runnable, but has run method)
    try {
        Method runMethod = command.getClass().getMethod("run");
        return new Command() {
            @Override
            public void schedule() {
                try {
                    runMethod.invoke(command);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to invoke run() on command: " + command, e);
                }
            }
        };
    } catch (NoSuchMethodException e) {
        // Fallthrough
    }

    throw new IllegalArgumentException("Provided object is not a valid Command. " +
            "Must implement Command, Runnable, or have a schedule()/run() method. " +
            "Got: " + command.getClass().getName());
  }

  /**
   * Get a registered command by name.
   *
   * @param name The name of the command to retrieve
   * @return The registered command, or a no-op InstantCommand if not found
   */
  public static Command getCommand(String name) {
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Command name cannot be null or empty");
    }

    String trimmedName = name.trim();
    Command command = commands.get(trimmedName);

    if (command == null) {
      System.err.println("Warning: No command registered with name: " + trimmedName);
      // Return a safe no-op command instead of null
      return new InstantCommand(
          () ->
              System.out.println(
                  "Warning: Attempted to execute unregistered command: " + trimmedName));
    }

    return command;
  }

  /**
   * Check if a command is registered with the given name.
   *
   * @param name The name to check
   * @return true if a command is registered with that name
   */
  public static boolean hasCommand(String name) {
    if (name == null) return false;
    return commands.containsKey(name.trim());
  }

  /**
   * Get the description of a registered command.
   *
   * @param name The name of the command
   * @return The command description, or empty string if not found
   */
  public static String getCommandDescription(String name) {
    if (name == null) return "";
    return commandDescriptions.getOrDefault(name.trim(), "");
  }

  /**
   * Get all registered command names.
   *
   * @return Array of all registered command names
   */
  public static String[] getAllCommandNames() {
    return commands.keySet().toArray(new String[0]);
  }

  /** Clear all registered commands. Useful for testing or resetting state. */
  public static void clearAllCommands() {
    commands.clear();
    commandDescriptions.clear();
  }

  /**
   * Remove a specific command by name.
   *
   * @param name The name of the command to remove
   * @return true if the command was removed, false if it didn't exist
   */
  public static boolean removeCommand(String name) {
    if (name == null) return false;

    String trimmedName = name.trim();
    boolean removed = commands.remove(trimmedName) != null;
    commandDescriptions.remove(trimmedName);

    return removed;
  }

  /**
   * Get the number of registered commands.
   *
   * @return Count of registered commands
   */
  public static int getCommandCount() {
    return commands.size();
  }

  /** Print all registered commands to console. Useful for debugging. */
  public static void listAllCommands(Telemetry tell) {
    tell.addLine("=== Registered NamedCommands ===");
    for (String name : commands.keySet()) {
      // Note: This will print the wrapper class name usually (e.g. named subclass of Command or anonymous),
      // but commandDescriptions has the original name.
      tell.addLine(
          name
              + "  |  "
              + getCommandDescription(name)); // Improved to just show name and description
    }
    tell.addLine("Total: " + getCommandCount() + " commands");
    tell.addLine("===============================");
  }
}

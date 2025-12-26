package com.pedropathingplus.pathing;

import com.pedropathingplus.pathing.command.Command;
import com.pedropathingplus.pathing.command.InstantCommand;
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
   * @param command The command to register
   */
  public static void registerCommand(String name, Command command) {
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Command name cannot be null or empty");
    }

    if (command == null) {
      throw new IllegalArgumentException("Command cannot be null");
    }

    String trimmedName = name.trim();
    commands.put(trimmedName, command);
    commandDescriptions.put(trimmedName, command.getClass().getSimpleName());
  }

  /**
   * Register a command with a specific name and description.
   *
   * @param name The name to register the command under
   * @param command The command to register
   * @param description Description of what the command does
   */
  public static void registerCommand(String name, Command command, String description) {
    registerCommand(name, command);
    commandDescriptions.put(name.trim(), description);
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
      tell.addLine(
          name
              + "  |  "
              + commands.get(name).getClass().getSimpleName()
              + "  |  "
              + getCommandDescription(name));

      //      System.out.printf(
      //          "%-20s -> %s (%s)%n",
      //          name, commands.get(name).getClass().getSimpleName(), getCommandDescription(name));
    }
    tell.addLine("Total: " + getCommandCount() + " commands");
    tell.addLine("===============================");
  }
}

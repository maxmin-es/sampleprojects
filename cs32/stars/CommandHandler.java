package edu.brown.cs.mmines.stars;

/**
 * Command Handler is an interface for all classes handling commands from a
 * REPL.
 *
 * @author Max Mines
 * @param <T>
 *          - T is the type of object inheriting Command Handler
 */
public interface CommandHandler<T> {
  /**
   * checks if inputed string is a command.
   *
   * @param command
   *          - the command as a string to be tested
   * @return - returns null if command worked, otherwise returns error message
   *         as a string
   */
  String isCommand(String command);
}

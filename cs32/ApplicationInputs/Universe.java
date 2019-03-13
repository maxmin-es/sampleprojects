package edu.brown.cs.mmines.ApplicationInputs;

/**
 * Interface for application universes.
 *
 * @author maxmines
 *
 */
public interface Universe {

  /**
   * A method that takes in CommandManager and installs the commands.
   *
   * @param cm
   *          - commandmanager to install commands.
   */
  void installCommands(CommandManager cm);

}

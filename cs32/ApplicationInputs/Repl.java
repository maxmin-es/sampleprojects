package edu.brown.cs.mmines.ApplicationInputs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

/**
 * Class for a generic repl.
 *
 * @author maxmines
 *
 */
public class Repl {
  private CommandManager cm;

  /**
   * Constructor for Repl. Takes in a command manager where it will send all
   * inputs.
   *
   * @param cm
   *          - the command manager.
   */
  public Repl(CommandManager cm) {
    this.cm = cm;
  }

  /**
   * Main run method, starts the repl.
   */
  public void run() {
    String line;
    try {
      BufferedReader reader = new BufferedReader(
          new InputStreamReader(System.in));
      PrintWriter writer = new PrintWriter(System.out);
      while ((line = reader.readLine()) != null) {

        String newInput = line;
        cm.process(newInput, writer);
        writer.flush();
      }
      reader.close();
      writer.close();
    } catch (IOException e) {
      System.out.println("ERROR: Unable to start buffered reader.");
    }

  }
}

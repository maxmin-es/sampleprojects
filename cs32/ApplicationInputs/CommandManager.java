package edu.brown.cs.mmines.ApplicationInputs;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Class for CommandManager which is a generic class that manages commands for
 * programs.
 *
 * @author maxmines
 *
 */
public class CommandManager {
  // hashmap storing regex expressions, and associated command
  private HashMap<String, Command> cmnds = new HashMap<String, Command>();

  /**
   * Interface for commands that can be added to the bank of commands.
   *
   * @author maxmines
   *
   */
  public interface Command {
    /**
     * execute function, needed for each command to execute itself.
     *
     * @param tokens
     *          - the arguments needed to call the function.
     * @param pw
     *          - a printwriter to write any results of the call.
     */
    void execute(List<String> tokens, PrintWriter pw);
  }

  /**
   * Register adds a new command to the bank of commands.
   *
   * @param pattern
   *          - the regex pattern that would match with given command.
   * @param c
   *          - the command itself.
   */
  public void register(String pattern, Command c) {
    cmnds.put(pattern, c);
  }

  /**
   * The method that actually does the processing, generally from a repl input.
   *
   * @param line
   *          - The input line attempting to call a method.
   * @param pw
   *          - a print writer of where to write the results.
   */
  public void process(String line, PrintWriter pw) {
    boolean found = false;
    for (String rgx : cmnds.keySet()) {
      boolean matched = Pattern.matches(rgx, line);
      if (matched) {
        List<String> matchesList = new LinkedList<String>();
        matchesList = stringParser("", line, false, matchesList);
        cmnds.get(rgx).execute(matchesList, pw);
        found = true;
      }
    }
    if (!found) {
      pw.println("ERROR: No command found.");
    }
  }

  /**
   * Method for properly parsing String input into words without dividing up
   * what comes between quotes.
   *
   * @param soFar
   *          - the current word being built so far, in the beginning this is ""
   * @param rest
   *          - the rest of the large string we're parsing
   * @param quote
   *          - whether or not we have seen a quote so far.
   * @param words
   *          - the list of words we have parsed so far.
   * @return
   */
  List<String> stringParser(String soFar, String rest, Boolean quote,
      List<String> words) {
    if (rest.isEmpty()) {
      if (soFar.isEmpty()) {
        return words;
      } else {
        words.add(soFar);
        return words;
      }
    }
    if (rest.charAt(0) == '\"') {
      if (quote) {
        words.add(soFar + "\"");
        return stringParser("", rest.substring(1), false, words);
      } else {
        return stringParser("\"", rest.substring(1), true, words);
      }
    } else if (rest.charAt(0) == ' ') {
      if (quote) {
        return stringParser(soFar + " ", rest.substring(1), true, words);
      } else {
        if (soFar.isEmpty()) {
          return stringParser("", rest.substring(1), false, words);
        } else {
          words.add(soFar);
          return stringParser("", rest.substring(1), false, words);
        }
      }
    } else {
      char current = rest.charAt(0);
      return stringParser(soFar + current, rest.substring(1), quote, words);
    }
  }
}

package edu.brown.cs.mmines.AutoCorrect;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import edu.brown.cs.mmines.ApplicationInputs.CommandManager;
import edu.brown.cs.mmines.ApplicationInputs.CorpusReader;
import edu.brown.cs.mmines.ApplicationInputs.Universe;
import edu.brown.cs.mmines.TextAnalysisUtilities.WhiteSpace;
import edu.brown.cs.mmines.Trie.TrieNode;

/**
 * Class for the universe of AutoCorrect.
 *
 * @author maxmines
 *
 */
public class AutoCorrectUniverse implements Universe {
  private boolean whitespace = false;
  private boolean prefix = false;
  private boolean smart = false;
  private int ledValue = 0;
  private CorpusReader cr = new CorpusReader();
  private TrieNode root = new TrieNode(null);
  private HashSet<String> smartAugmented = new HashSet<String>();

  @Override
  public void installCommands(CommandManager cm) {
    cm.register("^smart(\\s(on|off))?$", new SmartCommand());
    cm.register("^whitespace(\\s(on|off))?$", new WhitespaceCommand());
    cm.register("^prefix(\\s(on|off))?$", new PrefixCommand());
    cm.register("^corpus\\s([^\\s]+)$", new CorpusCommand());
    cm.register("^led(\\s[^\\s]+)?$", new LEDCommand());
    cm.register("^ac\\s.*$", new ACCommand());

  }

  /**
   * Class for the CorpusCommand to load in new text bodies.
   *
   * @author maxmines
   *
   */
  public class CorpusCommand implements CommandManager.Command {
    @Override
    public void execute(List<String> tokens, PrintWriter pw)
        throws IllegalArgumentException {
      if ((tokens.get(0).equals("corpus")) && (tokens.size() == 2)) {
        try {
          cr.readCorpus(tokens.get(1), root);
          pw.println("corpus " + tokens.get(1) + " added");
        } catch (FileNotFoundException fnfe) {
          pw.println(fnfe.getMessage());
        } catch (IOException ioe) {
          pw.println(ioe.getMessage());
        }
      } else {
        pw.println("ERROR: corpus <filepath>");
      }
    }
  }

  /**
   * Class for the PrefixCommand to toggle prefix.
   *
   * @author maxmines
   *
   */
  public class PrefixCommand implements CommandManager.Command {
    @Override
    public void execute(List<String> tokens, PrintWriter pw) {
      if (tokens.size() == 1) {
        if (prefix) {
          pw.println("prefix on");
        } else {
          pw.println("prefix off");
        }
      } else {
        if (tokens.get(1).equals("on")) {
          prefix = true;
        } else if (tokens.get(1).equals("off")) {
          prefix = false;
        }
      }
    }
  }

  /**
   * Class for the WhitespaceCommand to toggle Whitespace.
   *
   * @author maxmines
   *
   */
  public class WhitespaceCommand implements CommandManager.Command {
    @Override
    public void execute(List<String> tokens, PrintWriter pw) {
      if (tokens.size() == 1) {
        if (whitespace) {
          pw.println("whitespace on");
        } else {
          pw.println("whitespace off");
        }
      } else {
        if (tokens.get(1).equals("on")) {
          whitespace = true;
        } else if (tokens.get(1).equals("off")) {
          whitespace = false;
        }
      }
    }
  }

  /**
   * Class for the SmartCommand to toggle Smart mode.
   *
   * @author maxmines
   *
   */
  public class SmartCommand implements CommandManager.Command {
    @Override
    public void execute(List<String> tokens, PrintWriter pw) {
      if (tokens.size() == 1) {
        if (smart) {
          pw.println("smart on");
        } else {
          pw.println("smart off");
        }
      } else {
        if (tokens.get(1).equals("on")) {
          smart = true;
        } else if (tokens.get(1).equals("off")) {
          smart = false;
          if (cr.getInit()) {
            HashMap<String, Integer> unigramDict = cr.getDictionary();
            for (String s : smartAugmented) {
              Integer value = unigramDict.get(s);
              if (value != null) {
                unigramDict.put(s, value - 3);
              }
            }
          }
        }
      }
    }
  }

  /**
   * Class for the LED to change LED.
   *
   * @author maxmines
   *
   */
  public class LEDCommand implements CommandManager.Command {
    @Override
    public void execute(List<String> tokens, PrintWriter pw) {
      if (tokens.size() == 1) {
        pw.println("led " + ledValue);
      } else {
        try {
          int newLED = Integer.parseInt(tokens.get(1));
          if (newLED >= 0) {
            ledValue = newLED;
          } else {
            pw.print("ERROR: LED value must be non-negative.");
          }
        } catch (NumberFormatException nfe) {
          pw.print("ERROR: led <int>");
        }
      }
    }
  }

  /**
   * Class for the AC command.
   *
   * @author maxmines
   *
   */
  public class ACCommand implements CommandManager.Command {
    @Override
    public void execute(List<String> tokens, PrintWriter pw) {
      // spit back input
      String spitback = "ac";

      List<String> tokensWOAC = tokens.subList(1, tokens.size());
      String reunite = "";
      for (String s : tokensWOAC) {
        reunite = reunite + " " + s;
        spitback = spitback + " " + s;
      }
      String[] cleaned = cleanUpAC(reunite);
      pw.println(spitback);
      if (cr.getInit()) {
        if (cleaned.length == 0) {
          pw.println("ERROR: must say something after ac");
        } else if (cleaned.length == 1) {
          List<String> suggestions = generateResponse(cleaned[0], null);
          for (String s : suggestions) {
            pw.println(s);
          }
        } else {
          int numWordsNotAnalyzed = cleaned.length - 2;
          String appendToFront = "";
          if (numWordsNotAnalyzed > 0) {
            for (int i = 0; i < numWordsNotAnalyzed; i++) {
              appendToFront = appendToFront + " " + cleaned[i];
            }
            appendToFront = appendToFront.substring(1);
          }
          if (smart) {
            addSmart(appendToFront.split(" "), false);
          }
          List<String> suggestions = generateResponse(
              cleaned[cleaned.length - 1], cleaned[cleaned.length - 2]);
          if (!appendToFront.equals("")) {
            for (String s : suggestions) {
              pw.println(appendToFront + " " + s);
            }
          } else {
            for (String s : suggestions) {
              pw.println(s);
            }
          }
        }
      }
    }
  }

  /**
   * Method for cleaning up an AC input.
   *
   * @param input
   *          - a string to clean up
   * @return - the string, cleaned and chopped on " "
   */
  public static String[] cleanUpAC(String input) {
    input = input.replaceAll("[^a-zA-Z]", " ");
    input = input.replaceAll("\\s{2,}", " ");
    input = input.replaceAll("\\s$", "");
    input = input.replaceAll("^\\s", "");
    input = input.toLowerCase();
    return input.split(" ");
  }

  /**
   * Generates a list of responses. Option for uni or bigrams.
   *
   * @param word
   *          - the word being corrected
   * @param wordBefore
   *          - the word before in the input, null if none.
   * @return - the ranked list of suggestions.
   */
  public List<String> generateResponse(String word, String wordBefore) {
    Set<String> toReturn = new HashSet<String>();

    // prefix value
    if (this.prefix) {
      List<String> prefixResults = new LinkedList<String>();
      TrieNode lookedUp = root.findNode(word);
      if (lookedUp != null) {
        toReturn.addAll(lookedUp.returnWordChildren(prefixResults));
      }
    }

    // led
    List<String> ledResults = new LinkedList<String>();
    toReturn.addAll(root.lev(word, this.ledValue, 0, ledResults));

    // whitespace
    if (this.whitespace) {
      toReturn.addAll(WhiteSpace.whitespace(word, cr.getDictionary()));
    }

    PriorityQueue<Rankable> sortedToReturn = new PriorityQueue<Rankable>(
        Collections.reverseOrder());
    if (wordBefore == null) {
      for (String s : toReturn) {
        sortedToReturn.add(new Rankable(s, false, word));
      }
    } else {
      for (String s : toReturn) {
        sortedToReturn.add(new Rankable(wordBefore + " " + s, true, word));
      }
    }

    List<String> toReturnRanked = new LinkedList<String>();
    int i = 0;
    while (i < 5 && (!sortedToReturn.isEmpty())) {
      toReturnRanked.add(sortedToReturn.poll().suggestion);
      i++;
    }
    return toReturnRanked;

  }

  /**
   * Rankable class for ranking results for autocorrect.
   *
   * @author maxmines
   *
   */
  public class Rankable implements Comparable<Rankable> {
    private String suggestion;
    private int score = 0;
    private int tieScore = 0;

    Rankable(String suggestion, boolean wordBefore, String wordToCorrect) {
      String[] chopped = suggestion.split(" ");
      this.suggestion = suggestion;

      // unigram
      if (!wordBefore) {
        // check to see if exact match
        if (chopped[0].equals(wordToCorrect)) {
          score = Integer.MAX_VALUE;
        } else {
          if (chopped.length == 2) {
            // case where whitespace has found two, compare on first.
            score = cr.getUnigramValue(chopped[0]);
            tieScore = 0;
          } else {
            score = cr.getUnigramValue(suggestion);
            tieScore = 0;
          }
        }
      } else { // bigram
        if (chopped[1].equals(wordToCorrect)) {
          score = Integer.MAX_VALUE;
          tieScore = Integer.MAX_VALUE;
        } else {
          if (chopped.length == 2) {
            Integer toBeScore = cr.getBigramValue(suggestion);
            if (toBeScore != null) {
              score = toBeScore;
            }
            tieScore = cr.getUnigramValue(chopped[1]);
          } else {
            Integer toBeScore = cr
                .getBigramValue(chopped[0] + " " + chopped[1]);
            if (toBeScore != null) {
              score = toBeScore;
            }
            tieScore = cr.getUnigramValue(chopped[1]);
          }
        }
      }
    }

    @Override
    public int compareTo(Rankable r) {
      int firstComparison = Integer.compare(this.score, r.score);
      if (firstComparison != 0) {
        return firstComparison;
      } else {
        int secondComparison = Integer.compare(this.tieScore, r.tieScore);
        if (secondComparison != 0) {
          return secondComparison;
        } else {
          PriorityQueue<String> pqCompare = new PriorityQueue<String>();
          pqCompare.add(this.suggestion);
          pqCompare.add(r.getSug());
          String ahead = pqCompare.poll();
          if (this.suggestion.equals(ahead)) {
            return 1;
          } else {
            return -1;
          }

        }
      }
    }

    /**
     * Returns this.score.
     *
     * @return - score for this rankable.
     */
    public int getScore() {
      return this.score;
    }

    /**
     * Getter method for suggestion.
     *
     * @return - this.suggestion.
     */
    public String getSug() {
      return this.suggestion;
    }

  }

  /**
   * Getter method for corpus reader.
   *
   * @return - corpus reader.
   */
  public CorpusReader getCr() {
    return cr;
  }

  /**
   * Add smart method, takes in words that have already been typed and not
   * corrected. If they exist in dictionary, we presume that the user likes
   * these words, weight more heavily.
   *
   * @param previousWords
   *          - the words passed and not corrected.
   */
  private void addSmart(String[] previousWords, boolean fromGui) {
    for (String s : previousWords) {
      if (fromGui) {
        if (!this.smartAugmented.contains(s)) {
          HashMap<String, Integer> unigramDict = cr.getDictionary();
          Integer value = unigramDict.get(s);
          if (value != null) {
            unigramDict.put(s, value + 3);
            this.smartAugmented.add(s);
          }
        }
      } else {
        HashMap<String, Integer> unigramDict = cr.getDictionary();
        Integer value = unigramDict.get(s);
        if (value != null) {
          unigramDict.put(s, value + 3);
        }
      }

    }
  }

  /**
   * Prepares the text recieved from a GUI for sending to a processor.
   *
   *
   * @param txtResp
   *          - the inputted string.
   * @return - A string list, the processed input.
   */
  public List<String> fromBoxToResults(String txtResp) {
    // cleaning up what we got back
    String[] cleaned = cleanUpAC(txtResp);
    if (cr.getInit()) {
      if (cleaned.length == 1) {
        return generateResponse(cleaned[0], null);
      } else {
        int numWordsNotAnalyzed = cleaned.length - 2;
        String appendToFront = "";
        if (numWordsNotAnalyzed > 0) {
          for (int i = 0; i < numWordsNotAnalyzed; i++) {
            appendToFront = appendToFront + " " + cleaned[i];
          }
          appendToFront = appendToFront.substring(1);
        }
        if (smart) {
          addSmart(appendToFront.split(" "), true);
        }
        List<String> suggestions = generateResponse(cleaned[cleaned.length - 1],
            cleaned[cleaned.length - 2]);
        List<String> toReturn = new LinkedList<String>();
        for (String s : suggestions) {
          toReturn.add(appendToFront + " " + s);
        }
        return toReturn;
      }
    } else {
      List<String> toReturn = new LinkedList<String>();
      toReturn.add("Need to load in corpus through terminal first.");
      return toReturn;
    }
  }

  /**
   * Method that allows other class to set parameters for this universe without
   * interacting through commands.
   *
   * @param prefixBool
   *          - boolean, prefix on or off.
   * @param led
   *          - int, led max distance.
   * @param whitespaceBool
   *          - boolean, whitespace on or off.
   * @param smartBool
   *          - boolean, smart on or off.
   */
  public void turnMeOn(boolean prefixBool, int led, boolean whitespaceBool,
      boolean smartBool) {
    this.prefix = prefixBool;
    this.whitespace = whitespaceBool;
    this.ledValue = led;
    this.smart = smartBool;
  }

  /**
   * Allows setup of a database as query, not through commandManager.
   *
   * @param db
   *          - path to .sqlite3 database
   * @param query
   *          - the string, a SQL query, that returns desired entries in column
   *          1 as strings.
   * @throws SQLException
   *           - db input or query input was problematic for SQL connection.
   * @throws ClassNotFoundException
   *           - something is wrong with SQL connection.
   */
  public void databaseCorpus(String db, String query)
      throws ClassNotFoundException, SQLException {
    cr.readDatabase(db, query, root);
  }
}

package edu.brown.cs.mmines.TextAnalysisUtilities;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Class for performing whitespace method, which splits strings to figure out
 * typo.
 *
 * @author maxmines
 *
 */
public final class WhiteSpace {
  private WhiteSpace() {
    // private constructor
  }

  /**
   * Method for splitting words in two to find subwords.
   *
   * @param word
   *          - word to be analyzed.
   * @param dictionary
   *          - dictionary to determine what a valid word is.
   * @return - a list of all the bigrams found within.
   */
  public static List<String> whitespace(String word,
      HashMap<String, ?> dictionary) {
    List<String> toReturn = new LinkedList<String>();
    for (int i = 0; i < word.length(); i++) {
      String word1 = word.substring(0, i);
      String word2 = word.substring(i, word.length());
      if (dictionary.containsKey(word1) && dictionary.containsKey(word2)) {
        toReturn.add(word1 + " " + word2);
      }
    }
    return toReturn;
  }
}

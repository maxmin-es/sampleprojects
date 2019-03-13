package edu.brown.cs.mmines.TextAnalysisUtilities;

/**
 * Class for computing levenshtein distance between two words.
 *
 * @author maxmines
 *
 */
public final class LevDistance {
  private LevDistance() {
    // private constructor.
  }

  /**
   * Given two words, returns the LED between the two.
   *
   * @param word1
   *          - first word.
   * @param word2
   *          - second word.
   * @return - an integer representing the LED between two inputs.
   */
  public static int getLevDist(String word1, String word2) {
    char[] w1 = word1.toCharArray();
    char[] w2 = word2.toCharArray();

    int[][] table = new int[w1.length + 1][w2.length + 1];

    // making edge values compared to blank string
    for (int i = 0; i <= w1.length; i++) {
      table[i][0] = i;
    }
    for (int j = 0; j <= w2.length; j++) {
      table[0][j] = j;
    }

    // filling in table
    for (int i = 1; i <= w1.length; i++) {
      for (int j = 1; j <= w2.length; j++) {
        int cost = 1;
        if (w1[i - 1] == w2[j - 1]) {
          cost = 0;
        }
        table[i][j] = Math.min(table[i - 1][j] + 1,
            Math.min(table[i][j - 1] + 1, (table[i - 1][j - 1] + cost)));
      }
    }
    return table[w1.length][w2.length];
  }
}

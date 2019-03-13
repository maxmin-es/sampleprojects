package edu.brown.cs.mmines.Trie;

import java.util.HashMap;
import java.util.List;

import edu.brown.cs.mmines.TextAnalysisUtilities.LevDistance;

/**
 * Class for Tries/trie nodes. To create a new trie, create a new TrieNode, then
 * insert words to it.
 *
 * @author maxmines
 *
 */
public class TrieNode {
  private String word;
  private HashMap<Character, TrieNode> children = new HashMap<Character, TrieNode>();

  /**
   * Constructor for TrieNode.
   *
   * @param word
   *          - the word at the given node. Null if no word.
   */
  public TrieNode(String word) {
    this.word = word;
  }

  /**
   * Public accessor method for insertHelper, allows users to insert new words
   * to Trie. Should only be called on root node.
   *
   * @param wordToInsert
   *          - word to insert.
   */
  public void insert(String wordToInsert) {
    insertHelper(wordToInsert, wordToInsert);
  }

  /**
   * Inserts a new word below the current node. To start, this should be called
   * on root.
   *
   * @param wordToPlace
   *          - the word to be placed.
   * @param rest
   *          - used for recursion. For outside users, this should be the same
   *          as the full word to be placed.
   */
  private void insertHelper(String wordToPlace, String rest) {
    TrieNode nextNode = this.children.get(rest.charAt(0));
    if (nextNode == null) {
      if (rest.length() == 1) {
        this.children.put(rest.charAt(0), new TrieNode(wordToPlace));
      } else {
        TrieNode nodeToPlace = new TrieNode(null);
        this.children.put(rest.charAt(0), nodeToPlace);
        nodeToPlace.insertHelper(wordToPlace, rest.substring(1));
      }
    } else {
      if (rest.length() == 1) {
        nextNode.word = wordToPlace;
      } else {
        nextNode.insertHelper(wordToPlace, rest.substring(1));
      }
    }
  }

  /**
   * Returns all the word children given a specific node.
   *
   * @param toReturn
   *          - the string list where the method puts all the values found.
   * @return a string list with all the word children of the inputted node.
   */
  public List<String> returnWordChildren(List<String> toReturn) {
    if (this.word != null) {
      toReturn.add(this.word);
    }
    for (TrieNode tn : this.children.values()) {
      tn.returnWordChildren(toReturn);
    }
    return toReturn;
  }

  /**
   * Finds the node corresponding to a given word for.
   *
   * @param rest
   *          - on the recursive call, this is the rest of the word. First call
   *          this is full word.
   * @return returns the node if the word (or word so far) was found in the
   *         tree, otherwise null.
   */
  public TrieNode findNode(String rest) {
    if (rest.length() == 0) {
      return this;
    } else {
      TrieNode nextNode = this.children.get(rest.charAt(0));
      if (nextNode == null) {
        return null;
      } else {
        return nextNode.findNode(rest.substring(1));
      }
    }
  }

  /**
   * Getter method for the current node's word.
   *
   * @return a string, the word.
   */
  public String getWord() {
    return this.word;
  }

  /**
   * Method for computing lev distance between a given word using the Trie as
   * the dictionary of words. For the initial call, this method should be called
   * on the root of the tree with a depth of 0
   *
   * @param wordComp
   *          - the word to evaluate lev distances against.
   * @param led
   *          - the maximum distance allowed.
   * @param depth
   *          - the current depth in the tree. At root, depth = 0.
   * @param list
   *          - the place to add new findings.
   * @return - the list containing the results.
   */
  public List<String> lev(String wordComp, int led, int depth,
      List<String> list) {
    if (wordComp.length() + led < depth) {
      return list;
    } else {
      if (this.word != null) {
        int currentLed = LevDistance.getLevDist(wordComp, this.word);
        if (currentLed <= led) {
          list.add(this.word);
        }
      }
      for (TrieNode tn : this.children.values()) {
        tn.lev(wordComp, led, depth + 1, list);
      }
      return list;
    }
  }
}

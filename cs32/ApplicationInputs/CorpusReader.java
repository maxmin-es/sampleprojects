package edu.brown.cs.mmines.ApplicationInputs;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import edu.brown.cs.mmines.Trie.TrieNode;

/**
 * Class for reading in corpora and returns tabs of uni/bigrams. Can optionally
 * add to a given trie.
 *
 * @author maxmines
 *
 */
public class CorpusReader {
  private boolean init = false;
  private HashMap<String, Integer> unigrams = new HashMap<String, Integer>();
  private HashMap<String, Integer> bigrams = new HashMap<String, Integer>();
  private Connection conn;

  /**
   * Method for reading in a database as a corpus.
   *
   * @param db
   *          - path to the sqlite database
   * @param query
   *          - the string representing the SQl query. Must return desired entry
   *          in first column, as string.
   * @param trie
   *          - trie node to add to.
   * @throws ClassNotFoundException
   *           - throws if something is wrong with database setup.
   * @throws SQLException
   *           - throws if something is wrong with SQL, could be user input
   *           query.
   */
  public void readDatabase(String db, String query, TrieNode trie)
      throws ClassNotFoundException, SQLException {

    // Set up a connection and store it in a field
    Class.forName("org.sqlite.JDBC");
    String url = "jdbc:sqlite:" + db;
    conn = DriverManager.getConnection(url);
    Statement stat = conn.createStatement();
    stat.executeUpdate("PRAGMA foreign_keys = ON;");
    stat.close();

    // now reading in database
    // Create a PreparedStatement
    PreparedStatement prep;
    prep = conn.prepareStatement(query);
    ResultSet rs = prep.executeQuery();

    // go through, treat and add to unigrams, etc.
    while (rs.next()) {
      String currentLine = rs.getString(1);
      currentLine = currentLine.replaceAll("[^a-zA-Z]", " ");
      currentLine = currentLine.replaceAll("\\s{2,}", " ");
      currentLine = currentLine.replaceAll("\\s$", "");
      currentLine = currentLine.toLowerCase();
      String[] chopped = currentLine.split(" ");

      addUnigrams(chopped, trie);
      addBigrams(chopped, null);

    }
    init = true;
  }

  /**
   * Reads in a new corpus to the class library.
   *
   * @param filepath
   *          - a string to the file we wish to read in.
   * @param trie
   *          - root of Trie where this corpus will add to. If null, doesn't.
   * @throws FileNotFoundException
   *           not found exception when can't read file.
   * @throws IOException
   *           when reader throws one too.
   */
  public void readCorpus(String filepath, TrieNode trie)
      throws FileNotFoundException, IOException {
    try {
      FileReader fr = new FileReader(filepath);
      BufferedReader br = new BufferedReader(fr);

      String currentLine;
      String lastLineWord = null;
      while ((currentLine = br.readLine()) != null) {
        currentLine = currentLine.replaceAll("[^a-zA-Z]", " ");
        currentLine = currentLine.replaceAll("\\s{2,}", " ");
        currentLine = currentLine.replaceAll("\\s$", "");
        currentLine = currentLine.toLowerCase();
        String[] chopped = currentLine.split(" ");

        if (chopped.length != 0) {
          addUnigrams(chopped, trie);
          addBigrams(chopped, lastLineWord);
          lastLineWord = chopped[chopped.length - 1];
        } else {
          lastLineWord = null;
        }
      }
      init = true;

    } catch (FileNotFoundException e) {
      throw new FileNotFoundException(
          "ERROR: CorpusReader could not locate file.");
    } catch (IOException ioe) {
      throw new IOException("ERROR: Something wrong with bufferedreader.");
    }
  }

  /**
   * Alternative method constructor for readCorpus. In this case, no trie is
   * specified, and thus the read in doesn't add to trie.
   *
   * @param filepath
   *          - a string to the file we wish to read in.
   * @throws FileNotFoundException
   *           - not found exception when can't read file.
   * @throws IOException
   *           - something was wrong with buffered reader.
   */
  public void readCorpus(String filepath)
      throws FileNotFoundException, IOException {
    readCorpus(filepath, null);
  }

  /**
   * Short method for adding unigrams to unigrams hashmap.
   *
   * @param currentLine
   *          - the current line being analyzed.
   */
  private void addUnigrams(String[] currentLine, TrieNode trie) {
    for (String word : currentLine) {
      if (word.length() != 0) {
        if (unigrams.containsKey(word)) {
          unigrams.put(word, (unigrams.get(word) + 1));
        } else {
          unigrams.put(word, 1);
          if (trie != null) {
            trie.insert(word);
          }
        }
      }
    }
  }

  /**
   * Adds bigrams to the bigrams hashmap.
   *
   * @param currentLine
   *          - an array of strings representing the current line of text.
   * @param previousLineWord
   *          - the last word of the previous line if any.
   */
  private void addBigrams(String[] currentLine, String previousLineWord) {
    // dealing with case of combining with line before
    if (previousLineWord != null) {
      String toAdd = previousLineWord + " " + currentLine[0];
      if (bigrams.containsKey(toAdd)) {
        bigrams.put(toAdd, bigrams.get(toAdd) + 1);
      } else {
        bigrams.put(toAdd, 1);
      }
    }

    // adding bigrams within this line
    int i = 0;
    while (i <= currentLine.length - 2) {
      String toAdd2 = currentLine[i] + " " + currentLine[i + 1];
      if (bigrams.containsKey(toAdd2)) {
        bigrams.put(toAdd2, bigrams.get(toAdd2) + 1);
      } else {
        bigrams.put(toAdd2, 1);
      }
      i++;
    }
  }

  /**
   * A getter method saying whether corpusreader has been initialized.
   *
   * @return - boolean true if initialized.
   */
  public boolean getInit() {
    return this.init;
  }

  /**
   * Getter method for accessing the unigram hashmap.
   *
   * @param unigram
   *          - the key to be searched.
   * @return - the value Integer to be returned.
   */
  public Integer getUnigramValue(String unigram) {
    return unigrams.get(unigram);
  }

  /**
   * Getter method for accessing the bigram hashmap.
   *
   * @param bigram
   *          - the key to be searched.
   * @return - the value Integer to be returned.
   */
  public Integer getBigramValue(String bigram) {
    return bigrams.get(bigram);
  }

  /**
   * Getter method for the unigram dicitonary.
   *
   * @return - the unigram dict.
   */
  public HashMap<String, Integer> getDictionary() {
    return this.unigrams;
  }
}

package edu.brown.cs.mmines.Database;

import java.util.List;

/**
 * Generic interface for all database readers.
 *
 * @author maxmines
 *
 */
public interface DatabaseReader {

  /**
   * Method for making query, returns a list with the results, null if query
   * failed.
   *
   * @param queryText
   *          - string used for making query.
   * @return - list of strings, results, otherwise null if query failed.
   */
  List<String> makeQuery(String queryText);

}

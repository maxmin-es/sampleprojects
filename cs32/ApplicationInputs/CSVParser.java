package edu.brown.cs.mmines.ApplicationInputs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.MalformedParametersException;
import java.util.LinkedList;
import java.util.List;

/**
 * Class for parsing CSV files, handcrafted.
 */
public final class CSVParser {

  /**
   * Private constructor.
   */
  private CSVParser() {
    // not called
  }

  /**
   * parseFile method which parses the .csv file for content.
   *
   * @param filePath
   *          - a string representing the path to the desired file to be parsed.
   * @param headstandards
   *          - a string representing what the user expects the first line to
   *          look like. In practical terms, what are the categories of the
   *          data. In the case of cs32 project "Stars" this looks like
   *          "StarID,ProperName,X,Y,Z".
   * @return - returns a list of String[]s each array containing the data from a
   *         line of the inputted file. The array entries are the dimensions of
   *         the data found in the file, confirmed by the headstandards input.
   * @throws MalformedParametersException
   *           - parser found and opened file, but content was malformed.
   * @throws FileNotFoundException
   *           - trouble opening or reading file
   */

  public static List<String[]> parseFile(String filePath, String headstandards)
      throws MalformedParametersException, FileNotFoundException {
    int numberOfParams = headstandards.split(",").length;
    List<String[]> toReturn = new LinkedList<>();
    File file;
    BufferedReader br;
    try {
      file = new File(filePath);
      br = new BufferedReader(new FileReader(file));

      String firstLine = br.readLine();
      if (!(firstLine.equals(headstandards))) {
        throw new MalformedParametersException(
            "first line doesn't match header standards");
      }
      String line = br.readLine();
      while (line != null) {
        String[] parsed = line.split(",");
        if (parsed.length != numberOfParams) {
          throw new MalformedParametersException("CSV data is malformed");
        } else {
          toReturn.add(parsed);
        }

        line = br.readLine();
      }
    } catch (IOException e) {
      throw new FileNotFoundException("couldn't open file");
    }

    try {
      br.close();
    } catch (IOException e) {
      throw new FileNotFoundException("trouble closing reader");
    }

    return toReturn;

  }

}

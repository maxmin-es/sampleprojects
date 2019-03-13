package edu.brown.cs.mmines.stars;

import java.io.FileNotFoundException;
import java.lang.reflect.MalformedParametersException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import edu.brown.cs.mmines.ApplicationInputs.CSVParser;

/**
 * A handler for inputs to the Stars application from cs32.
 */
public class StarsHandler implements CommandHandler<StarsHandler> {
  private KDTree<Star> tree = null;
  private HashMap<String, Star> namesAndStars = new HashMap<String, Star>();
  private List<Star> results = new LinkedList<Star>();
  private String initMsg = null;

  @Override
  public String isCommand(String command) {
    List<String> parsedList = stringParser("", command, false,
        new LinkedList<String>());
    String[] parsedCommand = parsedList.toArray(new String[parsedList.size()]);
    parsedCommand[0] = parsedCommand[0].toLowerCase();

    if (this.tree == null) {
      if ((parsedCommand.length == 2) && (parsedCommand[0].equals("stars"))) {
        return buildNewTree(parsedCommand[1]);
      } else {
        return "ERROR: stars <filepath>";
      }
    } else {
      if ((parsedCommand.length == 2) && (parsedCommand[0].equals("stars"))) {
        return buildNewTree(parsedCommand[1]);
      } else if ((parsedCommand.length == 3)
          && (parsedCommand[0].equals("neighbors"))) {
        return neighborsWithName(parsedCommand[1], parsedCommand[2]);
      } else if ((parsedCommand.length == 5)
          && (parsedCommand[0].equals("neighbors"))) {
        return neighborsCoords(parsedCommand[1], parsedCommand[2],
            parsedCommand[3], parsedCommand[4]);
      } else if ((parsedCommand[0].equals("radius"))
          && (parsedCommand.length == 3)) {
        return radiusWithName(parsedCommand[1], parsedCommand[2]);
      } else if ((parsedCommand[0].equals("radius"))
          && (parsedCommand.length == 5)) {
        return radiusCoords(parsedCommand[1], parsedCommand[2],
            parsedCommand[3], parsedCommand[4]);
      } else {
        return "ERROR: Not a valid command.";
      }
    }
  }

  /**
   * Attempts to build a new tree with data from a csv filepath. Also populates
   * the hashmap of names --> star
   *
   * @param filePath
   *          - a filepath hopefully leading to a well-formed csv file.
   * @return - a string, null if all went well, otherwise the error message.
   */
  private String buildNewTree(String filePath) {
    List<String[]> stringExtractedData = new LinkedList<>();

    try {
      stringExtractedData = CSVParser.parseFile(filePath,
          "StarID,ProperName,X,Y,Z");
    } catch (FileNotFoundException e) {
      return "ERROR: During csv parse, file not found.";
    } catch (MalformedParametersException f) {
      return "ERROR: CSV file malformed.";
    }

    List<Star> stars = new LinkedList<>();
    try {
      for (String[] s : stringExtractedData) {
        int id = Integer.parseInt(s[0]);
        String name = s[1];
        double[] coords = new double[3];
        coords[0] = Double.parseDouble(s[2]);
        coords[1] = Double.parseDouble(s[3]);
        coords[2] = Double.parseDouble(s[4]);
        Star s1 = new Star(id, name, coords);
        stars.add(s1);

        if (!(name.equals(""))) {
          this.namesAndStars.put(name, s1);
        }
      }
    } catch (NumberFormatException e) {
      return "ERROR: Unable to parse CSV "
          + "information into id and/or coordinates";
    }

    tree = new KDTree<>(stars);
    this.initMsg = "Read " + stars.size() + " stars from " + filePath;
    System.out.println(initMsg);
    return "";
  }

  /**
   * Attempts to call the neighbors method, but first checks that the string
   * inputs are satisfactory.
   *
   * @param k
   *          - a string, hopefully which can be converted to a positive
   *          integer, number of neighbors
   * @param name
   *          - the name of the desired star. Method will look this up in
   *          hashmap.
   * @return - null if all is successful, otherwise a string error message
   */
  private String neighborsWithName(String k, String name) {
    int neighbors;
    if ((name.charAt(0) == '\"') || (name.charAt(name.length() - 1) == '\"')) {
      name = name.substring(1, name.length() - 1);
    } else {
      return "ERROR: Name must be in quotes.";
    }
    try {
      neighbors = Integer.parseInt(k);
      if (neighbors < 0) {
        return "ERROR: Number of neighbors must be non-negative.";
      }
      if (neighbors == 0) {
        this.results = new LinkedList<Star>();
        return null;
      }
      Star returnedStar = namesAndStars.get(name);
      if (returnedStar == null) {
        return "ERROR: Could not find a star with that name.";
      }
      List<Star> returnedList = tree.nearestNeighbor(returnedStar,
          neighbors + 1);
      returnedList.remove(returnedStar);
      this.results = returnedList;
      for (Star st : returnedList) {
        System.out.println(st.toString());
      }
      return null;

    } catch (NumberFormatException e) {
      return "ERROR: Number of neighbors must be an integer.";
    }
  }

  /**
   * Attempts to call the neighbors method, but first checks that the string
   * inputs are satisfactory.
   *
   * @param k
   *          - a string, hopefully which can be converted to a positive
   *          integer, number of neighbors
   * @param x
   *          - a string, hopefully parsable to a double, x coord
   * @param y
   *          - a string, hopefully parsable to a double, y coord
   * @param z
   *          - a string, hopefully parsable to a double, z coord
   * @return - null if all is successful, otherwise a string error message
   */
  private String neighborsCoords(String k, String x, String y, String z) {
    int neighbors;
    try {
      neighbors = Integer.parseInt(k);
      if (neighbors < 0) {
        return "ERROR: Number of neighbors must be non-negative.";
      }
      if (neighbors == 0) {
        this.results = new LinkedList<Star>();
        return null;
      }
    } catch (NumberFormatException e) {
      return "ERROR: Number of neighbors must be an integer.";
    }
    double[] coords = new double[3];
    try {
      double coordx = Double.parseDouble(x);
      double coordy = Double.parseDouble(y);
      double coordz = Double.parseDouble(z);

      coords[0] = coordx;
      coords[1] = coordy;
      coords[2] = coordz;
    } catch (NumberFormatException e) {
      return "ERROR: Coordinates must be parsable to doubles.";
    }
    Star s = new Star(0, "", coords);
    List<Star> returnedList = tree.nearestNeighbor(s, neighbors);
    this.results = returnedList;
    for (Star st : returnedList) {
      System.out.println(st.toString());
    }
    return null;
  }

  /**
   * Attempts to call radiusSearch given 2 strings which hopefully can be
   * successfully converted to the proper params.
   *
   * @param r
   *          - a string hopefully convertable to a non-zero double representing
   *          radius.
   * @param name
   *          - hopefully the name of a star, given in quotes, which is part of
   *          the tree.
   * @return - A string, null if all went well, otherwise the error message.
   */
  private String radiusWithName(String r, String name) {
    double radius;
    if ((name.charAt(0) == '\"') || (name.charAt(name.length() - 1) == '\"')) {
      name = name.substring(1, name.length() - 1);
    } else {
      return "ERROR: Name must be in quotes.";
    }
    try {
      radius = Double.parseDouble(r);
      if (radius < 0) {
        return "ERROR: radius must be non-negative value.";
      }
    } catch (NumberFormatException e) {
      return "ERROR: radius must be parsable to double.";
    }

    Star returnedStar = namesAndStars.get(name);
    if (returnedStar == null) {
      return "ERROR: Could not find a star with that name.";
    }

    List<Star> returnedList = tree.radiusSearch(radius, returnedStar);
    returnedList.remove(returnedStar);
    this.results = returnedList;
    for (Star st : returnedList) {
      System.out.println(st.toString());
    }
    return null;
  }

  /**
   * Attempts to call radiusSearch given 4 strings which hopefully can be
   * successfully converted to the proper params.
   *
   * @param r
   *          - String hopefully convertable to a non-zero double representing
   *          the radius.
   * @param x
   *          - String hopefully representing a double, x cord.
   * @param y
   *          - String hopefully representing a double, y cord.
   * @param z
   *          - String hopefully representing a double, z cord.
   * @return - a string, null if all went well, otherwise the error message.
   */
  private String radiusCoords(String r, String x, String y, String z) {
    double radius;
    try {
      radius = Double.parseDouble(r);
      if (radius <= 0) {
        return "ERROR: radius must be non-negative value.";
      }
    } catch (NumberFormatException e) {
      return "ERROR: radius must be parsable to double.";
    }

    double[] coords = new double[3];
    try {
      double coordx = Double.parseDouble(x);
      double coordy = Double.parseDouble(y);
      double coordz = Double.parseDouble(z);

      coords[0] = coordx;
      coords[1] = coordy;
      coords[2] = coordz;
    } catch (NumberFormatException e) {
      return "ERROR: coordinates must be parsable to doubles.";
    }

    Star s = new Star(0, "", coords);
    List<Star> returnedList = tree.radiusSearch(radius, s);
    this.results = returnedList;
    for (Star st : returnedList) {
      System.out.println(st.toString());
    }
    return null;
  }

  /**
   * Getter for results, used by GUI.
   *
   * @return - a list of stars, the result from the last call.
   */
  List<Star> getResults() {
    return this.results;
  }

  /**
   * Getter for initialized tree message (for GUI).
   *
   * @return - the init msg.
   */
  String initializedMsg() {
    return this.initMsg;
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

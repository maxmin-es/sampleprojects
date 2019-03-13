package edu.brown.cs.mmines.stars;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.lang.reflect.MalformedParametersException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import edu.brown.cs.mmines.ApplicationInputs.CSVParser;
import edu.brown.cs.mmines.ApplicationInputs.CommandManager;
import edu.brown.cs.mmines.ApplicationInputs.Universe;

/**
 * Class for Stars universe.
 *
 * @author maxmines
 *
 */
public class StarsUniverse implements Universe {
  private KDTree<Star> tree = null;
  private HashMap<String, Star> namesAndStars = new HashMap<String, Star>();
  private List<Star> results = new LinkedList<Star>();
  private String initMsg = null;

  @Override
  public void installCommands(CommandManager cm) {
    cm.register("^stars\\s([^\\s]+)$", new BuildTreeCommand());
    cm.register("^neighbors\\s([^\\s]+)\\s\\\".+\\\"$",
        new NeighborsNameCommand());
    cm.register("^neighbors\\s([^\\s]+)\\s([^\\s]+)\\s([^\\s]+)\\s([^\\s]+)$",
        new NeighborsCoordsCommand());
    cm.register("^radius\\s([^\\s]+)\\s\\\".+\\\"$", new RadiusNameCommand());
    cm.register("^radius\\s([^\\s]+)\\s([^\\s]+)\\s([^\\s]+)\\s([^\\s]+)$",
        new RadiusCoordsCommand());
  }

  /**
   * Class for the CorpusCommand to load in new text bodies.
   *
   * @author maxmines
   *
   */
  public class BuildTreeCommand implements CommandManager.Command {
    @Override
    public void execute(List<String> tokens, PrintWriter pw) {
      String filePath = tokens.get(1).toLowerCase();
      List<String[]> stringExtractedData = new LinkedList<>();
      boolean errored = false;

      try {
        stringExtractedData = CSVParser.parseFile(filePath,
            "StarID,ProperName,X,Y,Z");
      } catch (FileNotFoundException e) {
        pw.println("ERROR: During csv parse, file not found.");
        errored = true;
      } catch (MalformedParametersException f) {
        pw.println("ERROR: CSV file malformed.");
        errored = true;
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
            namesAndStars.put(name, s1);
          }
        }
      } catch (NumberFormatException e) {
        pw.println("ERROR: Unable to parse CSV "
            + "information into id and/or coordinates");
        errored = true;
      }

      if (!errored) {
        tree = new KDTree<>(stars);
        initMsg = "Read " + stars.size() + " stars from " + filePath;
        pw.println((initMsg));
      }
    }
  }

  /**
   * Class for Neighbors command with the name of the star.
   *
   * @author maxmines
   *
   */
  public class NeighborsNameCommand implements CommandManager.Command {
    @Override
    public void execute(List<String> tokens, PrintWriter pw) {
      if (!(tree == null)) {

        String k = tokens.get(1);
        String name = tokens.get(2);
        int neighbors;
        boolean errored = false;
        if ((name.charAt(0) == '\"')
            || (name.charAt(name.length() - 1) == '\"')) {
          name = name.substring(1, name.length() - 1);
        } else {
          pw.println("ERROR: Name must be in quotes.");
          errored = true;
        }
        try {
          neighbors = Integer.parseInt(k);
          if (neighbors < 0) {
            pw.println("ERROR: Number of neighbors must be non-negative.");
            errored = true;
          }
          if (neighbors == 0) {
            results = new LinkedList<Star>();
            errored = true;
          }
          Star returnedStar = namesAndStars.get(name);
          if (returnedStar == null) {
            pw.println("ERROR: Could not find a star with that name.");
            errored = true;
          }

          if (!errored) {
            List<Star> returnedList = tree.nearestNeighbor(returnedStar,
                neighbors + 1);
            returnedList.remove(returnedStar);
            results = returnedList;
            for (Star st : returnedList) {
              pw.println(st.toString());
            }
          }
        } catch (NumberFormatException e) {
          pw.println("ERROR: Number of neighbors must be an integer.");
        }
      } else {
        pw.println("ERROR: instantiate tree first.");
      }
    }
  }

  /**
   * Class for neighbors command with coordinates.
   *
   * @author maxmines
   *
   */
  public class NeighborsCoordsCommand implements CommandManager.Command {
    @Override
    public void execute(List<String> tokens, PrintWriter pw) {
      if (!(tree == null)) {

        String k = tokens.get(1);
        String x = tokens.get(2);
        String y = tokens.get(3);
        String z = tokens.get(4);
        boolean errored = false;

        int neighbors = -1;
        try {
          neighbors = Integer.parseInt(k);
          if (neighbors < 0) {
            pw.println("ERROR: Number of neighbors must be non-negative.");
            errored = true;
          }
          if (neighbors == 0) {
            results = new LinkedList<Star>();
            errored = true;
          }
        } catch (NumberFormatException e) {
          pw.println("ERROR: Number of neighbors must be an integer.");
          errored = true;

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
          pw.println("ERROR: Coordinates must be parsable to doubles.");
          errored = true;
        }

        if (!errored) {
          Star s = new Star(0, "", coords);
          List<Star> returnedList = tree.nearestNeighbor(s, neighbors);
          results = returnedList;
          for (Star st : returnedList) {
            pw.println(st.toString());
          }
        }
      } else {
        pw.println("ERROR: instantiate tree first.");
      }
    }
  }

  /**
   * Class for radius command with name.
   *
   * @author maxmines
   *
   */
  public class RadiusNameCommand implements CommandManager.Command {
    @Override
    public void execute(List<String> tokens, PrintWriter pw) {
      if (!(tree == null)) {
        String r = tokens.get(1);
        String name = tokens.get(2);
        double radius = -1;
        boolean errored = false;
        if ((name.charAt(0) == '\"')
            || (name.charAt(name.length() - 1) == '\"')) {
          name = name.substring(1, name.length() - 1);
        } else {
          pw.println("ERROR: Name must be in quotes.");
          errored = true;
        }
        try {
          radius = Double.parseDouble(r);
          if (radius < 0) {
            pw.println("ERROR: radius must be non-negative value.");
            errored = true;
          }
        } catch (NumberFormatException e) {
          pw.println("ERROR: radius must be parsable to double.");
          errored = true;
        }

        Star returnedStar = namesAndStars.get(name);
        if (returnedStar == null) {
          pw.println("ERROR: Could not find a star with that name.");
          errored = true;
        }

        if (!errored) {
          List<Star> returnedList = tree.radiusSearch(radius, returnedStar);
          returnedList.remove(returnedStar);
          results = returnedList;
          for (Star st : returnedList) {
            pw.println(st.toString());
          }
        }
      } else {
        pw.println("ERROR: instantiate tree first.");
      }
    }
  }

  /**
   * Class for radius command with coordinates.
   *
   * @author maxmines
   *
   */
  public class RadiusCoordsCommand implements CommandManager.Command {
    @Override
    public void execute(List<String> tokens, PrintWriter pw) {
      if (!(tree == null)) {
        String r = tokens.get(1);
        String x = tokens.get(2);
        String y = tokens.get(3);
        String z = tokens.get(4);
        boolean errored = false;
        double radius = -1.0;
        try {
          radius = Double.parseDouble(r);
          if (radius <= 0) {
            pw.println("ERROR: radius must be non-negative value.");
            errored = true;
          }
        } catch (NumberFormatException e) {
          pw.println("ERROR: radius must be parsable to double.");
          errored = true;
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
          pw.println("ERROR: coordinates must be parsable to doubles.");
          errored = true;
        }

        if (!errored) {
          Star s = new Star(0, "", coords);
          List<Star> returnedList = tree.radiusSearch(radius, s);
          results = returnedList;
          for (Star st : returnedList) {
            pw.println(st.toString());
          }
        }
      } else {
        pw.println("ERROR: instantiate tree first.");
      }
    }
  }

  /**
   * Getter method for results.
   *
   * @return - a list of stars, result of previous call.
   */
  public List<Star> returnResults() {
    return this.results;
  }
}

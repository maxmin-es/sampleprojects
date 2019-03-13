package edu.brown.cs.mmines.Bacon;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import edu.brown.cs.mmines.ApplicationInputs.CommandManager;
import edu.brown.cs.mmines.ApplicationInputs.Universe;
import edu.brown.cs.mmines.AutoCorrect.AutoCorrectUniverse;
import edu.brown.cs.mmines.Bacon.BaconProxy.Actor;
import edu.brown.cs.mmines.Bacon.BaconProxy.Film;
import edu.brown.cs.mmines.Graphs.Dijkstra;

/**
 * Class for the Bacon Universe.
 *
 * @author maxmines
 *
 */
public class BaconUniverse implements Universe {
  private BaconProxy bp = null;
  private AutoCorrectUniverse ac;

  @Override
  public void installCommands(CommandManager cm) {
    cm.register("^mdb\\s([^\\s]+)$", new MDBCommand());
    cm.register("^connect\\s\".+\"\\s\".+\"$", new ConnectCommand());
  }

  /**
   * Class for the MDBCommand to load new database.
   *
   * @author maxmines
   *
   */
  private class MDBCommand implements CommandManager.Command {
    @Override
    public void execute(List<String> tokens, PrintWriter pw)
        throws IllegalArgumentException {
      try {
        // if there already was a proxy, this will reassign the pointer, thus
        // making old one
        // eligible for garbage collection.
        bp = new BaconProxy(tokens.get(1));
        ac = new AutoCorrectUniverse();
        ac.turnMeOn(true, 1, true, false);
        ac.databaseCorpus(tokens.get(1), "SELECT name FROM actor");
        pw.println("db set to " + tokens.get(1));
      } catch (SQLException sql) {
        sql.printStackTrace();
        pw.println("ERROR: unable to initiate database");
      } catch (ClassNotFoundException c) {
        pw.println("ERROR: unable to initiate database");
      } catch (IOException io) {
        pw.println("ERROR: file doesn't exist.");
      }
    }
  }

  /**
   * Class for the ConnectCommand to connect two actors names.
   *
   * @author maxmines
   *
   */
  private class ConnectCommand implements CommandManager.Command {
    @Override
    public void execute(List<String> tokens, PrintWriter pw)
        throws IllegalArgumentException {
      try {
        if (bp != null) {
          bp.reset();
          // edit out quotes
          String startName = tokens.get(1).substring(1,
              tokens.get(1).length() - 1);
          String endName = tokens.get(2).substring(1,
              tokens.get(2).length() - 1);
          if (startName.equals(endName)) {
            pw.print("ERROR: you gave same actor twice");
          } else {
            Dijkstra<Actor, Film> dijk = new Dijkstra<Actor, Film>();

            Actor start = bp.getActor(startName);
            Actor end = bp.getActor(endName);

            if ((start != null) && (end != null)) {
              List<Film> path = dijk.dijkstra(start, end);

              // case where no path
              if (path.isEmpty()) {
                pw.println(startName + " -/- " + endName);
              } else {
                Stack<String> toReturn = new Stack<String>();
                Actor storedActor = bp.getActor(endName);
                Film currentPrev = bp.getActor(endName).getPrev();
                while (currentPrev != null) {
                  Actor prevActor = currentPrev.otherVertex(storedActor);
                  String segment = prevActor.getName() + " -> "
                      + storedActor.getName() + " : " + currentPrev.getName();
                  toReturn.push(segment);
                  storedActor = prevActor;
                  currentPrev = prevActor.getPrev();
                }
                while (!toReturn.isEmpty()) {
                  pw.println(toReturn.pop());
                }
              }
            } else {
              pw.println("ERROR: Actor not found.");
            }
          }
        } else {
          pw.println("ERROR: must add database first.");
        }
      } catch (SQLException sql) {
        pw.println("ERROR: Film id/name not found or mismatch");
      }
    }
  }

  /**
   * Getter method for autocorrect universe.
   *
   * @return - autocorrect associated with this.
   */
  public AutoCorrectUniverse getAC() {
    return this.ac;
  }

  /**
   * Method for running connect searches without having to go through command
   * manager. For gui.
   *
   * @param actor1
   *          - start actor name.
   * @param actor2
   *          - end actor name.
   * @return null if no path found, otherwise actor/film/actor path.
   * @throws IllegalArgumentException
   *           - thrown if actor doesn't exist in db.
   * @throws IOException
   *           - thrown if no database loaded.
   * @throws SQLException
   *           - thrown if something's wrong with film name.
   */
  public List<String> connectNonCommand(String actor1, String actor2)
      throws IllegalArgumentException, IOException, SQLException {
    if (bp != null) {
      bp.reset();
      Actor start = bp.getActor(actor1);
      Actor end = bp.getActor(actor2);
      Dijkstra<Actor, Film> dijk = new Dijkstra<Actor, Film>();
      List<String> toReturn = new LinkedList<String>();

      if ((start != null) && (end != null)) {
        List<Film> path = dijk.dijkstra(start, end);
        if (path.isEmpty()) {
          String line = "<a href=\"/bacon/actor/"
              + URLEncoder.encode(start.getId()) + "\">" + start.getName()
              + "</a>" + " shares no connections with "
              + "<a href=\"/bacon/actor/" + URLEncoder.encode(end.getId())
              + "\">" + end.getName() + "</a>";
          toReturn.add(line);

        } else {
          Actor storedActor = bp.getActor(actor2);
          Film currentPrev = bp.getActor(actor2).getPrev();
          while (currentPrev != null) {
            Actor prevActor = currentPrev.otherVertex(storedActor);
            String line = "<a href=\"/bacon/actor/"
                + URLEncoder.encode(prevActor.getId()) + "\">"
                + prevActor.getName() + "</a>" + " -> "
                + "<a href=\"/bacon/actor/"
                + URLEncoder.encode(storedActor.getId()) + "\">"
                + storedActor.getName() + "</a>" + " : "
                + "<a href=\"/bacon/film/"
                + URLEncoder.encode(currentPrev.getId()) + "\">"
                + currentPrev.getName() + "</a></br>";

            storedActor = prevActor;
            currentPrev = prevActor.getPrev();
            toReturn.add(line);
          }
          Collections.reverse(toReturn);
        }
        return toReturn;

      } else {
        throw new IllegalArgumentException("ERROR: Actor not found.");
      }
    } else {
      throw new IOException("ERROR: must add database first.");
    }
  }

  /**
   * Allows outside classes to get actor name given id.
   *
   * @param id
   *          - actor's id.
   * @return - actor's name.
   */
  public String getActorName(String id) {
    try {
      return bp.getNameFromID(id);
    } catch (SQLException e) {
      System.out.println("Something's wrong.");
      return null;
    }
  }

  /**
   * Gets all the films of the actor as links.
   *
   * @param id
   *          - actor's id.
   * @return list of strings, linkable to film.
   * @throws SQLException
   *           - shouldn't happen.
   */
  public List<String> getFilmsOfActor(String id) throws SQLException {
    return bp.getFilmsOfActor(id);
  }

  /**
   * Gets the actors of given film.
   *
   * @param id
   *          - film id
   * @return - list of actors as html links
   * @throws SQLException
   *           - shoudln't happen.
   */
  public List<String> getActorsOfFilm(String id) throws SQLException {
    return bp.getActorsOfFilm(id);
  }

  /**
   * Gets film name given id.
   *
   * @param id
   *          - the id.
   * @return - the film name.
   * @throws SQLException
   *           - shouldn't happen.
   */
  public String getFilmName(String id) throws SQLException {
    return bp.getFilmName(id);
  }

}

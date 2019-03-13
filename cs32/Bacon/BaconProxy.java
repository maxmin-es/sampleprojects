package edu.brown.cs.mmines.Bacon;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import edu.brown.cs.mmines.Graphs.Edge;
import edu.brown.cs.mmines.Graphs.Vertex;

/**
 * Class for SQLReader, for interacting with SQL databases.
 *
 * @author maxmines
 *
 */
public class BaconProxy {

  // CACHE: actor name (string) --> Actor (vertex)
  private LoadingCache<String, Actor> actorCache = CacheBuilder.newBuilder()
      .maximumSize(500).build(new CacheLoader<String, Actor>() {

        @Override
        public Actor load(String name) throws SQLException {
          return new Actor(name);
        }
      });

  private Connection conn;

  /**
   * Constructor for BaconPRoxy.
   *
   * @param db
   *          - path to database
   * @throws SQLException
   *           - if something's wrong with sql
   * @throws ClassNotFoundException
   *           - shouldn't happen.
   * @throws IOException
   *           - if file doesn't exist.
   */
  BaconProxy(String db)
      throws SQLException, ClassNotFoundException, IOException {

    // check if file exists
    File dbFile = new File(db);
    if (!dbFile.exists()) {
      throw new IOException("File doesn't exist");
    }
    // Set up a connection and store it in a field
    Class.forName("org.sqlite.JDBC");
    String url = "jdbc:sqlite:" + db;
    conn = DriverManager.getConnection(url);
    Statement stat = conn.createStatement();
    stat.executeUpdate("PRAGMA foreign_keys = ON;");
    stat.close();
  }

  /**
   * Method for getting an actor from the proxy.
   *
   * @param actorName
   *          - name of actor wished to receive
   * @return - actor object
   */
  public Actor getActor(String actorName) {
    try {
      return actorCache.get(actorName);
    } catch (ExecutionException e) {
      // means that no actor was found.
      return null;
    }
  }

  /**
   * Method for resetting all distance and prev values of actors stored in
   * cache.
   */
  public void reset() {
    for (String s : actorCache.asMap().keySet()) {
      try {
        Actor a = actorCache.get(s);
        a.setDistance(Double.POSITIVE_INFINITY);
        a.setPrev(null);
      } catch (ExecutionException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Method for getting edges, given an actor id.
   *
   * @param id
   *          - an actor id
   * @param name
   *          - actor name
   * @return - a list of Film edges from the actor id.
   * @throws SQLException
   *           - something is wrong with SQL query.
   * @throws ExecutionException
   *           - something is wrong with cache.
   */
  private List<Film> edgesQuery(String name, String id)
      throws SQLException, ExecutionException {

    // toReturn
    List<Film> toReturn = new LinkedList<Film>();
    // Create a PreparedStatement
    PreparedStatement prep;
    prep = conn.prepareStatement("SELECT film FROM actor_film WHERE actor = ?");
    prep.setString(1, id);
    ResultSet rs = prep.executeQuery();

    // go through each film to go through each film to det. which are ok edge.
    while (rs.next()) {
      String filmID = rs.getString(1);
      // this creates a ResultSet of all other actor names in the given film.
      prep = conn
          .prepareStatement("SELECT actor.name FROM actor, actor_film WHERE"
              + " actor_film.film = ? AND actor_film.actor = actor.id");
      prep.setString(1, filmID);
      ResultSet rs2 = prep.executeQuery();
      int count = 1;
      List<String> otherActorNames = new LinkedList<String>();
      while (rs2.next()) {
        count += 1;
        otherActorNames.add(rs2.getString(1));
      }
      double weight = 1.0 / count;
      // we got the other actor's name, not to see if this is a legal
      // edge or not.
      for (String otherName : otherActorNames) {
        if (!otherName.equals(name)) {
          String[] chopped = otherName.split(" ");
          char firstCharFirstName = chopped[0].toCharArray()[0];
          String[] chopped2 = name.split(" ");
          char firstCharLastName = chopped2[chopped2.length - 1]
              .toCharArray()[0];
          // valid edge, add to to return
          if (firstCharLastName == firstCharFirstName) {
            Film f = new Film(filmID, actorCache.get(name),
                actorCache.get(otherName), weight);
            toReturn.add(f);
          }
        }
      }
    }

    // Close the ResultSet and the PreparedStatement
    rs.close();
    prep.close();

    return toReturn;
  }

  /**
   * Method used to retrieve an actor ID given the name of the actor.
   *
   * @param name
   *          - the name of the actor.
   * @return - the string representation of the associated actor ID.
   * @throws SQLException
   *           when something went wrong looking for id.
   */
  private String idQuery(String name) throws SQLException {
    String query = "SELECT id FROM actor WHERE name= ?";
    // Create a PreparedStatement
    PreparedStatement prep;
    prep = conn.prepareStatement(query);
    prep.setString(1, name);

    // Execute the query and retrieve a ResultStatement
    ResultSet resset = prep.executeQuery();

    String toReturn = resset.getString(1);

    // Close the ResultSet and the PreparedStatement
    resset.close();
    prep.close();

    return toReturn;
  }

  /**
   * Method for querying the film database in order to find a name given a
   * certain id.
   *
   * @param filmId
   *          - the film id we are looking for name from.
   * @return - the name associated.
   * @throws SQLException
   *           - if something has gone wrong with connecting to or querying
   *           database.
   */
  private String filmNameQuery(String filmId) throws SQLException {
    // Create a PreparedStatement
    PreparedStatement prep;
    prep = conn.prepareStatement("SELECT name FROM film WHERE id= ?");
    prep.setString(1, filmId);

    // Execute the query and retrieve a ResultStatement
    ResultSet rs = prep.executeQuery();

    String toReturn = rs.getString(1);

    // Close the ResultSet and the PreparedStatement
    rs.close();
    prep.close();

    return toReturn;
  }

  /**
   * A class for Films, which implement the interface Edge. These will be used
   * to create our bacon measures.
   *
   * @author maxmines
   *
   */
  public class Film implements Edge<Actor, Film> {
    private String name = null;
    private Actor v1;
    private Actor v2;
    private double weight;
    private String id;

    Film(String id, Actor v1, Actor v2, double weight) {
      this.id = id;
      this.v1 = v1;
      this.v2 = v2;
      this.weight = weight;
    }

    @Override
    public double getWeight() {
      return this.weight;
    }

    @Override
    public Actor otherVertex(Actor vertex) {
      if (vertex == v1) {
        return v2;
      } else if (vertex == v2) {
        return v1;
      } else {
        return null;
      }
    }

    /**
     * getter method for getting name from Film edge.
     *
     * @return - the name of the film associated with this edge.
     * @throws SQLException
     *           - when something went wrong querying database.
     */
    public String getName() throws SQLException {
      if (this.name == null) {
        String gotName = filmNameQuery(this.id);
        this.name = gotName;
      }
      return this.name;
    }

    /**
     * Getter method for id.
     *
     * @return - this film's id.
     */
    public String getId() {
      return this.id;
    }
  }

  /**
   * Private class for the Actor, a type of vertex used to make bacon
   * calculations.
   *
   * @author maxmines
   *
   */
  protected class Actor implements Vertex<Actor, Film> {
    private double distance = Double.POSITIVE_INFINITY;
    private List<Film> edges = null;
    private Film prev = null;
    private String name;
    private String id;

    Actor(String name) throws SQLException {
      this.name = name;
      this.id = idQuery(this.name);
    }

    @Override
    public int compareTo(Actor a) {
      return Double.compare(this.distance, a.getDistance());
    }

    @Override
    public List<Film> getEdges() throws NoSuchElementException {
      if (this.edges != null) {
        return this.edges;
      } else {
        List<Film> films;
        try {
          films = edgesQuery(this.name, this.id);
          this.edges = films;
          return this.edges;
        } catch (SQLException e) {
          throw new NoSuchElementException();
        } catch (ExecutionException e) {
          throw new NoSuchElementException();
        }
      }
    }

    @Override
    public double getDistance() {
      return this.distance;
    }

    @Override
    public void setDistance(double d) {
      this.distance = d;
    }

    @Override
    public Film getPrev() {
      return prev;
    }

    @Override
    public void setPrev(Film edge) {
      this.prev = edge;
    }

    /**
     * Getter method for returning the name of this actor.
     *
     * @return - String representing name of this actor.
     */
    public String getName() {
      return this.name;
    }

    /**
     * Getter method for the id of this Actor.
     *
     * @return - the String representing actor's id.
     */
    public String getId() {
      return this.id;
    }
  }

  /**
   * Method for getting the films of a given actor as links.
   *
   * @param id
   *          - the id of the actor
   * @return - a list of html links as strings.
   * @throws SQLException
   *           - if something went wrong with call (Shouldn't happen).
   */
  public List<String> getFilmsOfActor(String id) throws SQLException {
    // toReturn
    List<String> toReturn = new LinkedList<String>();
    // Create a PreparedStatement
    PreparedStatement prep;
    prep = conn.prepareStatement("SELECT film.name, film.id FROM actor_film, "
        + "film WHERE actor_film.actor = ? AND actor_film.film = film.id;");
    prep.setString(1, id);
    ResultSet rs = prep.executeQuery();
    while (rs.next()) {
      String name = rs.getString(1);
      String filmid = rs.getString(2);
      String toAdd = "<a href=\"/film/" + URLEncoder.encode(filmid) + "\">"
          + name + "</a>";
      toReturn.add(toAdd);
    }
    return toReturn;
  }

  /**
   * Get name from id for actors.
   *
   * @param id
   *          - the actor id
   * @return - the actor name
   * @throws SQLException
   *           - if no name found.
   */
  public String getNameFromID(String id) throws SQLException {

    // Create a PreparedStatement
    PreparedStatement prep;
    prep = conn.prepareStatement("SELECT name FROM actor " + "WHERE id = ?;");
    prep.setString(1, id);
    ResultSet rs = prep.executeQuery();
    return rs.getString(1);

  }

  /**
   * MEthod for getting actors given a film id.
   *
   * @param id
   *          - film id.
   * @return - returns the list of links.
   * @throws SQLException
   *           - shouldn't happen.
   */
  public List<String> getActorsOfFilm(String id) throws SQLException {
    // toReturn
    List<String> toReturn = new LinkedList<String>();
    // Create a PreparedStatement
    PreparedStatement prep;
    prep = conn.prepareStatement("SELECT actor.name, actor.id FROM actor_film, "
        + "actor WHERE actor_film.film = ? AND actor_film.actor = actor.id;");
    prep.setString(1, id);
    ResultSet rs = prep.executeQuery();
    while (rs.next()) {
      String name = rs.getString(1);
      String actorID = rs.getString(2);
      String toAdd = "<a href=\"/actor/" + URLEncoder.encode(actorID) + "\">"
          + name + "</a>";
      toReturn.add(toAdd);
    }
    return toReturn;
  }

  /**
   * Returns the name of a film given id.
   *
   * @param id
   *          - the film id
   * @return - the name of the film
   * @throws SQLException
   *           - shouldn't happen.
   */
  public String getFilmName(String id) throws SQLException {
    // Create a PreparedStatement
    PreparedStatement prep;
    prep = conn.prepareStatement("SELECT name FROM film " + "WHERE id = ?;");
    prep.setString(1, id);
    ResultSet rs = prep.executeQuery();
    return rs.getString(1);
  }

}

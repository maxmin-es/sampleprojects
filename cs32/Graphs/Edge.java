package edu.brown.cs.mmines.Graphs;

/**
 * Generic interface for edges used to construct graphs.
 *
 * @author maxmines
 *
 * @param <V>
 *          class that extends vertex.
 * @param <E>
 *          class that extends edge.
 */
public interface Edge<V extends Vertex<V, E>, E extends Edge<V, E>> {

  /**
   * Returns the weight of this edge.
   *
   * @return weight of this edge.
   */
  double getWeight();

  /**
   * Given one vertex of the edge, returns the other. If given vertex is not
   * part of this edge, returns null.
   *
   * @param vertex
   *          - the vertex not wanted to return.
   * @return - the other vertex, or null if input was invalid.
   * @throws IllegalArgumentException
   *           when the inputted vertex is not found in one of the two vertex
   *           fields.
   */
  V otherVertex(V vertex) throws IllegalArgumentException;

}

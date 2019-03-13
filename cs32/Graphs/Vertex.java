package edu.brown.cs.mmines.Graphs;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Generic interface for vertices used to build graphs.
 *
 * @author maxmines
 *
 * @param <V>
 *          - the type of vertex being implemented.
 * @param <E>
 *          - the type of edge these vertices are attached to.
 */
public interface Vertex<V extends Vertex<V, E>, E extends Edge<V, E>>
    extends Comparable<V> {
  /**
   * Returns a list of edges coming off of the current vertex.
   *
   * @return - a list of edges.
   * @throws NoSuchElementException
   *           when could not get edges.
   */
  List<E> getEdges() throws NoSuchElementException;

  /**
   * Returns the distance field.
   *
   * @return - the distance, however it may be calculated or stored.
   */
  double getDistance();

  /**
   * allows outsider to set distance of this vertex.
   *
   * @param d
   *          - distance to set.
   */
  void setDistance(double d);

  /**
   * Returns the previous vertex.
   *
   * @return previous vertex.
   */
  E getPrev();

  /**
   * Sets the previous node.
   *
   * @param edge
   *          - the vertex to set as the previous.
   */
  void setPrev(E edge);
}

package edu.brown.cs.mmines.Graphs;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;

/**
 * Class containing methods to compute Dijkstra's algorithm.
 *
 * @author maxmines
 *
 * @param <V>
 *          - the type for vertex.
 * @param <E>
 *          - the type for edge.
 */
public class Dijkstra<V extends Vertex<V, E>, E extends Edge<V, E>> {

  /**
   * Method for dijkstra's algorithm given a start and an end vertex.
   *
   * @param start
   *          - the vertex to start search from.
   * @param end
   *          - the destination vertex.
   * @return - a list of vertices, the shortest path from start to end.
   * @throws IllegalArgumentException
   *           when no path was found.
   */
  public List<E> dijkstra(V start, V end) throws IllegalArgumentException {
    PriorityQueue<V> unvisited = new PriorityQueue<V>();
    HashSet<V> visited = new HashSet<V>();

    // setting distance to the origin to 0
    start.setDistance(0.0);
    // adding the origin vertex to set of visited.
    unvisited.add(start);

    while (!unvisited.isEmpty()) {
      V currentV = unvisited.poll();
      visited.add(currentV);

      // we haven't reached the end
      if (currentV != end) {
        List<E> edges = null;
        try {
          edges = currentV.getEdges();

          for (E edge : edges) {
            V nextV = edge.otherVertex(currentV);
            double possNewDist = currentV.getDistance() + edge.getWeight();
            if (nextV.getDistance() > possNewDist) {
              unvisited.remove(nextV);
              nextV.setDistance(possNewDist);
              nextV.setPrev(edge);
            }
            if ((!visited.contains(nextV)) && (!(unvisited.contains(nextV)))) {
              unvisited.add(nextV);
            }
          }

        } catch (NoSuchElementException nsee) {
          throw new IllegalArgumentException("couldn't get edges");
        }
      } else { // we have reached the end
        return buildReturn(currentV);
      }
    }
    return new LinkedList<E>();
  }

  /**
   * Method that takes in the final destination of the shortest path search, and
   * returns a list, the ordered path to the end.
   *
   * @param end
   *          - a vertex, the end of the shortest path search.
   * @return - list of path built using previous vertices.
   */
  private List<E> buildReturn(V end) {
    List<E> toReturn = new LinkedList<E>();
    E prev = end.getPrev();
    V currentV = end;
    while (prev != null) {
      toReturn.add(prev);
      E oldPrev = prev;
      prev = prev.otherVertex(currentV).getPrev();
      currentV = oldPrev.otherVertex(currentV);
    }
    Collections.reverse(toReturn);
    return toReturn;
  }

}

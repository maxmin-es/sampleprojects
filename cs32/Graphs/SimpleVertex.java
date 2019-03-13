package edu.brown.cs.mmines.Graphs;

import java.util.LinkedList;
import java.util.List;

/**
 * Simple implementation of vertex.
 *
 * @author maxmines
 *
 */
public class SimpleVertex implements Vertex<SimpleVertex, SimpleEdge> {
  private SimpleEdge prev = null;
  private double distance = Double.POSITIVE_INFINITY;
  private List<SimpleEdge> edges = new LinkedList<SimpleEdge>();

  @Override
  public int compareTo(SimpleVertex o) {
    return Double.compare(this.distance, o.getDistance());
  }

  @Override
  public List<SimpleEdge> getEdges() {
    return this.edges;
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
  public SimpleEdge getPrev() {
    return this.prev;
  }

  @Override
  public void setPrev(SimpleEdge edge) {
    this.prev = edge;
  }

  /**
   * Adds edge manually.
   *
   * @param edge
   *          - the edge to add.
   */
  public void addEdge(SimpleEdge edge) {
    this.edges.add(edge);
  }
}

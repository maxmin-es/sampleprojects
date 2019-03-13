package edu.brown.cs.mmines.Graphs;

/**
 * Simple implementation of edge.
 *
 * @author maxmines
 *
 */
public class SimpleEdge implements Edge<SimpleVertex, SimpleEdge> {
  private double weight;
  private SimpleVertex v1;
  private SimpleVertex v2;

  SimpleEdge(double weight, SimpleVertex v1, SimpleVertex v2) {
    this.weight = weight;
    this.v1 = v1;
    this.v2 = v2;
  }

  @Override
  public double getWeight() {
    return this.weight;
  }

  @Override
  public SimpleVertex otherVertex(SimpleVertex vertex)
      throws IllegalArgumentException {
    if (vertex == v1) {
      return v2;
    } else if (vertex == v2) {
      return v1;
    } else {
      throw new IllegalArgumentException("ERROR: Vertex not part of edge.");
    }
  }

}

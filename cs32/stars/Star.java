package edu.brown.cs.mmines.stars;

/**
 * Class for Stars, an object in cs32 Stars project.
 *
 * @author Max Mines
 *
 */
public class Star implements KDable<Star> {
  private int id;
  private String name; // not used, but put there in case of future.
  private double[] coordinates;

  Star(int id, String name, double[] coordinates) {
    if ((coordinates.length == 0) || (coordinates == null)) {
      throw new IllegalArgumentException("Coordinates must have some value.");
    } else {
      this.id = id;
      this.name = name;
      this.coordinates = coordinates;
    }
  }

  /**
   * Getter for this.name.
   *
   * @return - the name as a string.
   */
  public String getName() {
    return this.name;
  }

  /**
   * Getter for this.id.
   *
   * @return - the name as a string.
   */
  public String getCoordsAsString() {
    String toReturn = "(";
    for (double d : coordinates) {
      toReturn = toReturn + d + " ";
    }
    toReturn = toReturn.substring(0, toReturn.length() - 1) + ")";
    return toReturn;
  }

  @Override
  public double euclideanDistance(KDable<Star> p1) {
    double currentSum = 0;
    for (int i = 0; i < this.coordinates.length; i++) {
      currentSum = currentSum
          + Math.pow((this.coordinates[i] - p1.getCoordinates()[i]), 2);
    }
    return Math.sqrt(currentSum);
  }

  @Override
  public double[] getCoordinates() {
    return this.coordinates;
  }

  @Override
  public String toString() {
    return Integer.toString(this.id);
  }
}

package edu.brown.cs.mmines.stars;

/**
 * An interface for objects that can be used to construct KDTrees.
 *
 * @author maxmines
 *
 * @param <T>
 *          - An object that implements this interface.
 */
public interface KDable<T> {

  /**
   * euclidean Distance, calculates mathematical euclidean distance between two
   * given KDable objects.
   *
   * @param p1
   *          - comparison KDable, of same type
   * @return - a double representing the euclidean distance between the two
   *         inputed objects.
   *
   */
  double euclideanDistance(KDable<T> p1);

  /**
   * Get coordinates method.
   *
   * @return - returns the coordinates of this KDable as double[].
   */
  double[] getCoordinates();
}

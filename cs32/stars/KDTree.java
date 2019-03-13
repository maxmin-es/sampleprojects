package edu.brown.cs.mmines.stars;

import static com.google.common.collect.Lists.reverse;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Class for KDTrees.
 *
 * @author maxmines
 *
 * @param <T>
 *          - a KDable object type.
 */
public class KDTree<T extends KDable<T>> {
  private int k;
  private KDNode root;
  private int neighbors;
  private T target;

  /**
   * Constructor for KDTree.
   *
   * @param toBeNodes
   *          - a list of KDables, to be converted to nodes.
   *
   */
  KDTree(List<T> toBeNodes) throws IllegalArgumentException {
    if (toBeNodes.isEmpty()) {
      throw new IllegalArgumentException(
          "Input data must not be empty list, two lists must be same size");
    } else { // setting up KDNodes for tree construction
      List<KDNode> nodes = new LinkedList<>();

      for (T tbn : toBeNodes) {
        KDNode toAdd = new KDNode(tbn);
        nodes.add(toAdd);
      }

      // automatically determines k based on the inputted list.
      this.k = toBeNodes.get(0).getCoordinates().length - 1;

      // build tree
      this.root = buildTree(nodes, 0);
    }
  }

  /**
   * buildTree method, used to construct the KDTree. Called by constructor.
   *
   * @param nodeList
   *          - a list of nodes to split into median, smaller than median, and
   *          greater than median
   * @param currentDim
   *          - the current depth, or dimension of the tree.
   * @return a KDNode which will be recursively bound to the parents.
   */
  private KDNode buildTree(List<KDNode> nodeList, int currentDim) {

    Collections.sort(nodeList, new Comparator<KDNode>() {
      @Override
      public int compare(KDNode o1, KDNode o2) {
        return Double.compare(o1.getCoordinates()[currentDim],
            o2.getCoordinates()[currentDim]);
      }
    });

    if (nodeList.size() == 0) {
      return null;
    } else if (nodeList.size() == 1) {
      return nodeList.get(0);
    } else {
      // next dimension
      int nextDim;
      if (currentDim == this.k) {
        nextDim = 0;
      } else {
        nextDim = currentDim + 1;
      }

      // median
      int listSize = nodeList.size();
      KDNode median = nodeList.get(listSize / 2);

      // less than list
      List<KDNode> l = new LinkedList<>();
      for (int i = 0; i < (listSize / 2); i++) {
        l.add(nodeList.get(i));
      }
      // greater than list
      List<KDNode> g = new LinkedList<>();
      for (int i = ((listSize / 2) + 1); i < listSize; i++) {
        g.add(nodeList.get(i));
      }
      median.rightChild = buildTree(g, nextDim);
      median.leftChild = buildTree(l, nextDim);
      return median;
    }
  }

  /**
   * A subclass representing the KDNodes of the tree.
   *
   * @author maxmines
   *
   *
   */
  public class KDNode implements Comparable<KDNode> {
    private T kdable;
    private KDNode rightChild = null;
    private KDNode leftChild = null;

    /**
     * Constructor for KDNode class.
     *
     * @param kdable
     *          - a kdable object
     */
    KDNode(T kdable) {
      this.kdable = kdable;
    }

    /**
     * Gets coordinates of this node through the kdable.
     *
     * @return a double[] representing the coordinates of this node.
     */
    public double[] getCoordinates() {
      return this.kdable.getCoordinates();
    }

    /**
     * Returns the associated kdable. For testing.
     *
     * @return this.kdable.
     */
    public T getKDable() {
      return this.kdable;
    }

    /**
     * Returns the right child. For testing.
     *
     * @return - this.rightChild
     */
    public KDNode getRight() {
      return this.rightChild;
    }

    /**
     * Returns the left child. For testing.
     *
     * @return - this.leftChild
     */
    public KDNode getLeft() {
      return this.leftChild;
    }

    @Override
    public int compareTo(KDNode other) {
      return Double.compare(kdable.euclideanDistance(target),
          other.kdable.euclideanDistance(target));
    }

    @Override
    public String toString() {
      return this.kdable.toString();
    }
  }

  List<T> nearestNeighbor(T target1, int nn) throws IllegalArgumentException {
    if (nn < 1) {
      throw new IllegalArgumentException("K must be at least 1");
    } else {

      this.neighbors = nn;
      this.target = target1;

      PriorityQueue<KDNode> pq = nearestNeighborRecur(target1, this.root, 0,
          new PriorityQueue<>(Collections.reverseOrder()));
      List<T> toReturn = new LinkedList<>();

      while (!(pq.isEmpty())) {
        toReturn.add(pq.poll().kdable);
      }
      return reverse(toReturn);
    }
  }

  /**
   * This method is used by nearestNeighbor to recur down the tree. It does most
   * of the computation of nearest neighbor method.
   *
   * @param target
   *          - a double[], the coordinates of the target point
   * @param currentNode
   *          - current node being evaluated
   * @param depth
   *          - the current depth to be used to compare certain coordinates
   * @param pq
   *          - the priority queue of top nodes so far
   * @return - a priority queue with the results of the search.
   */
  private PriorityQueue<KDNode> nearestNeighborRecur(T target1,
      KDNode currentNode, int depth, PriorityQueue<KDNode> pq) {
    if (currentNode != null) {
      if (pq.size() < this.neighbors) {
        pq.add(currentNode);
      } else if (pq.peek().compareTo(currentNode) > 0) {
        pq.poll();
        pq.add(currentNode);
      }

      // calculating depth to feed to recursion
      int nextDepth;
      if (depth == this.k) {
        nextDepth = 0;
      } else {
        nextDepth = depth + 1;
      }

      double currentDimComparison = target1.getCoordinates()[depth]
          - currentNode.getCoordinates()[depth];
      boolean wentLeft = true;
      PriorityQueue<KDNode> returnedFromRecur;
      if (currentDimComparison <= 0) {
        returnedFromRecur = nearestNeighborRecur(target1, currentNode.leftChild,
            nextDepth, pq);
      } else {
        returnedFromRecur = nearestNeighborRecur(target1,
            currentNode.rightChild, nextDepth, pq);
        wentLeft = false;
      }

      double euclidToFar = pq.peek().kdable.euclideanDistance(target);
      boolean rootAndNotEnough = false;
      if ((currentNode == root)
          && (returnedFromRecur.size() < this.neighbors)) {
        rootAndNotEnough = true;
      }

      // go both sides
      if ((Math.abs(currentDimComparison) < euclidToFar)
          || (rootAndNotEnough)) {
        if (wentLeft) {
          returnedFromRecur = nearestNeighborRecur(target,
              currentNode.rightChild, nextDepth, returnedFromRecur);
        } else {
          returnedFromRecur = nearestNeighborRecur(target,
              currentNode.leftChild, nextDepth, returnedFromRecur);
        }
      }
      return returnedFromRecur;
    } else {
      return pq;
    }
  }

  List<T> radiusSearch(double r, T target1) throws IllegalArgumentException {
    if (r < 0) {
      throw new IllegalArgumentException(
          "radius must be an integer greater than 0.");
    } else {
      this.target = target1;
      PriorityQueue<KDNode> pq = radiusSearchRecur(this.root, 0,
          new PriorityQueue<KDNode>(), r);
      List<T> toReturn = new LinkedList<>();
      while (!pq.isEmpty()) {
        T kda = pq.poll().kdable;
        toReturn.add(kda);
      }
      return toReturn;
    }
  }

  private PriorityQueue<KDNode> radiusSearchRecur(KDNode currentNode, int depth,
      PriorityQueue<KDNode> pq, double radius) {
    if (currentNode == null) {
      return pq;
    } else if (this.target.euclideanDistance(currentNode.kdable) <= radius) {
      pq.add(currentNode);
    }

    // calculating depth to feed to recursion
    int nextDepth;
    if (depth == this.k) {
      nextDepth = 0;
    } else {
      nextDepth = depth + 1;
    }

    boolean wentLeft = true;
    double oneDDistance = (target.getCoordinates()[depth]
        - currentNode.getCoordinates()[depth]);
    if (oneDDistance <= 0) {
      pq = radiusSearchRecur(currentNode.leftChild, nextDepth, pq, radius);
    } else {
      wentLeft = false;
      pq = radiusSearchRecur(currentNode.rightChild, nextDepth, pq, radius);
    }

    if (Math.abs(oneDDistance) <= radius) {
      if (wentLeft) {
        pq = radiusSearchRecur(currentNode.rightChild, nextDepth, pq, radius);
      } else {
        pq = radiusSearchRecur(currentNode.leftChild, nextDepth, pq, radius);
      }
    }
    return pq;
  }

  /**
   * Getter for root, used to test.
   *
   * @return - this.root.
   */
  KDNode getRoot() {
    return this.root;
  }

}

/**
 * @Author: John Nestor <nestorj>
 * @Date:   2020-06-24T20:53:11-04:00
 * @Email:  nestorj@lafayette.edu
 * @Last modified by:   nestorj
 * @Last modified time: 2020-06-24T20:53:11-04:00
 */



import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedList;
import java.lang.Comparable;

/** Keeps track of a (node, edge) pair for the BOI algorithm */

public class STNEPair implements Comparable/*<STNEPair>*/ {

  private STNode node;
  private STEdge edge;
  private Point steinerPoint;
  private boolean steinerGood;
  private int cost;
  private int gain;
  private STEdge elimEdge;

  public STNEPair(STNode n, STEdge e) {
    int minX, maxX, minY, maxY;
    int tx, ty; // coordinates of new SP
    int dx, dy; // distance of new SP
    node = n;
    edge = e;
    Point p1 = edge.getP1().getLocation();
    Point p2 = edge.getP2().getLocation();
    Point nloc = node.getLocation();
    if (p1.x < p2.x) {
      minX = p1.x;
      maxX = p2.x;
    } else {
      minX = p2.x;
      maxX = p1.x;
    }
    if (nloc.x < minX) {
      dx = minX - nloc.x;
      tx = minX;
    } else if (nloc.x > maxX) {
      dx = nloc.x - maxX;
      tx = maxX;
    } else {
      dx = 0;
      tx = nloc.x;
    }
    if (p1.y < p2.y) {
      minY = p1.y;
      maxY = p2.y;
    } else {
      minY = p2.y;
      maxY = p1.y;
    }
    if (nloc.y < minY) {
      dy = minY - nloc.y;
      ty = minY;
    } else if (nloc.y > maxY) {
      dy = nloc.y - maxY;
      ty = maxY;
    } else {
      dy = 0;
      ty = nloc.y;
    }
    steinerPoint = new Point(tx,ty);
    if (steinerPoint.equals(p1) || steinerPoint.equals(p2) ||
    steinerPoint.equals(nloc)) steinerGood = false;
    else steinerGood = true;

    cost =  dx + dy;
  }

  public int getCost() { return cost; }

  public int getGain() { return gain; }

  public int setElimEdge(STEdge el) {
    elimEdge = el;
    gain = elimEdge.length() - cost;
    return gain;
  }

  public STNode getNode() { return node; }

  public STEdge getEdge() { return edge; }

  public STEdge getElimEdge() { return elimEdge; }

  public Point getSteiner() { return steinerPoint; }

  public boolean getSteinerGood() { return steinerGood; }

  public boolean modValid() {
    if ((gain <= 0) || edge.getDeleteMark() || elimEdge.getDeleteMark()) return false;
    else return true;
  }

  /** apply the BOI modification if gain is positive and the edges still exist -
  return false if move not applied */
  public STNode applyMod(STGraph gr) {
    if (!modValid()) return null;
    STNode nn = new STNode(steinerPoint, false);  // the new Steiner point
    gr.addNode(nn);
//    System.out.println("disconnecting " + elimEdge);
    elimEdge.disconnect();
//    System.out.println("Graph after elimEdge disconnect: " + gr);
    STNode n1 = edge.getP1();
    STNode n2 = edge.getP2();
//    System.out.println("disconnecting " + edge);
    edge.disconnect();
//    System.out.println("Graph after edge disconnect: " + gr);
    gr.addEdge(n1, nn);
    gr.addEdge(n2, nn);
    gr.addEdge(node, nn);
    return nn;
  }

  /** used to sort in DESCENDING order! */
  public int compareTo(Object o) {
    STNEPair b = (STNEPair)o;
    return Integer.valueOf(b.getGain()).compareTo(getGain());
  }

  public String toString() {
    return "STNEPair: node " + node + " edge " + edge + " elimEdge " + elimEdge + " gain " + gain;
  }
  
  public String toTableString(){
      return "n"+node.getID()+ " || e" + edge.getID() + " || e" + elimEdge.getID();
  }
  
//  public boolean equals(Object ob){
//      if (!(ob instanceof STNEPair)){
//          return false;
//      }
//      STNEPair b = (STNEPair) ob;
//      if(edge.equals(b.getEdge())&&elimEdge.equals(b.getElimEdge())&&gain==b.getGain()){
//          return true;
//      }
//      if(edge.equals(b.getElimEdge())&&elimEdge.equals(b.getEdge())&&gain==b.getGain()){
//          return true;
//      }
//      return false;
//  }

}

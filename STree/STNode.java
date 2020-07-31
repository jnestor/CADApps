/**
 * @Author: John Nestor <nestorj>
 * @Date:   2020-06-24T21:05:06-04:00
 * @Email:  nestorj@lafayette.edu
 * @Last modified by:   nestorj
 * @Last modified time: 2020-06-24T21:05:06-04:00
 */



import java.awt.Point;
import java.util.LinkedList;

public class STNode {
  private int id;
  private Point loc;
  private boolean isTerm;
  private boolean visitFlag = false;
  private LinkedList <STEdge> edges = new LinkedList<STEdge>(); // adjacent edges
  private static int next_id = 1;

  public STNode(Point l, boolean isT) {
    id = next_id++;
    loc = l;
    isTerm = isT;
  }

  public STNode(Point l) {
    this(l, true);
  }

  public STNode(int x, int y) {
    this(new Point(x,y));
  }

  public int getID() { return id; }
  
  public Point getLocation() { return loc; }

  public void setLocation(Point newloc) {
    loc = newloc;
  }

  public boolean nodeFound(Point searchLoc, int window) {
    int lx = searchLoc.x - window/2;
    int ly = searchLoc.y - window/2;
    int ux = searchLoc.x + window/2;
    int uy = searchLoc.y + window/2;
    return ( loc.x > lx && loc.x < ux &&
    loc.y > ly && loc.y < uy );
  }

  /** return true if the x location of searchLoc is close to the x position of the node */
  public boolean nodeMatchX(Point searchLoc, int window) {
    int lx = searchLoc.x - window/2;
    int ux = searchLoc.x + window/2;
    return ( loc.x > lx && loc.x < ux );
  }

  /** return true if the y location of searchLoc is close to the y position of the node */
  public boolean nodeMatchY(Point searchLoc, int window) {
    int ly = searchLoc.y - window/2;
    int uy = searchLoc.y + window/2;
    return ( loc.y > ly && loc.y < uy );
  }

  public boolean isTerminal() { return isTerm; }

  public void setVisited(boolean b) { visitFlag = b; }

  public boolean isVisited() { return visitFlag; }


  public int distanceL1(STNode p) {
    return ( Math.abs(loc.x - p.loc.x) + Math.abs(loc.y - p.loc.y) );
  }

  public void addEdge(STEdge e) {
    edges.add(e);
  }

  public void removeEdge(STEdge e) {
    edges.remove(e);
  }

  public STEdge getEdge(int i) {
    return (STEdge)edges.get(i);
  }

  public int numEdges() {
    return edges.size();
  }

  public static void countReset(){
      next_id = 1;
  }

  public String toString() {
    String s = "Node " + id + " (" + loc.x + "," + loc.y + "){";
    for (int i=0; i<edges.size(); i++)
    s = s + getEdge(i);
    s = s + "}";
    return s;
  }
  
  @Override
  public boolean equals(Object ob){
      if(ob instanceof STNode){
          STNode node = (STNode) ob;
          return id == node.getID();
      }
      return false;
  }
}

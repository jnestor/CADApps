package MazeRouter;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

class GridPoint {

  public GridPoint(int x, int y, int z) {
    posx = x;
    posy = y;
    posz = z;
  }

  public GridPoint(int x, int y) {
    this(x, y, 0);
  }

  /** return the north neighbor of this point.  returns null if this
      point is on the northern edge of the grid */
  public GridPoint northNeighbor() {
    return myGrid.gridPointAt(posx, posy-1, posz);
  }

  /** return the south neighbor of this point.  returns null if this
      point is on the southern edge of the grid */
  public GridPoint southNeighbor() {
    return myGrid.gridPointAt(posx, posy+1, posz);
  }

  /** return the east neighbor of this point.  returns null if this
      point is on the eastern edge of the grid */
  public GridPoint eastNeighbor() {
    return myGrid.gridPointAt(posx+1, posy, posz);
  }

  /** return the west neighbor of this point.  returns null if this
      point is on the western edge of the grid */
  public GridPoint westNeighbor() {
    return myGrid.gridPointAt(posx-1, posy, posz);
  }

  /** return the neighbor on the next level up.  Returns null if there
      is no layer above current layer */
  public GridPoint upNeighbor() {
    return myGrid.gridPointAt(posx, posy, posz-1);
  }

  /** return the neighbor on the next level down.  Returns null if there
      is no layer below current layer */
  public GridPoint downNeighbor() {
    return myGrid.gridPointAt(posx, posy, posz+1);
  }

  /* expand a routing search */
  public int expand() throws InterruptedException {
    GridPoint xp;
    if ( (xp = westNeighbor()) != null && xp.val == UNROUTED ) {
      xp.val = val+1;
      if (xp.isTarget()) return xp.val;
      else myGrid.enqueueGridPoint(xp);
    }
    if ( (xp = eastNeighbor()) != null && xp.val == UNROUTED ) {
      xp.val = val+1;
      if (xp.isTarget()) return xp.val;
      else myGrid.enqueueGridPoint(xp);
    }
    if ( (xp = southNeighbor()) != null && xp.val == UNROUTED ) {
      xp.val = val+1;
      if (xp.isTarget()) return xp.val;
      else myGrid.enqueueGridPoint(xp);
    }
    if ( (xp = northNeighbor()) != null && xp.val == UNROUTED ) {
      xp.val = val+1;
      if (xp.isTarget()) return xp.val;
      else myGrid.enqueueGridPoint(xp);
    }
    if ( (xp = upNeighbor()) != null && xp.val == UNROUTED ) {
      xp.val = val+1;
      if (xp.isTarget()) return xp.val;
      else myGrid.enqueueGridPoint(xp);
    }
    if ( (xp = downNeighbor()) != null && xp.val == UNROUTED ) {
      xp.val = val+1;
      if (xp.isTarget()) return xp.val;
      else myGrid.enqueueGridPoint(xp);
    }
    return -1;
  }


  public boolean isTarget() {
    return ( this == myGrid.getTarget() );
  }

  public boolean isSource() {
    return ( this == myGrid.getSource() );
  }

  public boolean isEmpty() {
    return val == UNROUTED;
  }

  public void paintGridPoint(Graphics g) {
    g.setColor(Color.black);
    g.drawRect(myGrid.gridPanelX(posx,posy,posz),myGrid.gridPanelY(posx,posy,posz),
        myGrid.GRIDSIZE,myGrid.GRIDSIZE);
    if (isRouted()) {
      g.setColor(routedColor);
      fillGridPoint(g);
    } else if (isObstacle()) {
      g.setColor(Color.red);
      fillGridPoint(g);
    }
    else if (isSource()) {
      if (highlighted) g.setColor(Color.yellow);
      else g.setColor(Color.red);
      fillGridPoint(g);
      g.setColor(Color.black);
      labelGridPoint(g, "S");
    } else if (isTarget()) {
      if (highlighted) {
        g.setColor(Color.yellow);
        fillGridPoint(g);
        g.setColor(Color.black);
        labelGridPoint(g, Integer.toString(val % 10));
      } else {
        g.setColor(Color.red);
        fillGridPoint(g);
        g.setColor(Color.black);
        labelGridPoint(g, "T");
      }
    } else if (val < UNROUTED) {
      if (isEnqueued()) g.setColor(Color.orange);
      else g.setColor(Color.yellow);
      fillGridPoint(g);
      g.setColor(Color.black);
      labelGridPoint(g, Integer.toString(val % 10));
    }
  }

  private void fillGridPoint(Graphics g) {
    g.fillRect(myGrid.gridPanelX(posx,posy,posz)+2,myGrid.gridPanelY(posx,posy,posz)+2,
        myGrid.GRIDSIZE-3,myGrid.GRIDSIZE-3);
  }
  private void labelGridPoint(Graphics g, String s) {
    g.drawString(s, myGrid.gridPanelX(posx,posy,posz)+myGrid.CHARXOFFSET,
                    myGrid.gridPanelY(posx,posy,posz)+myGrid.CHARYOFFSET);
  }

  public String toString() {
    return "GridPoint(" + posx + "," + posy  + "," + posz + ")[" + val + "]";
  }

  public void reset() { val = UNROUTED; }

  public void initExpand() { val = 0; }

  public void setRouted() {
    val = ROUTED;
    routedColor = cseq.current();
  }

  public boolean isRouted() { return (val == ROUTED); }

  public void setObstacle() { val = OBSTACLE; }

  public boolean isObstacle() { return (val == ROUTED || val == OBSTACLE); }

  public boolean lessThan(GridPoint p2) { return val != 0 && val < p2.val; }

  private static final int ROUTED = -2;
  private static final int OBSTACLE = -1;
  private static final int UNROUTED = 9999;

  public int manhattanDistance(GridPoint p2) {
    return ( Math.abs(p2.posx - posx) + Math.abs(p2.posy - posy) +
      Math.abs(p2.posz - posz) );
  }

  private int posx;
  private int posy;
  private int posz;
  private boolean highlighted = false;
  private boolean enqueued = false;
  boolean isEnqueued()  { return enqueued; }
  void setEnqueued(boolean e) { enqueued = e; }

  private int val = UNROUTED;

  private Color routedColor = null;

  public int getVal() { return val; }

  public void highlight(boolean h) { highlighted = h; }

  static Grid myGrid;

  private static ColorSequencer cseq = new ColorSequencer();

  public static void nextRouteColor() { cseq.next(); }
}

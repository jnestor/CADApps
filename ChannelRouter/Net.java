package ChannelRouter;
import java.io.*;
import java.awt.*;
import javax.swing.*;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Enumeration;

//import ChannelRouter.Terminal;

public class Net {

  /** Symbolic constant for unrouted net */
  public static final int TRACK_UNASSIGNED = 0;

  private String name;
  private Vector<Terminal> terms = new Vector<Terminal>(5);
  private Vector<Net> constraints = new Vector<Net>(2);  // contains vertical constraints (if any)
  private int leftEdge = Integer.MAX_VALUE;
  private int rightEdge = 0;
  private int track = TRACK_UNASSIGNED;
  private boolean mark = false;

  /** Create a new net from a string containing the net name
     */
  public Net(String n) { ;
      name = n;
  }

  /** @return true if net assigned to a track */
  public boolean isRouted() {
    return (getTrack() != Net.TRACK_UNASSIGNED);
  }

  /** Get the assigned track of this net
     * @return 	the assigned track of this net
     */
  public int getTrack() { return track; }

  /** Assign this net to a track
      * @param t	The track this net will be assigned to.
      */
  public void setTrack(int t) { track = t; }

  /** get the left edge of this net
     * @return the leftmost column of this net
     */
  public int getLeftEdge() { return leftEdge; }

 /** get the right edge of this net
     * @return the rightmost column of this net
     */
  public int getRightEdge() { return rightEdge; }

  public boolean overlap(Net n2) {
    int l1 = getLeftEdge();
    int r1 = getRightEdge();
    int l2 = n2.getLeftEdge();
    int r2 = n2.getRightEdge();
    return (l2 <= r1 && r2 >= l1) || (l1 <= r2 && r1 >= l2);
  }

  /** write out the net */
  public void writeNet(PrintStream os) {
    os.print(name + " ");
    if (getTrack() != TRACK_UNASSIGNED) os.print(track + " ");
    for (Enumeration<Terminal> e = terms.elements(); e.hasMoreElements() ; )
      (e.nextElement()).writeTerminal(os);
    os.println();
  }

  /** @return the name of this net
     */
  public String getName() { return name; }

  /** Add a terminal to this net
      * @param trm	The terminal to be added to this net.
      */
  public void addTerminal(Terminal trm){
    terms.addElement(trm);
    trm.setNet(this);
    if (trm.getColumn() < leftEdge) leftEdge = trm.getColumn();
    if (trm.getColumn() > rightEdge) rightEdge = trm.getColumn();
  }

  /** @return an enumeration containing the terminals of this net */
  public Enumeration<Terminal> getTerminals() {
    return terms.elements();
  }

  public void addConstraint(Net nt) {
    if (!constraints.contains(nt))
      constraints.addElement(nt);
  }

  public boolean checkConstraint() {
    if (constraints == null) return true;
    boolean result = true;
    setMark(true);
    for ( int i = 0; i < constraints.size(); i++ ) {
      Net ctgt = constraints.elementAt(i);
      if (ctgt.getMark()) {
        //System.out.println("Constraint cycle found from Net " + getName() + //debug
        //    " to " + ctgt.getName() + " ... removing");
        constraints.removeElementAt(i);
        result = false;
      } else {
        if (!ctgt.checkConstraint()) result = false;
      }
    }
    setMark(false);
    return result;
  }

  public Enumeration<Net> getConstraints() {
    return constraints.elements();
  }

  /** Test vertical constraints on this net;  @return true if all nets in constraint are already routed */
  public boolean testConstraints() {
    for (Enumeration<Net> e = getConstraints(); e.hasMoreElements(); ) {
      Net testnet = e.nextElement();
      if (!testnet.isRouted()) return false;
    }
    return true;
  }

  public void setMark(boolean m) { mark = m; }

  public boolean getMark() { return mark; }

}

package ChannelRouter;

import java.io.*;
import java.awt.*;
import javax.swing.*;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Enumeration;

class Netlist
{
  private Terminal[] upperTerms;
  private Terminal[] lowerTerms;
  private Vector<Net> nlist;
  private int maxTrack = Net.TRACK_UNASSIGNED;
  private boolean cyclesPresent = false;

  public static final int NUM_COLS = 10;

  public Netlist() {
    nlist = new Vector<Net>();
    upperTerms = new Terminal[NUM_COLS];
    lowerTerms = new Terminal[NUM_COLS];
    for (int i = 0; i < NUM_COLS; i++) {
      upperTerms[i] = new Terminal(i, Terminal.TOP);
      lowerTerms[i] = new Terminal(i, Terminal.BOTTOM);
    }
  }

  public boolean getCyclesPresent() { return cyclesPresent; }

  /** return terminal object for the given position */
  public Terminal getTerminal(int pos, boolean topOrBottom) {
    if (topOrBottom == Terminal.TOP) return upperTerms[pos];
    else return lowerTerms[pos];
  }

  /** insert net into netList sorted by left edge */
  public void addNet(Net n) {
    int i;
    for (i = 0; i < nlist.size(); i++) {
      if ((nlist.elementAt(i)).getLeftEdge() > n.getLeftEdge()) {
        nlist.insertElementAt(n, i);
        return;
      }
    }
    nlist.addElement(n);
  }

  void unmarkNets() {
    for (Enumeration<Net> e = getNets() ; e.hasMoreElements() ; )
      (e.nextElement()).setMark(false);
  }

  /** make vertical constraints and check for cycles - if they occur, just remove one constraint
       even though it's wrong. */
  public void makeConstraints() {
    for (int i = 0; i < NUM_COLS; i++) {
      Net unet = upperTerms[i].getNet();
      Net lnet = lowerTerms[i].getNet();
      if (unet != null && lnet != null && unet != lnet) {
        //System.out.println("Adding constraint " + unet.getName()
        //  + " above " + lnet.getName());
        lnet.addConstraint(unet);
     }
   }
   // Search for cycles in constraints - just delete a constraint when one is found (for now)
   cyclesPresent = false;
   for (int i = 0; i < NUM_COLS; i++) {
     unmarkNets();
     Net lnet = lowerTerms[i].getNet();
     if (lnet != null && !lnet.checkConstraint()) cyclesPresent = true;
    }
  }



  public void clear() {
    nlist.removeAllElements();
    for (int i = 0; i < NUM_COLS; i++) {
      upperTerms[i].setNet(null);
      lowerTerms[i].setNet(null);
    }
  }

  public Enumeration<Net> getNets() { return nlist.elements(); }

  public Net findNet(String findName) {
    for (int i = 0; i < nlist.size(); i++) {
      Net nt = nlist.elementAt(i);
      if (nt.getName().equals(findName)) return nt;
    }
    return null;
  }

  public void write(PrintStream os) {
    os.println("Writing netlist");
    for (Enumeration<Net> e = nlist.elements() ; e.hasMoreElements() ;) {
      (e.nextElement()).writeNet(os);
   }
  }

  /** determine whether we can assign net testNet to track trk by scanning nets */
  private boolean canRoute(Net testNet, int trk) {
    for (Enumeration<Net> ne = nlist.elements(); ne.hasMoreElements() ; ) {
      Net n = ne.nextElement();
      if (n.getTrack() == trk && testNet.overlap(n)) return false;
    }
    return true;
  }

  /** assign nets to tracks in channel using the Left Edge Algorithm */
  public void leftEdgeAlgorithm() {
    makeConstraints();
    int track = 0;
    boolean done = false;
    while (!done) {
      track++;
      done = true;
      // place unrouted nets in current track if they fit
      for (Enumeration<Net> e = nlist.elements(); e.hasMoreElements() ; ) {
        Net n = e.nextElement();
        if (n.isRouted()) continue;  // skip nets already done
        if (n.testConstraints() && canRoute(n, track)) {
          //System.out.println("Assigning net " + n.getName() +
          //    "(" + n.getLeftEdge() + ":" + n.getRightEdge() +
          //    ") to track" + track);
          n.setTrack(track);
        }
        else done = false;  // at least one net (this one) remains unrouted!
      }
    }
    maxTrack = track;
  }

  public int getMaxTrack() { return maxTrack; }

}

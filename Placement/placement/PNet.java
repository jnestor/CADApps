package placement;

import java.awt.*;
import java.io.*;
import java.util.Vector;
import java.util.StringTokenizer;

public class PNet {
  private Vector terminals = new Vector();
  private String name;
  public String getName() { return name; }

  public PNet(String n) {
    name = n;
  }

  public String toString() {
    String s = "PNet " + getName() + "[";
    for (int i=0; i < terminals.size(); i++) {
      PTerminal pt = getTerminal(i);
      s += " " + pt;
    }
    s += " ]";
    return s;
  }

  /** half-permeter netlength estimation */
  public int netLength() {
    int minx = getTerminal(0).getLayoutX();
    int maxx = minx;
    int miny = getTerminal(0).getLayoutY();
    int maxy = miny;
    for (int i=1; i<numTerminals(); i++) {
      PTerminal t = getTerminal(i);
      minx = Math.min(minx,t.getLayoutX());
      maxx = Math.max(maxx,t.getLayoutX());
      miny = Math.min(miny,t.getLayoutY());
      maxy = Math.max(maxy,t.getLayoutY());
    }
    return (maxy - miny) + (maxx - minx);
  }

  void addTerminal(PTerminal t) { terminals.addElement(t); }

  public int numTerminals() { return terminals.size(); }

  public PTerminal getTerminal(int i) { return (PTerminal)terminals.elementAt(i); }

  /** draw the net - currently only supports 2-terminal nets? */
  public void paintNet(Graphics g) {
    g.setColor(Color.black);
    if (numTerminals() <= 1) return;
    PTerminal ot = getTerminal(0);
    g.setColor(Color.red);
    for (int i=1; i<numTerminals(); i++) {
      PTerminal dt = getTerminal(i);
      g.drawLine(ot.getX(),ot.getY(),dt.getX(),dt.getY());
    }
    // add some goofy code to draw the net here
  }


  /** read net in format netName mod1Name term1Name mod2Name term2Name ... */
  static PNet parseNet(StringTokenizer t, PLayout pl) {
    String nname = t.nextToken();
    PNet n = new PNet(nname);
    while (t.hasMoreTokens()) {
      String mtname = t.nextToken();
      String tname = t.nextToken();
      PModule mt = pl.findModule(mtname);
      if (mt == null) {
        System.out.println("Net.parseNet: could not find connecting module: " + mtname);
        continue;
      }
      PTerminal pt = mt.findTerminal(tname);
      if (pt == null) {
        System.out.println("Net.parseNet: could not find connecting terminal: " +
		mtname + ":" + tname);
        continue;
      }
      n.addTerminal(pt);
    }
    pl.addNet(n);
    return n;
  }


}



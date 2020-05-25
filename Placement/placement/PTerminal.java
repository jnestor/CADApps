package placement;

import java.util.Vector;
import java.util.Enumeration;
import java.io.*;
import java.util.StringTokenizer;
import java.awt.*;

public class PTerminal {
  public PTerminal(String n, PModule p, int xo, int yo) {
    name = n;
    parent = p;
    xoff = xo;
    yoff = yo;
  }

  private String name;
  public String getName() { return name; }

  public PModule getParent(){
    return parent;
  }
  private PModule parent;
  private int xoff;
  private int yoff;

  private PNet tnet;

  public String toString() { return "PTerminal " + name + " module " +
      getParent().getName() + "(" + xoff + "," + yoff + ")";
  }


  /** rotate a terminal assuming the parent module has already been rotated*/
  void rotate() {
    int newx = getParent().width() - yoff;
    yoff = xoff;
    xoff = newx;
  }

  void flipVertical() {
    yoff = getParent().height() - yoff;
  }

  void flipHorizontal() {
    xoff = getParent().width() - xoff;
  }

  public int getX() { return xoff; }

  public int getY() { return yoff; }

  public int getLayoutX() { return parent.getX() + xoff; }

  public int getLayoutY() { return parent.getY() + yoff; }

  public void connectNet(PNet n) { tnet = n; }

  public void disconnectNet() { tnet = null; }

  public void paintTerminal(Graphics g) {
    g.setColor(Color.black);
    g.fillRect(getX()-2, getY()-2, 5, 5);
  }



  public static PTerminal parseTerminal(StringTokenizer t, PLayout pl) {
     String tname = t.nextToken();
     String mname = t.nextToken();
     PModule m = pl.findModule(mname);
     int x = Integer.parseInt(t.nextToken());
     int y = Integer.parseInt(t.nextToken());
     PTerminal pt = new PTerminal(tname,m,x,y);
     m.addTerminal(pt);
     return pt;
  }
}







package placement;
import placement.moves.PMove;

import java.awt.*;
import java.util.StringTokenizer;
import java.util.Vector;

public class PModule {
    private int x;
    private int y;
    private int w;
    private int h;
    private String name;
    private static PLayout layout;

    public static void setLayout(PLayout p) { layout = p; }

    public String getName() { return name; }
    /**@shapeType AggregationLink
    @associates <b>PTerminal</b>
  * @clientCardinality 1
  * @supplierCardinality **/
    private Vector lnkTerminals = new Vector();

    public boolean isMoveModule() {
      PMove lm = layout.getLastMove();
      if (lm == null) return false;
      else return (lm.getMoveModule() == this);
    }

    public Color getColor() {
      if (isMoveModule()) {
        PMove lm = layout.getLastMove();
	return lm.getMoveStatus().getColor();
      } else return Color.yellow;
    }

    public PTerminal getTerminal(int i) { return (PTerminal)(lnkTerminals.elementAt(i)); }

    public int numTerminals() { return lnkTerminals.size(); }

    public void addTerminal(PTerminal t) {
        if (!lnkTerminals.contains(t)) lnkTerminals.addElement(t);
    }

    public PTerminal findTerminal(String tname) {
      for (int i = 0; i < lnkTerminals.size(); i++) {
        PTerminal pt = (PTerminal)lnkTerminals.elementAt(i);
        if (tname.equals(pt.getName())) return pt;
      }
      return null;
    }

    public PModule(String n, int xi, int yi, int wi, int hi) {
		name = n;
		x = xi;
		y = yi;
		w = wi;
		h = hi;
	}

	public void setLocation(int nx, int ny) { x = nx; y = ny; layout.calcCost(); }

        public int area() { return w * h; }

	public int getX() { return x; }

	public int getY() { return y; }

        public int getCenterX() { return getX() + (width() / 2); }

        public int getCenterY() { return getY() + (height() / 2); }

	public int leftEdge() { return x; }

	public int rightEdge() { return x+w; }

	public int topEdge() { return y; }

	public int bottomEdge() { return y+h; }

	public int width() { return w; }

	public int height() { return h; }

	public boolean overlaps(PModule c) {
		if (c.leftEdge() > rightEdge()) return false;
		else if (c.rightEdge() < leftEdge()) return false;
		else if (c.bottomEdge() < topEdge()) return false;
		else if (c.topEdge() > bottomEdge()) return false;
		else return true;
  	}

  	public int overlapArea(PModule c) {
    	if (!overlaps(c)) return 0;
		int left = Math.max(c.leftEdge(), leftEdge());
		int right = Math.min(c.rightEdge(), rightEdge());
		int top = Math.max(c.topEdge(), topEdge());
		int bottom = Math.min(c.bottomEdge(), bottomEdge());
		return (bottom-top) * (right-left);
	}

        public int move(int dx, int dy) {
          x = x + dx;
          y = y + dy;
          layout.calcCost();
          return layout.getDeltaCost();
        }

	public int rotate() {
	  int tw = w;
	  w = h;
	  h = tw;
          for (int i=0; i < lnkTerminals.size(); i++) {
	    PTerminal pt = (PTerminal)lnkTerminals.elementAt(i);
	    pt.rotate();
          }
          layout.calcCost();
          return layout.getDeltaCost();
	}

        /** flip module vertically (move ports) */
        public int flipVertical() {
          for (int i=0; i < lnkTerminals.size(); i++) {
	    PTerminal pt = (PTerminal)lnkTerminals.elementAt(i);
	    pt.flipVertical();
          }
          layout.calcCost();
          return layout.getDeltaCost();
	}

        /** flip module horizontally (move ports) */
        public int flipHorizontal() {
          for (int i=0; i < lnkTerminals.size(); i++) {
	    PTerminal pt = (PTerminal)lnkTerminals.elementAt(i);
	    pt.flipHorizontal();
          }
          layout.calcCost();
          return layout.getDeltaCost();
	}

	public String toString() {
		return "PModule " + name + "(" + x + "," + y + "," + w + "," + h + ") area=" + area();
	}

	public static PModule parseModule(StringTokenizer t) {
		String rname = t.nextToken();
		int x = Integer.parseInt(t.nextToken());
		int y = Integer.parseInt(t.nextToken());
		int w = Integer.parseInt(t.nextToken());
		int h = Integer.parseInt(t.nextToken());
		return new PModule(rname,x,y,w,h);
	}
	public static void main(String[] args) {
		PModule c1, c2, c3;
		c1 = new PModule("r1",3,3,4,4);
		c2 = new PModule("r2",2,5,3,3);
		c3 = new PModule("r3",8,2,2,5);
		System.out.println("c1: " + c1);
		System.out.println("c2: " + c2);
		System.out.println("c3: " + c3);
		System.out.println("c1.overlaps(c2) => " + c1.overlaps(c2));
		System.out.println("c1.overlaps(c3) => " + c1.overlaps(c3));
		System.out.println("c2.overlaps(c2) => " + c2.overlaps(c3));
		System.out.println("c1.overlapArea(c2) => " + c1.overlapArea(c2));
		System.out.println("c1.overlapArea(c3) => " + c1.overlapArea(c3));
		System.out.println("c2.overlapArea(c3) => " + c2.overlapArea(c3));
	}

}











package placement;
import placement.moves.PMove;

import java.util.Vector;
import java.util.Enumeration;
import java.io.*;
import java.util.StringTokenizer;
import java.awt.*;
import placement.PModule;

public class PLayout {

	private Vector modules = new Vector();	  // contains PModule objects
	private Vector nets = new Vector();	// contans PNet objects
	private int curArea = 0;
        private int curRightEdge = 0;
        private int curLeftEdge = 0;
        private int curBottomEdge = 0;
        private int curTopEdge = 0;
        private int oldArea = 0;
        private int curOlap = 0;
        private int oldOlap = 0;
        private int curWirelength = 0;
        private int oldWirelength = 0;
        private int curCost = 0;
	private int oldCost = 0;
        private PModule moveModule;
        private int moveModuleNumber;
	private boolean verbose = false;
	private static final int K_OLAP = 5;
        private static final int K_NETLENGTH = 10;

	public PLayout(String fname) {
		readLayout(fname);
                PModule.setLayout(this);
	}

        public PLayout(BufferedReader in) throws IOException {
                readLayout(in);
                PModule.setLayout(this);
        }



        private PMove lastMove = null;

        public PMove getLastMove() { return lastMove; }


        public void selectMove() {
          lastMove = PMove.selectMove(selectModule());
        }

        public int applyMove() {
          lastMove.apply();
          return lastMove.getDeltaCost();
        }

    public void resetMoveHistory() {
	PMove.resetHistory(currentCost());
    }
    
    public boolean greedyAccept() {
	return lastMove.greedyAccept();
    }

    public boolean metropolisAccept(double temp) {
	return lastMove.metropolisAccept(temp);
    }
    
    public void rejectMove() {
	lastMove.reject();
    }

	public void acceptMove() {
	  lastMove.accept();
	}

	public void completeMove() {
	  lastMove.complete();
          lastMove = null;
	}

        /** calculate all costs */
	public int calcCost() {
		oldCost = curCost;
                oldOlap = curOlap;
                oldWirelength = curWirelength;
                curArea = calcArea();
                curOlap = calcOverlap();
                curWirelength = calcWirelength();
                curCost = curArea + K_NETLENGTH*curWirelength + K_OLAP*curOlap*curOlap;
		return curCost;
	}
        /** return deltaCost from last move application */
        public int getDeltaCost() { return curCost - oldCost; }

	public int undoUpdateCost() {
                curArea = oldArea;
                curOlap = oldOlap;
                curWirelength = oldWirelength;
                curCost = oldCost;
		return curCost;
	}

        /** return current cost without re-calculating */
	public int currentCost() { return curCost; }

        public int currentArea() { return curArea; }

        public int currentWidth() { return curRightEdge - curLeftEdge; }

        public int currentHeight() { return curBottomEdge - curTopEdge; }

        public int currentRightEdge() { return curRightEdge; }
        public int currentLeftEdge() { return curLeftEdge; }
        public int currentBottomEdge() { return curBottomEdge; }
        public int currentTopEdge() { return curTopEdge; }

        public int currentOverlap() { return curOlap; }

        public int currentWirelength() { return curWirelength; }

	public int calcArea() {
		int minLeft, maxRight, minTop, maxBottom;
		maxRight = maxBottom = 0;
		minLeft = minTop = Integer.MAX_VALUE;
    	 	for (Enumeration e = modules.elements() ; e.hasMoreElements() ;) {
         		PModule pm = (PModule)e.nextElement();
			minLeft = Math.min(minLeft, pm.leftEdge());
			maxRight = Math.max(maxRight, pm.rightEdge());
			minTop = Math.min(minTop, pm.topEdge());
			maxBottom = Math.max(maxBottom, pm.bottomEdge());
		}
                curRightEdge = maxRight;
                curLeftEdge = minLeft;
                curBottomEdge = maxBottom;
                curTopEdge = minTop;
		return (maxBottom - minTop) * (maxRight - minLeft);
	}

        public int calcWirelength() {
          int wiresum = 0;
          for (Enumeration e = nets.elements() ; e.hasMoreElements() ;) {
            PNet pn = (PNet)e.nextElement();
            wiresum += pn.netLength();
          }
          return wiresum;
        }

	public void reportCost() {
		System.out.println("area=" + currentArea() + " wirelength=" + currentWirelength() + " olap=" + currentOverlap() + " cost=" + currentCost());
	}

	public int calcOverlap() {
		int olap_sum = 0;
		int i, j;
		for (i = 0; i < modules.size(); i++ ) {
			PModule pmi = (PModule)modules.elementAt(i);
			for (j = i+1; j < modules.size(); j++) {
				PModule pmj = (PModule)modules.elementAt(j);
				olap_sum += pmi.overlapArea(pmj);
			}
		}
		return olap_sum;
	}

  public PModule findModule(String mn) {
    for (int i = 0; i < modules.size(); i++ ) {
      PModule pm = (PModule)modules.elementAt(i);
      if (mn.equals(pm.getName())) return pm;
    }
    return null;
  }

  public void addModule(PModule m) {
    modules.addElement(m);
  }

  public PModule getModule(int i) { return (PModule)modules.elementAt(i); }

  /** choose a module at random */
  public PModule selectModule() {
    moveModuleNumber = (int)(Math.random() * modules.size());
    return moveModule = (PModule)modules.elementAt( moveModuleNumber );
  }

  /** clean this up later w.r.t. PMove */
  public int getMoveModuleNumber() { return moveModuleNumber; }

  public int numModules() { return modules.size(); }

  public int numNets() { return nets.size(); }

  public PNet getNet(int i) { return (PNet)nets.elementAt(i); }

  public PNet findNet(String mn) {
    for (int i = 0; i < nets.size(); i++ ) {
      PNet pn = (PNet)nets.elementAt(i);
      if (mn.equals(pn.getName())) return pn;
    }
    return null;
  }

  public void addNet(PNet n) {
    if (nets.contains(n))
      System.out.println("Net.addNet: trying to add duplicate net: " + n);
    else nets.addElement(n);
  }


  public void readLayout(BufferedReader in) throws IOException {
    String line;
      while ((line = in.readLine()) != null) {
        StringTokenizer t = new StringTokenizer(line);
        String kw = t.nextToken();
        if (kw.equals("module")) addModule(PModule.parseModule(t));
        else if (kw.equals("terminal")) {
          PTerminal.parseTerminal(t, this);
        }
        else if (kw.equals("net")) PNet.parseNet(t, this);
        else System.out.println("Unrecognized object: " + kw);
      }
  }

  public void readLayout(String fname) {
    try {
      BufferedReader in = new BufferedReader(new FileReader(fname));
      readLayout(in);
    } catch (FileNotFoundException e) {
      System.out.println("Couldn't open file: " + fname);
      System.exit(0);
    } catch (IOException e) {
      System.out.println("Caught IOException: " + e);
    }
  }

	public String toString() {
		String result = "PLayout [area=" + currentArea() + " overlap=" + currentOverlap() + "\n";
	    for (Enumeration e = modules.elements() ; e.hasMoreElements() ;) {
         	PModule pm = (PModule)e.nextElement();
			result = result + "\t" + pm + "\n";
		}
		result += "]\n";
		return result;
	}

	public static void main(String [] args) {
		PLayout pl = new PLayout(args[0]);
		System.out.println(pl);
	}


}



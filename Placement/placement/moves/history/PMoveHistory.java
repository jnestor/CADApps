import java.util.List;
import java.util.ArrayList;

//package placement.moves
//import placement.*;
//import placement.ui.*;

/**
 * Title:        PlacementApplet
 * Description:  Animation of VLSI Module Placement
 * Copyright:    Copyright (c) 2001,2003
 * Company:      Lafayette College
 * @author John Nestor
 * @version 1.0
 */

/** PMoveHistory keeps track of information about a sequence of moves */

class PMoveHistory {
    private List points = new ArrayList(500);

    private int maxCost = 0;
    private int minCost= 0;
    private int initCost = 0;
    private int lastCost = 0;

    /** erase all history information */
    public void reset() {
	points.clear();
	maxCost = minCost = initCost = lastCost = 0;
    }

    public void add(PMoveHistoryPoint p) {
	points.add(p);
	if (p.accepted()) {
	    lastCost = lastCost + p.getDeltaCost();
	    if (lastCost > maxCost) maxCost = lastCost;
	    if (lastCost < minCost) minCost = lastCost;
	}
    }

    public PMoveHistoryPoint get(int i) { return (PMoveHistoryPoint)points.get(i); }

    public int size() { return points.size(); }

    public void setInitCost(int icost) {
	initCost = icost;
	minCost = icost;
	maxCost = icost;
	lastCost = icost;
    }

    public int getInitCost() { return initCost; }

    public int getMinCost() { return minCost; }

    public int getMaxCost() { return maxCost; }

//    public static void main(String [] args) {}
	
}

    

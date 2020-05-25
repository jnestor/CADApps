package placement.moves;

import java.util.List;
import java.util.ArrayList;
import placement.*;
import placement.ui.*;

/**
 * Title:        PlacementApplet
 * Description:  Animation of VLSI Module Placement
 * Copyright:    Copyright (c) 2001,2003
 * Company:      Lafayette College
 * @author John Nestor
 * @version 1.0
 */

/** PMoveHistory keeps track of information about a sequence of moves */

public class PMoveHistory {
    private List points = new ArrayList(500);

    private int maxCost = 0;
    private int minCost= 0;
    private int initCost = 0;
    private int lastCost = 0;
    private int acceptCount = 0;
    private double sumAcceptedCost = 0.0;

    /** erase all history information */
    public void reset(int cost) {
	points.clear();
	maxCost = minCost = initCost = lastCost = cost;
	acceptCount = 0;
	sumAcceptedCost = 0.0;
    }

    public void add(PMoveHistoryPoint p) {
	points.add(p);
	if (p.accepted()) {
	    acceptCount++;
	    lastCost = lastCost + p.getDeltaCost();
	    sumAcceptedCost += (double)lastCost;
	    if (lastCost > maxCost) maxCost = lastCost;
	    if (lastCost < minCost) minCost = lastCost;
	}
    }

    public PMoveHistoryPoint get(int i) { return (PMoveHistoryPoint)points.get(i); }

    public PMoveHistoryPoint getCurrent() { return (PMoveHistoryPoint)points.get(points.size()-1); }

    public int size() { return points.size(); }

    public int getAttemptCount() { return points.size(); }

    public int getAcceptCount() { return acceptCount; }


    public int getRejectCount() { return getAttemptCount() - acceptCount; }

    public double getAcceptPercent() {
	if (getAttemptCount() == 0) return 0.0;
	else return ((double)getAcceptCount() / (double)getAttemptCount()) * 100.0;
    }

    public void setInitCost(int icost) {
	initCost = icost;
	minCost = icost;
	maxCost = icost;
	lastCost = icost;
    }

    public int getInitCost() { return initCost; }

    public int getMinCost() { return minCost; }

    public int getMaxCost() { return maxCost; }

    public int getAverageCost() {
	return (int) (sumAcceptedCost / (double)acceptCount);
    }

    public double getStdev() {
	double stdevSum = 0;
	int curCost = initCost;
	double averageCost = getAverageCost();
	for (int i=0; i<points.size(); i++ ) {
	    curCost = curCost + ((PMoveHistoryPoint)points.get(i)).getDeltaCost();
	    double diff = (double)curCost - averageCost;
	    stdevSum += diff * diff;
	}
	return Math.sqrt(stdevSum / (acceptCount-1));
    }
    
    
}

    

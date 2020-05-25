package placement.anneal;

import java.util.List;
import java.util.ArrayList;

/**
 * Title:        PlacementApplet
 * Description:  Animation of VLSI Module Placement
 * Copyright:    Copyright (c) 2001
 * Company:      Lafayette College
 * @author John Nestor
 * @version 1.0
 */

/** Tracks history information during annealing.  A "history"
    is represented as an array of "temperature points" which store
    summary information about annealing at that temperature.  Each
    temperature records cost information in a number of "samples" - one
    for each accepted move, and keeps track of max, min, and stdev of cost. */
//
//    April 3, 2003 - Thisneeds to be modified to use the new PMoveHistory class!
//
//
public class PTempHistory {
  private double maxTemperature = 0.0;

  public double getOverallMaxTemperature() { return maxTemperature; }

//  private double overallMaxCost = 0.0;

  /** overall maximum cost <b>including rejected configurations</b> */
//  public double getOverallMaxCost() { return overallMaxCost; }

  public double maxAcceptedCost = 0.0;

  /** return maximum configuration accepted during annealing */
  public double getMaxAcceptedCost() { return maxAcceptedCost; }

  private double maxAverageCost = 0.0;

  public double getMaxAverageCost() { return maxAverageCost; }

  private double overallMinCost = Double.MAX_VALUE;

  public double getOverallMinCost() { return overallMinCost; }

  private List historyList = new ArrayList();
  private PTempHistoryPoint currentTempPoint = null;

  /** create a new temperature point and add to the list */
  public void addTempPoint(double t, int curCost) {
    if (currentTempPoint != null)// record max average cost for plotting
      maxAverageCost = Math.max(maxAverageCost, currentTempPoint.getAverageCost());
    currentTempPoint = new PTempHistoryPoint(t, curCost);
    historyList.add(currentTempPoint);
    maxTemperature = Math.max(maxTemperature,t);
  }

  public PTempHistoryPoint getTempPoint(int i) {
    return (PTempHistoryPoint)historyList.get(i);
  }

  public PTempHistoryPoint getCurrentTempPoint() {
    return currentTempPoint;
  }

    public void completeCurrentTempPoint() {
	currentTempPoint.complete();
	maxAcceptedCost = Math.max(maxAcceptedCost,currentTempPoint.getMaxCost());
	overallMinCost = Math.min(overallMinCost, currentTempPoint.getMinCost());
    }

  public int numTempPoints() { return historyList.size(); }

  /** prepare to restart anneling - erase all old temp. information */
  public void reset() {
    historyList.clear();
    maxTemperature = 0.0;
//    overallMaxCost = 0.0;
    maxAcceptedCost = 0.0;
    maxAverageCost = 0.0;
    overallMinCost = Double.MAX_VALUE;
  }

  /** record an annealing attempt for current temperature */
/*  public void addSample(double sampleCost, boolean accepted) {
    overallMaxCost = Math.max(overallMaxCost, sampleCost);
    if (accepted) maxAcceptedCost = Math.max(maxAcceptedCost, sampleCost);
    overallMinCost = Math.min(overallMinCost,sampleCost);
    currentTempPoint.addSample(sampleCost, accepted);
  }
*/
  public double getCurrentTemp() { return currentTempPoint.getTemperature(); }
}

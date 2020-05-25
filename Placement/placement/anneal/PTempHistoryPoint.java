package placement.anneal;
import placement.moves.PMove;

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
    summary information about annealing at that temperature.  

    Works with the static PMoveHistory object in the PMove class to
    record statistics for each move until the temperature is "completed" -
    at this point summary information is transferrred into the object fields. */


public class PTempHistoryPoint {
    
    private double temperature = 0.0;
    private double completedMaxCost = 0;
    private double completedMinCost = Double.MAX_VALUE;
    private double completedAverageCost = 0;
    private int completedAttemptCount = 0;
    private int completedAcceptCount = 0;
    private boolean isCompleted = false;

    public PTempHistoryPoint(double t, int initialCost) {
	temperature = t;
	PMove.resetHistory(initialCost);
    }

    public double getTemperature() { return temperature; }

    public double getMaxCost() {
	if (isCompleted) return completedMaxCost;
	else return PMove.getHistory().getMaxCost();
    }

    public double getMinCost() {
	if (isCompleted) return completedMinCost;
	else return PMove.getHistory().getMinCost();
    }

    public double getAverageCost() {
	if (isCompleted) return completedAverageCost;
	else return PMove.getHistory().getAverageCost();
    }

    public int getAttemptCount() {
	if (isCompleted) return completedAttemptCount;
	else return PMove.getHistory().getAttemptCount();
    }

    public int getAcceptCount() {
	if (isCompleted) return completedAcceptCount;
	else return PMove.getHistory().getAcceptCount();
    }

    public int getRejectCount() {
	if (isCompleted) return completedAttemptCount - completedAcceptCount;
	else return PMove.getHistory().getRejectCount();
    }

    public double getAcceptPercent() {
	if (isCompleted) return (double)completedAcceptCount / (double)(completedAttemptCount) * 100.0;
	else return PMove.getHistory().getAcceptPercent();
    }

    void complete() {
	completedMinCost = PMove.getHistory().getMinCost();
	completedMaxCost = PMove.getHistory().getMaxCost();
	completedAverageCost = PMove.getHistory().getAverageCost();
	completedAttemptCount = PMove.getHistory().getAttemptCount();
	completedAcceptCount = PMove.getHistory().getAcceptCount();
	isCompleted = true;
    }

}

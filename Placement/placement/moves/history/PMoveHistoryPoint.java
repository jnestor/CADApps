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

/** PMoveHistoryPoint keeps track of information about a move attempt.  
    Intended to support both plain iterative improvement and simulated annealing. */
    
public class PMoveHistoryPoint {
    private int  deltaCost;
    private double acceptProbability;
    private double acceptRandom;
//    private PMove pm;


    public PMoveHistoryPoint(int dc, double ap, double ar/*, PMove m*/) {
	deltaCost = dc;
	acceptProbability = ap;
	acceptRandom = ar;
//	pm = m;
    }

    public boolean accepted() { return deltaCost < 0 || acceptRandom > acceptProbability; }


    public int getDeltaCost() { return deltaCost; }

    public double getProb() { return acceptProbability; }

    public double getR() { return acceptRandom; }

//    public PMove getMove { return pm; }

}

package placement.anneal;

import java.util.Vector;

import placement.*;
import placement.moves.PMove;
import placement.ui.UIAnnealer;


/**
 * Title:        PlacementApplet
 * Description:  Animation of VLSI Module Placement
 * Copyright:    Copyright (c) 2001
 * Company:      Lafayette College
 * @author John Nestor
 * @version 1.0
 */

public class PAnnealer implements PAnnealInterface {
  private PTempHistory tempHistory = new PTempHistory();

  public PTempHistory getTempHistory() { return tempHistory; }

  private UIAnnealer myUI;
  private PLayout myLayout;
  private double currentTemperature;

  public PAnnealer(PLayout pl, UIAnnealer u) {
    myLayout = pl;
    myUI = u;
  }

  // void setUIAnnealer(UIAnnealer u) { myUI = u; }

  private static final int SIGMA_MULTIPLIER = 20;

  private double getT() { return currentTemperature; }

  private void setT(double temp) {
    currentTemperature = temp;
    PMove.setT(temp);
  }

  public void setT0(double temp) {
    currentTemperature = temp;
    PMove.setT0(temp);
  }

  public void findT0() throws InterruptedException {
    setT0(Double.POSITIVE_INFINITY);
    tempHistory.reset();
    showUpdateTemperature();
    tempHistory.addTempPoint(getT(),myLayout.currentCost());
    metropolis(tempHistory);
    double sigma = PMove.getHistory().getStdev();
    setT0(sigma * SIGMA_MULTIPLIER);
    // System.out.println("PAnnealer.findT0: sigma=" + sigma + " T0=" + currentTemperature);
  }

  /** check whether cost has improved more than given percentage during last N */
  public boolean terminate(int percentImp, int lastN) {
    int numTemps = tempHistory.numTempPoints();
    if (numTemps < lastN) return false;
    else {
      double lastCost = tempHistory.getTempPoint(numTemps-1).getMinCost();
      for (int i = numTemps-2; i >= numTemps - lastN; i--) {
        double prevCost = tempHistory.getTempPoint(i).getMinCost();
        if ( (lastCost < prevCost) && (
             ((int)((prevCost - lastCost)/prevCost * 100)) > percentImp) )
               return false;
        lastCost = prevCost;
      }
    }
    return true;
  }

  /** alternate form - returns true when no CHANGE (plus OR minus) for N temps */
  public boolean terminate(int lastN) {
    int numTemps = tempHistory.numTempPoints();
    if (numTemps < lastN) return false;
    else {
      double lastCost = tempHistory.getTempPoint(numTemps-1).getMinCost();
      for (int i = numTemps-2; i >= numTemps - lastN; i--) {
        double prevCost = tempHistory.getTempPoint(i).getMinCost();
        if (lastCost != prevCost)  return false;
      }
    }
    return true;
  }

  /** apply moves under control of the Metropolis Algorithm */
    public void metropolis(PTempHistory tempHistory) throws InterruptedException {
	int attemptsPerTemp = myUI.getOptMovesPerTemp();
	if (myUI.getOptMovesPerTempPerModule()) attemptsPerTemp *= myLayout.numModules();
	for (int i = 0; i < attemptsPerTemp; i++) {
	    myLayout.selectMove();
	    showSelectMove();
	    myLayout.applyMove();
	    if ( myLayout.metropolisAccept(getT()) ) {
		showAcceptMove();
	    } else {
		showRejectMove();
	    }
	    myLayout.completeMove();
	    showCompleteMove();
	}
    }
	
    /** run the anneal.  DOES NOT alter initial temperature! */
    public void anneal() throws InterruptedException {
	PMove.setWindow(2 * myLayout.currentWidth(),2 * myLayout.currentHeight());
	tempHistory.reset();
	showUpdateTemperature();
	do {
	    tempHistory.addTempPoint(getT(),myLayout.currentCost());
	    metropolis(tempHistory);
	    tempHistory.completeCurrentTempPoint();
	    showUpdateTemperature();
	    setT( getT() * myUI.getOptCoolRate() );
	} while (!terminate(5));
    }

  // Methods from PAnnealerInterface - use to dispatch updates in UI to different views

  public void showSelectMove() throws InterruptedException {
    myUI.showSelectMove();
  }

  public void showAcceptMove() throws InterruptedException {
    myUI.showAcceptMove();
//    tempHistory.addSample(myLayout.currentCost(),true);
  }

  public void showRejectMove() throws InterruptedException {
    myUI.showRejectMove();
//    tempHistory.addSample(myLayout.currentCost(),false);
  }

  public void showCompleteMove() throws InterruptedException {
    myUI.showCompleteMove();
  }

  public void showUpdateTemperature() throws InterruptedException {
    myUI.showUpdateTemperature();
  }
}

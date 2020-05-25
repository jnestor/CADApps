package placement.anneal;

import java.util.Vector;

import placement.*;
import placement.moves.PMove;
import placement.ui.UIIterImprove;


/**
 * Title:        PlacementApplet
 * Description:  Animation of VLSI Module Placement
 * Copyright:    Copyright (c) 2001
 * Company:      Lafayette College
 * @author John Nestor
 * @version 1.0
 */

// hacked from UIAnnealer - need to finish before adding to code!

public class PIterImprove implements PAnnealInterface {

  private UIIterImprove myUI;
  private PLayout myLayout;


    public PIterImprove(PLayout pl, UIIterImprove u) {
	myLayout = pl;
	myUI = u;
    }

    public void improve(int attempts) throws InterruptedException {
	PMove.setWindow(myLayout.currentWidth() / 2, myLayout.currentHeight() / 2);
	
	myLayout.resetMoveHistory();

	for (int i = 0; i < attempts; i++) {
	    myLayout.selectMove();
	    showSelectMove();
	    myLayout.applyMove();
	    if ( myLayout.greedyAccept() ) {
		showAcceptMove();
	    } else {
		showRejectMove();
	    }
	    myLayout.completeMove();
	    showCompleteMove();
	}
    }

    // Methods from PAnnealerInterface - use to dispatch updates in UI to different views
    
    public void showSelectMove() throws InterruptedException {
	myUI.showSelectMove();
    }
    
    public void showAcceptMove() throws InterruptedException {
	myUI.showAcceptMove();
    }
    
    public void showRejectMove() throws InterruptedException {
	myUI.showRejectMove();
    }
    
    public void showCompleteMove() throws InterruptedException {
	myUI.showCompleteMove();
    }
    
    public void showUpdateTemperature() throws InterruptedException {
	myUI.showUpdateTemperature();
    }
}

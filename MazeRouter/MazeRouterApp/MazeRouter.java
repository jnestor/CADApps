/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author 15002
 */
public class MazeRouter extends Router {
    
    public MazeRouter(Grid g){
        super(g);
    }
    
    /* expand a routing search */
    public int expandGrid(GridPoint gridPoint) throws InterruptedException {
        GridPoint xp;
        if ((xp = gridPoint.westNeighbor()) != null && xp.getGVal() == UNROUTED) {
            xp.setVals(gridPoint.getGVal() + 1);
            xp.setDisplayVal(gridPoint.getGVal() + 1);
            if (xp.isTarget()) {
                return xp.getGVal();
            } else {
                myGrid.enqueueGridPoint(xp);
            }
        }
        if ((xp = gridPoint.eastNeighbor()) != null && xp.getGVal() == UNROUTED) {
            xp.setVals(gridPoint.getGVal() + 1);
            xp.setDisplayVal(gridPoint.getGVal() + 1);
            if (xp.isTarget()) {
                return xp.getGVal();
            } else {
                myGrid.enqueueGridPoint(xp);
            }
        }
        if ((xp = gridPoint.southNeighbor()) != null && xp.getGVal() == UNROUTED) {
            xp.setVals(gridPoint.getGVal() + 1);
            xp.setDisplayVal(gridPoint.getGVal() + 1);
            if (xp.isTarget()) {
                return xp.getGVal();
            } else {
                myGrid.enqueueGridPoint(xp);
            }
        }
        if ((xp = gridPoint.northNeighbor()) != null && xp.getGVal() == UNROUTED) {
            xp.setVals(gridPoint.getGVal() + 1);
            xp.setDisplayVal(gridPoint.getGVal() + 1);
            if (xp.isTarget()) {
                return xp.getGVal();
            } else {
                myGrid.enqueueGridPoint(xp);
            }
        }
        if ((xp = gridPoint.upNeighbor()) != null && xp.getGVal() == UNROUTED) {
            xp.setVals(gridPoint.getGVal() + 1);
            xp.setDisplayVal(gridPoint.getGVal() + 1);
            if (xp.isTarget()) {
                return xp.getGVal();
            } else {
                myGrid.enqueueGridPoint(xp);
            }
        }
        if ((xp = gridPoint.downNeighbor()) != null && xp.getGVal() == UNROUTED) {
            xp.setVals(gridPoint.getGVal() + 1);
            xp.setDisplayVal(gridPoint.getGVal() + 1);
            if (xp.isTarget()) {
                return xp.getGVal();
            } else {
                myGrid.enqueueGridPoint(xp);
            }
        }
        return -1;
    }
    
    public int expansion() throws InterruptedException {
        myGrid.setState(EXPANDING);
        GridPoint gp;
        int actualLength;
        int curVal = 0;
        myGrid.setMessage("Expansion phase");
        myGrid.gridDelay(3);
        if (myGrid.getSource() != null && myGrid.getTarget() != null) {
            myGrid.getSource().initExpand();
            if ((actualLength = expandGrid(myGrid.getSource())) > 0) {
                myGrid.clearQueue();
                return actualLength; // found it right away!
            }
            while ((gp = myGrid.dequeueGridPoint()) != null && !stop) {
                if (myGrid.isPaused()) {
                    myGrid.setMessage("Current distance: " + myGrid.getTail().getGVal() + " Pause");
                    synchronized (this) {
                        wait();
                    }
                }
                myGrid.setMessage("Current distance: " + myGrid.getTail().getGVal());
                if (myGrid.getMode() && (gp.getGVal() > curVal)) {
                    curVal = gp.getGVal();
                    myGrid.redrawGrid();
                    myGrid.gridDelay(2);
                }
                if ((actualLength = expandGrid(gp)) > 0) {
                    myGrid.setMessage("Current distance: " + actualLength);
                    myGrid.gridDelay(5);
                    myGrid.clearQueue();
                    return actualLength;  // found it!
                }
            }
        }
        return -1;
    }
}

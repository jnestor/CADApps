import java.util.*;
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

    public MazeRouter(Grid g) {
        super(g);
    }

    /* expand a routing search */
    @Override
    public int expandGrid(GridPoint gridPoint) throws InterruptedException {
        GridPoint xp;
        if ((xp = gridPoint.westNeighbor()) != null && xp.getGVal() == UNROUTED) {
            xp.setVals(gridPoint.getGVal() + 1);
            xp.setDisplayVal(gridPoint.getGVal() + 1);
            if (xp.isTarget()) {
                return xp.getGVal();
            } else {
                enqueueGridPoint(xp);
            }
        }
        if ((xp = gridPoint.eastNeighbor()) != null && xp.getGVal() == UNROUTED) {
            xp.setVals(gridPoint.getGVal() + 1);
            xp.setDisplayVal(gridPoint.getGVal() + 1);
            if (xp.isTarget()) {
                return xp.getGVal();
            } else {
                enqueueGridPoint(xp);
            }
        }
        if ((xp = gridPoint.southNeighbor()) != null && xp.getGVal() == UNROUTED) {
            xp.setVals(gridPoint.getGVal() + 1);
            xp.setDisplayVal(gridPoint.getGVal() + 1);
            if (xp.isTarget()) {
                return xp.getGVal();
            } else {
                enqueueGridPoint(xp);
            }
        }
        if ((xp = gridPoint.northNeighbor()) != null && xp.getGVal() == UNROUTED) {
            xp.setVals(gridPoint.getGVal() + 1);
            xp.setDisplayVal(gridPoint.getGVal() + 1);
            if (xp.isTarget()) {
                return xp.getGVal();
            } else {
                enqueueGridPoint(xp);
            }
        }
        if ((xp = gridPoint.upNeighbor()) != null && xp.getGVal() == UNROUTED) {
            xp.setVals(gridPoint.getGVal() + 1);
            xp.setDisplayVal(gridPoint.getGVal() + 1);
            if (xp.isTarget()) {
                return xp.getGVal();
            } else {
                enqueueGridPoint(xp);
            }
        }
        if ((xp = gridPoint.downNeighbor()) != null && xp.getGVal() == UNROUTED) {
            xp.setVals(gridPoint.getGVal() + 1);
            xp.setDisplayVal(gridPoint.getGVal() + 1);
            if (xp.isTarget()) {
                return xp.getGVal();
            } else {
                enqueueGridPoint(xp);
            }
        }
        return -1;
    }

    @Override
    public int expansion() throws InterruptedException {
        System.out.println("EXAPNDING");
        myGrid.setState(EXPANDING);
        GridPoint gp;
        int actualLength;
        int curVal = 0;
        myGrid.setMessage("Expansion phase");
        myGrid.gridDelay(3);
        if (myGrid.getSource() != null && myGrid.getTarget() != null) {
            myGrid.getSource().initExpand();
            if ((actualLength = expandGrid(myGrid.getSource())) > 0) {
                clearQueue();
                return actualLength; // found it right away!
            }
            while ((gp = dequeueGridPoint()) != null && !stop) {
                if (myGrid.isPaused()) {
                    myGrid.setMessage("Current distance: " + getTail().getGVal() + " Pause");
                    synchronized (this) {
                        wait();
                    }
                }
                myGrid.setMessage("Current distance: " + getTail().getGVal());
                if (myGrid.getMode() && (gp.getGVal() > curVal)) {
                    curVal = gp.getGVal();
                    myGrid.redrawGrid();
                    myGrid.gridDelay(2);
                }
                if ((actualLength = expandGrid(gp)) > 0) {
                    myGrid.setMessage("Current distance: " + actualLength);
                    myGrid.gridDelay(5);
                    clearQueue();
                    return actualLength;  // found it!
                }
            }
        }
        return -1;
    }

    private final Queue<GridPoint> gridPointList = new LinkedList<GridPoint>();
    private GridPoint gridPointTail;

    @Override
    public GridPoint getTail() {
        return gridPointTail;
    }

    @Override
    public void printGridPointQueue() { // for debugging - package visible
        System.out.println(gridPointList.toString());
    }

    @Override
    public void enqueueGridPoint(GridPoint gp) throws InterruptedException {
        gridPointList.add(gp);
        gp.setEnqueued(true);
        gridPointTail = gp;
        if (!myGrid.getMode()) {
            myGrid.redrawGrid();
            myGrid.gridDelay();
        }
    }

    @Override
    public GridPoint dequeueGridPoint() {
        GridPoint gp = gridPointList.poll();
        if (gp == null) {
            return null;
        } else {
            gp.setEnqueued(false);
            // debug
//          System.out.println("GridPoint.dequeuePoint - " + gp);
            return gp;
        }
    }

    @Override
    public void clearQueue() {
        gridPointList.clear();
    }
}

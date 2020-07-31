
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
public class LeeRouter extends MazeRouter {

    public LeeRouter(Grid g) {
        super(g);
    }
    private boolean expanded=false;
    /* expand a routing search */
    @Override
    public int expandGrid(GridPoint gridPoint) throws InterruptedException {
        GridPoint xp;
        expanded=false;
        if ((xp = gridPoint.westNeighbor()) != null && xp.getGVal() == UNROUTED) {
            xp.setVals(gridPoint.getGVal() + 1);
            xp.setDisplayVal(gridPoint.getGVal() + 1);
            if (xp.isTarget()) {
                beep();
                return xp.getGVal();
            } else {
                enqueueGridPoint(xp);
                expanded=true;
            }
        }
        if ((xp = gridPoint.eastNeighbor()) != null && xp.getGVal() == UNROUTED) {
            xp.setVals(gridPoint.getGVal() + 1);
            xp.setDisplayVal(gridPoint.getGVal() + 1);
            if (xp.isTarget()) {
                beep();
                return xp.getGVal();
            } else {
                enqueueGridPoint(xp);
                expanded=true;
            }
        }
        if ((xp = gridPoint.southNeighbor()) != null && xp.getGVal() == UNROUTED) {
            xp.setVals(gridPoint.getGVal() + 1);
            xp.setDisplayVal(gridPoint.getGVal() + 1);
            if (xp.isTarget()) {
                beep();
                return xp.getGVal();
            } else {
                enqueueGridPoint(xp);
                expanded=true;
            }
        }
        if ((xp = gridPoint.northNeighbor()) != null && xp.getGVal() == UNROUTED) {
            xp.setVals(gridPoint.getGVal() + 1);
            xp.setDisplayVal(gridPoint.getGVal() + 1);
            if (xp.isTarget()) {
                beep();
                return xp.getGVal();
            } else {
                enqueueGridPoint(xp);
                expanded=true;
            }
        }
        if ((xp = gridPoint.upNeighbor()) != null && xp.getGVal() == UNROUTED) {
            xp.setVals(gridPoint.getGVal() + 1);
            xp.setDisplayVal(gridPoint.getGVal() + 1);
            if (xp.isTarget()) {
                beep();
                return xp.getGVal();
            } else {
                enqueueGridPoint(xp);
                expanded=true;
            }
        }
        if ((xp = gridPoint.downNeighbor()) != null && xp.getGVal() == UNROUTED) {
            xp.setVals(gridPoint.getGVal() + 1);
            xp.setDisplayVal(gridPoint.getGVal() + 1);
            if (xp.isTarget()) {
                beep();
                return xp.getGVal();
            } else {
                enqueueGridPoint(xp);
                expanded=true;
            }
        }
        if (!myGrid.isParallel()&&expanded) {
            beep();
        }

        return -1;
    }

    @Override
    public int expansion() throws InterruptedException {
        myGrid.setState(EXPANDING);
        GridPoint gp;
        int actualLength;
        int curVal = 0;
        myGrid.setMessage("Expansion phase");
        myGrid.gridDelay(3);
        if (myGrid.getSource() != null && myGrid.getTarget() != null) {
            myGrid.getSource().initExpand();
            myGrid.setMessage("Click Start or Step to Continue");
            synchronized (this) {
                wait();
            }
            if (stop) {
                return -1;
            }
            if ((actualLength = expandGrid(myGrid.getSource())) > 0) {
                beep();
                clearQueue();
                return actualLength; // found it right away!
            }
            expanded=true;
            while ((gp = dequeueGridPoint()) != null && !stop) {
                if (myGrid.isPaused() && !myGrid.isParallel()&&expanded) {
                    if (getTail() != null) {
                        myGrid.setMessage("Current distance: " + getTail().getGVal() + " Pause");
                    } else {
                        myGrid.setMessage("Click Start or Step to Continue");
                    }
                    synchronized (this) {
                        wait();
                    }
                }
                gp.setEnqueued(false);
                myGrid.setMessage("Current distance: " + getTail().getGVal());
                if (myGrid.isParallel() && (gp.getGVal() > curVal)) {
                    if (myGrid.isPaused()) {
                        myGrid.setMessage("Current distance: " + getTail().getGVal() + " Pause");
                        synchronized (this) {
                            wait();
                        }
                    }
                    curVal = gp.getGVal();
                    beep();
                    myGrid.redrawGrid();
                    myGrid.gridDelay(3);
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
        if (gridPointTail != null) {
            maxGVal = gridPointTail.getGVal();
        }
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
        if (!myGrid.isParallel()) {
            myGrid.redrawGrid();
            myGrid.gridDelay(2);
        }
    }

    @Override
    public GridPoint dequeueGridPoint() {
        GridPoint gp = gridPointList.poll();
        if (gp == null) {
            return null;
        } else {

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

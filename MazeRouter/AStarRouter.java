
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
public class AStarRouter extends MazeRouter {

    public AStarRouter(Grid g) {
        super(g);
    }
    private boolean expanded=false;
    @Override
    public int expandGrid(GridPoint gridPoint) throws InterruptedException {
        GridPoint xp;
        expanded=false;
        if ((xp = gridPoint.westNeighbor()) != null && xp.getGVal() == UNROUTED) {
            xp.setVals(gridPoint.getGVal() + 1);
            xp.setDisplayVal(xp.getFVal());
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
            xp.setDisplayVal(xp.getFVal());
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
            xp.setDisplayVal(xp.getFVal());
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
            xp.setDisplayVal(xp.getFVal());
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
            xp.setDisplayVal(xp.getFVal());
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
            xp.setDisplayVal(xp.getFVal());
            if (xp.isTarget()) {
                beep();
                return xp.getGVal();
            } else {
                enqueueGridPoint(xp);
                expanded=true;
            }
        }
        if(expanded)
        beep();
        return -1;
    }

    @Override
    public int expansion() throws InterruptedException {
        myGrid.setState(EXPANDING);
        GridPoint gp;
        int actualLength;
        myGrid.setMessage("Expansion phase");
        resetGridPointQueue();
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
                clearQueue();
                return actualLength; // found it right away!
            }
            expanded = true;
            while ((gp = dequeueGridPoint()) != null && !stop) {
                if (myGrid.isPaused()&&expanded) {
                    if (getTail() != null) {
                    int val = getTail().getGVal();
                    myGrid.setMessage("Current distance: " + val
                            + " || Current cost: " + ((GridPoint) gpq.last()).getFVal());
                } else {
                    myGrid.setMessage("ROUTING STOPPED");
                }
                    synchronized (this) {
                        wait();
                    }
                }
                if (getTail() != null) {
                    int val = getTail().getGVal();
                    myGrid.setMessage("Current distance: " + val
                            + " || Current cost: " + ((GridPoint) gpq.last()).getFVal());
                } else {
                    myGrid.setMessage("ROUTING STOPPED");
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

    private TreeSet<GridPoint> gpq = new TreeSet<GridPoint>();

    private int queueCount = 0;  // track number of insertions to queue - use to break ties on nodes

    void resetGridPointQueue() {
        gpq.clear();
        queueCount = 0;
    }

    @Override
    void printGridPointQueue() { // for debugging - package visible
        Iterator it = gpq.iterator();
        System.out.println("Queue contents [" + gpq.size() + "]: ");
        while (it.hasNext()) {
            GridPoint g = (GridPoint) it.next();
            System.out.print(g + " ");
        }
        System.out.println();
    }

    @Override
    public void enqueueGridPoint(GridPoint gp) throws InterruptedException {
        gp.setQVal(++queueCount);
        gpq.add(gp);
        gp.setEnqueued(true);
        //System.out.println("Added gridpoint: " + gp);
        myGrid.redrawGrid();
        myGrid.gridDelay(2);
    }

    @Override
    public GridPoint dequeueGridPoint() {
        if (gpq.isEmpty()) {
            return null;
        } else {
            GridPoint gp = (GridPoint) gpq.first();
            //printGridPointQueue();
            if (!gpq.remove(gp)) {
                System.out.println("-----REMOVE FAILED!");
            }
            return gp;
        }
    }

    @Override
    public void clearQueue() {
        gpq.clear();
    }

    @Override
    public GridPoint getTail() {
        LinkedList<GridPoint> a = new LinkedList<GridPoint>(gpq);
        if (a.isEmpty()) {
            return null;
        }
        GridPoint gp = Collections.max(a, new GValComparator());
        maxGVal = gp.getGVal();
        return gp;
    }

    private static class GValComparator implements Comparator<GridPoint> {

        @Override
        public int compare(GridPoint a, GridPoint b) {
            return ((Integer) a.getGVal()).compareTo(b.getGVal());
        }
    }
}

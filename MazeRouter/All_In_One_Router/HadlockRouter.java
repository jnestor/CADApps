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
public class HadlockRouter extends Router {

    public HadlockRouter(Grid g) {
        super(g);
    }

    /* expand a routing search */
    public int expandGrid(GridPoint gridPoint) throws InterruptedException {
        GridPoint xp;
        if ((xp = gridPoint.westNeighbor()) != null && xp.getGVal() == UNROUTED) {
            xp.setVals(gridPoint.getFVal() + gridPoint.detourNumber(xp), gridPoint.getGVal() + 1);
            xp.setDisplayVal(gridPoint.getFVal() + gridPoint.detourNumber(xp));
            if (xp.isTarget()) {
                return xp.getGVal();
            } else {
                enqueueGridPoint(xp);
            }
        }
        if ((xp = gridPoint.eastNeighbor()) != null && xp.getGVal() == UNROUTED) {
            xp.setVals(gridPoint.getFVal() + gridPoint.detourNumber(xp), gridPoint.getGVal() + 1);
            xp.setDisplayVal(gridPoint.getFVal() + gridPoint.detourNumber(xp));
            if (xp.isTarget()) {
                return xp.getGVal();
            } else {
                enqueueGridPoint(xp);
            }
        }
        if ((xp = gridPoint.southNeighbor()) != null && xp.getGVal() == UNROUTED) {
            xp.setVals(gridPoint.getFVal() + gridPoint.detourNumber(xp), gridPoint.getGVal() + 1);
            xp.setDisplayVal(gridPoint.getFVal() + gridPoint.detourNumber(xp));
            if (xp.isTarget()) {
                return xp.getGVal();
            } else {
                enqueueGridPoint(xp);
            }
        }
        if ((xp = gridPoint.northNeighbor()) != null && xp.getGVal() == UNROUTED) {
            xp.setVals(gridPoint.getFVal() + gridPoint.detourNumber(xp), gridPoint.getGVal() + 1);
            xp.setDisplayVal(gridPoint.getFVal() + gridPoint.detourNumber(xp));
            if (xp.isTarget()) {
                return xp.getGVal();
            } else {
                enqueueGridPoint(xp);
            }
        }
        if ((xp = gridPoint.upNeighbor()) != null && xp.getGVal() == UNROUTED) {
            xp.setVals(gridPoint.getFVal() + gridPoint.detourNumber(xp), gridPoint.getGVal() + 1);
            xp.setDisplayVal(gridPoint.getFVal() + gridPoint.detourNumber(xp));
            if (xp.isTarget()) {
                return xp.getGVal();
            } else {
                enqueueGridPoint(xp);
            }
        }
        if ((xp = gridPoint.downNeighbor()) != null && xp.getGVal() == UNROUTED) {
            xp.setVals(gridPoint.getFVal() + gridPoint.detourNumber(xp), gridPoint.getGVal() + 1);
            xp.setDisplayVal(gridPoint.getFVal() + gridPoint.detourNumber(xp));
            if (xp.isTarget()) {
                return xp.getGVal();
            } else {
                enqueueGridPoint(xp);
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
        resetGridPointQueue();
        myGrid.gridDelay(3);
        if (myGrid.getSource() != null && myGrid.getTarget() != null) {
            myGrid.getSource().initExpand();
            if ((actualLength = expandGrid(myGrid.getSource())) > 0) {
                clearQueue();
                return actualLength; // found it right away!
            }
            while ((gp = dequeueGridPoint()) != null&&!stop) {
                if (myGrid.isPaused()) {
                    myGrid.setMessage("Current distance: " + getTail().getGVal()+
                        " || Current detour: "+ ((GridPoint)gpq.last()).getFVal() + " Pause");
                    synchronized (this) {
                        wait();
                    }
                }
                myGrid.setMessage("Current distance: " + getTail().getGVal()+
                        " || Current detour: "+ ((GridPoint)gpq.last()).getFVal());
                // printGridPointQueue();
                if ((actualLength = expandGrid(gp)) > 0) {
                    myGrid.setMessage("Current distance: " + actualLength);
                    myGrid.gridDelay(5);
                    clearQueue();
                    System.out.println(gp.getFVal());
                    System.out.println(gp.getDisplayVal());
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
        myGrid.gridDelay();
        gp.setQVal(++queueCount);
        gpq.add(gp);
        gp.setEnqueued(true);
        //System.out.println("Added gridpoint: " + gp);
        myGrid.redrawGrid();
        myGrid.gridDelay();
    }

    @Override
    public GridPoint dequeueGridPoint() {
        if (gpq.size() == 0) {
            return null;
        } else {
            GridPoint gp = (GridPoint) gpq.first();
            // printGridPointQueue();
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
        return Collections.max(a, new GValComparator());
    }

    private static class GValComparator implements Comparator<GridPoint> {

        @Override
        public int compare(GridPoint a, GridPoint b) {
            return ((Integer) a.getGVal()).compareTo(b.getGVal());
        }
    }
}

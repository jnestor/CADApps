
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
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
public class Router {

    private Grid myGrid;
    private boolean stop = false;

    public Router(Grid grid) {
        myGrid = grid;
    }
    private static final int UNROUTED = 9999;

    /* expand a routing search */
    public int expandGrid(GridPoint gridPoint) throws InterruptedException {
        GridPoint xp;
        if ((xp = gridPoint.westNeighbor()) != null && xp.getVal() == UNROUTED) {
            xp.setVal(gridPoint.getVal() + 1);
            if (xp.isTarget()) {
                return xp.getVal();
            } else {
                myGrid.enqueueGridPoint(xp);
            }
        }
        if ((xp = gridPoint.eastNeighbor()) != null && xp.getVal() == UNROUTED) {
            xp.setVal(gridPoint.getVal() + 1);
            if (xp.isTarget()) {
                return xp.getVal();
            } else {
                myGrid.enqueueGridPoint(xp);
            }
        }
        if ((xp = gridPoint.southNeighbor()) != null && xp.getVal() == UNROUTED) {
            xp.setVal(gridPoint.getVal() + 1);
            if (xp.isTarget()) {
                return xp.getVal();
            } else {
                myGrid.enqueueGridPoint(xp);
            }
        }
        if ((xp = gridPoint.northNeighbor()) != null && xp.getVal() == UNROUTED) {
            xp.setVal(gridPoint.getVal() + 1);
            if (xp.isTarget()) {
                return xp.getVal();
            } else {
                myGrid.enqueueGridPoint(xp);
            }
        }
        if ((xp = gridPoint.upNeighbor()) != null && xp.getVal() == UNROUTED) {
            xp.setVal(gridPoint.getVal() + 1);
            if (xp.isTarget()) {
                return xp.getVal();
            } else {
                myGrid.enqueueGridPoint(xp);
            }
        }
        if ((xp = gridPoint.downNeighbor()) != null && xp.getVal() == UNROUTED) {
            xp.setVal(gridPoint.getVal() + 1);
            if (xp.isTarget()) {
                return xp.getVal();
            } else {
                myGrid.enqueueGridPoint(xp);
            }
        }
        return -1;
    }

    public int expansion() throws InterruptedException {
        GridPoint gp;
        int actualLength;
        int curVal = 0;
        myGrid.setMessage("Expansion phase");
        myGrid.gridDelay(3);

        if (myGrid.getSRC() != null && myGrid.getTGT() != null) {
            myGrid.getSRC().initExpand();
            if ((actualLength = expandGrid(myGrid.getSRC())) > 0) {
                myGrid.clearQueue();
                return actualLength; // found it right away!
            }

            while ((gp = myGrid.dequeueGridPoint()) != null && !stop) {
                if (myGrid.isPaused()) {
                    myGrid.setMessage("Current distance: " + myGrid.getTail().getVal() + " Pause");
                    synchronized (this) {
                        wait();
                    }
                }

                myGrid.setMessage("Current distance: " + myGrid.getTail().getVal());
                if (myGrid.getMode() && (gp.getVal() > curVal)) {
                    curVal = gp.getVal();
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

    public void traceBack() throws InterruptedException {
        MazeRouterFrame.changeStopBtn(false);
        // start at target, then work back
        GridPoint current = myGrid.getTGT();
        while (!current.isSource() && !stop) {
            GridPoint next;
            int curval = current.getVal();
            if (myGrid.isPaused()) {
                myGrid.setMessage("Traceback: distance = " + curval + " Pause");
                synchronized (this) {
                    wait();
                }
            }
            myGrid.setMessage("Traceback: distance = " + curval);
            current.setRouted();
            myGrid.redrawGrid();
            myGrid.gridDelay(3);
            next = current.westNeighbor();
            if (next != null && !next.isObstacle() && next.getVal() < curval) {
                current = next;
                continue;
            }
            next = current.eastNeighbor();
            if (next != null && !next.isObstacle() && next.getVal() < curval) {
                current = next;
                continue;
            }
            next = current.southNeighbor();
            if (next != null && !next.isObstacle() && next.getVal() < curval) {
                current = next;
                continue;
            }
            next = current.northNeighbor();
            if (next != null && !next.isObstacle() && next.getVal() < curval) {
                current = next;
                continue;
            }
            next = current.upNeighbor();
            if (next != null && !next.isObstacle() && next.getVal() < curval) {
                current = next;
                continue;
            }
            next = current.downNeighbor();
            if (next != null && !next.isObstacle() && next.getVal() < curval) {
                current = next;
                continue;
            }
            System.out.println("AWK! can't trace back! current= " + current);
            break;
        }
        MazeRouterFrame.changePauseBtn(false);
        MazeRouterFrame.changeStopBtn(false);
        if (current.isSource()) {
            myGrid.setMessage("Traceback complete");
            myGrid.flash(current);
            current.setRouted();
        } else {
            System.out.println("Warning: traceBack failed!");
        }
    }

    public int route() throws InterruptedException {
        MazeRouterFrame.changeClearBtn(false);
        MazeRouterFrame.changePauseBtn(true);
        MazeRouterFrame.changeStopBtn(true);
        if (myGrid.getSRC() == null || myGrid.getTGT() == null) {
            return -1;
        }
        GridPoint.nextRouteColor();
        myGrid.reset();
        if (myGrid.getSRC() == myGrid.getTGT()) {  // trivial case
            myGrid.getSRC().setRouted();
            return 0;
        } else {
            int actualLength = expansion();
            myGrid.clearQueue();
            myGrid.redrawGrid();
            if (actualLength > 0) {
                myGrid.setMessage("Target Found!");
                myGrid.flash(myGrid.getTGT());
                traceBack();
            } else {
                myGrid.setMessage("Target not found!");
            }
            myGrid.reset();
            stop = false;
            myGrid.redrawGrid();
            return actualLength;
        }
    }

    public int route(GridPoint s, GridPoint t) throws InterruptedException {
        myGrid.setSource(s);
        myGrid.setTarget(t);
        return route();
    }

    public int route(int sx, int sy, int sz, int dx, int dy, int dz) throws InterruptedException {
        return route(myGrid.gridPointAt(sx, sy, sz), myGrid.gridPointAt(dx, dy, dz));
    }

    public void stop() {
        stop = true;
    }
}

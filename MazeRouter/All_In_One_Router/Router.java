
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
abstract class Router {

    protected Grid myGrid;
    protected boolean stop = false;

    public Router(Grid grid) {
        myGrid = grid;
    }
    protected static final int UNROUTED = 9999;
    protected static final int EXPANDING = 2;
    protected static final int TRACKBACK = 3;
    protected static final int WAITFORSRC = 0;

    /* expand a routing search */
    abstract int expandGrid(GridPoint gridPoint) throws InterruptedException;

    abstract int expansion() throws InterruptedException;

    public void traceBack() throws InterruptedException {
        myGrid.setState(TRACKBACK);
        // start at target, then work back
        GridPoint current = myGrid.getTarget();
        while (!current.isSource() && !stop) {
            GridPoint next;
            int curval = current.getGVal();
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
            if (next != null && !next.isObstacle() && next.getGVal() < curval) {
                current = next;
                continue;
            }
            next = current.eastNeighbor();
            if (next != null && !next.isObstacle() && next.getGVal() < curval) {
                current = next;
                continue;
            }
            next = current.southNeighbor();
            if (next != null && !next.isObstacle() && next.getGVal() < curval) {
                current = next;
                continue;
            }
            next = current.northNeighbor();
            if (next != null && !next.isObstacle() && next.getGVal() < curval) {
                current = next;
                continue;
            }
            next = current.upNeighbor();
            if (next != null && !next.isObstacle() && next.getGVal() < curval) {
                current = next;
                continue;
            }
            next = current.downNeighbor();
            if (next != null && !next.isObstacle() && next.getGVal() < curval) {
                current = next;
                continue;
            }
            System.out.println("AWK! can't trace back! current= " + current);
            break;
        }
        if (current.isSource()) {
            myGrid.setMessage("Traceback complete");
            myGrid.flash(current);
            current.setRouted();
        } else {
            System.out.println("Warning: traceBack failed!");
        }
        myGrid.setState(WAITFORSRC);
    }

    public int route() throws InterruptedException {
        if (myGrid.getSource() == null || myGrid.getTarget() == null) {
            return -1;
        }
        GridPoint.nextRouteColor();
        myGrid.reset();
        if (myGrid.getSource() == myGrid.getTarget()) {  // trivial case
            myGrid.getSource().setRouted();
            return 0;
        } else {
            int actualLength = expansion();
            clearQueue();
            myGrid.redrawGrid();
            if (actualLength > 0) {
                myGrid.setMessage("Target Found!");
                myGrid.flash(myGrid.getTarget());
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
    
    abstract GridPoint getTail();
    abstract void printGridPointQueue();
    abstract void enqueueGridPoint(GridPoint gp) throws InterruptedException;
    abstract GridPoint dequeueGridPoint();
    abstract void clearQueue();
}

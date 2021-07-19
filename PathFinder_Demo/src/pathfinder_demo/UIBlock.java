/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pathfinder_demo;

import java.awt.Point;
import java.util.LinkedList;

/**
 *
 * @author 15002
 */
class UIBlock {

    private UIDot dot;
    private boolean used;
    private LinkedList<UIWire> wires = new LinkedList<UIWire>(); //For Channel
    private Point wireLoc;

    public UIBlock(UIDot d) {
        dot = d;
        wireLoc = d.getLoc();
    }

    public UIDot getDot() {
        return dot;
    }

    public void setDot(UIDot d) {
        dot = d;
    }

    public void setUsed() {
        used = true;
    }

    public boolean isUsed() {
        return used;
    }

    public void clearUsage() {
        used = false;
    }

    public LinkedList<UIWire> getWires() {
        return wires;
    }

    public void addWire(UIWire wire) {
        wires.add(wire);
    }

    public Point getWireLoc() {
        return wireLoc;
    }

    public void setWireLoc(Point wireLoc) {
        this.wireLoc = wireLoc;
    }
    @Override
    public boolean equals(Object bt){
        UIBlock b = (UIBlock) bt;
        return dot.equals(b.getDot());
    }

}

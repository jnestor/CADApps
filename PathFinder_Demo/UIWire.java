/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pathfinder_demo;

import java.awt.BasicStroke;
import java.util.concurrent.CopyOnWriteArrayList;
import java.awt.Color;
import java.awt.Point;
import java.util.LinkedList;

/**
 *
 * @author 15002
 */
public class UIWire {

    private UIBlock blockA;
    private UIBlock blockB;
    private UIDot terminalA;
    private UIDot terminalB;
    private Color color = new Color(0, 0, 0, 0); //Set to non transparent for non SWB wires
    private Color defColor = new Color(0, 0, 0);
    private Point locA, locB;
    private Point pA;
    private Point pB;
    private BasicStroke stroke = new BasicStroke(3);
    private PFNode availableSink;
    private LinkedList<PFNet> targetNets = new LinkedList<PFNet>();
    private UIBlock swBlock;
    //private boolean occupied;
    private boolean swOn = false;

    public UIWire(UIBlock a, UIBlock b) {
        blockA = a;
        blockB = b;
        terminalA = blockA.getDot();
        terminalB = blockB.getDot();
        pA = terminalA.getLoc();
        pB = terminalB.getLoc();
        if (pA.getX() != pB.getX()) {
            if (pA.getX() < pB.getX()) {
                locA = new Point((int) pA.getX() - terminalA.getHeight() / 2+ (int) (stroke.getLineWidth() / 2), (int) pA.getY());
                locB = new Point((int) pB.getX() + terminalB.getHeight() / 2- (int) (stroke.getLineWidth() / 2), (int) pB.getY());
            } else if (pA.getX() > pB.getX()) {
                locA = new Point((int) pB.getX() - terminalB.getHeight() / 2+ (int) (stroke.getLineWidth() / 2), (int) pB.getY());
                locB = new Point((int) pA.getX() + terminalA.getHeight() / 2- (int) (stroke.getLineWidth() / 2), (int) pA.getY());
            }
        } else if (pA.getY() != pB.getY()) {
            if (pA.getY() > pB.getY()) {
                locA = new Point((int) pA.getX(), (int) pA.getY() + terminalA.getHeight() / 2 - (int) (stroke.getLineWidth() / 2));
                locB = new Point((int) pB.getX(), (int) pB.getY() - terminalB.getHeight() / 2 + (int) (stroke.getLineWidth() / 2));
            } else if (pA.getY() < pB.getY()) {
                locB = new Point((int) pB.getX(), (int) pB.getY() + terminalB.getHeight() / 2 - (int) (stroke.getLineWidth() / 2));
                locA = new Point((int) pA.getX(), (int) pA.getY() - terminalA.getHeight() / 2 + (int) (stroke.getLineWidth() / 2));
            }
        }
    }

    public UIWire(UIBlock a, UIBlock b, Color c, int i) {
        this(a, b);
        stroke = new BasicStroke(i);
        color = c;
        defColor=c;
    }

    public UIWire(UIBlock a, UIBlock b, int i, Point pA, Point pB, Color c) {
        this(a, b,c,i);
        locA = pA;
        locB = pB;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color c) {
        color = c;
    }

    //Get the locations of terminals
    public Point getLocA() {
        return locA;
    }

    public Point getLocB() {
        return locB;
    }

    //Get the x/y coordinate of the two UIDots owned by the wire
    public int getAX() {
        return (int) pA.getX();
    }

    public int getAY() {
        return (int) pA.getY();
    }

    public int getBX() {
        return (int) pB.getX();
    }

    public int getBY() {
        return (int) pB.getY();
    }

    public UIDot getTerminalA() {
        return terminalA;
    }

    public UIDot getTerminalB() {
        return terminalB;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UIWire) {
            UIWire w = (UIWire) obj;
            if (locA.equals(w.getLocA()) && locB.equals(w.getLocB())) {
                return true;
            }
        }
        return false;
    }

    public BasicStroke getStroke() {
        return stroke;
    }

    public void setBlockA(UIBlock blockA) {
        this.blockA = blockA;
    }

    public void setBlockB(UIBlock blockB) {
        this.blockB = blockB;
    }

    public UIBlock getBlockA() {
        return blockA;
    }

    public UIBlock getBlockB() {
        return blockB;
    }

    public PFNode getAvailableSink() {
        return availableSink;
    }

    public LinkedList<PFNet> getTargetNets() {
        return targetNets;
    }

    public void setAvailableSink(PFNode availableSink) {
        this.availableSink = availableSink;
        terminalA.setAvailableSink(availableSink);
        terminalB.setAvailableSink(availableSink);
        if(swBlock!=null){
            swBlock.getDot().setAvailableSink(availableSink);
        }
    }

    public void addTargetNet(PFNet targetNet) {
        targetNets.add(targetNet);
        terminalA.addTargetNet(targetNet);
        terminalB.addTargetNet(targetNet);
        if(swBlock!=null){
            swBlock.getDot().addTargetNet(targetNet);
        }
    }

    public void clearTargets() {
        targetNets.clear();
        terminalA.clearTargetNets();
        terminalB.clearTargetNets();
        if(swBlock!=null){
            swBlock.getDot().clearTargetNets();
        }
        //swOn=false;
    }

    public Color getDefColor() {
        return defColor;
    }

    public void setDefColor(Color defColor) {
        this.defColor = defColor;
    }

    public void resetColor(){
        color=defColor;
        if(!terminalA.getType().equals("SW"))
        blockA.getDot().resetColor();
        if(!terminalB.getType().equals("SW"))
        blockB.getDot().resetColor();
        if(swBlock!=null) swBlock.getDot().resetColor();
        swOn=false;
    }

//    public boolean isOccupied() {
//        return occupied;
//    }

//    public void setOccupied(boolean occupied) {
//        this.occupied = occupied;
//        terminalA.setOccupied(occupied);
//        terminalB.setOccupied(occupied);
//    }

    public UIBlock getSwBlock() {
        return swBlock;
    }

    public void setSwBlock(UIBlock swBlock) {
        this.swBlock = swBlock;
    }

    public boolean isSwOn() {
        return swOn;
    }

    public void setSwOn(boolean swOn) {
        this.swOn = swOn;
    }
    
    
}

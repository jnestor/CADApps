package pathfinder_demo;


import java.awt.Color;
import java.util.LinkedList;
import java.util.PriorityQueue;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author 15002
 */
public class PFNet {
    private LinkedList<PFNode> sinks;
    private PFNode source;
    private LinkedList<PFNode> path;
    private LinkedList<UIWire> wires;
    private Color color;
    
    public PFNet(LinkedList<PFNode> sinks, PFNode source) {
        this.sinks = sinks;
        this.source = source;
        path = new LinkedList<PFNode>();
        wires=new LinkedList<UIWire>();
    }

    public LinkedList<PFNode> getSinks() {
        return sinks;
    }

    public PFNode getSource() {
        return source;
    }
    
    /**
     * add a node to the tail of the LinkedList
     * @param n  the node to add
     */
    public void addNode(PFNode n){
        path.addLast(n);
    }
    
    public void clearPath(){
        path.clear();
        wires.clear();
    }

    public LinkedList<PFNode> getPath() {
        return path;
    }
    /**
     * add a UIwire to the tail of LinkedList
     * @param w UIWire to add
     */
    public void addWire(UIWire w){
        wires.addLast(w);
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public LinkedList<UIWire> getWires() {
        return wires;
    }
    
    
    
}

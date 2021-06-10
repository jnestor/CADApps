/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pathfinder_demo;

import java.util.LinkedList;

/**
 *
 * @author 15002
 */
public class PFEdge {
    private PFNode start;
    private PFNode end;
    private int cost;
    private LinkedList <UIWire> wires;
    public PFEdge(PFNode s, PFNode e){
        start = s;
        end = e;
        wires = new LinkedList<UIWire>();
    }
    
    /**
     * Get the target of this one direction node
     * @return the toNode
     */
    public PFNode getEnd(){
        return end;
    }
    
    public PFNode getStart(){
        return start;
    }
    
        @Override 
    public boolean equals(Object obj){
        if(obj instanceof PFEdge){
            PFEdge edge = (PFEdge)obj;
            if(start.equals(edge.start)&&
                    end.equals(edge.end))
                return true;
        }
        return false;
    }
    
    public void addWire(UIWire w){
        wires.add(w);
    }
    
    public LinkedList<UIWire> getWires(){
        return wires;
    }
    
    public int getCost(){
        return cost;
    }
    
    public void setCost(int c){
        cost = c;
    }

}

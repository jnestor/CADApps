/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pathfinder_demo;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 * @author 15002
 */
public class Channel implements NodeContainer{
    private CopyOnWriteArrayList<UIWire> wires = new CopyOnWriteArrayList<UIWire>();
    private PFNode node;
    private CopyOnWriteArrayList<PFEdge> edges = new CopyOnWriteArrayList<PFEdge>();
    
    
    public void setNode(PFNode n){
        node=n;
    }
    
    public void addEdge(PFEdge e){
        edges.add(e);
    }
    
    public void addWire(UIWire w){
        wires.add(w);
    }
    
    public PFNode getNode(){
        return node;
    }
}

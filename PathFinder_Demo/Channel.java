/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pathfinder_demo;
import java.util.LinkedList;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 * @author 15002
 */
public class Channel implements NodeContainer{
    private CopyOnWriteArrayList<UIWire> wires = new CopyOnWriteArrayList<UIWire>();
    private PFNode node;
    
    
    public void setNode(PFNode n){
        node=n;
    }
    
    
    public void addWire(UIWire w){
        wires.add(w);
    }
    
    public PFNode getNode(){
        return node;
    }
    
    public LinkedList<PFEdge> getEdges(){
        return node.getEdges();
    }
    
    @Override 
    public boolean equals(Object obj){
        if(obj instanceof Channel){
            Channel c = (Channel) obj;
            return node.equals(c.getNode());
        }
        return false;
    }
    
    public CopyOnWriteArrayList<UIWire> getWires(){
        return wires;
    }
}

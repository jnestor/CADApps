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
public class PFNode {
    private static int currID = 0;
    private int ID;
    private int capacity;
    private int occupied;
    private LinkedList<PFEdge> edges = new LinkedList<PFEdge>();
    public boolean inCapacity(){
        return occupied < capacity;
    }
    public PFNode(int c){
        capacity = c;
        ID = currID;
        currID++;
    }
    
    public void addEdge(PFEdge edge){
        edges.add(edge);
    }
    public LinkedList<PFEdge> getEdges(){
        return edges;
    }
    public void itirate(){
        occupied++;
    }
    public void clearStats(){
        occupied = 0;
    }
    
        @Override 
    public boolean equals(Object obj){
        if(obj instanceof PFNode){
            if (ID==((PFNode)obj).ID) return true;
        }
        return false;
    }
    
    public int getID(){
        return ID;
    }
//    public void clearEdges(){
//        edges.clear();
//    }
}

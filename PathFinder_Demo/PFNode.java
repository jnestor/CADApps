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
    private boolean visited = false;
    private int capacity;
    private int occupied;
    private LinkedList<PFEdge> edges = new LinkedList<PFEdge>();
    public boolean inCapacity(){
        return occupied < capacity;
    }
    public void addEdge(PFEdge edge){
        edges.add(edge);
        occupied ++;
    }
    public LinkedList<PFEdge> getEdges(){
        return edges;
    }
    public void itirate(){
        visited=true;
    }
    public boolean isUsed(){
        return visited;
    }
    public void clearStats(){
        visited = false;
    }
    public void clearEdges(){
        edges.clear();
    }
}

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
public class PFGraph {
    private CopyOnWriteArrayList<PFNode> nodes = new CopyOnWriteArrayList<PFNode>();
    private CopyOnWriteArrayList<PFEdge> edges = new CopyOnWriteArrayList<PFEdge>();
    
    public void addNode(PFNode n){
        nodes.add(n);
    }
    
    public void addEdge(PFEdge e){
        edges.add(e);
    }
}

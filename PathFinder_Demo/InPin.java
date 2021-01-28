/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pathfinder_demo;

/**
 *
 * @author 15002
 */
public class InPin extends UIBlock implements NodeContainer{
//    PFEdge fromInEdge;
    PFEdge edge;
    Switch swA;
    Switch swB;
    PFNode node;
    
    public InPin (UIDot d,PFNode n){
        super(d);
        node = n;
    }
    
//    public void setFromInEdge(PFEdge e){
//        fromInEdge = e;
//    }
//    
//    public void setToInEdge(PFEdge e){
//        toInEdge = e;
//    }
    
    public void setNode(PFNode n){
        node = n;
    }
    
    public PFNode getNode(){
        return node;
    }
    
    public PFEdge getEdge(){
        return node.getEdges().get(0);
    }
}

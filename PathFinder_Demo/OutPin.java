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
public class OutPin extends UIDotContainer{
//    PFEdge fromOutEdge;
//    PFEdge toOutEdge;
    Switch swA;
    Switch swB;
    PFNode node;
    
    public OutPin (UIDot d, PFNode n){
        super(d);
        node  = n;
    }
//    public void setFromOutEdge(PFEdge e){
//        fromOutEdge = e;
//    }
//    
//    public void setToOutEdge(PFEdge e){
//        toOutEdge = e;
//    }
    
    public void setNode(PFNode n){
        node = n;
    }
    
    public PFNode getNode(){
        return node;
    }
}

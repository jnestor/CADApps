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
public class PFEdge {
    private PFNode start;
    private PFNode end;
    private int cost;
    
    public PFEdge(PFNode s, PFNode e){
        start = s;
        end = e;
    }
    
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
}

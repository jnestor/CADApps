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
public class Switch extends UIDotContainer{
    PFEdge edge;
    public Switch(UIDot n){
        super(n);
    }
    
    //the edge links to the pin and wire segment
    public void setEdge(PFEdge e){
        edge = e;
    }

}

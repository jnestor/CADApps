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
abstract class UIDotContainer{
    private UIDot dot;
    
    public UIDotContainer(UIDot d){
        dot = d;
    }
    
    public UIDot getDot(){
        return dot;
    }
    
    public void setDot(UIDot d){
        dot=d;
    }
}

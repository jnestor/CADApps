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
abstract class UIBlock{
    private UIDot dot;
    private boolean used;
    private PFNode target;
    private PFNode source;


    
    public UIBlock(UIDot d){
        dot = d;
    }
    
    public UIDot getDot(){
        return dot;
    }
    
    public void setDot(UIDot d){
        dot=d;
    }
    
    public void setUsed(){
        used=true;
    }
    
    public boolean isUsed(){
        return used;
    }
    
    public void clearUsage(){
        used=false;
    }
    
    public PFNode getTarget(){
        return target;
    }
    
    public void setTarget(PFNode target) {
        this.target = target;
    }

    public void setSource(PFNode source) {
        this.source = source;
    }

    public PFNode getSource() {
        return source;
    }
    
}

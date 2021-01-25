/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pathfinder_demo;

import java.util.concurrent.CopyOnWriteArrayList;
import java.awt.Color;
import java.awt.Point;

/**
 *
 * @author 15002
 */
public class UIWire {
    private UIDot terminalA;
    private UIDot terminalB;
    private CopyOnWriteArrayList<UIDot> switches = new CopyOnWriteArrayList<UIDot>();
    private CopyOnWriteArrayList<PFNode> wireSegs = new CopyOnWriteArrayList<PFNode>();
    private int length;
    private Color color = Color.black;
    private Point locA, locB;
    
    public UIWire(UIDot a, UIDot b, int l){
        terminalA = a;
        terminalB = b;
        length = l;
        Point pA = terminalA.getLoc();
        Point pB = terminalB.getLoc();
        if(pA.getX()!=pB.getX()){
            if(pA.getX()<pB.getX()){
                locA=new Point((int)pA.getX()-terminalA.getSize()/2,(int)pA.getY());
                locB=new Point((int)pB.getX()+terminalB.getSize()/2,(int)pB.getY());
            }
            else if(pA.getX()>pB.getX()){
                locA=new Point((int)pB.getX()-terminalB.getSize()/2,(int)pB.getY());
                locB=new Point((int)pA.getX()+terminalA.getSize()/2,(int)pA.getY());
            }
        }
        else if(pA.getY()!=pB.getY()){
            if(pA.getY()>pB.getY()){
                locA=new Point((int)pA.getX(),(int)pA.getY()+terminalA.getSize()/2);
                locB=new Point((int)pB.getX(),(int)pB.getY()-terminalB.getSize()/2);
            }
            else if(pA.getY()<pB.getY()){
                locA=new Point((int)pB.getX(),(int)pB.getY()+terminalB.getSize()/2);
                locB=new Point((int)pA.getX(),(int)pA.getY()-terminalA.getSize()/2);
            }
        }
    }
    
    public Color getColor(){
        return color;
    }
    
    public void setColor(Color c){
        color = c;
    }
    
    
}

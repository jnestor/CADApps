/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pathfinder_demo;

import java.awt.BasicStroke;
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
    private Color color = Color.black;
    private Point locA, locB;
    private Channel channel;
    private Point pA;
    private Point pB;
    private BasicStroke stroke = new BasicStroke(5);
    public UIWire(UIDot a, UIDot b){
        terminalA = a;
        terminalB = b;
        pA = terminalA.getLoc();
        pB = terminalB.getLoc();
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
                locA=new Point((int)pA.getX(),(int)pA.getY()+terminalA.getSize()/2-(int)(stroke.getLineWidth()/2));
                locB=new Point((int)pB.getX(),(int)pB.getY()-terminalB.getSize()/2+(int)(stroke.getLineWidth()/2));
            }
            else if(pA.getY()<pB.getY()){
                locB=new Point((int)pB.getX(),(int)pB.getY()+terminalB.getSize()/2-(int)(stroke.getLineWidth()/2));
                locA=new Point((int)pA.getX(),(int)pA.getY()-terminalA.getSize()/2+(int)(stroke.getLineWidth()/2));
            }
        }
    }
    
    public UIWire(UIDot a,UIDot b, int i,Point pA, Point pB){
        this(a,b);
        stroke = new BasicStroke(i);
        locA = pA;
        locB = pB;
    }
    
    public void setChan(Channel c){
        channel = c;
    }
    
    public Color getColor(){
        return color;
    }
    
    public void setColor(Color c){
        color = c;
    }
    
    //Get the locations of terminals
    public Point getLocA(){
        return locA;
    }
    
    public Point getLocB(){
        return locB;
    }
    
    //Get the x/y coordinate of the two UIDots owned by the wire
    public int getAX(){
        return (int)pA.getX();
    }
    
    public int getAY(){
        return (int)pA.getY();
    }
    
    public int getBX(){
        return (int)pB.getX();
    }
    
    public int getBY(){
        return (int)pB.getY();
    }
    
    public UIDot getTermianlA(){
        return terminalA;
    }
    
    public UIDot getTermianlB(){
        return terminalB;
    }
    
    @Override
    public boolean equals(Object obj){
        if(obj instanceof UIWire){
            UIWire w = (UIWire) obj;
            if(locA.equals(w.getLocA())&&locB.equals(w.getLocB()))
                return true;
        }
        return false;
    }
    
    public BasicStroke getStroke(){
        return stroke;
    }
}

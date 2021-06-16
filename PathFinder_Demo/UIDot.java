/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pathfinder_demo;
import java.util.*;
import java.awt.Point;
import java.awt.Color;
/**
 *
 * @author 15002
 */
public class UIDot {
    public static final String SW = "SW";
    public static final String LB = "LB";
    public static final String OP = "OP";
    public static final String IP = "IP";
    public static final String TM = "TM";
    public static final String SB = "SB";
    public static final String CH = "CH";
    private final Point loc;
    private final String kind;
    private final int height;
    private Color color;
    private Color defColor;
    private boolean clicked;
    private boolean isVertical;
    //private LinkedList<UIEdge> wires = new LinkedList<UIEdge>();
    private PFNode availableSink;
    private LinkedList<PFNet> targetNets = new LinkedList<PFNet>();
    private int width;
    private Color defEdgeColor;
    private Color edgeColor = new Color(0,0,0);//for Channels
    //private boolean occupied = false;
    public UIDot(Point l,String k,int s, boolean iV){
        loc = l;
        kind = k;
        isVertical = iV;
        if(k.equals(CH)){
            width = (180)*s/90;
            if(isVertical){
                height=width;
                width=s;
            }
            else height = s;
        }  
        else{ 
            height = s;
            width = height;
        }
        if(k.equals(OP))color=Color.black;
        else if(k.equals(IP)){
            color=Color.white;
            edgeColor = new Color(0,0,0,0);
        }
        else if(k.equals(SW))color=Color.white;
        else if(k.equals(LB)){
            color=new Color(179, 179, 179);
            edgeColor = new Color(0,0,0,0);
        }
        else if(k.equals(SB))color=new Color(0, 0, 0,0);
        else if(k.equals(TM)){
            color = new Color((float)0.8,(float)0.8,(float)0.8,(float)0.8);
            edgeColor = new Color(0,0,0,0);
        }
        else if(k.equals(CH)){
            color = new Color(0,0,0,0);
            edgeColor = new Color(0,0,0,0);
        }
        defColor=color;
        defEdgeColor = edgeColor;
    }

    public boolean dotFound(Point searchLoc) {
        int window = height;
        int lx = searchLoc.x - window / 2;
        int ly = searchLoc.y - window / 2;
        int ux = searchLoc.x + window / 2;
        int uy = searchLoc.y + window / 2;
        return (loc.x > lx && loc.x < ux
                && loc.y > ly && loc.y < uy);
    }
    
    public Point getLoc(){
        return loc;
    }
    
    public int getHeight(){
        return height;
    }
    
    public void setColor(Color c){
        color = c;
    }
    
    public Color getColor(){
        return color;
    }
    
    public String getType(){
        return kind;
    }
    
    public void setClicked(){
        clicked=true;
    }
    
    public boolean isClicked(){
        return clicked;
    }
    
    @Override
    public boolean equals(Object obj){
        if(obj instanceof UIDot){
            UIDot d = (UIDot) obj;
            if(loc.equals(d.getLoc())){
                return true;
            }
        }
        return false;
    }
    
//    public void addWire(UIEdge e){
//        wires.add(e);
//    }

    public PFNode getAvailableSink() {
        return availableSink;
    }
    
    public void setAvailableSink(PFNode availableSink) {
        this.availableSink = availableSink;
    }
    
    public LinkedList<PFNet> getTargetNets() {
        return targetNets;
    }
    
    public void clearTargetNets(){
        targetNets.clear();
    }
    
    public void addTargetNet(PFNet targetNet) {
        targetNets.add(targetNet);
    }
    
    public void resetColor(){
        color=defColor;
        edgeColor = defEdgeColor;
    }

    public boolean isIsVertical() {
        return isVertical;
    }

    public int getWidth() {
        return width;
    }

    public Color getEdgeColor() {
        return edgeColor;
    }

    public void setEdgeColor(Color edgeColor) {
        this.edgeColor = edgeColor;
    }

//    public boolean isOccupied() {
//        return occupied;
//    }
//
//    public void setOccupied(boolean occupied) {
//        this.occupied = occupied;
//    }
    
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pathfinder_demo;

import java.awt.Point;
import java.awt.Color;
/**
 *
 * @author 15002
 */
public class UIDot {
    public final String SW = "SW";
    public final String LB = "LB";
    public final String OP = "OP";
    public final String IP = "IP";
    public final String TM = "TM";
    public final String SB = "SB";
    private final Point loc;
    private final String kind;
    private final int size;
    private Color color;
    private boolean clicked;
    public UIDot(Point l,String k,int s){
        loc = l;
        kind = k;
        size = s;
        if(k.equals(OP))color=Color.black;
        else if(k.equals(IP))color=Color.white;
        else if(k.equals(SW))color=new Color(77,77,77);
        else if(k.equals(LB))color=new Color(179, 179, 179);
        else if(k.equals(SB))color=new Color(255, 0, 0);
        else if(k.equals(TM))color = new Color((float)0.8,(float)0.8,(float)0.8,(float)0.8);
    }

    public boolean dotFound(Point searchLoc) {
        int window = size;
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
    
    public int getSize(){
        return size;
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
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pathfinder_demo;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 * @author 15002
 */
public class UIGraph {

    public final String SW = "SW";
    public final String LB = "LB";
    public final String OP = "OP";
    public final String IP = "IP";
    
    private CopyOnWriteArrayList<UIDot> nodes = new CopyOnWriteArrayList<UIDot>();
    private CopyOnWriteArrayList<UIWire> wires = new CopyOnWriteArrayList<UIWire>();
    private void drawNode(Graphics g, UIDot d, boolean isSelected) {
        Color fillColor;
        Point loc = d.getLoc();
        int TERM_SIZE = d.getSize();
        int orig_x = loc.x - TERM_SIZE / 2;
        int orig_y = loc.y - TERM_SIZE / 2;
        g.setColor(d.getColor());
        if (d.getType().equals(SW)) {
            g.fillOval(orig_x, orig_y, TERM_SIZE, TERM_SIZE);
            g.setColor(Color.black);
            g.drawOval(orig_x, orig_y, TERM_SIZE, TERM_SIZE);
        } else if (d.getType().equals(IP)) {
            g.fillRect(orig_x, orig_y, TERM_SIZE, TERM_SIZE);
            g.setColor(Color.black);
            g.drawRect(orig_x, orig_y, TERM_SIZE, TERM_SIZE);
            g.drawString("in", orig_x + TERM_SIZE + 4/15*TERM_SIZE, orig_y + TERM_SIZE - 2/15*TERM_SIZE);
        } else if (d.getType().equals(IP)) {
            g.fillRect(orig_x, orig_y, TERM_SIZE, TERM_SIZE);
            g.setColor(Color.black);
            g.drawRect(orig_x, orig_y, TERM_SIZE, TERM_SIZE);
            g.drawString("out", orig_x + TERM_SIZE - 8/15*TERM_SIZE, orig_y + TERM_SIZE + 9/15*TERM_SIZE );
        }
    }
    private UIDot findNode(Point searchLoc){
        for(UIDot dot:nodes){
            if(dot.dotFound(searchLoc)) return dot;
        }
        return null;
    }
}

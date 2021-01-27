/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pathfinder_demo;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Point;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.JPanel;
import javax.swing.*;

/**
 *
 * @author 15002
 */
public class UIGraph extends JPanel {

    public static final String SW = "SW";
    public static final String SB = "SB";
    public static final String LB = "LB";
    public static final String OP = "OP";
    public static final String IP = "IP";
    public static final String TM = "TM";

    private CopyOnWriteArrayList<UIDotContainer> nodes = new CopyOnWriteArrayList<UIDotContainer>();
    private CopyOnWriteArrayList<UIWire> wires = new CopyOnWriteArrayList<UIWire>();



    public UIGraph() {
        
    }

    public void addNode(UIDotContainer d){
        nodes.add(d);
    }
    
    public void addWire(UIWire w){
        wires.add(w);
    }
    
    private void drawNode(Graphics g, UIDot d) {
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
            g.drawString("in", orig_x + TERM_SIZE + (int) ((float) 4 / 15 * TERM_SIZE), orig_y + TERM_SIZE - (int) ((float) 2 / 15 * TERM_SIZE));
        } else if (d.getType().equals(OP)) {
            g.fillRect(orig_x, orig_y, TERM_SIZE, TERM_SIZE);
            g.setColor(Color.black);
            g.drawRect(orig_x, orig_y, TERM_SIZE, TERM_SIZE);
            g.drawString("out", orig_x + TERM_SIZE - (int) ((float) 8 / 15 * TERM_SIZE), orig_y + TERM_SIZE + (int) ((float) 9 / 15 * TERM_SIZE));
        } else {
            g.fillRect(orig_x, orig_y, TERM_SIZE, TERM_SIZE);
        }
    }

    private void drawWire(Graphics g2, UIWire w) {
        Graphics2D g = (Graphics2D) g2;
        g.setColor(w.getColor());
        g.setStroke(new BasicStroke(7));
        int aX = (int) w.getLocA().getX();
        int aY = (int) w.getLocA().getY();
        int bX = (int) w.getLocB().getX();
        int bY = (int) w.getLocB().getY();
        //	System.out.println("drawing edge from " + loc1 + " to " + loc2);
        g.drawLine(aX, aY, bX, bY);
        g.setStroke(new BasicStroke(1));
//        // label it
//        int dx, dy, midx, midy, offx, offy;
//        dx = loc2.x - loc1.x;
//        dy = loc2.y - loc1.y;
//        midx = loc1.x + (dx / 2);
//        midy = loc1.y + (dy / 2);
//        offx = 5;
//        offy = 5;
//        if (dx != 0) {  // hack to space edges - could probably do better
//            double slope = (double) (dy) / (double) (dx);
//            if (slope >= 0 && slope < 1) {
//                offy = -5;
//            } else if (slope < 0 && slope > -1) {
//                offy = 10;
//            }
//        }
//        g.drawString("e" + e.getID(), midx + offx, midy + offy);
//        g.setColor(Color.black);
    }

    private UIDot findNode(Point searchLoc) {
        for (UIDotContainer comp : nodes) {
            UIDot dot=comp.getDot();
            if (dot.dotFound(searchLoc)) {
                return dot;
            }
        }
        return null;
    }

    protected void drawNodes(Graphics g) {
        if (!nodes.isEmpty()) {
            for (UIDotContainer comp : nodes) {
            UIDot dot=comp.getDot();
                drawNode(g, dot);
            }
        }
    }

    protected void drawWires(Graphics g) {
        if (!wires.isEmpty()) {
            for (UIWire wire : wires) {
                drawWire((Graphics2D) g, wire);
            }
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawWires(g);
        drawNodes(g);
    }
}

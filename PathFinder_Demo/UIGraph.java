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

    public final String SW = "SW";
    public final String SB = "SB";
    public final String LB = "LB";
    public final String OP = "OP";
    public final String IP = "IP";
    public final String TM = "TM";

    private CopyOnWriteArrayList<UIDot> nodes = new CopyOnWriteArrayList<UIDot>();
    private CopyOnWriteArrayList<UIWire> wires = new CopyOnWriteArrayList<UIWire>();

    public static void main(String[] args) {
        UIGraph u = new UIGraph();
        JFrame f = new JFrame();
        f.add(u);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(1000, 1000);
        f.setVisible(true);
    }

    public UIGraph() {
        for (int i = 0; i < 3; i++) {
            int sbY = 45 + 270 * i;
            for (int j = 0; j < 3; j++) {
                int sbX = 45 + 270 * j;
                nodes.add(new UIDot(new Point(sbX, sbY), SB, 90));
            }
        }
        for (int i = 0; i < 2; i++) {
            int lbY = 180 + 270 * i;
            for (int j = 0; j < 2; j++) {
                int lbX = 180 + 270 * j;
                nodes.add(new UIDot(new Point(lbX, lbY), LB, 90));
            }
        }
        
        for (int j = 0; j < 3; j++) {
            int baseY = 270 * j;
            for (int i = 0; i < 2; i++) {
                int opX = 90 + 15 / 2 + i * 270;
                for (int k = 0; k < 4; k++) {
                    int opY = 15 / 2 + k * (15 + 10) + baseY;
                    UIDot a = new UIDot(new Point(opX, opY), TM, 15);
                    UIDot b = new UIDot(new Point(opX + 180 - 15, opY), TM, 15);
                    nodes.add(a);
                    nodes.add(b);
                    wires.add(new UIWire(a, b));
                }
            }
        }
        for (int j = 0; j < 3; j++) {
            int baseX = 270 * j;
            for (int i = 0; i < 2; i++) {
                int opY = 90 + 15 / 2 + i * 270;
                for (int k = 0; k < 4; k++) {
                    int opX = 15 / 2 + k * (15 + 10) + baseX;
                    UIDot a = new UIDot(new Point(opX, opY), TM, 15);
                    UIDot b = new UIDot(new Point(opX, opY + 180 - 15), TM, 15);
                    nodes.add(a);
                    nodes.add(b);
                    wires.add(new UIWire(a, b));
                }
            }
        }
        for(int i =0;i<2;i++){
            int opX = 270*i+45+90+15/2;
            for(int j =0;j<2;j++){
            int opY = 270*j+90+30+15/2;
            UIDot a = new UIDot(new Point(opX,opY),IP,15);
            UIDot b = new UIDot(new Point(opX+90-15,opY+90+15),IP,15);
            UIDot saA = new UIDot(new Point(opX,opY-45),SW,15);
            UIDot saB = new UIDot(new Point(opX,opY-45-50),SW,15);
            UIDot sbA = new UIDot(new Point(opX+90-15,opY+90+15+45),SW,15);
            UIDot sbB = new UIDot(new Point(opX+90-15,opY+90+15+45+50),SW,15);
            nodes.add(a);
            nodes.add(b);
            nodes.add(saA);
            nodes.add(saB);
            nodes.add(sbA);
            nodes.add(sbB);
            wires.add(new UIWire(saA,a));
            wires.add(new UIWire(saA,saB));
            wires.add(new UIWire(sbA,b));
            wires.add(new UIWire(sbA,sbB));
            }
        }
        
        for(int i =0;i<2;i++){
            int opY = 270*i+45+90+15/2;
            for(int j =0;j<2;j++){
            int opX = 270*j+90+30+15/2;
            UIDot a = new UIDot(new Point(opX,opY+90-15),OP,15);
            UIDot b = new UIDot(new Point(opX+90+15,opY),OP,15);
            UIDot saA = new UIDot(new Point(opX-45,opY+90-15),SW,15);
            UIDot saB = new UIDot(new Point(opX-45-50,opY+90-15),SW,15);
            UIDot sbA = new UIDot(new Point(opX+90+15+45,opY),SW,15);
            UIDot sbB = new UIDot(new Point(opX+90+15+45+50,opY),SW,15);
            nodes.add(a);
            nodes.add(b);
            nodes.add(saA);
            nodes.add(saB);
            nodes.add(sbA);
            nodes.add(sbB);
            wires.add(new UIWire(saA,a));
            wires.add(new UIWire(saA,saB));
            wires.add(new UIWire(sbA,b));
            wires.add(new UIWire(sbA,sbB));
            }
        }

        System.out.println(nodes.isEmpty());
        System.out.println(wires.isEmpty());
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
        int aX=(int)w.getLocA().getX();
        int aY=(int)w.getLocA().getY();
        int bX=(int)w.getLocB().getX();
        int bY=(int)w.getLocB().getY();
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
        for (UIDot dot : nodes) {
            if (dot.dotFound(searchLoc)) {
                return dot;
            }
        }
        return null;
    }

    protected void drawNodes(Graphics g) {
        if (!nodes.isEmpty()) {
            for (UIDot dot : nodes) {
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

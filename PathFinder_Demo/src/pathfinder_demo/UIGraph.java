/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pathfinder_demo;

//import currentlyNoUse.Terminal;
//import currentlyNoUse.LogicBlock;
//import currentlyNoUse.OutPin;
//import currentlyNoUse.InPin;
//import currentlyNoUse.Channel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.JPanel;
import java.awt.event.MouseListener;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.Locale;
import javax.swing.JOptionPane;

/**
 *
 * @author 15002
 */
public class UIGraph extends JPanel implements MouseListener {

    private static final int DONE = 0;
    private static final int EXPANDING = 1;

    public static final String SW = "SW";
    public static final String SB = "SB";
    public static final String LB = "LB";
    public static final String OP = "OP";
    public static final String IP = "IP";
    public static final String TM = "TM";
    public static final String CH = "CH";
    private int chanStroke = 2;

    private int state = DONE;

    private CopyOnWriteArrayList<UIBlock> blocks = new CopyOnWriteArrayList<UIBlock>();
    private CopyOnWriteArrayList<UIBlock> top = new CopyOnWriteArrayList<UIBlock>();
    private CopyOnWriteArrayList<UIBlock> bottom = new CopyOnWriteArrayList<UIBlock>();
    private CopyOnWriteArrayList<UIWire> wires = new CopyOnWriteArrayList<UIWire>();
    private CopyOnWriteArrayList<UIWire> swWires = new CopyOnWriteArrayList<UIWire>();
    private CopyOnWriteArrayList<UIBlock> chanBlocks = new CopyOnWriteArrayList<UIBlock>();
    private CopyOnWriteArrayList<PFNode> sources = new CopyOnWriteArrayList<PFNode>();
    private CopyOnWriteArrayList<PFNode> sinks = new CopyOnWriteArrayList<PFNode>();
    private DecimalFormat numberFormat = new DecimalFormat("#.00");

    private boolean hSw;
    private boolean pSw;
    private int hmPXC;
    private int hmPYC;
    private int hmPW;
    private int hmPH;

    private int hmHXC;
    private int hmHYC;

    private double maxPenalty = 1;
    private double maxHVal = 4;
    private int iteration = 1;
    private GradientPaint gradientP;
    private GradientPaint gradientH;

    

    public UIGraph() {
        addMouseListener(this);
    }

    public void addNode(UIBlock d) {
        blocks.add(d);
    }

    public void addToTop(UIBlock d) {
        top.add(d);
        addNode(d);
    }

    public void addToBottom(UIBlock d) {
        bottom.add(d);
        addNode(d);
    }

    public void addWire(UIWire w) {
        wires.add(w);
    }

    public void addSwWire(UIWire w) {
        swWires.add(w);
    }

    public void addChan(UIBlock b) {
        chanBlocks.add(b);
    }

    private void drawNode(Graphics g, UIDot d, boolean edge) {
        Point loc = d.getLoc();
        int height = d.getHeight();
        int width = d.getWidth();
        int orig_x = loc.x - width / 2;
        int orig_y = loc.y - height / 2;
        g.setColor(d.getColor());
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(1));
        if (d.getType().equals(SW)) {
            g.fillOval(orig_x, orig_y, width, height);
            g.setColor(Color.black);
            g.drawOval(orig_x, orig_y, width, height);
        } else if (d.getType().equals(IP)) {
            g.setColor(Color.white);
            g.fillRect(orig_x, orig_y, width, height);
            if (hSw) {
                g.setColor(d.getColor());
                g.fillRect(orig_x, orig_y, width, height);
            }
            if (pSw) {
                g2.setColor(d.getEdgeColor());
                g2.setStroke(new BasicStroke(3));
                g.drawRoundRect​(orig_x, orig_y, width, height, 5, 5);
            }
            g.setColor(Color.black);
            g.drawString("in", orig_x + width + (int) ((float) 4 / 15 * width), orig_y + height - (int) ((float) 2 / 15 * height));
        } else if (d.getType().equals(OP)) {
            g.fillRect(orig_x, orig_y, width, height);
            g.setColor(Color.black);
//            g.drawRect(orig_x, orig_y, width, height);
            g.drawString("out", orig_x + width - (int) ((float) 8 / 15 * width), orig_y + height + (int) ((float) 9 / 15 * height));
        } else if (d.getType().equals(CH)) {
//            g2.setColor(Color.blue);
            if (!edge && hSw) {
                g.fillRect(orig_x, orig_y, width, height);
            } else if (pSw) {
                g2.setColor(d.getEdgeColor());
//                g2.setColor(Color.blue);
                g2.setStroke(new BasicStroke(3));
                g.drawRoundRect​(orig_x, orig_y, width, height, 5, 5);
            }
        } else if (d.getType().equals(SB)) {

            g.setColor(d.getEdgeColor());
            g.drawRect(orig_x, orig_y, width - 1, height - 1);
        } else {
            g.fillRect(orig_x, orig_y, width, height);
        }
    }

//    public void paintNets(Graphics g2, LinkedList<PFNet> nets) {
//        for (PFNet net : nets) {
//            for (UIWire wire : net.getPathWires()) {
//                drawWire(g2, wire);
//                drawNode(g2, wire.getTerminalA());
//                drawNode(g2, wire.getTerminalB());
//            }
//        }
//    }
    private void drawWire(Graphics g2, UIWire w) {
        Graphics2D g = (Graphics2D) g2;
        g.setColor(w.getColor());
        g.setStroke(w.getStroke());
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

    public PFNode findSource(Point searchLoc) {
        for (PFNode source : sources) {
            if (source.getBlock().getDot().dotFound(searchLoc)) {
                return source;
            }
        }
        return null;
    }

    public PFNode findSink(Point searchLoc) {
        for (PFNode sink : sinks) {
            if (sink.getBlock().getDot().dotFound(searchLoc)) {
                return sink;
            }
        }
        return null;
    }

    protected void drawNodes(CopyOnWriteArrayList<UIBlock> n, Graphics g, boolean edge) {
        if (!n.isEmpty()) {
            for (UIBlock comp : n) {
                UIDot dot = comp.getDot();
                drawNode(g, dot, edge);
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

    protected void drawSwWires(Graphics g) {
        if (!swWires.isEmpty()) {
            for (UIWire wire : swWires) {
                drawWire((Graphics2D) g, wire);
            }
        }
    }

    public void addSource(PFNode s) {
        sources.add(s);
    }

    public void addSink(PFNode s) {
        sinks.add(s);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        drawNodes(chanBlocks, g, false);
        drawWires(g);
        drawNodes(bottom, g, false);
        drawSwWires(g);
        drawNodes(top, g, false);
        drawNodes(chanBlocks, g, true);

        g.setColor(Color.black);
        g.setFont(new Font("TimesRoman", Font.PLAIN, 15));
        g.drawString("Iteration: "+iteration, hmPXC, hmPYC - hmPW);
        
        if (pSw) {
            ((Graphics2D) g).setPaint(gradientP);
//        g.setColor(Color.red);
            g.fillRect(hmPXC, hmPYC, hmPW, hmPH);
            g.setColor(Color.black);
            g.drawString("0", hmPXC + (int) (hmPW * 1.5), hmPYC + hmPH);
            g.drawString(numberFormat.format(maxPenalty), hmPXC + (int) (hmPW * 1.5), hmPYC + hmPW / 4);
            g.drawString("Penalty", hmPXC, hmPYC - hmPW / 4);
        }

        if (hSw) {
            ((Graphics2D) g).setPaint(gradientH);
            g.fillRect(hmHXC, hmHYC, hmPW, hmPH);
            g.setColor(Color.black);
            g.drawString("1", hmHXC + (int) (hmPW * 1.5), hmHYC + hmPH);
            g.drawString(Double.toString(maxHVal).substring(0, Math.min(5, Double.toString(maxHVal).length()))+"+", hmHXC + (int) (hmPW * 1.5), hmHYC + hmPW / 4);
            g.drawString("h(n)", hmHXC, hmHYC - hmPW / 4);
        }
        
    }

    @Override
    public void mouseExited(MouseEvent e) { // do nothing
    }

    @Override
    public void mouseEntered(MouseEvent e) { // do nothing
    }

    @Override
    public void mouseReleased(MouseEvent e) {
//        System.out.println("released");
//        if (curNode != null) {
//            if (state == WAITFORSRC) {
//                if (curNode.getOccupied() < 1) {
//                    selNet.setSource(curNode);
//                    curNode.occupy();
//                } else {
//                    Toolkit.getDefaultToolkit().beep();
//                    JOptionPane.showMessageDialog(
//                            this, "This source is used in another net", "Invalid Source", JOptionPane.ERROR_MESSAGE);
//                    repaint();
//                }
//            } else if (state == WAITFORTGT) {
//                if (curNode.getOccupied() < 3) {
//                    selNet.addSink(curNode);
//                    curNode.occupy();
//                } else {
//                    Toolkit.getDefaultToolkit().beep();
//                    JOptionPane.showMessageDialog(
//                            this, "This sink is fully occupied", "Invalid Sink", JOptionPane.ERROR_MESSAGE);
//                    repaint();
//                }
//            }
//        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
//        System.out.println("pressed");
//        if (state == WAITFORSRC) {
//            curNode = findSource(e.getPoint());
//        } else if (state == WAITFORSRC) {
//            curNode = findSource(e.getPoint());
//        }
//        if (curNode == null) {
//            System.out.println("not found");
//        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    public void setHeatMap(int hmXC, int hmYC, int hmW, int hmH) {
        this.hmPXC = hmXC;
        this.hmPYC = hmYC;
        this.hmPW = hmW;
        this.hmPH = hmH;
        hmHXC = hmXC;
        hmHYC = hmPYC+hmH+50;
        gradientP = new GradientPaint(hmXC, hmYC + hmH, new Color(255, 0, 0, 0), hmXC, hmYC, Color.red);
        gradientH = new GradientPaint(hmHXC, hmH + hmHYC, new Color(255, 153, 51,0), hmHXC, hmHYC, new Color(255, 153, 51));
    }

    public void setMaxPenalty(double maxPenalty) {
        this.maxPenalty = maxPenalty;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getState() {
        return state;
    }

    public void setMaxHVal(double maxHVal) {
        this.maxHVal = maxHVal;
    }

    public CopyOnWriteArrayList<UIBlock> getBlocks() {
        return blocks;
    }

    public CopyOnWriteArrayList<UIBlock> getBottom() {
        return bottom;
    }

    public CopyOnWriteArrayList<UIBlock> getChanBlocks() {
        return chanBlocks;
    }

    public void setHSw(boolean hSw) {
        this.hSw = hSw;
    }

    public void setPSw(boolean pSw) {
        this.pSw = pSw;
    }

    public void setIteration(int iteration) {
        this.iteration = iteration;
    }

    public boolean ishSw() {
        return hSw;
    }

    public boolean ispSw() {
        return pSw;
    }

    
    
}

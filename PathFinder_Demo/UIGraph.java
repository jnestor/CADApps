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
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.JPanel;
import java.awt.event.MouseListener;
import java.util.Locale;

/**
 *
 * @author 15002
 */
public class UIGraph extends JPanel implements MouseListener {

    public static final String SW = "SW";
    public static final String SB = "SB";
    public static final String LB = "LB";
    public static final String OP = "OP";
    public static final String IP = "IP";
    public static final String TM = "TM";
    public static final String CH = "CH";
    private static final int END = 0;
    private static final int START = 1;
    private int state = END;
    private boolean firstTime = true;

    private CopyOnWriteArrayList<UIBlock> blocks = new CopyOnWriteArrayList<UIBlock>();
    private CopyOnWriteArrayList<UIBlock> top = new CopyOnWriteArrayList<UIBlock>();
    private CopyOnWriteArrayList<UIBlock> bottom = new CopyOnWriteArrayList<UIBlock>();
    private CopyOnWriteArrayList<UIWire> wires = new CopyOnWriteArrayList<UIWire>();
    private CopyOnWriteArrayList<UIWire> swWires = new CopyOnWriteArrayList<UIWire>();
    private CopyOnWriteArrayList<UIBlock> chanBlocks = new CopyOnWriteArrayList<UIBlock>();
    private CopyOnWriteArrayList<UIWire> path = new CopyOnWriteArrayList<UIWire>();


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
    
    public void addSwWire(UIWire w){
        swWires.add(w);
    }
    
    public void addChan(UIBlock b){
        chanBlocks.add(b);
    }

    private void drawNode(Graphics g, UIDot d) {
        Point loc = d.getLoc();
        int height = d.getHeight();
        int width = d.getWidth();
        int orig_x = loc.x - width / 2;
        int orig_y = loc.y - height / 2;
        g.setColor(d.getColor());
        if (d.getType().equals(SW)) {
            g.fillOval(orig_x, orig_y, width, height);
            g.setColor(Color.black);
            g.drawOval(orig_x, orig_y, width, height);
        } else if (d.getType().equals(IP)) {
            g.fillRect(orig_x, orig_y, width, height);
            g.setColor(Color.black);
            g.drawRect(orig_x, orig_y, width, height);
            g.drawString("in", orig_x + width + (int) ((float) 4 / 15 * width), orig_y + height - (int) ((float) 2 / 15 * height));
        } else if (d.getType().equals(OP)) {
            g.fillRect(orig_x, orig_y, width, height);
            g.setColor(Color.black);
            g.drawRect(orig_x, orig_y, width, height);
            g.drawString("out", orig_x + width - (int) ((float) 8 / 15 * width), orig_y + height + (int) ((float) 9 / 15 * height));
        }else if (d.getType().equals(CH)){
            g.fillRect(orig_x, orig_y, width, height);
            g.setColor(d.getEdgeColor());
            g.drawRect(orig_x, orig_y, width, height);
        } 
        else{
            g.fillRect(orig_x, orig_y, width, height);
        }
    }

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

    public UIBlock findNode(Point searchLoc) {
        for (UIBlock block : blocks) {
            if (block.getDot().dotFound(searchLoc)) {
                return block;
            }
        }
        return null;
    }

    protected void drawNodes(CopyOnWriteArrayList<UIBlock> n,Graphics g) {
        if (!n.isEmpty()) {
            for (UIBlock comp : n) {
                UIDot dot = comp.getDot();
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
        firstTime = false;
    }
    
    protected void drawSwWires(Graphics g) {
        if (!swWires.isEmpty()) {
            for (UIWire wire : swWires) {
                drawWire((Graphics2D) g, wire);
            }
        }
        firstTime = false;
    }
    
    protected void drawPath(Graphics g){
        if (!path.isEmpty()) {
            for (UIWire wire : path) {
                drawWire((Graphics2D) g, wire);
            }
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawNodes(chanBlocks,g);
        drawWires(g);
        drawNodes(bottom,g);
        drawSwWires(g);
        drawPath(g);
        drawNodes(top,g);
    }
    

    @Override
    public void mouseExited(MouseEvent e) { // do nothing
    }

    @Override
    public void mouseEntered(MouseEvent e) { // do nothing
    }

    @Override
    public void mouseReleased(MouseEvent e) { // do nothing
    }

    @Override
    public void mousePressed(MouseEvent e) {
        System.out.println("pressed");
    }

    @Override
    public void mouseClicked(MouseEvent e) {
//        System.out.println("clicked");
//        Point mouseLoc = e.getPoint();
//        UIBlock selBlock = findNode(mouseLoc);
//        boolean formWire = false;
//        boolean accepted = false;
//        if (selBlock != null) {
//            String type = selBlock.getDot().getType();
//            System.out.println(selBlock.getDot().getType());
//            if (state == START) {
//                System.out.println("try to start");
//                UIBlock last = pathBlocks.get(pathBlocks.size() - 1);
//                if (type.equals(IP)) {
//                    InPin in = (InPin) selBlock;
//                    if (last.getDot().getType().equals(SW)) {
//                        Switch swLast = (Switch) last;
//                        if (swLast.getEdge().getEnd().equals(in.getNode())) {
//                            pathBlocks.add(in);
//                            //in.getDot().setColor(colorSeq.current());
//                            formWire = true;
//                            in.getNode().itirate();
//                            accepted = true;
//                        }
//                    }
//                } else if (type.equals(LB)) {
//                    LogicBlock lb = (LogicBlock) selBlock;
//                    if (last.getDot().getType().equals(IP)) {
//                        InPin inLast = (InPin) last;
//                        if (inLast.getEdge().getEnd().equals(lb.getSink())) {
//                            pathBlocks.add(lb);
//                            lb.getSink().itirate();
//                            state = END;
//                            colorSeq.next();
//                            pathBlocks.clear();
//                            accepted = true;
//                        }
//                    }
//                } else if (type.equals(OP)) {
////                    System.out.println("try to connect to OP");
//                    OutPin out = (OutPin) selBlock;
//                    if (last.getDot().getType().equals(LB)) {
////                        System.out.println("last one is LB");
//                        LogicBlock lbLast = (LogicBlock) last;
//                        if (lbLast.getOutPins().get(0).getNode().equals(out.getNode())) {
//                            pathBlocks.add(out);
//                            //out.getDot().setColor(colorSeq.current());
//                            out.getNode().itirate();
//                            accepted = true;
//                        }
//                    }
//                } else if (type.equals(SW)) {
//                    Switch sw = (Switch) selBlock;
//                    if (last.getDot().getType().equals(OP)) {
//                        OutPin outLast = (OutPin) last;
//                        if (outLast.getNode().getEdges().get(0).equals(sw.edge)) {
//                            pathBlocks.add(sw);
//                            sw.getDot().setColor(colorSeq.current());
//                            formWire = true;
//                            accepted = true;
//                        }
//                    } else if (last.getDot().getType().equals(TM)) {
//                        Terminal tmLast = (Terminal) last;
//                        Channel cLast = tmLast.getChannel();
//                        for (PFEdge edge : cLast.getEdges()) {
//                            if (sw.getEdge().equals(edge)) {
//                                pathBlocks.add(sw);
//                                sw.getDot().setColor(colorSeq.current());
//                                formWire = true;
//                                accepted = true;
//                                break;
//                            }else {
////                                System.out.println(cLast.getEdges().size());
////                                System.out.println("swStart: "+sw.getEdge().getStart().getID());
////                                System.out.println("swEnd: "+sw.getEdge().getEnd().getID());
////                                System.out.println("edgeStart: "+edge.getStart().getID());
////                                System.out.println("edgeEnd: "+edge.getEnd().getID());
////                                System.out.println();
//                            }
//                        }
//                    }
//                } else if (type.equals(TM)) {
//                    Terminal tm = (Terminal) selBlock;
//                    Channel channel = tm.getChannel();
//                    if (last.getDot().getType().equals(SW)) {
//                        Switch swLast = (Switch) last;
////                        System.out.println("last one is switch");
//                        if (swLast.getEdge().getEnd().equals(channel.getNode())) {
////                            System.out.println("share the same edge");
//                            if ((tm.getDot().getLoc().getX() == swLast.getDot().getLoc().getX())
//                                    || (tm.getDot().getLoc().getY() == swLast.getDot().getLoc().getY())) {
//                                pathBlocks.add(tm);
//                                tm.getDot().setColor(colorSeq.current());
//                                formWire = true;
//                                accepted = true;
//                            }
//                        } else {
////                            System.out.println("tmChannel: " + tm.getChannel().getNode().getID());
////                            System.out.println("swEdgeEnd: " + swLast.getEdge().getEnd().getID());
//                        }
//
//                    } else if (last.getDot().getType().equals(TM)) {
//                        Terminal tmLast = (Terminal) last;
//                        Channel channelLast = tmLast.getChannel();
//                        if (!channel.equals(channelLast)) {
////                            System.out.println("not same Channel");
////                            System.out.println("current: "+channel.getNode().getID());
////                            System.out.println("last: "+channelLast.getNode().getID());
//                            for (PFEdge edgeLast : channelLast.getEdges()) {
//                                if (edgeLast.getEnd().equals(channel.getNode())) {
//                                    pathBlocks.add(tm);
//                                    tm.getDot().setColor(colorSeq.current());
////                                    System.out.println(tm.getWireLoc()==null);
////                                    System.out.println(tmLast.getWireLoc()==null);
//                                    formPathWire(tm,tmLast,2,tm.getWireLoc(),tmLast.getWireLoc());
//                                    accepted = true;
//                                    break;
//                                }
//                            }
//                        } else if(!tmLast.getDot().equals(tm.getDot())){
////                            System.out.println("same Channel");
//                            for(UIWire wire:channelLast.getWires()){
//                                if(wire.getTerminalA().equals(tm.getDot())^wire.getTerminalB().equals(tm.getDot())){
//                                    if(wire.getTerminalA().equals(tmLast.getDot())^wire.getTerminalB().equals(tmLast.getDot())){
////                                    System.out.println("same Wire");
////                                    System.out.println("current: " + tm.getDot().getLoc().toString());
////                                    System.out.println("lastA: " + wire.getTerminalA().getLoc().toString());
////                                    System.out.println("lastA: " + wire.getTerminalB().getLoc().toString());
//                                    pathBlocks.add(tm);
//                                    tm.getDot().setColor(colorSeq.current());
//                                    formWire = true;
//                                    accepted = true;
//                                    break;}
//                                }
//                            }
//                        }
//                    }
//                }
//            } else if (state == END) {
//
//                if (type.equals(LB)) {
//                    LogicBlock lb = (LogicBlock) selBlock;
//                    if (pathBlocks.isEmpty()) {
//                        pathBlocks.add(lb);
//                        lb.getSource().occupy();
//                        state = START;
//                        accepted = true;
//                    } else {
//                        System.out.println("Not empty");
//                    }
//                }
//            }
//
//            if (formWire) {
////                System.out.println("Form wire");
////                UIWire w = new UIWire(selBlock.getDot(), pathBlocks.get(pathBlocks.size() - 2).getDot());
////                w.setColor(colorSeq.current());
////                path.add(w);
//                formPathWire(selBlock,pathBlocks.get(pathBlocks.size() - 2));
//            }
//        }
//
//        if (accepted) {
//            System.out.println("accepted");
//            repaint();
//            System.out.println("current pathLength: " + pathBlocks.size());
//        } else {
//            System.out.println("denied");
//        }
//        System.out.println("state: " + state);

    }
//    
//    public void formPathWire(UIBlock a, UIBlock b){
//        System.out.println("Form wire");
//        UIWire w = new UIWire(a.getDot(), b.getDot());
//        w.setColor(colorSeq.current());
//        path.add(w);
//    }
//    
//    public void formPathWire(UIBlock a, UIBlock b, int i, Point pA, Point pB){
//        System.out.println("Form wire");
//        UIWire w = new UIWire(a.getDot(), b.getDot(),i,pA,pB);
//        w.setColor(colorSeq.current());
//        path.add(w);
//    }

    
}

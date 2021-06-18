package pathfinder_demo;

import java.awt.Color;
import java.util.LinkedList;
import java.util.PriorityQueue;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author 15002
 */
public class PFNet {

    public static final String SW = "SW";
    public static final String LB = "LB";
    public static final String OP = "OP";
    public static final String IP = "IP";
    public static final String TM = "TM";
    public static final String SB = "SB";
    public static final String CH = "CH";

    private LinkedList<PFNode> sinks;
    private PFNode source;
    private LinkedList<PFNode> path;
    private LinkedList<UIWire> wires;
    private LinkedList<UIBlock> blocks;
    private Color color;
    private static int NETID = 0;
    private int id;

    public PFNet(LinkedList<PFNode> sinks, PFNode source) {
        this.sinks = sinks;
        this.source = source;
        path = new LinkedList<PFNode>();
        wires = new LinkedList<UIWire>();
        blocks = new LinkedList<UIBlock>();
        id = NETID;
        NETID++;
    }

    public PFNet() {
        id = NETID;
        NETID++;
    }

    public LinkedList<PFNode> getSinks() {
        return sinks;
    }

    public PFNode getSource() {
        return source;
    }

    /**
     * add a node to the tail of the LinkedList
     *
     * @param n the node to add
     */
    public void addNode(PFNode n) {
        path.addLast(n);
    }

    public void clearPath() {
        path.clear();
        wires.clear();
        blocks.clear();
    }

    public LinkedList<PFNode> getPathNodes() {
        return path;
    }

    /**
     * add a UIwire to the tail of LinkedList
     *
     * @param w UIWire to add
     */
    public void addWire(UIWire w) {
        wires.addLast(w);
        blocks.add(w.getBlockA());
        blocks.add(w.getBlockB());
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public LinkedList<UIWire> getPathWires() {
        return wires;
    }

    public void paintPath() {
        PFNet net = this;
        for (UIWire wire : wires) {
            //color a switch block' target node matches a sink
            if (wire.isSwOn()&&(sinks.contains(wire.getAvailableSink())||source.equals(wire.getAvailableSink()))) {
                wire.getSwBlock().getDot().setColor(color);
            }
            wire.setColor(color);

            //Only color the non switch blocks
            if (!wire.getTerminalA().getType().equals(SW) && !wire.getTerminalA().getType().equals(IP)) {
                wire.getTerminalA().setColor(color);
            }
            if (!wire.getTerminalB().getType().equals(SW) && !wire.getTerminalB().getType().equals(IP)) {
                wire.getTerminalB().setColor(color);
            }
        }
    }

    public void clearNodes(boolean h) {
        for (PFNode n : path) {
            n.clearStats(h);
        }
        for (PFNode n : sinks) {
            n.clearStats(h);
        }
        source.clearStats(h);
    }

    public void backUpHVal() {
        for (PFNode n : path) {
            n.backUpHVal();
        }
        for (PFNode n : sinks) {
            n.backUpHVal();
        }
        source.backUpHVal();
    }

    public void resetChannels() {
        for (PFNode n : path) {
            n.resetWires();
        }
    }

    public void clearWires() {
        for (UIWire wire : wires) {
            wire.clearTargets();
        }
    }

    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        PFNet n = (PFNet) obj;
        return id == n.getId();
    }

    public void addSink(PFNode sink) {
        sinks.add(sink);
    }

    public void setSource(PFNode source) {
        this.source = source;
    }

}

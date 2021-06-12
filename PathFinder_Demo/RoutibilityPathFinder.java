/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pathfinder_demo;

import static java.lang.Math.max;
import java.util.LinkedList;
import java.util.PriorityQueue;

/**
 *
 * @author 15002
 */
public class RoutibilityPathFinder {

    private boolean legal = false;
    private double pFac = 0.5;//increase by 1.5 to 2 times each iteration
    //for easy ones, pfac = 10000 at first
    private double hFac = 0.5;//remain constant
    private int iteration = 1;
    private int threshold = 45;
    private LinkedList<PFNet> nets;
    private LinkedList<PFNode> nodes;

    public RoutibilityPathFinder(LinkedList<PFNet> nets, LinkedList<PFNode> nodes) {
        this.nets = nets;
        this.nodes = nodes;
    }

    /**
     * Cost function for pathFinder algorithm
     *
     * @param n the current node
     * @param m the previous node
     * @return Cost for pathFinder
     */
    private double cost(PFNode n, PFNode m) {
        return n.getBaseCost() * h(n) * p(n) + bendCost(n, m);
    }

    private double p(PFNode n) {
        double curV = (n.getOccupied() + 1 - n.getCapacity()) * pFac;
        return 1 + max(0, curV);
    }

    private double h(PFNode n) {
        double hCurr = 0;
        if (iteration == 1) {
            hCurr = 1;
        } else {
            double curV = (n.getOccupied() - n.getCapacity()) * hFac;
            hCurr = n.getHVal();
            hCurr = hCurr + max(0, curV);
        }
        n.setCost(hCurr);
        return hCurr;
    }

    private int bendCost(PFNode n, PFNode m) {
        if (n.isIsVertical() ^ m.isIsVertical()) {
            //System.out.println("bend"+n.getID()+" "+m.getID());
            return 1;
        } else {
            return 0;
        }
    }

    public boolean route() {
        iteration = 1;
        while (!legal) {
            if (iteration == threshold) {
                break;
            }
            //set a color to each net when the net is created
            //Wires and nodes are cleared before each iteration
            for (PFNet net : nets) {
                net.clearNodes();
                net.resetChannels();
                net.clearWires();
                net.clearPath();
            }
            globalRoute();
            wireArrange();
            for (PFNet net : nets) {
                net.clearNodes();

                net.clearWires();
                net.clearPath();
            }

//            for (PFNode n : nodes) {
//                n.clearStats();
//            }
//
//            for (PFNet net : nets) {
////            for(PFNode node:net.getPathNodes()){
////                node.clearStats();
////                //node.resetWires();
////            }
//                for (UIWire wire : net.getPathWires()) {
//                    wire.setOccupied(false);
//                }
//            }
//            System.out.println("done reset");
            for (PFNet net : nets) {
                System.out.println("real route");
                for (PFNode n : nodes) {
                    n.setPrev(null);
                }
                //set a color to each net when the net is created
                //net.setColor(ColorSequencer.next());
                PFNode source = net.getSource();
                //System.out.println("sourceID: " + source.getID());
                for (PFNode sink : net.getSinks()) {
                    PriorityQueue<PFNode> pQueue = new PriorityQueue<PFNode>();
                    pQueue.add(source);
                    source.setPathCost(0);
                    for (PFNode n : net.getPathNodes()) {
                        n.setPathCost(0);
                    }
                    while (!pQueue.isEmpty()) {
                        PFNode m = pQueue.poll();
//                        System.out.println("curr ID: "+m.getID());
                        for (PFEdge e : m.getEdges()) {

                            PFNode n = e.getEnd();
//                            if(m.getID()==33)System.out.println("leave ID: "+n.getID());
                            if (n.getPrev() == null) {
                                n.setPrev(m);
                                if (n.equals(sink)) {
                                    if (m.getPrev().getPrev().getBlock().getDot().getType().equals("OP")) {
//                                        System.out.println(m.getPrev().getPrev().getBlock().getDot().getType());
                                        n.setPrev(null);
                                    } else {
                                        pQueue.clear();
//                                        System.out.println("found");
                                        break;
                                    }
                                } else {
                                    n.setPathCost(m.getPathCost() + this.cost(n, m));
//                                    if(n.getID()==34){
//                                        System.out.println("34 cost: "+n.getPathCost());
//                                    }
//                                    if(n.getID()==22){
//                                        System.out.println("22 cost: "+n.getPathCost());
//                                    }
                                    pQueue.add(n);
                                }
                            }
                        }
                    }
                    //Back Trace
                    backTrace(sink, source, net);
                }
            }
            iteration++;
            pFac *= 1.5;
            legal = true;
            for (PFNode n : nodes) {
                if (!n.inCapacity()) {
                    legal = false;
                }
            }
        }
        if (legal) {
            System.out.println("Succeed");
        } else {
            System.out.println("Fail");
        }
        return legal;
    }

    private void backTrace(PFNode sink, PFNode source, PFNet net) {
        //Back Trace
        PFNode backNode = sink;
        System.out.println("realLastChan: " + sink.getPrev().getPrev().getID());
        System.out.println("sinkID: " + sink.getID());
        while (!backNode.equals(source)) {
            //Trace back a node
            PFNode prev = backNode.getPrev();
            net.addNode(backNode);
            backNode.occupy();

            //Choose a wire segment in the channel
            if (!backNode.getWires().isEmpty()) {
                System.out.println("ChannelID: " + backNode.getID());
                UIWire lastWire = net.getPathWires().getLast();
                LinkedList<UIWire> nodeWires = backNode.getWires();
                boolean added = false;
                for (UIWire wire : nodeWires) {
                    if (wire.getTargetSink() != null) {
                        //System.out.println("cuurentWireTargetID: " + wire.getTargetSink().getID());
                    }
                    //Check if the wire segment connects to a sink
                    if (wire.getTargetSink() != null
                            && (wire.getTargetSink().equals(sink)
                            || wire.getTargetSink().equals(source))) {
                        net.addWire(wire);
                        wire.setOccupied(true);
                        added = true;
                        System.out.println("mactch target, " + nodeWires.indexOf(wire));
                        //System.out.println("add");
                        break;
                    }
                }

                if (!added) {
                    for (UIWire wire : nodeWires) {
                        //Check if the wire segment is not used and can be used 
                        if (wire.getTargetSink() == null
                                && !wire.isOccupied()) {
                            net.addWire(wire);
                            wire.setOccupied(true);
                            added = true;
                            //System.out.println("add");
                            break;
                        }
                    }
                }
                //If the capacity is above, choose the first one always
                if (!added) {

                    net.addWire(nodeWires.getFirst());
                    nodeWires.getFirst().setOccupied(true);
                    //System.out.println("add");
                }
                //System.out.println("add done");
            }

            //Traceback an edge
            for (PFEdge edge : prev.getEdges()) {
                if (edge.getEnd().equals(backNode) && !edge.getWires().isEmpty()) { //last term to avoid outpint to sink situation
                    LinkedList<UIWire> edgeWires = edge.getWires();
                    if (edgeWires.size() == 1) {
                        net.addWire(edgeWires.getFirst());
                        break;
                    } //Switch block stuation
                    else {
                        System.out.println("add edge");
                        boolean added = false;
                        UIWire lastWire = net.getPathWires().getLast();
                        UIWire backupWire = null;
                        for (UIWire wire : edgeWires) {
                            UIBlock lastA = lastWire.getBlockA();
                            UIBlock lastB = lastWire.getBlockB();
                            UIBlock curA = wire.getBlockA();
                            UIBlock curB = wire.getBlockB();
                            UIBlock[] lasts = new UIBlock[]{lastA, lastB};
                            UIBlock[] curs = new UIBlock[]{curA, curB};

                            for (int i = 0; i < 2; i++) {
                                for (int j = 0; j < 2; j++) {
                                    if (lasts[i].equals(curs[j])) {
                                        backupWire = wire;
                                        int x = 0;
                                        if (j == 0) {
                                            x = 1;
                                        }
                                        if (curs[x].getDot().getTargetSink() != null
                                                && (curs[x].getDot().getTargetSink().equals(source)
                                                || curs[x].getDot().getTargetSink().equals(sink))) {
                                            net.addWire(wire);
                                            System.out.println("add match");
                                            added = true;
                                            break;
                                        }
                                    }
                                }
                                if (added) {
                                    break;
                                }
                            }
                            if (added) {
                                break;
                            }
                        }
                        if (!added) {
                            for (UIWire wire : edgeWires) {
                                UIBlock lastA = lastWire.getBlockA();
                                UIBlock lastB = lastWire.getBlockB();
                                UIBlock curA = wire.getBlockA();
                                UIBlock curB = wire.getBlockB();

                                UIBlock[] lasts = new UIBlock[]{lastA, lastB};
                                UIBlock[] curs = new UIBlock[]{curA, curB};
                                for (int i = 0; i < 2; i++) {
                                    for (int j = 0; j < 2; j++) {
                                        if (lasts[i].equals(curs[j])) {
                                            backupWire = wire;
                                            int x = 0;
                                            if (j == 0) {
                                                x = 1;
                                            }
                                            if (curs[x].getDot().getTargetSink() == null
                                                    && !curs[x].getDot().isOccupied()) {
                                                net.addWire(wire);
                                                wire.setOccupied(true);
                                                added = true;
                                                System.out.println("add empty");
                                                break;
                                            }
                                        }
                                    }
                                    if (added) {
                                        break;
                                    }
                                }
                                if (added) {
                                    break;
                                }
                            }
                        }
                        if (!added) {
                            net.addWire(backupWire);
                            backupWire.setOccupied(true);
                            System.out.println("add duplicate");
                        }
                        System.out.println("add done");
                        if (added) {
                            break;
                        }
                    }
                }
            }
            backNode = backNode.getPrev();
        }
        net.paintPath();
    }

    //A rough routing without visualization just so that I know which wire seg in a channel will be used for which net
    private void globalRoute() {
//        for (PFNode n : nodes) {
//            n.clearStats();
//            n.resetWires(); // clear targetNode of channels
//        }

//        for(PFNet net:nets){
////            for(PFNode node:net.getPathNodes()){
////                node.clearStats();
////                node.resetWires();
////            }
//            for(UIWire wire:net.getPathWires()){
//                wire.setOccupied(false);
//            }
//        }
        resetColor();
        for (PFNet net : nets) {
//            System.out.println("Ah");
//            net.clearPath();
            for (PFNode n : nodes) {
                n.setPrev(null);
            }
            PFNode source = net.getSource();
//            System.out.println("sourceID: "+source.getID());
//            System.out.println(net.getSinks().isEmpty());
            for (PFNode sink : net.getSinks()) {
                PriorityQueue<PFNode> pQueue = new PriorityQueue<PFNode>();
                pQueue.add(source);
                source.setPathCost(0);
                for (PFNode n : net.getPathNodes()) {
                    n.setPathCost(0);
                }
                while (!pQueue.isEmpty()) {
                    PFNode m = pQueue.poll();
                    //System.out.println("currentID: "+m.getID());
                    for (PFEdge e : m.getEdges()) {
                        PFNode n = e.getEnd();
                        if (n.getPrev() == null) {
                            //System.out.println("leafID: "+n.getID());
                            n.setPrev(m);
                            //System.out.println("current Prev: "+n.getPrev().getID()+"\n");
                            if (n.equals(sink)) {
                                if (m.getPrev().getPrev().getBlock().getDot().getType().equals("OP")) {
//                                    System.out.println(m.getPrev().getPrev().getBlock().getDot().getType());
                                    n.setPrev(null);
                                } else {
                                    pQueue.clear();
//                                    System.out.println("found");
                                    break;
                                }
                            } else {
                                n.setPathCost(m.getPathCost() + this.cost(n, m));
                                pQueue.add(n);
                            }
                        }
                    }
                }

                PFNode backNode = sink.getPrev();
                sink.occupy();
                source.occupy();
                while (!backNode.equals(source)) {
                    //if(backNode.getID()==25){
                    //System.out.println("current Prev: "+backNode.getPrev().getID());
                    //}
                    net.addNode(backNode);
                    backNode.occupy();
                    backNode = backNode.getPrev();
                }
                System.out.println("fakeLastChan: " + net.getPathNodes().get(1).getID());
                System.out.println("sinkID: " + sink.getID());

            }
        }
    }

    private void wireArrange() {
        System.out.println("arrange");
        for (PFNet net : nets) {
            System.out.println("one net");
            LinkedList<PFNode> path = net.getPathNodes();
            PFNode lastChannel = path.get(1);
            PFNode firstChannel = path.get(path.size() - 2);
            System.out.println("lastChan ID: " + lastChannel.getID());
            for (PFNode sink : net.getSinks()) {
                for (UIWire wire : lastChannel.getWires()) {
                    boolean inCapMatch = lastChannel.inCapacity()
                            && wire.getAvailableSink().equals(sink) && !wire.isOccupied();
                    boolean outOfCapMatch = !lastChannel.inCapacity()
                            && wire.getAvailableSink().equals(sink);

                    System.out.println("avaSink: " + wire.getAvailableSink().getID());
                    System.out.println("inCap: " + inCapMatch);
                    System.out.println("outCap: " + outOfCapMatch);
                    if (inCapMatch || outOfCapMatch) {
                        System.out.println("math sink, sinkID: " + sink.getID() + " channelID: " + lastChannel.getID() + "wireIndex: " + lastChannel.getWires().indexOf(wire));
                        wire.setTargetSink(sink);
                        wire.setOccupied(true);
                        break;
                    }
                }
            }
            for (UIWire wire : firstChannel.getWires()) {
                boolean inCapMatch = firstChannel.inCapacity()
                        && wire.getAvailableSink().equals(net.getSource()) && !wire.isOccupied();
                boolean outOfCapMatch = !lastChannel.inCapacity()
                        && wire.getAvailableSink().equals(net.getSource());
                if (inCapMatch || outOfCapMatch) {
                    wire.setTargetSink(net.getSource());
                    wire.setOccupied(true);
                    break;
                }
            }
            System.out.println();
//            net.clearPath();
        }
        System.out.println("done arrange");
    }

    private void resetColor() {
        for (PFNet net : nets) {
            for (PFNode node : net.getPathNodes()) {
                node.getBlock().getDot().resetColor();
            }
            for (UIWire wire : net.getPathWires()) {
                wire.resetColor();
            }
        }
    }

}

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
    private PriorityQueue<PFNode> queue;
    private double pFac = 0.5;//increase by 1.5 to 2 times each iteration
    //for easy ones, pfac = 10000 at first
    private double hFac = 0.5;//remain constant
    private int iteration = 1;
    private int threshHold = 45;
    private LinkedList<UIDot> dots;
    private LinkedList<PFNet> nets;
    private LinkedList<PFNode> nodes;
    private LinkedList<PFEdge> edges;

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
            return 1;
        } else {
            return 0;
        }
    }

    public void route() {
        while (!legal) {
            ColorSequencer.reset();
            globalRoute();
            for (PFNode n : nodes) {
                n.clearStats();
            }
            for (PFNet net : nets) {
                net.setColor(ColorSequencer.next());
                net.clearPath();
                PFNode source = net.getSource();
                for (PFNode sink : net.getSinks()) {
                    PriorityQueue<PFNode> pQueue = new PriorityQueue<PFNode>();
                    pQueue.add(source);
                    source.setPathCost(0);
                    for (PFNode n : net.getPath()) {
                        n.setPathCost(0);
                    }
                    while (!pQueue.isEmpty()) {
                        PFNode m = pQueue.poll();
                        for (PFEdge e : m.getEdges()) {
                            PFNode n = e.getEnd();
                            n.setPrev(m);
                            if (n.equals(sink)) {
                                pQueue.clear();
                                break;
                            } else {
                                n.setPathCost(m.getPathCost() + this.cost(n, m));
                                pQueue.add(n);
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
    }

    private void globalRoute() {
        for (PFNode n : nodes) {
            n.clearStats();
            n.resetWires();
        }
        resetColor();
        wireArrange();
        for (PFNet net : nets) {
            net.clearPath();
            PFNode source = net.getSource();
            for (PFNode sink : net.getSinks()) {
                PriorityQueue<PFNode> pQueue = new PriorityQueue<PFNode>();
                pQueue.add(source);
                source.setPathCost(0);
                for (PFNode n : net.getPath()) {
                    n.setPathCost(0);
                }
                while (!pQueue.isEmpty()) {
                    PFNode m = pQueue.poll();
                    for (PFEdge e : m.getEdges()) {
                        PFNode n = e.getEnd();
                        n.setPrev(m);
                        if (n.equals(sink)) {
                            pQueue.clear();
                            break;
                        } else {
                            n.setPathCost(m.getPathCost() + this.cost(n, m));
                            pQueue.add(n);
                        }
                    }
                }
                PFNode backNode = sink.getPrev();
                sink.occupy();
                source.occupy();
                while (!backNode.equals(source)) {
                    net.addNode(backNode);
                    backNode.occupy();
                    backNode = backNode.getPrev();
                }
            }
        }
    }

    private void wireArrange() {
        for (PFNet net : nets) {
            LinkedList<PFNode> path = net.getPath();
            PFNode lastChannel = path.get(path.size() - 2);
            PFNode firstChannel = path.get(1);
            for (PFNode sink : net.getSinks()) {
                for (UIWire wire : lastChannel.getWires()) {
                    boolean inCapMatch = lastChannel.inCapacity()
                            && wire.getAvailableNode().equals(sink) && !wire.isOccupied();
                    boolean outOfCapMatch = !lastChannel.inCapacity()
                            && wire.getAvailableNode().equals(sink);
                    if (inCapMatch || outOfCapMatch) {
                        wire.setTargetNode(sink);
                        break;
                    }
                }
            }
            for (UIWire wire : firstChannel.getWires()) {
                boolean inCapMatch = firstChannel.inCapacity()
                        && wire.getAvailableNode().equals(net.getSource()) && !wire.isOccupied();
                boolean outOfCapMatch = !lastChannel.inCapacity()
                        && wire.getAvailableNode().equals(net.getSource());
                if (inCapMatch || outOfCapMatch) {
                    wire.setTargetNode(net.getSource());
                    break;
                }
            }
        }
    }

    private void resetColor() {
        for (PFNode node : nodes) {
            if (!node.getWires().isEmpty()) {
                for (UIWire wire : node.getWires()) {
                    wire.resetColor();
                }
            }
            node.getBlock().getDot().resetColor();
        }

        for (PFEdge edge : edges) {
            for (UIWire wire : edge.getWires()) {
                wire.resetColor();
            }
        }
    }

    private void backTrace(PFNode sink, PFNode source, PFNet net) {
        //Back Trace
        PFNode backNode = sink;
        
        while (!backNode.equals(source)) {
            //Trace back a node
            PFNode prev = backNode.getPrev();
            net.addNode(backNode);
            backNode.occupy();

            //Choose a wire segment in the channel
            if (!backNode.getWires().isEmpty()) {
                UIWire lastWire = net.getWires().getLast();
                UIBlock lastA = lastWire.getBlockA();
                UIBlock lastB = lastWire.getBlockB();
                LinkedList<UIWire> nodeWires = backNode.getWires();
                boolean added = false;
                for (UIWire wire : nodeWires) {
                    UIBlock curA = wire.getBlockA();
                    UIBlock curB = wire.getBlockB();
                    //Check if the wire segment connects to a sink
                    if (wire.getTargetNode() != null
                            && (wire.getTargetNode().equals(sink)
                            || wire.getTargetNode().equals(source))) {
                        net.addWire(wire);
                        wire.setOccupied(true);
                        added = true;
                        break;
                    } else if (curA.equals(lastA) || curA.equals(lastB)
                            || curB.equals(lastA) || curB.equals(lastB)) {
                        net.addWire(wire);
                        wire.setOccupied(true);
                        added = true;
                        break;
                    } //Check if the wire segment is not used and can be used 
                    else if (wire.getTargetNode() == null
                            && !wire.isOccupied()) {
                        net.addWire(wire);
                        wire.setOccupied(true);
                        added = true;
                        break;
                    }
                }
                //If the capacity is above, choose the first one always
                if (!added) {
                    net.addWire(nodeWires.getFirst());
                    nodeWires.getFirst().setOccupied(true);
                }
            }

            //Traceback an edge
            for (PFEdge edge : prev.getEdges()) {
                if (edge.getEnd().equals(backNode) && !edge.getWires().isEmpty()) { //last term to avoid outpint to sink situation
                    LinkedList<UIWire> edgeWires = edge.getWires();
                    if (edgeWires.size() == 1) {
                        net.addWire(edgeWires.getFirst());
                    } //Switch block stuation
                    else {
                        boolean added = false;
                        UIWire lastWire = net.getWires().getLast();
                        UIWire backupWire = null;
                        for (UIWire wire : edgeWires) {
                            UIBlock lastA = lastWire.getBlockA();
                            UIBlock lastB = lastWire.getBlockB();
                            UIBlock curA = wire.getBlockA();
                            UIBlock curB = wire.getBlockB();
                            if (curA.equals(lastA) || curA.equals(lastB)
                                    || curB.equals(lastA) || curB.equals(lastB)) {
                                backupWire = wire;
                                if (wire.getTargetNode() != null
                                        && (wire.getTargetNode().equals(source)
                                        || wire.getTargetNode().equals(sink))) {
                                    net.addWire(wire);
                                    added = true;
                                    break;
                                } else if (wire.getTargetNode() == null
                                        && !wire.isOccupied()) {
                                    net.addWire(wire);
                                    wire.setOccupied(true);
                                    added = true;
                                    break;
                                }
                            }
                        }
                        if (!added) {
                            net.addWire(backupWire);
                            backupWire.setOccupied(true);
                        }
                    }
                }
            }
            backNode = backNode.getPrev();
        }
    }

}

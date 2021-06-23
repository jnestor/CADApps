/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pathfinder_demo;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import static java.lang.Math.max;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 *
 * @author 15002
 */
public class RoutibilityPathFinder {

    private static final int DONE = 0;
    private static final int EXPANDING = 1;

    public static final String SW = "SW";
    public static final String SB = "SB";
    public static final String LB = "LB";
    public static final String OP = "OP";
    public static final String IP = "IP";
    public static final String TM = "TM";
    public static final String CH = "CH";

    private double maxPenalty = 1;
    private double maxHVal = 25;

    private boolean legal = false;
    private double pFac = 0.5;//increase by 1.5 to 2 times each iteration
    //for easy ones, pfac = 10000 at first
    private double hFac = 0.5;//remain constant
    private int iteration = 1;
    private int threshold = 45;
    private CopyOnWriteArrayList<PFNet> nets;
    private LinkedList<PFNode> nodes;
    private boolean pause;
//    private boolean step;
    private PFNet selNet;
    private boolean done = true;
    private boolean stop;
    private PFNet ghostNet = new PFNet(null, null);
    private PFNet firstNet;
    Iterator iterator;

    UIGraph graph;
    Timer routingTimer = new Timer(400, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            netIterate(selNet);
        }
    });

    public RoutibilityPathFinder(CopyOnWriteArrayList<PFNet> nets, LinkedList<PFNode> nodes, UIGraph g) {
        this.nets = nets;
        this.nodes = nodes;
        graph = g;
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
        n.setHVal(hCurr);
        return hCurr;
    }

    private double heatMapVal(PFNode n) {
        return n.getBaseCost() * p(n) * n.getHVal();
    }

    private int bendCost(PFNode n, PFNode m) {
        if ((n.isIsVertical() ^ m.isIsVertical()) && (n.getBlock().getDot().getType().equals(CH) && m.getBlock().getDot().getType().equals(CH))) {
            return 1;
        } else {
            return 0;
        }
    }

    public void iterateReset() {
        System.out.println("real route");
        graph.setIteration(iteration);
        resetColor(false);
        //set a color to each net when the net is created
        //Wires and nodes are cleared before each iteration
        for (PFNet net : nets) {
            if (!net.equals(ghostNet)) {
                net.clearNodes(false);
                net.resetChannels();
                net.clearWires();
                net.clearPath();
            }
        }
        for (PFNode node : nodes) {
            node.backUpHVal();
            node.setPathCost(0);
        }
        globalRoute();
        for (PFNode node : nodes) {
            node.recoverHVal();
            node.setPathCost(0);
        }

        //reset nets
        for (PFNet net : nets) {
            if (net != ghostNet) {
                net.clearNodes(false);
                net.clearWires();
                net.clearPath();
            }
        }
    }

//    public void iterate() {
////        iterateReset();
//        while (iterator.hasNext()) {
//            
//            while (!done) {
//                if (stop) {
//                    return;
//                }
//                if (!routingTimer.isRunning() && !pause && !done) {
//                    if (!step) {
//                        routingTimer.setInitialDelay(delay);
//                    }
//                    routingTimer.start();
////                    System.out.println("started");
//                    if (step) {
//                        pause = true;
//                        step = false;
//                    }
//                }
//            }
////            System.out.println("Step: " + step);
//            done = false;
//        }
//        //graph.repaint();
//    }

    private void legalCheck() {
        iterator = nets.iterator();
//        System.out.println(iterator.hasNext());
        boolean pass = true;
        for (PFNode n : nodes) {
            if (!n.inCapacity()) {
                pass = false;
//                System.out.println(n.getID() + "is illegal " + n.getType());
            }
        }
        legal = pass;
        iteration++;
        if (!legal && !(iteration > threshold)) {
            resetColor(false);
//            System.out.println(iteration);
        }

        if (legal || iteration > threshold) {
            graph.setState(DONE);
            pause = true;
//            step = true;
            routingTimer.stop();
        }
        pFac *= 1.5;
//        System.out.println(pFac);
    }

    private void netIterate(PFNet net) {
        System.out.println("test");
        if (pause) {
            System.out.println("pause");
            routingTimer.stop();
            
        }
        if (done) {
            System.out.println("start");
            done = false;
            if (net.equals(firstNet)) {
                iterateReset();
            }
            if (!net.equals(ghostNet)) {
                PFNode source = net.getSource();
                for (PFNode sink : net.getSinks()) {

                    for (PFNode n : nodes) {
                        n.setPrev(null);
                    }
                    graph.repaint();
                    PriorityQueue<PFNode> pQueue = new PriorityQueue<PFNode>();
                    pQueue.add(source);
                    source.setPathCost(0);
                    for (PFNode n : net.getPathNodes()) {
                        n.setPathCost(0);
                    }
                    while (!pQueue.isEmpty()) {
                        PFNode m = pQueue.poll();
                        for (PFEdge e : m.getEdges()) {
                            PFNode n = e.getEnd();
                            if (n.getPrev() == null) {
                                n.setPrev(m);
                                if (n.equals(sink)) {

                                    if (m.getPrev().getPrev().getBlock().getDot().getType().equals("OP")) {
                                        n.setPrev(null);
//                                    System.out.println("wrong");
                                    } else {
                                        pQueue.clear();
//                                    System.out.println("found");
                                        break;
                                    }
                                } else {
                                    if (!net.getPathNodes().contains(n)) {
                                        n.setPathCost(m.getPathCost() + cost(n, m));
                                    }
                                    pQueue.add(n);
                                }
                            }
                        }
                    }
                    //Back Trace
                    backTrace(sink, source, net);
                }
            }
            done = true;
//            routingTimer.stop();
//        System.out.println("done");

            //heatmap
            for (PFNode n : nodes) {
                if ((n.getBlock().getDot().getType().equals(IP)) || (n.getBlock().getDot().getType().equals(CH))) {
                    if (!n.inCapacity()) {
                        double costN = heatMapVal(n);
                        if (costN > maxPenalty) {
                            maxPenalty = costN + pFac * Math.pow(pFac, 0.15);
                            graph.setMaxPenalty(maxPenalty);
                        }
                        n.getBlock().getDot().setEdgeColor(new Color(255, 0, 0, (int) (costN / maxPenalty * 255)));
                    }
                    double hVal = n.getHVal();
                    if (hVal > maxHVal) {
                        maxHVal = hVal + 1;
                        graph.setMaxHVal(maxHVal);
                    }
                    n.getBlock().getDot().setColor(new Color(255, 153, 51, (int) ((n.getHVal()) / maxHVal * 255)));
                    graph.repaint();
                }
            }
            if (net.equals(ghostNet)) {
                legalCheck();
                if (legal) {
                    return;
                }
            }
            graph.repaint();
            System.out.println(iterator.hasNext());
            selNet = (PFNet) iterator.next();
        }
    }

//    public boolean route() {
//        iteration = 1;
////        pause = false;
//        
//        while (!legal) {
//            if (iteration > threshold) {
//                break;
//            }
//            iterate();
//        }
////        routingTimer.stop();
////        if (legal) {
////            System.out.println("Succeed");
////        } else {
////            System.out.println("Fail");
////            System.out.println(iteration);
////        }
//////        restartReset();
//
//        return legal;
//    }

    private void backTrace(PFNode sink, PFNode source, PFNet net) {
        //Back Trace
        PFNode backNode = sink;
//        PFNode lastChannel = sink.getPrev().getPrev();
//        boolean addLast = false;
//        System.out.println(lastChannel.getID());
        while (!backNode.equals(source)) {
            //System.out.println("trace" + backNode.getID());
            if (!net.getPathNodes().contains(backNode)) {
                //Trace back a node
                PFNode prev = backNode.getPrev();
                net.addNode(backNode);
                if (!backNode.getType().equals(CH)) {
                    backNode.occupy();
                }
                //Choose a wire segment in the channel
                if (!backNode.getWires().isEmpty()) {
                    //System.out.println("ChannelID: " + backNode.getID());
                    LinkedList<UIWire> nodeWires = backNode.getWires();
                    boolean added = false;
                    for (UIWire wire : nodeWires) {
//                        if (wire.getTargetNet() != null) {
//                            System.out.println("cuurentWireTargetID: " + wire.getTargetSink().getID());
//                        }
                        //Check if the wire segment connects to a sink
                        if (wire.getTargetNets().contains(net)) {
                            net.addWire(wire);
                            backNode.occupy();
//                            wire.addTargetNet(net);
//                            wire.setOccupied(true);
                            added = true;
//                            System.out.println(backNode.getID() + "mactch target, " + nodeWires.indexOf(wire));
                            //System.out.println("add");
                        }
                    }

                    if (!added) {
                        for (UIWire wire : nodeWires) {
                            //Check if the wire segment is not used and can be used 
                            if (wire.getTargetNets().isEmpty()) {
                                net.addWire(wire);
                                //wire.setOccupied(true);
                                wire.addTargetNet(net);
                                backNode.occupy();
                                added = true;
                                //System.out.println("add");
                                break;
                            }
                        }
                    }
                    //If the capacity is above, choose the first one always
                    if (!added) {
                        net.addWire(nodeWires.getFirst());
                        backNode.occupy();
                        nodeWires.getFirst().addTargetNet(net);
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
                            boolean added = false;
//                            for (UIWire lastWire : lastChan.getWires()) {
                            for (UIWire lastWire : backNode.getWires()) {
                                if (lastWire.getTargetNets().contains(net)) {
                                    added = false;
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
                                                    int x = 0;
                                                    if (j == 0) {
                                                        x = 1;
                                                    }
                                                    if (curs[x].getDot().equals(prev.getWires().getFirst().getTerminalA())
                                                            || curs[x].getDot().equals(prev.getWires().getFirst().getTerminalB())) {
                                                        backupWire = wire;
                                                    }
                                                    if (curs[x].getDot().getTargetNets().contains(net)) {
                                                        net.addWire(wire);
                                                        wire.addTargetNet(net);

                                                        //System.out.println("add match");
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
                                                        int x = 0;
                                                        if (j == 0) {
                                                            x = 1;
                                                        }
                                                        if (curs[x].getDot().getTargetNets().isEmpty()) {
                                                            net.addWire(wire);
                                                            //wire.setOccupied(true);
                                                            wire.addTargetNet(net);
                                                            added = true;
                                                            //System.out.println("add empty");
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
                                        backupWire.addTargetNet(net);
                                        //System.out.println("add duplicate");
                                    }
                                }
                            }
                            //System.out.println("add done");
                            if (added) {
                                break;
                            }
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
        for (PFNet net : nets) {
            if (net != ghostNet) {
//            System.out.println("Ah");

                PFNode source = net.getSource();
                //source.occupy();
                boolean firstSink = true;
//            System.out.println("sourceID: "+source.getID());
//            System.out.println(net.getSinks().isEmpty());
                for (PFNode sink : net.getSinks()) {
                    //reset nodes
                    for (PFNode n : nodes) {
                        n.setPrev(null);
                    }
                    //routing
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
//                                        System.out.println("found");
                                        break;
                                    }
                                } else {
                                    if (!net.getPathNodes().contains(n)) {
                                        n.setPathCost(m.getPathCost() + cost(n, m));
                                    }
                                    pQueue.add(n);
                                }
                            }
                        }
                    }
                    //Back trace
                    PFNode backNode = sink.getPrev();
                    //sink.occupy();
                    PFNode lastChannel = sink.getPrev().getPrev();

                    while (!backNode.equals(source)) {
                        if (!net.getPathNodes().contains(backNode)) {
                            net.addNode(backNode);
                            backNode.occupy();
                        }
                        backNode = backNode.getPrev();
                    }
                    //System.out.println("fakeLastChan: " + net.getPathNodes().get(1).getID());
                    //System.out.println("sinkID: " + sink.getID());
                    //Wire arrangement
                    //Find the target switch
                    if (firstSink) {
                        firstSink = false;
                        LinkedList<PFNode> path = net.getPathNodes();
                        PFNode firstChannel = path.get(path.size() - 2);
                        for (UIWire wire : firstChannel.getWires()) {
                            if (wire.getAvailableSink() != null) {
                                boolean inCapMatch = firstChannel.inCapacity()
                                        && wire.getAvailableSink().equals(net.getSource()) && wire.getTargetNets().isEmpty();
                                boolean outOfCapMatch = !firstChannel.inCapacity()
                                        && wire.getAvailableSink().equals(net.getSource());
                                if (inCapMatch || outOfCapMatch) {
                                    wire.addTargetNet(net);
                                    wire.setSwOn(true);
                                    break;
                                }
                            }
                        }
                    }
                    //find the sink switch
                    UIWire tempWire = null;
                    boolean added = false;
                    int useCount = 0;
                    for (UIWire wire : lastChannel.getWires()) {
                        if (wire.getAvailableSink() != null) {
                            if(wire.getAvailableSink().equals(sink))tempWire = wire;
                            boolean inCapMatch = lastChannel.inCapacity() && wire.getAvailableSink().equals(sink) && wire.getTargetNets().isEmpty();
//                            boolean outOfCapMatch = !lastChannel.inCapacity()
//                                    && wire.getAvailableSink().equals(sink);

                            //System.out.println("avaSink: " + wire.getAvailableSink().getID());
                            //System.out.println("inCap: " + inCapMatch);
                            //System.out.println("outCap: " + outOfCapMatch);
                            if (inCapMatch /*|| outOfCapMatch*/) {
                                //System.out.println("math sink, sinkID: " + sink.getID() + " channelID: " + lastChannel.getID() + "wireIndex: " + lastChannel.getWires().indexOf(wire));
                                wire.addTargetNet(net);
                                wire.setSwOn(true);
                                added = true;
                                break;
                            }
                        }
                    }
                    if(!added) tempWire.addTargetNet(net);
                    for (UIWire wire : lastChannel.getWires()) {
                        if (wire.getTargetNets().contains(net)) {
                            useCount++;
                        }
                    }
                    if (useCount > 1) {
                        lastChannel.occupy();
                        System.out.println("added extra occupancy" + lastChannel.getID());
                    }
                    //Wire arrangment done
                }
            }
        }
    }

    private void resetColor(boolean resetHeatMap) {
        for (PFNet net : nets) {

            for (UIWire wire : net.getPathWires()) {
                wire.resetColor();
            }
            for (PFNode node : net.getPathNodes()) {
                node.getBlock().getDot().resetColor();
                if ((node.getBlock().getDot().getType().equals(IP) || node.getBlock().getDot().getType().equals(CH)) && !resetHeatMap) {
                    node.getBlock().getDot().setColor(new Color(255, 153, 51, (int) (node.getHVal() / maxHVal * 255)));
                }
            }
        }
    }

    public boolean isPause() {
        return pause;
    }

    public void setPause(boolean pause) {
        this.pause = pause;
    }

//    public boolean isStep() {
//        return step;
//    }
//
//    public void setStep(boolean step) {
//        this.step = step;
//    }

    public void resetAll() {
        System.out.println("reset");
        legal = false;
        stop = true;
        routingTimer.stop();
        resetColor(true);
        for (PFNode n : nodes) {
            n.clearStats(true);
            n.resetWires();
            n.getBlock().getDot().resetColor();
        }
        for (PFNet net : nets) {
            if (!net.equals(ghostNet)) {
                net.clearWires();
                net.clearPath();
            }
        }

        nets.clear();
        iteration = 1;
        graph.repaint();

    }

    public void restartReset() {
        System.out.println("reset");
        legal = false;
        stop = false;
        done = true;
        resetColor(true);
        for (PFNode n : nodes) {
            n.clearStats(true);
            n.resetWires();
            n.getBlock().getDot().resetColor();
        }
        for (PFNet net : nets) {
            if (!net.equals(ghostNet)) {
                net.clearWires();
                net.clearPath();
            }
        }

//        nets.clear();
        pFac = 0.5;
        iteration = 1;
        maxHVal = 25;
        maxPenalty = 1;
        graph.setIteration(1);
        graph.setMaxHVal(maxHVal);
        graph.setMaxPenalty(maxPenalty);
        graph.repaint();

    }

    public CopyOnWriteArrayList<PFNet> getNets() {
        return nets;
    }

    public void setNets(CopyOnWriteArrayList<PFNet> nets) {
        firstNet = nets.get(0);
        this.nets = nets;
        nets.add(ghostNet);
        iterator = nets.iterator();
        selNet = (PFNet) iterator.next();
    }


    public Timer getRoutingTimer() {
        return routingTimer;
    }
   
}

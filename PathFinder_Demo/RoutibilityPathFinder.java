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
import java.util.LinkedList;
import java.util.PriorityQueue;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 *
 * @author 15002
 */
public class RoutibilityPathFinder {

    public static final String SW = "SW";
    public static final String SB = "SB";
    public static final String LB = "LB";
    public static final String OP = "OP";
    public static final String IP = "IP";
    public static final String TM = "TM";
    public static final String CH = "CH";

    private double maxPenalty = 1;
    private double maxHVal = 2;
    private boolean legal = false;
    private double pFac = 0.5;//increase by 1.5 to 2 times each iteration
    //for easy ones, pfac = 10000 at first
    private double hFac = 0.5;//remain constant
    private int iteration = 1;
    private int threshold = 45;
    private LinkedList<PFNet> nets;
    private LinkedList<PFNode> nodes;
    private boolean pause;
    private boolean step;
    private PFNet selNet;
    private boolean done;
    private PFNet ghostNet = new PFNet(null, null);

    UIGraph graph;
    private Timer routingTimer = new Timer(400, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            netIterate(selNet);
        }
    });

    public RoutibilityPathFinder(LinkedList<PFNet> nets, LinkedList<PFNode> nodes, UIGraph g) {
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
            //System.out.println("bend"+n.getID()+" "+m.getID());
            return 1;
        } else {
            return 0;
        }
    }

    public void iterate() {
        System.out.println("real route");
        resetColor();
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
        for (PFNet net : nets) {
            selNet = net;
            while (!done) {
                if (!routingTimer.isRunning() && !pause) {
                    if (!step) {
                        routingTimer.setInitialDelay(1000);
                    }
                    System.out.println("Start");
                    routingTimer.start();
                    if (step) {
                        pause = true;
                        step = false;
                    }
                }
            }
            done = false;
            for (PFNode n : nodes) {
//                System.out.println(n.getID() + "NODE" + n.getOccupied());
                //oveeruse heatmap

                if ((n.getBlock().getDot().getType().equals(IP)) || (n.getBlock().getDot().getType().equals(CH))) {
                    if (!n.inCapacity()) {
                        double costN = heatMapVal(n);
                        if (costN > maxPenalty) {
                            maxPenalty = costN + 2;
                            graph.setMaxPenalty(maxPenalty);
                            System.out.println(costN);
                        }
                        n.getBlock().getDot().setEdgeColor(new Color(255, 0, 0, (int) (costN / maxPenalty * 255)));
                        System.out.println(n.getID() + "is illegal " + n.getType());
                    }
                    double hVal = n.getHVal();
                    if(hVal>maxHVal){
                        maxHVal = hVal + 1;
                        graph.setMaxHVal(maxHVal);
                        System.out.println(maxHVal);
                    }
                    n.getBlock().getDot().setColor(new Color(255, 153, 51, (int) (n.getHVal() / maxHVal * 255)));
                    graph.repaint();
                }
//                        else if () {
//                        double costN = heatMapVal(n);
//                        if (costN > maxPenalty) {
//                            maxPenalty = costN + 2;
//                            graph.setMaxPenalty(maxPenalty);
//                        }
//                        n.getBlock().getDot().setColor(new Color(255, 0, 0, (int) (costN / maxPenalty * 255)));
//                    }
                

            }
            graph.repaint();
        }
        iteration++;
        pFac *= 1.5;
        boolean pass = true;
        for (PFNode n : nodes) {
            if (!n.inCapacity()) {
                pass = false;
                System.out.println(n.getID() + "is illegal " + n.getType());
            }

        }
        legal = pass;

        //graph.repaint();
    }

    private void netIterate(PFNet net) {
        if (!net.equals(ghostNet)) {

            //set a color to each net when the net is created
            //net.setColor(ColorSequencer.next());
            PFNode source = net.getSource();
            //System.out.println("sourceID: " + source.getID());
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

//                            if(m.getID()==33)System.out.println("leave ID: "+n.getID());
                        if (n.getPrev() == null) {
                            n.setPrev(m);
                            if (n.equals(sink)) {

                                if (m.getPrev().getPrev().getBlock().getDot().getType().equals("OP")) {
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
        done = true;
        routingTimer.stop();

    }

    public boolean route() {
        iteration = 1;
        //routingTimer.setRepeats(true);
        //routingTimer.start();
        boolean run = true;
        //routingTimer.start();
        while (!legal) {
            if (iteration == threshold) {
                break;
            }
//            if(legal){
//                run = false;
//                break;
//            }

            iterate();
            //System.out.println(run);

        }
        routingTimer.stop();
        if (legal) {
            System.out.println("Succeed");
        } else {
            System.out.println("Fail");
            System.out.println(iteration);
        }
        return legal;
    }

    private void backTrace(PFNode sink, PFNode source, PFNet net) {
        //Back Trace
        PFNode backNode = sink;
        PFNode lastChannel = sink.getPrev().getPrev();
        boolean addLast = false;
        int index = 0;
//        for (UIWire wire : lastChannel.getWires()) {
//            System.out.println("try");
//            if (wire.getTargetNets().contains(net) && wire.getAvailableSink().equals(sink)&&net.getPathWires().contains(wire)) {
//                System.out.println("NET5currSink: " + sink.getID()+"yes"+index+"avaSink"+wire.getAvailableSink().getID());
//                addLast = true;
//                break;
//            }
//            index++;
//        }
//        if (!addLast) {
//            lastChannel.occupy();
//            System.out.println("no");
//        }
//        if (lastChannel.getID() == 30) {
//            System.out.println("NET5currSink: " + sink.getID() + "currOccu: " + lastChannel.getOccupied());
//        }
        while (!backNode.equals(source)) {
            //System.out.println("trace" + backNode.getID());
            if (!net.getPathNodes().contains(backNode)) {
                //Trace back a node
                PFNode prev = backNode.getPrev();
                net.addNode(backNode);
                if (!backNode.equals(sink)) {
                    if (!backNode.getType().equals(CH)) {
                        backNode.occupy();
                    }
//                    if (backNode.getID() == 30) {
//                        System.out.println("currSink: " + sink.getID() + "currOccu: " + backNode.getOccupied());
//                    }
                }
                //Choose a wire segment in the channel
                if (!backNode.getWires().isEmpty()) {
//                    lastChan = backNode;
                    //System.out.println("ChannelID: " + backNode.getID());
//                    UIWire lastWire = net.getPathWires().getLast();
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
                                        System.out.println("found");
                                        break;
                                    }
                                } else {
                                    if (!net.getPathNodes().contains(n)) {
                                        n.setPathCost(m.getPathCost() + this.cost(n, m));
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
                        //if(backNode.getID()==25){
                        //System.out.println("current Prev: "+backNode.getPrev().getID());
                        //}
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
                    //find the sink swith
                    int useCount = 0;
                    for (UIWire wire : lastChannel.getWires()) {
                        if (wire.getTargetNets().contains(net)) {
                            useCount++;
                        }
                        if (wire.getAvailableSink() != null) {
                            boolean inCapMatch = lastChannel.inCapacity() && wire.getAvailableSink().equals(sink) && wire.getTargetNets().isEmpty();
                            boolean outOfCapMatch = !lastChannel.inCapacity()
                                    && wire.getAvailableSink().equals(sink);

                            //System.out.println("avaSink: " + wire.getAvailableSink().getID());
                            //System.out.println("inCap: " + inCapMatch);
                            //System.out.println("outCap: " + outOfCapMatch);
                            if (inCapMatch || outOfCapMatch) {
                                //System.out.println("math sink, sinkID: " + sink.getID() + " channelID: " + lastChannel.getID() + "wireIndex: " + lastChannel.getWires().indexOf(wire));
                                wire.addTargetNet(net);
                                wire.setSwOn(true);
                                useCount++;
                                break;
                            }
                        }
                    }
                    if (useCount > 1) {
                        lastChannel.occupy();
                    }
                    if (lastChannel.getID() == 30) {
                        System.out.println("NET5currSink: " + sink.getID() + "currOccu: " + lastChannel.getOccupied());
                    }
                    //Wire arrangment done
                }
            }
        }
    }

    private void wireArrange() {
////        System.out.println("arrange");
//        for (PFNet net : nets) {
//            System.out.println("one net");
//            LinkedList<PFNode> path = net.getPathNodes();
//            PFNode lastChannel = path.get(1);
//            PFNode firstChannel = path.get(path.size() - 2);
//            System.out.println("lastChan ID: " + lastChannel.getID());
//            for (PFNode sink : net.getSinks()) {
//                int i = -1;
//                for (UIWire wire : lastChannel.getWires()) {
//                    if (wire.getAvailableSink() != null) {
//                        boolean inCapMatch = lastChannel.inCapacity() && wire.getAvailableSink().equals(sink) && !wire.isOccupied();
//                        boolean outOfCapMatch = !lastChannel.inCapacity()
//                                && wire.getAvailableSink().equals(sink);
//
//                        //System.out.println("avaSink: " + wire.getAvailableSink().getID());
//                        //System.out.println("inCap: " + inCapMatch);
//                        //System.out.println("outCap: " + outOfCapMatch);
//                        if (inCapMatch || outOfCapMatch) {
//                            //System.out.println("math sink, sinkID: " + sink.getID() + " channelID: " + lastChannel.getID() + "wireIndex: " + lastChannel.getWires().indexOf(wire));
//                            wire.setTargetNet(net);
//                            wire.setOccupied(true);
//                            break;
//                        }
//                    }
//                }
//            }
//            for (UIWire wire : firstChannel.getWires()) {
//                if (wire.getAvailableSink() != null) {
//                    boolean inCapMatch = firstChannel.inCapacity()
//                            && wire.getAvailableSink().equals(net.getSource()) && !wire.isOccupied();
//                    boolean outOfCapMatch = !firstChannel.inCapacity()
//                            && wire.getAvailableSink().equals(net.getSource());
//                    if (inCapMatch || outOfCapMatch) {
//                        wire.setTargetNet(net);
//                        wire.setOccupied(true);
//                        break;
//                    }
//                }
//            }
//            //System.out.println();
////            net.clearPath();
//        }
//        //System.out.println("done arrange");
    }

    private void resetColor() {
        for (PFNet net : nets) {

            for (UIWire wire : net.getPathWires()) {
                wire.resetColor();
            }
            for (PFNode node : net.getPathNodes()) {
                node.getBlock().getDot().resetColor();
                if (node.getBlock().getDot().getType().equals(IP)||node.getBlock().getDot().getType().equals(CH)) {
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

    public boolean isStep() {
        return step;
    }

    public void setStep(boolean step) {
        this.step = step;
    }

    public void resetAll() {
        System.out.println("reset");
        resetColor();
        for (PFNet net : nets) {
            net.clearNodes(true);
            net.resetChannels();
            net.clearWires();
            net.clearPath();
        }

        nets.clear();
        iteration = 1;
    }

    public LinkedList<PFNet> getNets() {
        return nets;
    }

    public void setNets(LinkedList<PFNet> nets) {
        this.nets = nets;
        nets.addLast(ghostNet);
    }

}

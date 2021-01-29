/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pathfinder_demo;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JFrame;
import static pathfinder_demo.UIGraph.IP;
import static pathfinder_demo.UIGraph.LB;
import static pathfinder_demo.UIGraph.OP;
import static pathfinder_demo.UIGraph.SB;
import static pathfinder_demo.UIGraph.SW;
import static pathfinder_demo.UIGraph.TM;
import java.util.concurrent.CopyOnWriteArrayList;
import static pathfinder_demo.UIGraph.IP;
import static pathfinder_demo.UIGraph.SW;
import javax.swing.JPanel;

/**
 *
 * @author 15002
 */
public class PathFinder_demo  {


    private UIGraph u = new UIGraph();
    private PFGraph pfG = new PFGraph();
    

    public PathFinder_demo(int w, int h) {
        //addMouseListener(this);
        SwitchBlock[][] sbArr = new SwitchBlock[w][h];
        for (int i = 0; i < h; i++) {
            int sbY = 45 + 270 * i;
            for (int j = 0; j < w; j++) {
                int sbX = 45 + 270 * j;
                SwitchBlock sb = new SwitchBlock(new UIDot(new Point(sbX, sbY), SB, 90));
                u.addToBottom(sb);
                sbArr[j][i] = sb;
            }
        }

        //Construction logic blocks
        LogicBlock[][] lbArr = new LogicBlock[w - 1][h - 1];
        for (int i = 0; i < h - 1; i++) {
            int lbY = 180 + 270 * i;
            for (int j = 0; j < w - 1; j++) {
                int lbX = 180 + 270 * j;
                LogicBlock lb = new LogicBlock(new UIDot(new Point(lbX, lbY), LB, 90));
                u.addToBottom(lb);
                lbArr[j][i] = lb;
                PFNode source = new PFNode(2);
                PFNode sink = new PFNode(2);
                lb.setSource(source);
                lb.setSink(sink);
                pfG.addNode(sink);
                pfG.addNode(source);
            }
        }

        Channel[][] channelHoriArr = new Channel[w - 1][h];
        for (int j = 0; j < h; j++) {
            int baseY = 270 * j;
            for (int i = 0; i < w - 1; i++) {
                int opX = 90 + 15 / 2 + i * 270;
                Channel c = new Channel();
                PFNode cN = new PFNode(4);
                c.setNode(cN);
                pfG.addNode(cN);
                for (int k = 0; k < 4; k++) {
                    int opY = 15 / 2 + k * (15 + 10) + baseY;
                    Terminal a = new Terminal(new UIDot(new Point(opX, opY), TM, 15));
                    Terminal b = new Terminal(new UIDot(new Point(opX + 180 - 15, opY), TM, 15));
                    u.addToTop(a);
                    u.addToTop(b);
                    UIWire wire = new UIWire(a.getDot(), b.getDot());
                    u.addWire(wire);
                    a.setWireLoc(wire.getLocA());
                    b.setWireLoc(wire.getLocB());
                    c.addWire(wire);
                    a.setChannel(c);
                    b.setChannel(c);
                }
                channelHoriArr[i][j] = c;
                sbArr[i][j].addChannel(c);
                sbArr[i + 1][j].addChannel(c);
            }
        }

        Channel[][] channelVertArr = new Channel[w][h - 1];
        for (int j = 0; j < w; j++) {
            int baseX = 270 * j;
            for (int i = 0; i < h - 1; i++) {
                int opY = 90 + 15 / 2 + i * 270;
                Channel c = new Channel();
                PFNode cN = new PFNode(4);
                c.setNode(cN);
                pfG.addNode(cN);
                for (int k = 0; k < 4; k++) {
                    int opX = 15 / 2 + k * (15 + 10) + baseX;
                    Terminal a = new Terminal(new UIDot(new Point(opX, opY), TM, 15));
                    Terminal b = new Terminal(new UIDot(new Point(opX, opY + 180 - 15), TM, 15));
                    u.addToTop(a);
                    u.addToTop(b);
                    UIWire wire = new UIWire(a.getDot(), b.getDot());
                    u.addWire(wire);
                    a.setWireLoc(wire.getLocA());
                    b.setWireLoc(wire.getLocB());
                    c.addWire(wire);
                    a.setChannel(c);
                    b.setChannel(c);
                }
                channelVertArr[j][i] = c;
                sbArr[j][i].addChannel(c);
                sbArr[j][i + 1].addChannel(c);
            }
        }

        //Construct edges between channels
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                CopyOnWriteArrayList<Channel> channels = sbArr[i][j].getChannels();
//                System.out.println("sw:" + i + ", " + j + ", num of chan: " + channels.size());
                for (int x = 0; x < channels.size(); x++) {
                    for (int y = x + 1; y < channels.size(); y++) {
                        Channel channel = channels.get(x);
                        Channel channel2 = channels.get(y);
                        if (!channel.equals(channel2)) {
                            PFEdge edge1 = new PFEdge(channel.getNode(), channel2.getNode());
                            PFEdge edge2 = new PFEdge(channel2.getNode(), channel.getNode());
                            channel.getNode().addEdge(edge1);
                            channel2.getNode().addEdge(edge2);
                            sbArr[i][j].addEdge(edge1);
                            sbArr[i][j].addEdge(edge2);
                        }
                    }
                }
//                for (Channel channel : channels) {
//                    System.out.println("num of edges: " + channel.getNode().getEdges().size());
//                }
            }
        }

//        for (int i = 0; i < w - 1; i++) {
//            for (int j = 0; j < h; j++) {
//                System.out.println("Horizontal channel " + i + ", " + j
//                        + " has " + channelHoriArr[i][j].getNode().getEdges().size()
//                        + "edges");
//            }
//        }
//
//        for (int i = 0; i < w; i++) {
//            for (int j = 0; j < h - 1; j++) {
//                System.out.println("Vertical channel " + i + ", " + j
//                        + " has " + channelVertArr[i][j].getNode().getEdges().size()
//                        + "edges");
//            }
//        }

        //Construct pins and switches
        //up and down pin and their switches
        for (int i = 0; i < w - 1; i++) {
            int opX = 270 * i + 45 + 90 + 15 / 2;
            for (int j = 0; j < h - 1; j++) {
                int opY = 270 * j + 90 + 30 + 15 / 2;
                UIDot a = new UIDot(new Point(opX, opY), IP, 15);
                UIDot b = new UIDot(new Point(opX + 90 - 15, opY + 90 + 15), IP, 15);
                UIDot saA = new UIDot(new Point(opX, opY - 45), SW, 15);
                UIDot saB = new UIDot(new Point(opX, opY - 45 - 50), SW, 15);
                UIDot sbA = new UIDot(new Point(opX + 90 - 15, opY + 90 + 15 + 45), SW, 15);
                UIDot sbB = new UIDot(new Point(opX + 90 - 15, opY + 90 + 15 + 45 + 50), SW, 15);

                PFNode upInN = new PFNode(1);
                PFNode downInN = new PFNode(1);
                pfG.addNode(upInN);
                pfG.addNode(downInN);

                Switch swaA = new Switch(saA);
                Switch swaB = new Switch(saB);
                Switch swbA = new Switch(sbA);
                Switch swbB = new Switch(sbB);
                InPin upIn = new InPin(a, upInN);
                InPin downIn = new InPin(b, downInN);
                lbArr[i][j].addIn(upIn);
                lbArr[i][j].addIn(downIn);

                u.addToTop(upIn);
                u.addToTop(downIn);
                u.addToTop(swaA);
                u.addToTop(swaB);
                u.addToTop(swbA);
                u.addToTop(swbB);
                u.addWire(new UIWire(saA, a));
                u.addWire(new UIWire(saA, saB));
                u.addWire(new UIWire(sbA, b));
                u.addWire(new UIWire(sbA, sbB));

                PFEdge upSWToUpIn = new PFEdge(channelHoriArr[i][j].getNode(), upIn.getNode());
                PFEdge downSWToDownIn = new PFEdge(channelHoriArr[i][j + 1].getNode(), downIn.getNode());
                PFEdge upInToSink = new PFEdge(upIn.getNode(), lbArr[i][j].getSink());
                PFEdge downInToSink = new PFEdge(downIn.getNode(), lbArr[i][j].getSink());
                pfG.addEdge(upSWToUpIn);
                pfG.addEdge(downSWToDownIn);
                pfG.addEdge(upInToSink);
                pfG.addEdge(downInToSink);
                upIn.getNode().addEdge(upInToSink);
                downIn.getNode().addEdge(downInToSink);
                channelHoriArr[i][j].getNode().addEdge(upSWToUpIn);
                channelHoriArr[i][j + 1].getNode().addEdge(downSWToDownIn);
                swaA.setEdge(upSWToUpIn);
                swaB.setEdge(upSWToUpIn);
                swbA.setEdge(downSWToDownIn);
                swbB.setEdge(downSWToDownIn);
            }
        }

        //left and right pin and their switches
        for (int i = 0; i < h - 1; i++) {
            int opY = 270 * i + 45 + 90 + 15 / 2;
            for (int j = 0; j < w - 1; j++) {
                int opX = 270 * j + 90 + 30 + 15 / 2;
                UIDot a = new UIDot(new Point(opX, opY + 90 - 15), IP, 15);
                UIDot b = new UIDot(new Point(opX + 90 + 15, opY), OP, 15);
                UIDot saA = new UIDot(new Point(opX - 45, opY + 90 - 15), SW, 15);
                UIDot saB = new UIDot(new Point(opX - 45 - 50, opY + 90 - 15), SW, 15);
                UIDot sbA = new UIDot(new Point(opX + 90 + 15 + 45, opY), SW, 15);
                UIDot sbB = new UIDot(new Point(opX + 90 + 15 + 45 + 50, opY), SW, 15);

                PFNode leftInN = new PFNode(1);
                PFNode rightOutN = new PFNode(1);
                pfG.addNode(leftInN);
                pfG.addNode(rightOutN);

                Switch swaA = new Switch(saA);
                Switch swaB = new Switch(saB);
                Switch swbA = new Switch(sbA);
                Switch swbB = new Switch(sbB);
                InPin leftIn = new InPin(a, leftInN);
                OutPin rightOut = new OutPin(b, rightOutN);
                lbArr[j][i].addIn(leftIn);
                lbArr[j][i].addOut(rightOut);

                u.addToTop(leftIn);
                u.addToTop(rightOut);
                u.addToTop(swaA);
                u.addToTop(swaB);
                u.addToTop(swbA);
                u.addToTop(swbB);
                u.addWire(new UIWire(saA, a));
                u.addWire(new UIWire(saA, saB));
                u.addWire(new UIWire(sbA, b));
                u.addWire(new UIWire(sbA, sbB));
                
                PFEdge leftSWToLeftIn = new PFEdge(channelVertArr[j][i].getNode(), leftIn.getNode());
                PFEdge rightOutToRightSW = new PFEdge(rightOut.getNode(), channelVertArr[j + 1][i].getNode());
                PFEdge leftInToSink = new PFEdge(leftIn.getNode(), lbArr[j][i].getSink());
                PFEdge sourceToRightOut = new PFEdge(lbArr[j][i].getSource(), rightOut.getNode());
                pfG.addEdge(leftSWToLeftIn);
                pfG.addEdge(rightOutToRightSW);
                pfG.addEdge(leftInToSink);
                pfG.addEdge(sourceToRightOut);
                leftIn.getNode().addEdge(leftInToSink);
                rightOut.getNode().addEdge(rightOutToRightSW);
                channelVertArr[j][i].getNode().addEdge(leftSWToLeftIn);
                lbArr[j][i].getSource().addEdge(sourceToRightOut);
                swaA.setEdge(leftSWToLeftIn);
                swaB.setEdge(leftSWToLeftIn);
                swbA.setEdge(rightOutToRightSW);
                swbB.setEdge(rightOutToRightSW);
            }
        }
    }

    public UIGraph getUIG() {
        return u;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        PathFinder_demo demo = new PathFinder_demo(4, 4);

        JFrame f = new JFrame();
        f.add(demo.getUIG());
        //f.add(demo);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(1000, 1000);
        f.setVisible(true);
    }


}

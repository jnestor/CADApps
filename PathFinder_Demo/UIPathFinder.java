/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pathfinder_demo;

import java.awt.Color;
import java.awt.Point;
import java.util.LinkedList;

/**
 *
 * @author 15002
 */
public class UIPathFinder {

    public static final String SW = "SW";
    public static final String LB = "LB";
    public static final String OP = "OP";
    public static final String IP = "IP";
    public static final String TM = "TM";
    public static final String SB = "SB";
    public static final String CH = "CH";
    private int blockSize = 50;
    private int wireLength = 120;
    private int tmSize = 11;
    private int wireDis = (blockSize-tmSize*4)/3;
   
    int twStroke = 3;
    private UIGraph u = new UIGraph();

    private LinkedList<PFNode> nodes = new LinkedList<PFNode>();
    PFNode[][] sources;
    PFNode[][] sinks;
    PFNode[][] chanVer;
    PFNode[][] chanHori;
    int w;
    int h;
    protected int size = 1;

    public UIPathFinder(int w, int h) {
        //Construct switchBlocks
        UIBlock[][] sbArr = new UIBlock[w][h];
        for (int i = 0; i < h; i++) {
            int sbY = (blockSize/2 + (blockSize+wireLength) * i) * size;
            for (int j = 0; j < w; j++) {
                int sbX = blockSize/2 + (blockSize+wireLength) * j;
                UIBlock sb = new UIBlock(new UIDot(new Point(sbX, sbY), SB, blockSize,blockSize, false));
                u.addToBottom(sb);
                sbArr[j][i] = sb;
            }
        }
        u.setHeatMap((blockSize/2 + (blockSize+wireLength) * (w - 1) + blockSize), 45, 30, 180);

        //Construct logic blocks
        PFNode[][] sourceArr = new PFNode[w - 1][h - 1];
        PFNode[][] sinkArr = new PFNode[w - 1][h - 1];
        for (int i = 0; i < h - 1; i++) {
            int lbY = blockSize+wireLength/2 + (blockSize+wireLength) * i;
            for (int j = 0; j < w - 1; j++) {
                int lbX = blockSize+wireLength/2 + (blockSize+wireLength) * j;
                UIBlock lb = new UIBlock(new UIDot(new Point(lbX, lbY), LB, blockSize,blockSize, false));
                u.addToBottom(lb);
                PFNode source = new PFNode(1, 1, lb);
                PFNode sink = new PFNode(3, 0, lb);
                nodes.add(sink);
                nodes.add(source);
                sourceArr[j][i] = source;
                sinkArr[j][i] = sink;
                u.addSource(source);
                u.addSink(sink);
            }
        }
        sources = sourceArr;
        sinks = sinkArr;

        //Construct horizontal channels
        PFNode[][] chanNodeHoriArr = new PFNode[w - 1][h];
        for (int j = 0; j < h; j++) {
            int baseY = (blockSize+wireLength) * j;
            for (int i = 0; i < w - 1; i++) {
                int opX = blockSize + tmSize / 2 + i * (blockSize+wireLength);
                UIBlock c = new UIBlock(new UIDot(new Point(opX - tmSize / 2 + (blockSize+wireLength) / 2 - blockSize/2, baseY + blockSize/2), CH, wireLength,blockSize, false));
                u.addChan(c);
                for (int k = 0; k < 4; k++) {
                    int opY = tmSize / 2 + k * (tmSize + wireDis) + baseY;
                    UIBlock a = new UIBlock(new UIDot(new Point(opX, opY), TM, tmSize,tmSize, false));
                    UIBlock b = new UIBlock(new UIDot(new Point(opX + wireLength - tmSize, opY), TM, tmSize,tmSize, false));
                    u.addToTop(a);
                    u.addToTop(b);
                    UIWire wire = new UIWire(a, b);
                    wire.setColor(Color.black);
                    u.addWire(wire);
                    a.setWireLoc(wire.getLocA());
                    b.setWireLoc(wire.getLocB());
                    c.addWire(wire);
                }
                PFNode chan = new PFNode(4, 1, c);
                nodes.add(chan);
                chanNodeHoriArr[i][j] = chan;
            }
        }

        //Construct vertical channels
        PFNode[][] chanNodeVertArr = new PFNode[w][h - 1];
        for (int j = 0; j < w; j++) {
            int baseX = (blockSize+wireLength) * j;
            for (int i = 0; i < h - 1; i++) {
                int opY = blockSize + tmSize / 2 + i * (blockSize+wireLength);
                UIBlock c = new UIBlock(new UIDot(new Point(baseX + blockSize/2, opY - tmSize / 2 + (blockSize+wireLength) / 2 - blockSize/2), CH, blockSize,wireLength, true));
                u.addChan(c);
                for (int k = 0; k < 4; k++) {
                    int opX = tmSize / 2 + k * (tmSize + wireDis) + baseX;
                    UIBlock a = new UIBlock(new UIDot(new Point(opX, opY), TM, tmSize,tmSize, true));
                    UIBlock b = new UIBlock(new UIDot(new Point(opX, opY + wireLength - tmSize), TM, tmSize,tmSize, true));
                    u.addToTop(a);
                    u.addToTop(b);
                    UIWire wire = new UIWire(a, b);
                    wire.setColor(Color.black);
                    u.addWire(wire);
                    a.setWireLoc(wire.getLocA());
                    b.setWireLoc(wire.getLocB());
                    c.addWire(wire);
                }
                PFNode chan = new PFNode(4, 1, c);
                nodes.add(chan);
                chanNodeVertArr[j][i] = chan;
            }
        }

        PFNode[][] upInArr = new PFNode[w - 1][h - 1];
        PFNode[][] downInArr = new PFNode[w - 1][h - 1];
        UIWire[][] upSwWireArr = new UIWire[w - 1][h - 1];
        UIWire[][] downSwWireArr = new UIWire[w - 1][h - 1];
        //Construct pins and switches
        //up and down pin and their switches
        for (int i = 0; i < w - 1; i++) {
            int opX = (blockSize+wireLength) * i + (wireLength/2-blockSize/2) + blockSize + tmSize / 2;
            for (int j = 0; j < h - 1; j++) {
                int opY = (blockSize+wireLength) * j + blockSize + wireLength/2-blockSize/2- tmSize / 2;
                UIDot upDot = new UIDot(new Point(opX, opY-1), IP, tmSize,tmSize, false);
                UIDot downDot = new UIDot(new Point(opX + blockSize - tmSize, opY + blockSize + tmSize), IP, tmSize,tmSize, false);
                UIDot upSWLowDot = new UIDot(new Point(opX, opY - (wireLength/2-blockSize/2)), SW, tmSize,tmSize, false);
                UIDot upSWHiDot = new UIDot(new Point(opX, opY - (wireLength/2-blockSize/2)- wireDis*2-tmSize*2), SW, tmSize,tmSize, false);
                UIDot downSWHiDot = new UIDot(new Point(opX + blockSize - tmSize, opY + blockSize + tmSize + (wireLength/2-blockSize/2)), SW, tmSize,tmSize, false);
                UIDot downSWLowDot = new UIDot(new Point(opX + blockSize - tmSize, opY + blockSize + tmSize + (wireLength/2-blockSize/2) + wireDis*2+tmSize*2), SW, tmSize,tmSize, false);

                UIBlock upSWLow = new UIBlock(upSWLowDot);
                UIBlock upSWHi = new UIBlock(upSWHiDot);
                UIBlock downSWHi = new UIBlock(downSWHiDot);
                UIBlock downSWLow = new UIBlock(downSWLowDot);
                UIBlock upIn = new UIBlock(upDot);
                UIBlock downIn = new UIBlock(downDot);
                chanNodeHoriArr[i][j].getWires().get(1).setSwBlock(upSWHi);
                chanNodeHoriArr[i][j].getWires().get(3).setSwBlock(upSWLow);
                chanNodeHoriArr[i][j + 1].getWires().get(0).setSwBlock(downSWHi);
                chanNodeHoriArr[i][j + 1].getWires().get(2).setSwBlock(downSWLow);

                UIWire upWire = new UIWire(upSWHi, upIn);
                UIWire downWire = new UIWire(downIn, downSWLow);
                upWire.setColor(Color.black);
                downWire.setColor(Color.black);

                upInArr[i][j] = new PFNode(1, 0.95, upIn);
                downInArr[i][j] = new PFNode(1, 0.95, downIn);
                nodes.add(upInArr[i][j]);
                nodes.add(downInArr[i][j]);
                upSwWireArr[i][j] = upWire;
                downSwWireArr[i][j] = downWire;

                u.addToTop(upIn);
                u.addToTop(downIn);
                u.addToTop(upSWLow);
                u.addToTop(upSWHi);
                u.addToTop(downSWHi);
                u.addToTop(downSWLow);
                u.addWire(upWire);
                u.addWire(downWire);
            }
        }

        //left and right pin and their switches
        PFNode[][] leftInArr = new PFNode[w - 1][h - 1];
        PFNode[][] rightOutArr = new PFNode[w - 1][h - 1];
        UIWire[][] leftSwWireArr = new UIWire[w - 1][h - 1];
        UIWire[][] rightSwWireArr = new UIWire[w - 1][h - 1];
        for (int i = 0; i < h - 1; i++) {
            int opY = (blockSize+wireLength)* i + (wireLength/2-blockSize/2) + blockSize + tmSize / 2;
            for (int j = 0; j < w - 1; j++) {
                int opX = (blockSize+wireLength) * j + blockSize + (wireLength/2-blockSize/2)-tmSize/2-1;
                UIDot leftDot = new UIDot(new Point(opX, opY + blockSize - tmSize), IP, tmSize,tmSize, false);
                UIDot rightDot = new UIDot(new Point(opX + blockSize + tmSize, opY), OP, tmSize,tmSize, false);
                UIDot leftSWCloseDot = new UIDot(new Point(opX - (wireLength/2-blockSize/2), opY + blockSize - tmSize), SW, tmSize,tmSize, false);
                UIDot leftSWFarDot = new UIDot(new Point(opX - (wireLength/2-blockSize/2)- wireDis*2-tmSize*2, opY + blockSize - tmSize), SW, tmSize,tmSize, false);
                UIDot rightSWCloseDot = new UIDot(new Point(opX + blockSize + tmSize + (wireLength/2-blockSize/2), opY), SW, tmSize,tmSize, false);
                UIDot rightSWFarDot = new UIDot(new Point(opX + blockSize + tmSize + (wireLength/2-blockSize/2) + wireDis*2+tmSize*2, opY), SW, tmSize,tmSize, false);

                UIBlock leftSWClose = new UIBlock(leftSWCloseDot);
                UIBlock leftSWFar = new UIBlock(leftSWFarDot);
                UIBlock rightSWClose = new UIBlock(rightSWCloseDot);
                UIBlock rightSWFar = new UIBlock(rightSWFarDot);
                UIBlock leftIn = new UIBlock(leftDot);
                UIBlock rightOut = new UIBlock(rightDot);

                chanNodeVertArr[j][i].getWires().get(1).setSwBlock(leftSWFar);
                chanNodeVertArr[j][i].getWires().get(3).setSwBlock(leftSWClose);
                chanNodeVertArr[j + 1][i].getWires().get(0).setSwBlock(rightSWClose);
                chanNodeVertArr[j + 1][i].getWires().get(2).setSwBlock(rightSWFar);

                UIWire leftWire = new UIWire(leftIn, leftSWFar);
                UIWire rightWire = new UIWire(rightOut, rightSWFar);
                leftWire.setColor(Color.black);
                rightWire.setColor(Color.black);

                leftInArr[j][i] = new PFNode(1, 0.95, leftIn);
                rightOutArr[j][i] = new PFNode(1, 1, rightOut);
                nodes.add(leftInArr[j][i]);
                nodes.add(rightOutArr[j][i]);
                leftSwWireArr[j][i] = leftWire;
                rightSwWireArr[j][i] = rightWire;

                u.addToTop(leftIn);
                u.addToTop(rightOut);
                u.addToTop(leftSWFar);
                u.addToTop(leftSWClose);
                u.addToTop(rightSWClose);
                u.addToTop(rightSWFar);
                u.addWire(leftWire);
                u.addWire(rightWire);
            }
        }

        //Construct edges from each logic block to channel
        for (int i = 0; i < w - 1; i++) {
            for (int j = 0; j < h - 1; j++) {
                PFNode source = sourceArr[i][j];

                PFNode outPin = rightOutArr[i][j];
                PFEdge sourceEdge = new PFEdge(source, outPin);
                source.addEdge(sourceEdge);
                PFNode rightChan = chanNodeVertArr[i + 1][j];
                PFEdge outPinEdge = new PFEdge(outPin, rightChan);
                outPinEdge.addWire(rightSwWireArr[i][j]);
                outPin.addEdge(outPinEdge);

                PFNode sink = sinkArr[i][j];

                PFNode leftIn = leftInArr[i][j];
                PFEdge sinkEdgeLeft = new PFEdge(leftIn, sink);
                leftIn.addEdge(sinkEdgeLeft);
                PFNode leftChan = chanNodeVertArr[i][j];
                PFEdge leftInEdge = new PFEdge(leftChan, leftIn);
                leftInEdge.addWire(leftSwWireArr[i][j]);
                leftChan.addEdge(leftInEdge);

                PFNode upIn = upInArr[i][j];
                PFEdge sinkEdgeUp = new PFEdge(upIn, sink);
                upIn.addEdge(sinkEdgeUp);
                PFNode upChan = chanNodeHoriArr[i][j];
                PFEdge upInEdge = new PFEdge(upChan, upIn);
                upInEdge.addWire(upSwWireArr[i][j]);
                upChan.addEdge(upInEdge);

                PFNode downIn = downInArr[i][j];
                PFEdge sinkEdgeDown = new PFEdge(downIn, sink);
                downIn.addEdge(sinkEdgeDown);
                PFNode downChan = chanNodeHoriArr[i][j + 1];
                PFEdge downInEdge = new PFEdge(downChan, downIn);
                downInEdge.addWire(downSwWireArr[i][j]);
                downChan.addEdge(downInEdge);
            }
        }

        
        Color wireC = new Color(0, 0, 0, 0);
        //Construct edges between channels
        for (int i = 0; i < w - 1; i++) {
            for (int j = 0; j < h; j++) {
                PFNode chanHori = chanNodeHoriArr[i][j];

                if (j > 0) {
                    PFNode upL = chanNodeVertArr[i][j - 1];
                    PFNode upR = chanNodeVertArr[i + 1][j - 1];
                    PFEdge chanToUpL = new PFEdge(chanHori, upL);
                    PFEdge upLToChan = new PFEdge(upL, chanHori);
                    PFEdge chanToUpR = new PFEdge(chanHori, upR);
                    PFEdge upRToChan = new PFEdge(upR, chanHori);
                    chanHori.addEdge(chanToUpL);
                    chanHori.addEdge(chanToUpR);
                    upL.addEdge(upLToChan);
                    upR.addEdge(upRToChan);
                    for (UIWire wire : chanHori.getWires()) {
                        //left up channel connection
                        for (UIWire wireL : upL.getWires()) {
                            int x = wire.getAX() - wire.getTerminalA().getWidth() / 2;
                            int y = wire.getAY();
                            Point wirePoint = new Point(x, y);

                            int xL = wireL.getBX();
                            int yL = wireL.getBY() + wireL.getTerminalB().getHeight() / 2;
                            Point wirePointL = new Point(xL, yL);

                            UIWire thinWire = new UIWire(wire.getBlockA(),
                                    wireL.getBlockB(), twStroke,
                                    wirePoint, wirePointL, wireC);
                            chanToUpL.addWire(thinWire);
                            upLToChan.addWire(thinWire);
                            u.addSwWire(thinWire);
                        }
                        //right up channel connection
                        for (UIWire wireR : upR.getWires()) {
                            int x = wire.getBX() + wire.getTerminalB().getWidth() / 2;
                            int y = wire.getBY();
                            Point wirePoint = new Point(x, y);

                            int xR = wireR.getBX();
                            int yR = wireR.getBY() + wireR.getTerminalB().getHeight() / 2;
                            Point wirePointR = new Point(xR, yR);

                            UIWire thinWire = new UIWire(wire.getBlockB(),
                                    wireR.getBlockB(), twStroke,
                                    wirePoint, wirePointR, wireC);
                            chanToUpR.addWire(thinWire);
                            upRToChan.addWire(thinWire);
                            u.addSwWire(thinWire);
                        }
                    }
                }
                if (j < h - 1) {
                    PFNode downL = chanNodeVertArr[i][j];
                    PFNode downR = chanNodeVertArr[i + 1][j];
                    PFEdge chanToDownL = new PFEdge(chanHori, downL);
                    PFEdge downLToChan = new PFEdge(downL, chanHori);
                    PFEdge chanToDownR = new PFEdge(chanHori, downR);
                    PFEdge downRToChan = new PFEdge(downR, chanHori);

                    chanHori.addEdge(chanToDownL);
                    chanHori.addEdge(chanToDownR);
                    downL.addEdge(downLToChan);
                    downR.addEdge(downRToChan);

                    for (UIWire wire : chanHori.getWires()) {
                        //left down channel connection
                        for (UIWire wireL : downL.getWires()) {
                            int x = wire.getAX() - wire.getTerminalA().getWidth() / 2;
                            int y = wire.getAY();
                            Point wirePoint = new Point(x, y);

                            int xL = wireL.getAX();
                            int yL = wireL.getAY() - wireL.getTerminalB().getHeight() / 2;
                            Point wirePointL = new Point(xL, yL);

                            UIWire thinWire = new UIWire(wire.getBlockA(),
                                    wireL.getBlockA(), twStroke,
                                    wirePoint, wirePointL, wireC);
                            chanToDownL.addWire(thinWire);
                            downLToChan.addWire(thinWire);
                            u.addSwWire(thinWire);
                        }
                        //right down channel connection
                        for (UIWire wireR : downR.getWires()) {
                            int x = wire.getBX() + wire.getTerminalB().getWidth() / 2;
                            int y = wire.getBY();
                            Point wirePoint = new Point(x, y);

                            int xR = wireR.getAX();
                            int yR = wireR.getAY() - wireR.getTerminalA().getHeight() / 2;
                            Point wirePointR = new Point(xR, yR);

                            UIWire thinWire = new UIWire(wire.getBlockB(),
                                    wireR.getBlockA(), twStroke,
                                    wirePoint, wirePointR, wireC);
                            chanToDownR.addWire(thinWire);
                            downRToChan.addWire(thinWire);
                            u.addSwWire(thinWire);
                        }
                    }
                }

                if (i > 0) {
                    PFNode left = chanNodeHoriArr[i - 1][j];
                    PFEdge chanToLeft = new PFEdge(chanHori, left);
                    PFEdge leftToChan = new PFEdge(left, chanHori);

                    chanHori.addEdge(chanToLeft);
                    left.addEdge(leftToChan);

                    for (UIWire wire : chanHori.getWires()) {
                        //left channel connection
                        for (UIWire wireL : left.getWires()) {
                            UIWire thinWire = new UIWire(wire.getBlockA(),
                                    wireL.getBlockB(), twStroke,
                                    wire.getLocA(), wireL.getLocB(), wireC);
                            chanToLeft.addWire(thinWire);
                            leftToChan.addWire(thinWire);
                            u.addSwWire(thinWire);
                        }
                    }
                }

                if (i < w - 2) {
                    PFNode right = chanNodeHoriArr[i + 1][j];
                    PFEdge chanToRight = new PFEdge(chanHori, right);
                    PFEdge rightToChan = new PFEdge(right, chanHori);

                    chanHori.addEdge(chanToRight);
                    right.addEdge(rightToChan);

                    for (UIWire wire : chanHori.getWires()) {
                        //left channel connection
                        for (UIWire wireL : right.getWires()) {
                            UIWire thinWire = new UIWire(wire.getBlockB(),
                                    wireL.getBlockA(), twStroke,
                                    wire.getLocB(), wireL.getLocA(), wireC);
                            chanToRight.addWire(thinWire);
                            rightToChan.addWire(thinWire);
                            u.addSwWire(thinWire);
                        }
                    }
                }
            }
        }

        //Connect vertical channels
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h - 1; j++) {
                PFNode chanVert = chanNodeVertArr[i][j];
                if (j > 0) {
                    PFNode up = chanNodeVertArr[i][j - 1];
                    PFEdge chanToUp = new PFEdge(chanVert, up);
                    PFEdge upToChan = new PFEdge(up, chanVert);
                    chanVert.addEdge(chanToUp);
                    up.addEdge(upToChan);

                    for (UIWire wire : chanVert.getWires()) {
                        for (UIWire wireU : up.getWires()) {
                            UIWire thinWire = new UIWire(wire.getBlockA(),
                                    wireU.getBlockB(), twStroke,
                                    wire.getLocA(), wireU.getLocB(), wireC);
                            chanToUp.addWire(thinWire);
                            upToChan.addWire(thinWire);
                            u.addSwWire(thinWire);
                        }
                    }
                }
                if (j < h - 2) {
                    PFNode down = chanNodeVertArr[i][j + 1];
                    PFEdge chanToDown = new PFEdge(chanVert, down);
                    PFEdge downToChan = new PFEdge(down, chanVert);
                    chanVert.addEdge(chanToDown);
                    down.addEdge(downToChan);

                    for (UIWire wire : chanVert.getWires()) {
                        for (UIWire wireU : down.getWires()) {
                            UIWire thinWire = new UIWire(wire.getBlockB(),
                                    wireU.getBlockA(), twStroke,
                                    wire.getLocB(), wireU.getLocA(), wireC);
                            chanToDown.addWire(thinWire);
                            downToChan.addWire(thinWire);
                            u.addSwWire(thinWire);
                        }
                    }
                }
            }
        }

        //Set available sinks
        //for horizontal channels
        for (int i = 0; i < w - 1; i++) {
            for (int j = 0; j < h; j++) {
                for (int k = 0; k < 4; k++) {
                    if ((k + 1) % 2 == 0) {
                        if (j < h - 1) {
                            chanNodeHoriArr[i][j].getWires().
                                    get(k).setAvailableSink(sinkArr[i][j]);
                        }
                    } else {
                        if (j > 0) {
                            chanNodeHoriArr[i][j].getWires().
                                    get(k).setAvailableSink(sinkArr[i][j - 1]);
                        }
                    }
                }
            }
        }

        //for vertical channels
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h - 1; j++) {
                for (int k = 0; k < 4; k++) {
                    if ((k + 1) % 2 == 0) {
                        if (i < w - 1) {
                            chanNodeVertArr[i][j].getWires().
                                    get(k).setAvailableSink(sinkArr[i][j]);
                        }
                    } else {
                        if (i > 0) {
                            chanNodeVertArr[i][j].getWires().
                                    get(k).setAvailableSink(sourceArr[i - 1][j]);
                        }
                    }

                }
            }
        }
        chanVer = chanNodeVertArr;
        chanHori = chanNodeHoriArr;
//        System.out.println("verT: "+ chanNodeVertArr[1][1].getID());
//        System.out.println("horiT: "+ chanNodeHoriArr[1][1].getID());

    }

    public UIGraph getGraph() {
        return u;
    }

    public LinkedList<PFNode> getNodes() {
        return nodes;
    }

    public PFNode[][] getSources() {
        return sources;
    }

    public PFNode[][] getSinks() {
        return sinks;
    }

    public PFNode[][] getChanVer() {
        return chanVer;
    }

    public PFNode[][] getChanHori() {
        return chanHori;
    }

    public int getW() {
        return w;
    }

    public int getH() {
        return h;
    }

}

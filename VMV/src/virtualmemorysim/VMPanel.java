/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package virtualmemorysim;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.List;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import java.lang.Math;

/**
 *
 * @author 15002
 */
public class VMPanel extends JLayeredPane {

    private enum States {
        PULL, PTECHECK, PTADDRF, PAGEFAULT, PTRCHECK_C, PTRCHECK_F, PTRCHECK_LU, PTEVICT, RAMWRITEB, PTUPDATE_PF, DISKWB, RAMACCESS, RAMWRITE,
        PTUPDATE_NOTLB, PTREPLACEC, TLBFLUSH, PULL_TLB, TLBCHECK, TLBADDRF, TLB_RAMWRITE, TLB_RAMACCESS,
        TLBUPDATE, TLBMISS, VPFOUND, /*TLBRCHECK_C,*/ TLBRCHECK_R, TLBRCHECK_LU, TLBRCHECK_F, TLBREPLACEC, PTWB, TLBEVICT, TLBUPDATE_TLBMISS, TLBSYNC
    };

    private final int PAGETABLE = 0;
    private final int TLB = 1;

    private VMJTable pageTable;
    private VMJTable tlbTable;
    private VMJTable ramTable;
    private VMJTable diskTable;
    private JTable vmAddrLine;
    private JTable pmAddrLine;
    private JTable missStatsTable;
    private JTable repTable;
    private VMJTable clockTable;
    private JTextArea msgPane;
    private ImagePanel hwPane;
    private ImagePanel osPane;
    private ImagePanel colorPane;
    private VMJTable instruTable;
    private LinePainter topLayer;
    private JPanel coverPane;

    private boolean tlbEnabled;
    private String offset;
    private int offsetBits;
    private int pageSize;
    private int diskNumLength;
    private int ramNumLength;
    private int offsetLengthHex;
    private JLabel output;
    private States state = States.PULL;
    private States currState;
    private PTRepAl ptR;
    private TLBRepAl tlbR;
    private LinkedList<Pair<Integer, Integer>> instructions = new LinkedList<Pair<Integer, Integer>>();
    private boolean done = false;
    private int ramCap;
    private int diskCap;
    private int tlbCap = 10;

    private int instru;

    private int currVPN;
    private int currPPN;

    private int currRamUse;
    private int currTLBUse;
    private int swapVPN;
    private int swapPPN;
    private int currTLB;
    private int swapTLB;

    private boolean pagefault = false;
    private boolean tlbMiss = false;
    private int clockHand_PT = 0;
    private int clockHand_TLB = 0;
    private boolean ptClockTick;
    private int instruCount = 0;

    private int memRefCount = 0;
    private int tlbFaultCount = 0;
    private int pageFaultCount = 0;

    private int currVPNBackup;
    private int currPPNBackup;
    private int currRamUseBackup;
    private int currTLBUseBackup;
    private int swapVPNBackup;
    private int swapPPNBackup;
    private int currTLBBackup;
    private int swapTLBBackup;
    private Object pmAddrBackup;
    private boolean pagefaultBackup = false;
    private boolean tlbMissBackup = false;
    private int clockHand_PTBackup;
    private int clockHand_TLBBackup = 0;
    private boolean ptClockTickBackup;
    private int instruCountBackup = 0;
    private int memRefCountBackup = 0;
    private int tlbFaultCountBackup = 0;
    private int pageFaultCountBackup = 0;

    private boolean skipReplacement = false;

    private Object[][] tlbData;
    private Object[][] ptData;
    private Object[][] ramData;
    private Object[][] vmData;
    private Object[][] ctData;

    private int tableY;

    private static int TLB_LRU = 4;
    private static int TLB_PM = 3;
    private static int TLB_D = 2;
    private static int TLB_R = -1;
    private static int TLB_V = 1;
    private static int TLB_VM = 0;
    private int tlbColSize = 4;
    private int tlbRightX = 0;

    public VMPanel(int tlbSize, int ramPageNum, int diskPageNum, int offsetSize, int ramSegNum, int diskSegNum, boolean tlbEn, PTRepAl ptRep, TLBRepAl tlbRep) {
        super();
        tlbCap = tlbSize;
        pageSize = offsetSize;
        ramCap = ramSegNum;
        diskCap = diskSegNum;
        tlbEnabled = tlbEn;
        ptR = ptRep;
        tlbR = tlbRep;
        uiSetUp(tlbSize, ramPageNum, diskPageNum, ramSegNum, diskSegNum);
        tableSetValues(tlbSize, ramPageNum, diskPageNum, offsetSize);
        tlbData = new Object[tlbCap][5];
        ptData = new Object[diskCap][5];
        ramData = new Object[ramCap][2];
        vmData = new Object[diskCap][2];
        ctData = new Object[ramCap][1];
        msgPane.setText("Press Play or Single Step to start simulation\n");
        if (tlbEn) {
            state = States.PULL_TLB;
        }

//        test();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        VMPanel demo = new VMPanel(10, 17, 30, 16, 4, 15, false, PTRepAl.CLOCK, TLBRepAl.LRU);
        JScrollPane a = new JScrollPane(demo);
        JFrame frame = new JFrame("AbsoluteLayoutDemo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(a);
        frame.setVisible(true);
        frame.setSize(1200, 700);
    }

    public void fsm() {
        if (tlbEnabled) {
            fsm_TLB();
        } else {
            fsm_NOTLB();
        }
    }

    public void fsm_TLB() {
//        System.out.println(currTLBUse);
        currState = state;
        topLayer.clearLines();
        topLayer.setPTLine(-1);
        topLayer.setRAMLine(-1);
        topLayer.setTLBLine(-1);
        int[] xsOffset = {220, 585, 585};
        int[] ysOffset = {45, 45, 80};
        topLayer.addLine(xsOffset, ysOffset, 3);
        if (clockHand_PT >= ramCap) {
            clockHand_PT = 0;
        }
        if (clockHand_TLB >= tlbCap) {
            clockHand_TLB = 0;
        }
        topLayer.setTLBClockLine(clockHand_TLB);
        if (!ptR.equals(PTRepAl.LFU) && !ptR.equals(PTRepAl.LRU)) {
            topLayer.setRAMClockLine(clockHand_PT);
        }
        pmAddrLine.setBackground(Color.LIGHT_GRAY);
//        System.out.println(currRamUse);
        switch (state) {
            case PULL_TLB: {
                pagefault = false;
                tlbMiss = false;
                msgPane.setText("Start Memory Access\n");
                setLeftRect(true);
                output.setText("Output: ");
                if (!instructions.isEmpty()) {
                    Pair<Integer, Integer> instruPair = instructions.poll();
                    instru = instruPair.getK();
                    String addr = Integer.toBinaryString(instruPair.getV());

                    if (instruPair.getV() < pageSize) {
                        currVPN = 0;
                        vmAddrLine.getModel().setValueAt(String.format("%0" + offsetLengthHex + "X", instruPair.getV()), 0, 1);
                        vmAddrLine.getModel().setValueAt(String.format("%" + offsetBits + "s", addr).replace(' ', '0'), 1, 1);

                    } else {
                        currVPN = Integer.parseInt(addr.substring(0, addr.length() - offsetBits), 2);
                        if (currVPN >= diskCap) {
                            msgPane.setText("Invalid virtual address\n");
                            state = States.PULL_TLB;
                            instruTable.setColor(instruCount, Color.GRAY);
                            instruCount++;
                            return;
                        }
                        String binaryOffset = addr.substring(addr.length() - offsetBits);
                        vmAddrLine.getModel().setValueAt(String.format("%0" + offsetLengthHex + "X", Integer.parseInt(binaryOffset,
                                2), 16), 0, 1);
                        vmAddrLine.getModel().setValueAt(String.format("%" + offsetBits + "s", binaryOffset).replace(' ', '0'), 1, 1);
                    }
                    instruTable.setColor(instruCount, Color.cyan);
                    state = States.TLBCHECK;
                    memRefCount++;
                    missStatsTable.setValueAt(memRefCount, 0, 0);
                    vmAddrLine.getModel().setValueAt(String.format("%0" + diskNumLength + "X", currVPN), 0, 0);
                    vmAddrLine.getModel().setValueAt(Integer.toBinaryString(currVPN), 1, 0);
                    pmAddrLine.getModel().setValueAt("", 0, 0);
                    pmAddrLine.getModel().setValueAt((String) vmAddrLine.getModel().getValueAt(0, 1), 0, 1);
                } else {
                    msgPane.setText(
                            "No more memory references\n"
                            + "Use the ADD button to create more\n"
                            + "or the OPEN button to load a new configuration"
                    );
                    done = true;
                }
                break;
            }
            case TLBCHECK: {
                instruTable.setColor(instruCount, Color.cyan);
                setLeftRect(true);
                if (instru == 0) {
                    msgPane.setText("Memory Read - Check if Virtual Page is in TLB");
                } else if (instru == 1) {
                    msgPane.setText("Memory Write - check if Virtual Page is in TLB");
                }

                boolean pageFound = false;
                state = States.TLBMISS;
                for (int i = 0; i < tlbCap; i++) {
                    if (tlbTable.getValueAt(i, TLB_V).equals(1)) {
                        int tlbCurVPN = Integer.parseInt((String) tlbTable.getValueAt(i, TLB_VM), 16);
                        if (currVPN == tlbCurVPN) {
                            pageFound = true;
                            state = States.TLBADDRF;
                            currTLB = i;
                            break;
                        }
                    }
                }

                int[] xs = {35, 35, 70};
                int[] ys = {55 + 14, 153 + 16 * (tlbCap - 1), 153 + 16 * (tlbCap - 1)};
                topLayer.addLine(xs, ys, 3);
                for (int i = 0; i < tlbCap - 1; i++) {
                    int[] xs1 = {35, 70};
                    int[] ys1 = {153 + 16 * i, 153 + 16 * i};
                    topLayer.addLine(xs1, ys1, 2);
                }
                backupState();
                break;
            }
            case TLBADDRF: {
                pmAddrLine.setBackground(Color.WHITE);
                int[] xs = {35, 35, 70};
                int[] ys = {55 + 14, 153 + 16 * (tlbCap - 1), 153 + 16 * (tlbCap - 1)};
                topLayer.addLine(xs, ys, 3);
                for (int i = 0; i < tlbCap - 1; i++) {
                    int[] xs1 = {35, 70};
                    int[] ys1 = {153 + 16 * i, 153 + 16 * i};
                    topLayer.addLine(xs1, ys1, 2);
                }
                int[] xs2 = {tlbRightX, tlbRightX + 20, tlbRightX + 20, 470, 470};
                int[] ys2 = {153 + currTLB * 16, 153 + currTLB * 16, 60, 60, 80};
                topLayer.addLine(xs2, ys2, 5);

                currPPN = Integer.parseInt((String) tlbTable.getModel().getValueAt(currTLB, TLB_PM), 16);
                pmAddrLine.getModel().setValueAt(tlbTable.getModel().getValueAt(currTLB, TLB_PM), 0, 0);
                if (instru == 0) {
                    msgPane.setText("Page found in TLB\n  Form Physical Memory Address,\n"
                            + "  Read Memory Location");
                    pmAddrLine.setBackground(Color.WHITE);
                    int[] xs3 = {470, 470, 495};
                    int[] ys3 = {135, tableY + 45 + currPPN * 16, tableY + 45 + currPPN * 16};
                    topLayer.addLine(xs3, ys3, 3);
                    state = States.TLBUPDATE;
                } else {
                    msgPane.setText("Page found in TLB\n  Form Physical Memory Address,\n"
                            + "  Write Memory Location");
                    pmAddrLine.setBackground(Color.WHITE);
                    int[] xs3 = {470, 470, 495};
                    int[] ys3 = {135, tableY + 45 + currPPN * 16, tableY + 45 + currPPN * 16};
                    topLayer.addLine(xs3, ys3, 3);
                    ramTable.getModel().setValueAt("M[" + pageTable.getModel().getValueAt(currVPN, 0) + "]", currPPN, 1);
                    output.setText("Output:");
                    state = States.TLBUPDATE;
                }
                break;
            }
            case TLBUPDATE: {
                msgPane.setText("Update TLB");
                tlbTable.setModify(true);
//                if (tlbR.equals(TLBRepAl.CLOCK)) {
//                    if (!tlbMiss) {
//                        tlbTable.getModel().setValueAt(1, currTLB, 2);
//                    } else {
//                        tlbTable.getModel().setValueAt(0, currTLB, 2);
//                    }
//                }
//                ramTable.setColor(currPPN, Color.cyan);
                if (instru == 1) {
                    tlbTable.getModel().setValueAt(1, currTLB, TLB_D);
//                    diskTable.setColor(currVPN, Color.orange);
                } else {
                    output.setText("Output: " + ramTable.getValueAt(currPPN, 1));
                }
                tlbTable.setModify(false);
                state = States.PULL_TLB;
                instruTable.setColor(instruCount, Color.gray);
                instruCount++;
                break;
            }
            case TLBMISS: {
                tlbMiss = true;
                msgPane.setText("TLB Miss -\n  Check in Page Table if Virtual Page in Physical Memory");
                setLeftRect(true);
                tlbFaultCount++;
                missStatsTable.setValueAt(tlbFaultCount, 0, 1);

                int[] xs = {35, 35, 70};
                int[] ys = {55 + 14, tableY + 45 + currVPN * 16, tableY + 45 + currVPN * 16};
                topLayer.addLine(xs, ys, 3);
                if (pageTable.getModel().getValueAt(currVPN, 1).equals(0)) {
                    state = States.TLBSYNC; //Page fault
                } else {
                    state = States.VPFOUND; //Page hit
                }
                break;
            }
            case TLBSYNC: {
                msgPane.setText("Page Fault -\n  OS will copy TLB to page table");
                setLeftRect(false);
                for (int i = 0; i < tlbCap; i++) {
                    if (tlbTable.getValueAt(i, TLB_V).equals(1)) {
                        int vpnTemp = Integer.parseInt((String) tlbTable.getValueAt(i, TLB_VM), 16);
                        int dirtyTemp = (Integer) tlbTable.getValueAt(i, TLB_D);
                        int refTemp = 0;
//                        if (tlbR.equals(TLBRepAl.CLOCK)) {
//                            refTemp = (Integer) tlbTable.getValueAt(i, 2);
//                        }
                        pageTable.setModify(true);
                        pageTable.setValueAt(dirtyTemp, vpnTemp, 3);
//                        if (refTemp == 1 && ptR.equals(PTRepAl.CLOCK) && tlbR.equals(TLBRepAl.CLOCK)) {
//                            pageTable.setValueAt(refTemp, vpnTemp, 2);
//                        }
                        pageTable.setModify(false);
                    }
                }
                state = States.TLBFLUSH;
                break;
            }
            case TLBFLUSH: {
                setLeftRect(true);
                clockHand_TLB = 0;
                currTLBUse = 0;
                swapTLB = 0;
                msgPane.setText("Hardware will flush TLB, and give control to OS to handle page fault");
                for (int i = 0; i < tlbCap; i++) {
                    tlbTable.setValueAt(0, i, TLB_V);
                }
                if (tlbR.equals(TLBRepAl.LRU)) {
                    for (int i = 0; i < tlbCap; i++) {
                        tlbTable.setValueAt(i, i, TLB_LRU);
                    }
                }
                state = States.PAGEFAULT;
                break;
            }
            case VPFOUND: {
                msgPane.setText("Page found in Physical Memory\n  Update TLB");
                ptUpdate();
                if (currTLBUse == tlbCap) {
//                    if (tlbR.equals(TLBRepAl.CLOCK)) {
//                        state = States.TLBRCHECK_C;
//                    } else
                    if (tlbR.equals(TLBRepAl.FIFO)) {
                        state = States.TLBRCHECK_F;
                    } else if (tlbR.equals(TLBRepAl.LRU)) {
                        state = States.TLBRCHECK_LU;
                    } else if (tlbR.equals(TLBRepAl.RANDOM)) {
                        state = States.TLBRCHECK_R;
                    }
                    if (skipReplacement) {
                        while (!state.equals(States.TLBREPLACEC)) {
                            fsm();
                        }
                    }
                } else {
                    state = States.TLBUPDATE_TLBMISS;
                }
                break;
            }
            case TLBRCHECK_F: {
                int corrVPN = Integer.parseInt((String) tlbTable.getValueAt(clockHand_TLB, TLB_VM), 16);
                topLayer.setTLBLine(clockHand_TLB);
                swapVPN = corrVPN;
                swapTLB = clockHand_TLB;
                state = States.TLBREPLACEC;
                msgPane.setText("Replace TLB Entry");
                clockHand_TLB++;
                break;
            }

            case TLBRCHECK_R: {
                clockHand_TLB = (int) (Math.random() * tlbCap);
                int corrVPN = Integer.parseInt((String) tlbTable.getValueAt(clockHand_TLB, TLB_VM), 16);
                topLayer.setTLBClockLine(clockHand_TLB);
                topLayer.setTLBLine(clockHand_TLB);
                swapVPN = corrVPN;
                swapTLB = clockHand_TLB;
                state = States.TLBREPLACEC;
                msgPane.setText("Replace TLB Entry");
                break;
            }

            case TLBRCHECK_LU: {
                int corrVPN = -1;
                int lruRank = -1;
                int tlbLruIndex = -1;
                for (int i = 0; i < tlbCap; i++) {
                    lruRank = (int) tlbTable.getValueAt(i, TLB_LRU);
                    if (lruRank == 0) {
                        corrVPN = Integer.parseInt((String) tlbTable.getValueAt(i, TLB_VM), 16);
                        tlbLruIndex = i;
                        break;
                    }
                }
                clockHand_TLB = tlbLruIndex;
                topLayer.setTLBLine(tlbLruIndex);
                swapVPN = corrVPN;
                swapTLB = tlbLruIndex;
                state = States.TLBREPLACEC;
                msgPane.setText("Replace TLB Entry");
                break;
            }

//            case TLBRCHECK_C: {
//                int corrVPN = Integer.parseInt((String) tlbTable.getValueAt(clockHand_TLB, 0), 16);
//                int ref = (Integer) tlbTable.getValueAt(clockHand_TLB, 2);
//                topLayer.setTLBLine(clockHand_TLB);
//                if (ref == 0) {
//                    swapVPN = corrVPN;
//                    swapTLB = clockHand_TLB;
//                    state = States.TLBREPLACEC;
//                    msgPane.setText("Replace TLB Entry");
//                } else {
//                    tlbTable.setModify(true);
//                    if (tlbR.equals(TLBRepAl.CLOCK)) {
//                        tlbTable.setValueAt(0, clockHand_TLB, 2);
//                    }
//                    tlbTable.setModify(false);
//                    state = States.TLBRCHECK_C;
//                    msgPane.setText("Look for TLB Entry to replace");
//                    clockHand_TLB++;
//                }
//
//                break;
//            }
            case TLBREPLACEC: {
//                currTLBUse--;
                topLayer.setTLBLine(swapTLB);
                topLayer.setPTLine(swapVPN);
                msgPane.setText("Replace TLB Element for Virtual Page " + tlbTable.getValueAt(swapTLB, TLB_VM));
                if ((Integer) tlbTable.getValueAt(swapTLB, TLB_D) == 1) {
                    state = States.PTWB;
                } else {
                    state = States.TLBEVICT;
                }
                break;
            }
            case PTWB: {
                msgPane.setText("Write TLB Element #" + swapTLB
                        + "\nto Page Table Entry " + pageTable.getValueAt(swapVPN, 0));
                int[] xs = {70, 27, 27, 70};
                int[] ys = {153 + swapTLB * 16, 153 + swapTLB * 16, tableY + 45 + swapVPN * 16, tableY + 45 + swapVPN * 16};
                topLayer.addLine(xs, ys, 4);
                topLayer.setPTLine(swapVPN);
                topLayer.setTLBLine(swapTLB);

                if ((Integer) tlbTable.getValueAt(swapTLB, TLB_D) == 1) {
                    pageTable.setModify(true);
                    pageTable.setValueAt(1, swapVPN, 3);
                    pageTable.setModify(false);
                }
                state = States.TLBEVICT;
                break;
            }
            case TLBEVICT: {
                msgPane.setText("Invalidate TLB Entry " + swapTLB);
                topLayer.setPTLine(swapVPN);
                topLayer.setTLBLine(swapTLB);
                tlbTable.setModify(true);
                tlbTable.setValueAt(0, swapTLB, TLB_V);
                tlbTable.setModify(false);
                currTLBUse--;
                state = States.TLBUPDATE_TLBMISS;
                break;
            }
            case TLBUPDATE_TLBMISS: {
                int[] xs = {70, 27, 27, 70};
                int[] ys = {tableY + 45 + currVPN * 16, tableY + 45 + currVPN * 16, 153 + swapTLB * 16, 153 + swapTLB * 16};
                topLayer.addLine(xs, ys, 4);
                tlbTable.setModify(true);
//                System.out.println("cur: " + currVPN + " swap: " + swapTLB);
                tlbTable.setValueAt(pageTable.getValueAt(currVPN, 0), swapTLB, TLB_VM);
                tlbTable.setValueAt(pageTable.getValueAt(currVPN, 4), swapTLB, TLB_PM);
                tlbTable.setValueAt(pageTable.getValueAt(currVPN, 3), swapTLB, TLB_D);
//                if (tlbR.equals(TLBRepAl.CLOCK)) {
//                    tlbTable.setValueAt(0, swapTLB, 2);
//                }
                if (tlbR.equals(TLBRepAl.LRU)) {
                    for (int i = 0; i < tlbCap; i++) {
                        if (i == swapTLB) {
                            tlbTable.setValueAt(tlbCap - 1, i, TLB_LRU);
                        } else {
                            tlbTable.setValueAt((int) tlbTable.getValueAt(i, TLB_LRU) - 1, i, TLB_LRU);
                            if ((int) tlbTable.getValueAt(i, TLB_LRU) == 0) {
                                clockHand_TLB = i;
                                topLayer.setTLBClockLine(clockHand_TLB);
                            }
                        }
                    }
                }
                tlbTable.setValueAt(1, swapTLB, TLB_V);
                tlbTable.setModify(false);
                currTLBUse++;
                swapTLB++;

//                if (tlbR.equals(TLBRepAl.CLOCK)) {
//                    clockHand_TLB++;
//                    msgPane.setText("Update TLB Entry " + swapTLB + " and Page Table\n  Advance TLB Clock Hand\n  Retry Memory Access");
//                }
                if (clockHand_TLB >= tlbCap) {
                    clockHand_TLB = 0;
                }
//                if(swapTLB>=tlbCap){
//                    swapTLB=0;
//                }
                topLayer.setTLBClockLine(clockHand_TLB);

                state = States.TLBCHECK;
                break;
            }
            case PAGEFAULT: {
                pagefault = true;
                msgPane.setText(
                        "Page Fault - Suspend program while OS swaps memory"
                );
                pageFaultCount++;
                missStatsTable.setValueAt(pageFaultCount, 0, 2);
                setLeftRect(false);
                ptClockTick = true;
                if (currRamUse == ramCap) {
                    state = ptRepSwitch(ptR);
                    ptClockTick = false;
                    if (skipReplacement) {
                        while (!state.equals(States.PTREPLACEC)) {
                            fsm();
                        }
                    }

                } else {
                    state = States.RAMWRITEB;
                }
                break;
            }
            case PTRCHECK_F: {
                ptRep_F();
                break;
            }
            case PTRCHECK_C: {
                ptRep_C();
                break;
            }
            case PTRCHECK_LU: {
                ptRep_LU();
                break;
            }
            case PTREPLACEC:
                topLayer.setPTLine(swapVPN);
                topLayer.setRAMLine(swapPPN);
                msgPane.setText("Replace Victim Page " + pageTable.getValueAt(swapVPN, 0) + " with Virtual Page " + pageTable.getValueAt(currVPN, 0));
                if ((Integer) pageTable.getValueAt(swapVPN, 3) == 1) {
                    state = States.DISKWB;
                } else {
                    state = States.PTEVICT;
                }
                break;
            case DISKWB: {
                msgPane.setText("Write Physical Page " + ramTable.getValueAt(swapPPN, 0)
                        + "\n  to Virtual Page " + pageTable.getValueAt(swapVPN, 0));
                int[] xs = {673, 680, 680, 747};
                int[] ys = {tableY + 45 + swapPPN * 16, tableY + 45 + swapPPN * 16, tableY + 45 + swapVPN * 16, tableY + 45 + swapVPN * 16};
                topLayer.addLine(xs, ys, 4);
                topLayer.setPTLine(swapVPN);
                topLayer.setRAMLine(swapPPN);
                state = States.PTEVICT;
                break;
            }
            case PTEVICT:
                msgPane.setText("Invalidate PTE " + ramTable.getValueAt(swapPPN, 0));
//                ramTable.setColor(swapPPN, Color.white);
//                diskTable.setColor(swapVPN, Color.white);
                topLayer.setPTLine(swapVPN);
                topLayer.setRAMLine(swapPPN);
                pageTable.setModify(true);
                pageTable.setValueAt(0, swapVPN, 1);
                pageTable.setModify(false);
//                clockTable.setColor(swapPPN, Color.white);
                currRamUse--;
                for (int i = 0; i < tlbCap; i++) {
                    if (tlbTable.getValueAt(i, TLB_V).equals(1)) {
                        int vpnTemp = Integer.parseInt((String) tlbTable.getValueAt(i, TLB_VM), 16);
                        if (vpnTemp == swapVPN) {
                            tlbTable.setValueAt(0, i, TLB_V);
                            swapTLB = i;
                            currTLBUse--;
                            msgPane.setText("Invalidate PTE  " + ramTable.getValueAt(swapPPN, 0)
                                    + "\n  and TLB Entry with Virtual Page " + tlbTable.getValueAt(swapTLB, TLB_VM));
                        }
                    }
                }
                state = States.RAMWRITEB;
                break;
            case RAMWRITEB: {
                msgPane.setText("OS copies Virtual Page "
                        + pageTable.getValueAt(currVPN, 0)
                        + " from disk to Physical Page "
                        + ((String) ramTable.getValueAt(swapPPN, 0)));
//                ramTable.setColor(swapPPN, Color.pink);
                int[] xs = {752, 740, 740, 680};
                int[] ys = {tableY + 45 + currVPN * 16, tableY + 45 + currVPN * 16, tableY + 45 + swapPPN * 16, tableY + 45 + swapPPN * 16};
                topLayer.addLine(xs, ys, 4);
                ramTable.getModel().setValueAt("M[" + pageTable.getModel().getValueAt(currVPN, 0) + "]", swapPPN, 1);
                state = States.PTUPDATE_PF;
                break;
            }
            case PTUPDATE_PF:
                ptUpdate_PF();
                state = States.TLBMISS;
                break;
            default:
//                System.out.println("Something is wrong");
                break;
        }
//        System.out.println(state);
//        System.out.println(clockHand_PT);
    }

    public void fsm_NOTLB() {
        currState = state;
        topLayer.clearLines();
        topLayer.setPTLine(-1);
        topLayer.setRAMLine(-1);
        topLayer.setTLBLine(-1);
        int[] xsOffset = {220, 585, 585};
        int[] ysOffset = {45, 45, 80};
        topLayer.addLine(xsOffset, ysOffset, 3);
        pmAddrLine.setBackground(Color.LIGHT_GRAY);
        if (clockHand_PT >= ramCap) {
            clockHand_PT = 0;
        }
        if (!ptR.equals(PTRepAl.LFU) && !ptR.equals(PTRepAl.LRU)) {
            topLayer.setRAMClockLine(clockHand_PT);
        }
        switch (state) {
            case PULL: {
                pagefault = false;
                msgPane.setText("Starting a Memory Reference\n");
                setLeftRect(true);
                output.setText("Output: ");
                if (!instructions.isEmpty()) {
                    Pair<Integer, Integer> instruPair = instructions.poll();
                    instru = instruPair.getK();
                    String addr = Integer.toBinaryString(instruPair.getV());
                    if (instruPair.getV() < pageSize) {
                        currVPN = 0;
                        vmAddrLine.getModel().setValueAt(String.format("%0" + offsetLengthHex + "X", instruPair.getV()), 0, 1);
                        vmAddrLine.getModel().setValueAt(String.format("%" + offsetBits + "s", addr).replace(' ', '0'), 1, 1);

                    } else {
                        currVPN = Integer.parseInt(addr.substring(0, addr.length() - offsetBits), 2);
                        if (currVPN >= diskCap) {
                            msgPane.setText("Invalid Virtual Address\n");
                            state = States.PULL;
                            instruTable.setColor(instruCount, Color.gray);
                            instruCount++;
                            return;
                        }
                        String binaryOffset = addr.substring(addr.length() - offsetBits);
                        vmAddrLine.getModel().setValueAt(String.format("%0" + offsetLengthHex + "X", Integer.parseInt(binaryOffset,
                                2), 16), 0, 1);
                        vmAddrLine.getModel().setValueAt(String.format("%" + offsetBits + "s", binaryOffset).replace(' ', '0'), 1, 1);
                    }
                    instruTable.setColor(instruCount, Color.cyan);
                    state = States.PTECHECK;
                    memRefCount++;
                    missStatsTable.setValueAt(memRefCount, 0, 0);
                    vmAddrLine.getModel().setValueAt(String.format("%0" + diskNumLength + "X", currVPN), 0, 0);
                    vmAddrLine.getModel().setValueAt(Integer.toBinaryString(currVPN), 1, 0);
                    pmAddrLine.getModel().setValueAt("", 0, 0);
                    pmAddrLine.getModel().setValueAt((String) vmAddrLine.getModel().getValueAt(0, 1), 0, 1);
                } else {
                    msgPane.setText(
                            "No more memory references\n"
                            + "Use the ADD button to create more\n"
                            + "or the OPEN button to load a new configuration"
                    );
                    done = true;
                }
                break;
            }
            case PTECHECK: {
                instruTable.setColor(instruCount, Color.cyan);
                setLeftRect(true);
                if (instru == 0) {
                    msgPane.setText("Memory Read\n  Check in Page Table to see if Virtual Page\n  is in Physical Memory");
                } else if (instru == 1) {
                    msgPane.setText("Memory Write\n  Check in Page Table to see if Virtual Page\n  is in Physical Memory");
                }
                int[] xs = {35, 35, 70};
                int[] ys = {55 + 15, tableY + 45 + currVPN * 16, tableY + 45 + currVPN * 16};
                topLayer.addLine(xs, ys, 3);
                if (pageTable.getModel().getValueAt(currVPN, 1).equals(0)) {
                    state = States.PAGEFAULT; //Page fault
                } else {
                    state = States.PTADDRF; //Page hit
                }
                backupState();
                break;
            }
            case PTADDRF: {
                pmAddrLine.setBackground(Color.WHITE);
                int[] xs = {35, 35, 70};
                int[] ys = {55 + 14, tableY + 45 + currVPN * 16, tableY + 45 + currVPN * 16};
                topLayer.addLine(xs, ys, 3);
                int[] xs1 = {363, 380, 380, 470, 470};
                int[] ys1 = {tableY + 45 + currVPN * 16, tableY + 45 + currVPN * 16, 60, 60, 80};
                topLayer.addLine(xs1, ys1, 5);
                currPPN = Integer.parseInt((String) pageTable.getModel().getValueAt(currVPN, 4), 16);
                pmAddrLine.getModel().setValueAt(pageTable.getModel().getValueAt(currVPN, 4), 0, 0);

                if (instru == 0) {
                    pmAddrLine.setBackground(Color.WHITE);
                    msgPane.setText("Page found in Page Table\n  Form Physical Memory Address,\n"
                            + "  Read Memory Location");
                    int[] xs2 = {470, 470, 495};
                    int[] ys2 = {135, tableY + 45 + currPPN * 16, tableY + 45 + currPPN * 16};
                    topLayer.addLine(xs2, ys2, 3);
                    state = States.PTUPDATE_NOTLB;
                } else {
                    pmAddrLine.setBackground(Color.WHITE);
                    msgPane.setText("Page found in Page Table\n  Form Physical Memory Address,\n"
                            + "  Write Memory Location");
                    int[] xs2 = {470, 470, 495};
                    int[] ys2 = {135, tableY + 45 + currPPN * 16, tableY + 45 + currPPN * 16};
                    topLayer.addLine(xs2, ys2, 3);
                    ramTable.getModel().setValueAt("M[" + pageTable.getModel().getValueAt(currVPN, 0) + "]", currPPN, 1);
                    output.setText("Output:");
                    state = States.PTUPDATE_NOTLB;
                }
                break;
            }
            case PTUPDATE_NOTLB:
                msgPane.setText("Update Page Table");
                pageTable.setModify(true);
                ptUpdate();
//                ramTable.setColor(currPPN, Color.cyan);
                if (instru == 1) {
                    pageTable.getModel().setValueAt(1, currVPN, 3);
//                    diskTable.setColor(currVPN, Color.orange);
                } else {
                    output.setText("Output: " + ramTable.getValueAt(currPPN, 1));
                }
                pageTable.setModify(false);
                state = States.PULL;
                instruTable.setColor(instruCount, Color.gray);
                instruCount++;
                break;
            case PAGEFAULT: {
                pagefault = true;
                msgPane.setText("Page Fault - Suspend program while OS swaps memory");
                pageFaultCount++;
                missStatsTable.setValueAt(pageFaultCount, 0, 1);
                setLeftRect(false);
                int[] xs = {35, 35, 70};
                int[] ys = {55 + 14, tableY + 45 + currVPN * 16, tableY + 45 + currVPN * 16};
                topLayer.addLine(xs, ys, 3);
                ptClockTick = true;
                if (currRamUse == ramCap) {
                    state = ptRepSwitch(ptR);
                    ptClockTick = false;
                    if (skipReplacement) {
                        while (!state.equals(States.PTREPLACEC)) {
                            fsm();
                        }
                    }
                } else {
                    state = States.RAMWRITEB;
                }
                break;
            }
            case PTRCHECK_F: {
                ptRep_F();
                break;
            }
            case PTRCHECK_C: {
                ptRep_C();
                break;
            }
            case PTRCHECK_LU: {
                ptRep_LU();
                break;
            }
            case PTREPLACEC:
                topLayer.setPTLine(swapVPN);
                topLayer.setRAMLine(swapPPN);
                msgPane.setText("Replace data from Virtual Page\n" + pageTable.getValueAt(swapVPN, 0));
                if ((Integer) pageTable.getValueAt(swapVPN, 3) == 1) {
                    state = States.DISKWB;
                } else {
                    state = States.PTEVICT;
                }
                break;
            case DISKWB: {
                msgPane.setText("Write Physical Page " + ramTable.getValueAt(swapPPN, 0)
                        + "\nto virtual page " + pageTable.getValueAt(swapVPN, 0));
                int[] xs = {673, 680, 680, 747};
                int[] ys = {tableY + 45 + swapPPN * 16, tableY + 45 + swapPPN * 16, tableY + 45 + swapVPN * 16, tableY + 45 + swapVPN * 16};
                topLayer.addLine(xs, ys, 4);
                topLayer.setPTLine(swapVPN);
                topLayer.setRAMLine(swapPPN);
                state = States.PTEVICT;
                break;
            }
            case PTEVICT:
                msgPane.setText("Invalidate PTE " + ramTable.getValueAt(swapPPN, 0));
//                ramTable.setColor(swapPPN, Color.white);
//                diskTable.setColor(swapVPN, Color.white);
                topLayer.setPTLine(swapVPN);
                topLayer.setRAMLine(swapPPN);
                pageTable.setModify(true);
                pageTable.setValueAt(0, swapVPN, 1);
                pageTable.setModify(false);
//                clockTable.setColor(swapPPN, Color.white);
                currRamUse--;
                state = States.RAMWRITEB;
                break;
            case RAMWRITEB: {
                msgPane.setText("OS copies Virtual Page "
                        + pageTable.getValueAt(currVPN, 0)
                        + " from Disk to Physical Page "
                        + ((String) ramTable.getValueAt(swapPPN, 0)));
//                ramTable.setColor(swapPPN, Color.pink);
                int[] xs = {752, 740, 740, 680};
                int[] ys = {tableY + 45 + currVPN * 16, tableY + 45 + currVPN * 16, tableY + 45 + swapPPN * 16, tableY + 45 + swapPPN * 16};
                topLayer.addLine(xs, ys, 4);
                ramTable.getModel().setValueAt("M[" + pageTable.getModel().getValueAt(currVPN, 0) + "]", swapPPN, 1);
                state = States.PTUPDATE_PF;
                break;
            }
            case PTUPDATE_PF: {
                ptUpdate_PF();
                state = States.PTECHECK;
                break;
            }
            default:
//                System.out.println("Something is wrong");
                break;
        }
//        System.out.println(state);
    }

    public void setCTVisible(boolean vis) {
        coverPane.setVisible(vis);
        skipReplacement = vis;
    }

    private void backupState() {
        currVPNBackup = currVPN;
        currPPNBackup = currPPN;
        currRamUseBackup = currRamUse;
        currTLBUseBackup = currTLBUse;
        swapVPNBackup = swapVPN;
        swapPPNBackup = swapPPN;
        currTLBBackup = currTLB;
        swapTLBBackup = swapTLB;
        pagefaultBackup = pagefault;
        tlbMissBackup = tlbMiss;
        clockHand_PTBackup = clockHand_PT;
        clockHand_TLBBackup = clockHand_TLB;
        ptClockTickBackup = ptClockTick;
        instruCountBackup = instruCount;
        memRefCountBackup = memRefCount;
        tlbFaultCountBackup = tlbFaultCount;
        pageFaultCountBackup = pageFaultCount;

        for (int col = 0; col < tlbColSize; col++) {
            for (int row = 0; row < tlbCap; row++) {
                tlbData[row][col] = tlbTable.getValueAt(row, col);
            }
            for (int row = 0; row < diskCap; row++) {
                ptData[row][col] = pageTable.getValueAt(row, col);
            }
        }
        for (int col = 0; col < 2; col++) {
            for (int row = 0; row < ramCap; row++) {
                ramData[row][col] = ramTable.getValueAt(row, col);
            }
            for (int row = 0; row < diskCap; row++) {
                vmData[row][col] = diskTable.getValueAt(row, col);
            }
        }
        for (int row = 0; row < ramCap; row++) {
            ctData[row][0] = clockTable.getValueAt(row, 0);
        }
        pmAddrBackup = pmAddrLine.getValueAt(0, 0);
    }

    public void recoverState() {
        if (currState != States.PULL_TLB && currState != States.PULL) {
            currVPN = currVPNBackup;
            currPPNBackup = currPPN;
            currRamUse = currRamUseBackup;
            currTLBUse = currTLBUseBackup;
            swapVPN = swapVPNBackup;
            swapPPN = swapPPNBackup;
            currTLB = currTLBUseBackup;
            swapTLB = swapTLBBackup;
            pagefault = pagefaultBackup;
            tlbMiss = tlbMissBackup;
            clockHand_PT = clockHand_PTBackup;
            clockHand_TLB = clockHand_TLBBackup;
            ptClockTick = ptClockTickBackup;
            instruCount = instruCountBackup;
            memRefCount = memRefCountBackup;
            tlbFaultCount = tlbFaultCountBackup;
            pageFaultCount = pageFaultCountBackup;
            for (int col = 0; col < tlbColSize; col++) {
                if (tlbEnabled) {
                    for (int row = 0; row < tlbCap; row++) {
                        tlbTable.setModify(true);
                        tlbTable.setValueAt(tlbData[row][col], row, col);
                        tlbTable.setModify(false);
                    }
                }
                pageTable.setModify(true);
                for (int row = 0; row < diskCap; row++) {
                    pageTable.setValueAt(ptData[row][col], row, col);
                }
                pageTable.setModify(false);
            }
            for (int col = 0; col < 2; col++) {
                ramTable.setModify(true);
                for (int row = 0; row < ramCap; row++) {
                    ramTable.setValueAt(ramData[row][col], row, col);
                }
                ramTable.setModify(false);
                diskTable.setModify(true);
                for (int row = 0; row < diskCap; row++) {
                    diskTable.setValueAt(vmData[row][col], row, col);
                }
                diskTable.setModify(false);
            }
            clockTable.setModify(true);
            for (int row = 0; row < ramCap; row++) {
                clockTable.setValueAt(ctData[row][0], row, 0);
            }
            clockTable.setModify(false);
            pmAddrLine.setValueAt(pmAddrBackup, 0, 0);
            instruTable.setColor(instruCount, Color.cyan);
            if (tlbEnabled) {
                state = States.TLBCHECK;
            } else {
                state = States.PTECHECK;
            }
            missStatsTable.setValueAt(memRefCount, 0, 0);
            if (tlbEnabled) {
                missStatsTable.setValueAt(tlbFaultCount, 0, 1);
                missStatsTable.setValueAt(pageFaultCount, 0, 2);
            } else {
                missStatsTable.setValueAt(pageFaultCount, 0, 1);
            }
            fsm();
            repaint();
        }
    }

    private void uiSetUp(int tlbSize, int ramPageNum, int diskPageNum, int ramSegNum, int diskSegNum) {
        int tlbRecWid = 0;
        int tlbVMWid = 0;
        int tlbDWid = 0;
        int tlbVWid = 0;
        int tlbPMWid = 0;

        if (tlbR.equals(TLBRepAl.LRU)) {
            tlbTable = new VMJTable(tlbSize, 5, TLB);
            tlbRecWid = 41;
            tlbTable.getColumnModel().getColumn(TLB_LRU).setPreferredWidth(tlbRecWid);
            tlbTable.getColumnModel().getColumn(TLB_LRU).setHeaderValue("Recent");
        } else {
            tlbTable = new VMJTable(tlbSize, 4, TLB);
        }
//        if (tlbR.equals(TLBRepAl.CLOCK)) {
//            tlbTable.setReferenceEnabled(true);
//        }
        tlbTable.getTableHeader().setReorderingAllowed(false);
        tlbVMWid = 85;
        tlbDWid = 23;
        tlbVWid = 31;
        tlbPMWid = 85;
        tlbTable.getColumnModel().getColumn(TLB_VM).setPreferredWidth(tlbVMWid);
        tlbTable.getColumnModel().getColumn(TLB_V).setPreferredWidth(tlbVWid);
        tlbTable.getColumnModel().getColumn(TLB_D).setPreferredWidth(tlbDWid);
        tlbTable.getColumnModel().getColumn(TLB_PM).setPreferredWidth(tlbPMWid);

        tlbTable.getColumnModel().getColumn(TLB_VM).setHeaderValue("Virtual Page#");
        tlbTable.getColumnModel().getColumn(TLB_V).setHeaderValue("V");
        tlbTable.getColumnModel().getColumn(TLB_D).setHeaderValue("D");
        tlbTable.getColumnModel().getColumn(TLB_PM).setHeaderValue("Physical Page#");

        tlbTable.setMinimumSize(new Dimension(tlbRecWid + tlbVMWid + tlbDWid + tlbVWid + tlbPMWid, tlbSize * 16));
        tlbTable.setPreferredSize(new Dimension(tlbRecWid + tlbVMWid + tlbDWid + tlbVWid + tlbPMWid, tlbSize * 16));

        tlbRightX = 108 + tlbRecWid + tlbVMWid + tlbDWid + tlbVWid + tlbPMWid;

        JScrollPane tlbPane = new JScrollPane(tlbTable);
//        if (!tlbEnabled) {
//            tlbTable.setBackground(Color.GRAY);
//        }
        tlbTable.setEnabled(true);
//        VMJTable.setTLBEnabled(true);
        if (!tlbEnabled) {
            tlbPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                    "Translation Lookaside Buffer (Disabled)", TitledBorder.CENTER, TitledBorder.TOP));
        } else {
            tlbPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                    "Translation Lookaside Buffer", TitledBorder.CENTER, TitledBorder.TOP));
        }

        if (tlbEnabled) {
            missStatsTable = new JTable(1, 3);
            missStatsTable.setMinimumSize(new Dimension(100 * 3, 16));
            missStatsTable.setPreferredSize(new Dimension(100 * 3, 16));
            missStatsTable.getColumnModel().getColumn(0).setHeaderValue("# of Reference");
            missStatsTable.getColumnModel().getColumn(1).setHeaderValue("# of TLB Miss");
            missStatsTable.getColumnModel().getColumn(2).setHeaderValue("# of Page Fault");
            missStatsTable.setValueAt(0, 0, 0);
            missStatsTable.setValueAt(0, 0, 1);
            missStatsTable.setValueAt(0, 0, 2);

            repTable = new JTable(1, 2);
            repTable.getColumnModel().getColumn(0).setHeaderValue("TLB");
            repTable.getColumnModel().getColumn(1).setHeaderValue("Page Table");
            repTable.setValueAt(tlbR.toString(), 0, 0);
            repTable.setValueAt(ptR.toString(), 0, 1);

        } else {
            missStatsTable = new JTable(1, 2);
            missStatsTable.setMinimumSize(new Dimension(100 * 2, 16));
            missStatsTable.setPreferredSize(new Dimension(100 * 2, 16));
            missStatsTable.getColumnModel().getColumn(0).setHeaderValue("# of Reference");
            missStatsTable.getColumnModel().getColumn(1).setHeaderValue("# of Page Fault");
            missStatsTable.setValueAt(0, 0, 0);
            missStatsTable.setValueAt(0, 0, 1);

            repTable = new JTable(1, 1);
            repTable.getColumnModel().getColumn(0).setHeaderValue("Page Table");
            repTable.setValueAt(ptR.toString(), 0, 0);
//            repTable.setMinimumSize(new Dimension(100, 16));
//            repTable.setPreferredSize(new Dimension(100, 16));
        }
        repTable.setMinimumSize(new Dimension(100 * 2, 16));
        repTable.setPreferredSize(new Dimension(100 * 2, 16));

        pageTable = new VMJTable(diskPageNum, 5, PAGETABLE);
        if (ptR.equals(PTRepAl.CLOCK)) {
            pageTable.setReferenceEnabled(true);
        }
        pageTable.getTableHeader().setReorderingAllowed(false);
        pageTable.getColumnModel().getColumn(0).setPreferredWidth(85);
        pageTable.getColumnModel().getColumn(1).setPreferredWidth(31);
        pageTable.getColumnModel().getColumn(2).setPreferredWidth(31);
        pageTable.getColumnModel().getColumn(3).setPreferredWidth(23);
        pageTable.getColumnModel().getColumn(4).setPreferredWidth(85);
        pageTable.getColumnModel().getColumn(0).setHeaderValue("Virtual Page#");
        pageTable.getColumnModel().getColumn(1).setHeaderValue("V");
        pageTable.getColumnModel().getColumn(2).setHeaderValue("R");
        pageTable.getColumnModel().getColumn(3).setHeaderValue("D");
        pageTable.getColumnModel().getColumn(4).setHeaderValue("Physical Page#");
        pageTable.setMinimumSize(new Dimension(31 * 2 + 23 + 85 * 2, diskPageNum * 16));
        pageTable.setPreferredSize(new Dimension(31 * 2 + 23 + 85 * 2, diskPageNum * 16));
        JScrollPane pagePane = new JScrollPane(pageTable);
        pageTable.setEnabled(true);
        pagePane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "Page Table", TitledBorder.CENTER, TitledBorder.TOP));

        ramTable = new VMJTable(ramPageNum, 2, 2, ramSegNum);
        ramTable.getTableHeader().setReorderingAllowed(false);
        ramTable.getColumnModel().getColumn(0).setPreferredWidth(70);
        ramTable.getColumnModel().getColumn(1).setPreferredWidth(70);
        ramTable.getColumnModel().getColumn(0).setHeaderValue("Page#");
        ramTable.getColumnModel().getColumn(1).setHeaderValue("Data");
        ramTable.setMinimumSize(new Dimension(70 + 70, ramPageNum * 16));
        ramTable.setPreferredSize(new Dimension(70 + 70, ramPageNum * 16));
        JScrollPane ramPane = new JScrollPane(ramTable);
        ramTable.setEnabled(true);
        ramPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "Physical Memory", TitledBorder.CENTER, TitledBorder.TOP));

        diskTable = new VMJTable(diskPageNum, 2, 2, diskSegNum);
        diskTable.getTableHeader().setReorderingAllowed(false);
        diskTable.getColumnModel().getColumn(0).setPreferredWidth(70);
        diskTable.getColumnModel().getColumn(1).setPreferredWidth(70);
        diskTable.getColumnModel().getColumn(0).setHeaderValue("Page#");
        diskTable.getColumnModel().getColumn(1).setHeaderValue("Data");
        diskTable.setMinimumSize(new Dimension(70 + 70, diskPageNum * 16));
        diskTable.setPreferredSize(new Dimension(70 + 70, diskPageNum * 16));
        JScrollPane diskPane = new JScrollPane(diskTable);
        diskTable.setEnabled(true);
        diskPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "Disk", TitledBorder.CENTER, TitledBorder.TOP));

        pmAddrLine = new JTable(1, 2);
        pmAddrLine.getTableHeader().setReorderingAllowed(false);
        pmAddrLine.getColumnModel().getColumn(0).setPreferredWidth(85);
        pmAddrLine.getColumnModel().getColumn(1).setPreferredWidth(85);
        pmAddrLine.getColumnModel().getColumn(0).setHeaderValue("Physical Page#");
        pmAddrLine.getColumnModel().getColumn(1).setHeaderValue("Offset");
        pmAddrLine.setMinimumSize(new Dimension(85 + 85, 16));
        pmAddrLine.setPreferredSize(new Dimension(85 + 85, 16));
        JScrollPane pmlPane = new JScrollPane(pmAddrLine);
        pmAddrLine.setEnabled(false);
        pmlPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "Physical Memory Address", TitledBorder.CENTER, TitledBorder.BOTTOM));
        pmAddrLine.setBackground(Color.LIGHT_GRAY);

        vmAddrLine = new JTable(2, 2);
        vmAddrLine.getTableHeader().setReorderingAllowed(false);
        vmAddrLine.getColumnModel().getColumn(0).setPreferredWidth(85);
        vmAddrLine.getColumnModel().getColumn(1).setPreferredWidth(85);
        vmAddrLine.getColumnModel().getColumn(0).setHeaderValue("Virtual Page#");
        vmAddrLine.getColumnModel().getColumn(1).setHeaderValue("Offset");
        vmAddrLine.setMinimumSize(new Dimension(85 + 85, 32));
        vmAddrLine.setPreferredSize(new Dimension(85 + 85, 32));
        JScrollPane vmlPane = new JScrollPane(vmAddrLine);
        vmAddrLine.setEnabled(false);
        vmlPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "Virtual Memory Address", TitledBorder.CENTER, TitledBorder.TOP));
        if (ptR.equals(PTRepAl.LFU)) {
            clockTable = new VMJTable(ramSegNum, 2, 2, ramSegNum);
            clockTable.getTableHeader().setReorderingAllowed(false);
            clockTable.getColumnModel().getColumn(0).setPreferredWidth(65);
            clockTable.getColumnModel().getColumn(1).setPreferredWidth(65);
            clockTable.getColumnModel().getColumn(0).setHeaderValue("Virtual Page#");
            clockTable.getColumnModel().getColumn(1).setHeaderValue("# of Access");
            clockTable.setMinimumSize(new Dimension(65 * 2, 16 * ramSegNum));
        } else {
            clockTable = new VMJTable(ramSegNum, 1, 2, ramSegNum);
            clockTable.getTableHeader().setReorderingAllowed(false);
            clockTable.getColumnModel().getColumn(0).setPreferredWidth(85);
            clockTable.getColumnModel().getColumn(0).setHeaderValue("Virtual Page#");
            clockTable.setMinimumSize(new Dimension(85, 16 * ramSegNum));
        }
        JScrollPane clockPane = new JScrollPane(clockTable);
        clockPane.setPreferredSize(new Dimension(clockTable.getWidth(), clockTable.getHeight()));
        coverPane = new JPanel();
        coverPane.setPreferredSize(clockPane.getPreferredSize());
        coverPane.setBackground(this.getBackground());

        clockTable.setEnabled(true);
        switch (ptR) {
            case CLOCK: {
                clockPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                        "<html><center>Page Replacement"
                        + "<br> (Clock Table) </html>", TitledBorder.CENTER, TitledBorder.TOP));
                break;
            }
            case FIFO: {
                clockPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                        "<html><center>Page Replacement"
                        + "<br> (FIFO Queue) </html>", TitledBorder.CENTER, TitledBorder.TOP));
                break;
            }
            case LFU: {
                clockPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                        "<html><center>Page Replacement"
                        + "<br> (LFU Table) </html>", TitledBorder.CENTER, TitledBorder.TOP));
                break;
            }
            case LRU: {
                clockPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                        "<html><center>Page Replacement"
                        + "<br> (LRU Table) </html>", TitledBorder.CENTER, TitledBorder.TOP));
                break;
            }
        }

//        hhdPane = new ImagePanel("images/hardDisk.png");
//        hhdPane.setPreferredSize(new Dimension(300, 200));
//        hhdPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
//                "Virtual Memory", TitledBorder.CENTER, TitledBorder.TOP));
        hwPane = new ImagePanel("images/hardware.png", 0, 0);

        hwPane.setPreferredSize(
                new Dimension(80, 80));

        osPane = new ImagePanel("images/os.png", 0, 0);

        osPane.setPreferredSize(
                new Dimension(80, 80));
        if (ptR.equals(PTRepAl.CLOCK)) {
            colorPane = new ImagePanel("images/colorGuide.png", 25, 25);
        } else {
        colorPane = new ImagePanel("images/colorGuide_NoRef.png", 25, 25);
        }

        colorPane.setPreferredSize(
                new Dimension(275, 150));
        colorPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "Page Table Legend", TitledBorder.CENTER, TitledBorder.TOP));

        topLayer = new LinePainter();

        topLayer.setTlbRightX(113 + tlbRecWid + tlbVMWid + tlbDWid + tlbVWid + tlbPMWid);

        topLayer.setPreferredSize(
                new Dimension(1150, 365 + (diskPageNum + 4) * 16));
        topLayer.setBackground(Color.blue);

        topLayer.setOpaque(
                false);

        output = new JLabel("Output:");

        output.setPreferredSize(
                new Dimension(200, 50));
        output.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "Data Output", TitledBorder.CENTER, TitledBorder.TOP));

        msgPane = new JTextArea("");

        msgPane.setPreferredSize(
                new Dimension(160, 20));
        msgPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "Information", TitledBorder.CENTER, TitledBorder.TOP));

        setLayout(
                null);

        add(vmlPane, JLayeredPane.DEFAULT_LAYER);

        add(pmlPane, JLayeredPane.DEFAULT_LAYER);

        add(pagePane, JLayeredPane.DEFAULT_LAYER);

        add(ramPane, JLayeredPane.DEFAULT_LAYER);

        add(diskPane, JLayeredPane.DEFAULT_LAYER);
//        add(hwPane, JLayeredPane.DEFAULT_LAYER);
//        add(osPane, JLayeredPane.DEFAULT_LAYER);

        add(colorPane, JLayeredPane.DEFAULT_LAYER);

        add(topLayer, JLayeredPane.PALETTE_LAYER);
//        add(output, JLayeredPane.DEFAULT_LAYER);
//        add(msgPane, JLayeredPane.DEFAULT_LAYER);

        add(coverPane, JLayeredPane.POPUP_LAYER);

        add(clockPane, JLayeredPane.DEFAULT_LAYER);

        Insets insets = getInsets();

        Dimension size = vmAddrLine.getPreferredSize();

        vmlPane.setBounds(
                25 + insets.left, 0 + insets.top,
                size.width + 50, size.height + 45);

//        msgPane.setBackground(getBackground());
//        msgPane.setBounds(745 + insets.left, 120 + insets.top,
//                size.width + 220, size.height + 70);
//        msgPane.setEditable(false);
        size = pmAddrLine.getPreferredSize();

        pmlPane.setBounds(
                420 + insets.left, 75 + insets.top,
                size.width + 50, size.height + 45);

        tableY = pmlPane.getY() + pmlPane.getHeight() + 30;

        topLayer.setTableY(tableY
                + 37);

        if (tlbEnabled) {
            add(tlbPane, JLayeredPane.DEFAULT_LAYER);
            size = tlbTable.getPreferredSize();
            tlbPane.setBounds(65 + insets.left, vmlPane.getY() + vmlPane.getHeight() + 30 + insets.top,
                    size.width + 50, size.height + 45);
            int tempY = tlbPane.getY() + tlbPane.getHeight() + 30;
            int tlbY = tlbPane.getY();
            topLayer.setTlbY(tlbY);
            if (tableY < tempY) {
                tableY = tempY;
                topLayer.setTableY(tableY + 37);
            }

        }

        size = pageTable.getPreferredSize();

        pagePane.setBounds(
                65 + insets.left, tableY + insets.top,
                size.width + 50, size.height + 45);

        size = ramTable.getPreferredSize();

        ramPane.setBounds(
                450 + 40 + insets.left, tableY + insets.top,
                size.width + 50, size.height + 45);

        size = diskTable.getPreferredSize();

        diskPane.setBounds(
                750 + insets.left, tableY + insets.top,
                size.width + 50, size.height + 45);

        size = clockTable.getPreferredSize();

        clockPane.setBounds(
                1000 + insets.left, tableY + insets.top,
                size.width + 50, size.height + 62);
        coverPane.setBounds(
                1000 + insets.left, tableY + insets.top,
                size.width + 50, size.height + 62);
        coverPane.setVisible(
                false);

//        size = hwPane.getPreferredSize();
//        hwPane.setBounds(720 + insets.left, -15 + insets.top,
//                size.width + 30, size.height + 30);
//        size = osPane.getPreferredSize();
//        osPane.setBounds(820 + insets.left, -15 + insets.top,
//                size.width + 30, size.height + 30);
        size = colorPane.getPreferredSize();

        colorPane.setBounds(
                65 + insets.left, tableY + insets.top + pagePane.getHeight() + 10,
                size.width + 30, size.height + 30);

        size = topLayer.getPreferredSize();

        topLayer.setBounds(insets.left, insets.top,
                size.width, size.height);

//        size = output.getPreferredSize();
//        output.setBounds(745 + insets.left, 120 + insets.top,
//                size.width + 130, size.height + 20);
        size = msgPane.getPreferredSize();

        setPreferredSize(
                new Dimension(1150, tableY + (diskPageNum + 4) * 16 + colorPane.getHeight() - 12));

        //add tooltip
        vmAddrLine.setToolTipText(
                "<html>This field displays the current virtual address by"
                + "<br>virtual page number and offset</html>"
        );
        pmAddrLine.setToolTipText(
                "<html>This field displays the current physical address by"
                + "<br>physical page number and offset</html>"
        );
        tlbTable.setToolTipText(
                "<html>The <b>Translation Lookaside Buffer (TLB)</b> acts as a cache"
                + "<br>for page table entries.  When a virtual page reference is found"
                + "<br>in the TLB it eliminates the need to access the page table in physical memory.</html>"
        );
        ramTable.setToolTipText(
                "This table displays the current contents of physical memory");
        pageTable.setToolTipText(
                "<html>The <b>Page Table<b> resides in physical memory and"
                + "<br>maps Virtual Page numbers to Physical Page numbers</html>");
        diskTable.setToolTipText(
                "This table displays the virtual pages stored on disk");

        rtToolTip(ptR);
        //These are for the images
        osPane.setToolTipText(
                "OS Active - highlighted when OS is performing a swap");
        hwPane.setToolTipText(
                "HW Active - highlighted when hardware is accessing memory");
        //colorPane.setToolTipText("change from line 1298 of VMPanel.java");
        // page table legend (colorPane) is self-explanatory
    }

    private void tableSetValues(int tlbSize, int ramPageNum, int diskPageNum, int pageSize) {
        offset = String.format("%X", pageSize - 1);
        diskNumLength = Integer.toHexString(diskPageNum).length();
        ramNumLength = Integer.toHexString(ramPageNum).length();
        offsetLengthHex = Integer.toHexString(pageSize - 1).length();

        pageTable.setModify(true);
        for (int i = 0; i < diskPageNum; i++) {
            pageTable.getModel().setValueAt(String.format("%0" + diskNumLength + "X", i) /*+ offset*/, i, 0);
            pageTable.getModel().setValueAt(0, i, 1);
        }
        pageTable.setModify(false);

        if (tlbEnabled) {
            tlbTable.setModify(true);
            for (int i = 0; i < tlbSize; i++) {
                tlbTable.getModel().setValueAt(0, i, 1);
                if (tlbR.equals(TLBRepAl.LRU)) {
                    tlbTable.setValueAt(i, i, TLB_LRU);
                }
            }
            tlbTable.setModify(false);
        }

        for (int i = 0; i < ramPageNum; i++) {
            ramTable.getModel().setValueAt(String.format("%0" + diskNumLength + "X", i) /*+ offset*/, i, 0);
        }

        for (int i = 0; i < diskPageNum; i++) {
            diskTable.getModel().setValueAt(String.format("%0" + diskNumLength + "X", i) /*+ offset*/, i, 0);
            diskTable.getModel().setValueAt(String.format("M[" + "%0" + diskNumLength + "X", i) + "]", i, 1);
        }

        offsetBits = Integer.toBinaryString(pageSize - 1).length();
    }

    private void setLeftRect(boolean left) {
        if (left) {
            hwPane.setBorder(new LineBorder(Color.red, 4, true));
            osPane.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        } else {
            osPane.setBorder(new LineBorder(Color.red, 4, true));
            hwPane.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        }
    }

    public LinkedList<Pair<Integer, Integer>> getInstructions() {
        return instructions;
    }

    public void setInstructions(LinkedList<Pair<Integer, Integer>> instructions) {
        this.instructions = instructions;
        instruTable = new VMJTable(instructions.size(), 2, 2, instructions.size());
        instruTable.setEnabled(true);
        instruTable.getColumnModel().getColumn(0).setPreferredWidth(31);
        instruTable.getColumnModel().getColumn(1).setPreferredWidth(95);
        instruTable.getColumnModel().getColumn(0).setHeaderValue("r/w");
        instruTable.getColumnModel().getColumn(1).setHeaderValue("Virtual Address");
        instruTable.getTableHeader().setReorderingAllowed(false);
    }

    private void ptRep_F() {
        String corrVPNRaw = (String) ramTable.getValueAt(clockHand_PT, 1);
        int corrVPN = Integer.parseInt(corrVPNRaw.substring(2, corrVPNRaw.length() - 1), 16);
        topLayer.setPTLine(corrVPN);
        topLayer.setRAMLine(clockHand_PT);
        swapVPN = corrVPN;
        swapPPN = clockHand_PT;
        state = States.PTREPLACEC;
        msgPane.setText("PTE to replace found");
        clockHand_PT++;
    }

    private void ptRep_C() {
        String corrVPNRaw = (String) ramTable.getValueAt(clockHand_PT, 1);
        int corrVPN = Integer.parseInt(corrVPNRaw.substring(2, corrVPNRaw.length() - 1), 16);
        int ref = (Integer) pageTable.getValueAt(corrVPN, 2);
        topLayer.setPTLine(corrVPN);
        topLayer.setRAMLine(clockHand_PT);
        if (ref == 0) {
            swapVPN = corrVPN;
            swapPPN = clockHand_PT;
            state = States.PTREPLACEC;
            msgPane.setText("PTE to replace found");
        } else {
            pageTable.setModify(true);
            if (ptR.equals(PTRepAl.CLOCK)) {
                pageTable.setValueAt(0, corrVPN, 2);
            }
            pageTable.setModify(false);
            state = States.PTRCHECK_C;
            msgPane.setText("look for a new PTE to replace");
        }
        clockHand_PT++;
    }

    private void ptRep_LU() {
        swapVPN = Integer.parseInt((String) clockTable.getValueAt(currRamUse - 1, 0));
        String corrPPNRaw = (String) pageTable.getValueAt(swapVPN, 4);
        int corrPPN = Integer.parseInt(corrPPNRaw, 16);
        topLayer.setPTLine(swapVPN);
        swapPPN = corrPPN;
        state = States.PTREPLACEC;
        msgPane.setText("PTE to replace found");
    }

    private States ptRepSwitch(PTRepAl pageRepPolicy) {
        switch (pageRepPolicy) {
            case CLOCK:
                return States.PTRCHECK_C;
            case FIFO:
                return States.PTRCHECK_F;
            case LFU:
                return States.PTRCHECK_LU;
            case LRU:
                return States.PTRCHECK_LU;
        }
        return null;
    }

    private void ptUpdate() {
        switch (ptR) {
            case CLOCK:
                if (!pagefault) {
                    pageTable.getModel().setValueAt(1, currVPN, 2);
                } else {
                    pageTable.getModel().setValueAt(0, currVPN, 2);
                }
                break;
            case LFU:
                int row = findRTRow((String) pageTable.getValueAt(currVPN, 0));
                int access = (Integer) clockTable.getValueAt(row, 1) + 1;
                clockTable.setValueAt(access, row, 1);
                sortLFUTable();
                break;
            case LRU:
                shiftLRUTable((String) pageTable.getValueAt(currVPN, 0));
        }
    }

    private void ptUpdate_PF() {
        pageTable.setModify(true);
        String ppAddr = (String) ramTable.getValueAt(swapPPN, 0);
        pageTable.getModel().setValueAt(ppAddr, currVPN, 4);
        pageTable.getModel().setValueAt(1, currVPN, 1);
        if (ptR.equals(PTRepAl.CLOCK)) {
            pageTable.getModel().setValueAt(0, currVPN, 2);
        }

        if (instru == 1) {
            pageTable.getModel().setValueAt(1, currVPN, 3);

        } else {
            pageTable.getModel().setValueAt(0, currVPN, 3);
        }
        pageTable.setModify(false);
        switch (ptR) {
            case CLOCK:
                clockTable.setValueAt(pageTable.getValueAt(currVPN, 0), swapPPN, 0);
                if (ptClockTick) {
                    clockHand_PT++;
                    if (clockHand_PT >= ramCap) {
                        clockHand_PT = 0;
                    }
                    topLayer.setRAMClockLine(clockHand_PT);
                }
                break;
            case LFU:
                clockTable.setValueAt(pageTable.getValueAt(currVPN, 0), currRamUse, 0);
                clockTable.setValueAt(0, currRamUse, 1);
                break;
            default:
                break;
        }
        currRamUse++;
        swapPPN++;
        msgPane.setText("OS updates Page Table Entry for Virtual Page " + pageTable.getValueAt(currVPN, 0)
                + "\n  Program will resume and retry Memory Access");
    }

    private void shiftLRUTable(String addr) {
        int row = findRTRow(addr);
        if (row > -1) {
            for (int i = row - 1; i > -1; i--) {
                clockTable.setValueAt(clockTable.getValueAt(i, 0), i + 1, 0);
            }

        } else {
//            System.out.println(currRamUse);
            for (int i = currRamUse - 2; i > -1; i--) {
                clockTable.setValueAt(clockTable.getValueAt(i, 0), i + 1, 0);
            }
        }
        clockTable.setValueAt(pageTable.getValueAt(currVPN, 0), 0, 0);
    }

    private void rtToolTip(PTRepAl pageRepPolicy) {
        switch (ptR) {
            case CLOCK: {
                clockTable.setToolTipText(
                        "<html>The clock table is used by the operating system"
                        + "<br>to select a page for replacement when a page fault occurs."
                        + "<br>It maintains a list of the physical pages currently in use."
                        + "<br>The red arrow indicates an index used to scan this table"
                        + "<br>which initially points to the next empty page."
                        + "<br>When all physical memory pages are in use, it scan>"
                        + "<br>through the list starting at the current index and wrapping"
                        + "<br>searching for the first page where <b>R=0</b> to identify"
                        + "<br>a victim page for replacment. while clearing the R bit"
                        + "<br>for each page that it skips over."
                        + "<br>This is known as the <i>Clock Algorithm<i></html>."
                );
                break;
            }
            case FIFO: {
                clockTable.setToolTipText(
                        "<html>The FIFO queue is used by the operating system"
                        + "<br>to select a page for replacement when a page fault occurs."
                        + "<br>It maintains a list of the physical pages currently in use."
                        + "<br>The red arrow indicates first added element."
                        + "<br>When all physical memory pages are in use, the system will"
                        + "<br>evict the pointed page and advance arrow to the first added"
                        + "<br>element in the current queue."
                        + "<br>This is known as the <i>FIFO method<i></html>"
                );
                break;
            }
            case LFU: {
                clockTable.setToolTipText(
                        "<html>The LFU algorithm evicts the page that is least frequently used</html>"
                );
                break;
            }
        }
    }

    private static class LFUComparator implements Comparator {

        @Override
        public int compare(Object o1, Object o2) {
            return ((Pair<String, Integer>) o2).getV().compareTo(((Pair<String, Integer>) o1).getV());
        }

    }

    private void sortLFUTable() {
        ArrayList<Pair<String, Integer>> rows = new ArrayList<Pair<String, Integer>>();
        for (int i = 0; i < clockTable.getRowCount(); i++) {
            if (clockTable.getValueAt(i, 0) == null) {
                break;
            }
            Pair<String, Integer> row = new Pair<String, Integer>((String) clockTable.getValueAt(i, 0), (Integer) clockTable.getValueAt(i, 1));
            rows.add(row);
        }
        Collections.sort(rows, new LFUComparator());
        for (int i = 0; i < rows.size(); i++) {
            clockTable.setValueAt(rows.get(i).getK(), i, 0);
            clockTable.setValueAt(rows.get(i).getV(), i, 1);
        }
    }

    private int findRTRow(String addr) {
        for (int i = 0; i < currRamUse; i++) {
            if (addr.equals((String) clockTable.getValueAt(i, 0))) {
                return i;
            }
        }
        return -1;
    }

    public VMJTable getInstruTable() {
        return instruTable;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public int getDiskNumLength() {
        return diskNumLength;
    }

    public int getInstruCount() {
        return instruCount;
    }

    public JTextArea getMsgPane() {
        return msgPane;
    }

    public ImagePanel getHwPane() {
        return hwPane;
    }

    public ImagePanel getOsPane() {
        return osPane;
    }

    public JTable getMissStatsTable() {
        return missStatsTable;
    }

    public JTable getRepTable() {
        return repTable;
    }

    private void test() {
        instructions.add(new Pair<Integer, Integer>(0, 15));
        instructions.add(new Pair<Integer, Integer>(1, 15 + 16 * 3));
        instructions.add(new Pair<Integer, Integer>(1, 15 + 16 * 2));
        instructions.add(new Pair<Integer, Integer>(0, 15 + 16));
        instructions.add(new Pair<Integer, Integer>(0, 15));
        instructions.add(new Pair<Integer, Integer>(0, 15 + 16 * 3));
        fsm();
        fsm();
        fsm();
        fsm();
        fsm();
        fsm();
        fsm();
        fsm();
        fsm();
        fsm();
        fsm();
        fsm();
        fsm();
        fsm();
        fsm();
        fsm();
        fsm();
        fsm();
        fsm();
        fsm();
        fsm();
        fsm();
        fsm();
        fsm();
        fsm();
        fsm();
        fsm();
        fsm();
        fsm();
        fsm();
        fsm();
        fsm();
        fsm();
        fsm();
        fsm();
        fsm();
        fsm();
        fsm();
        fsm();
        fsm();
        fsm();
        fsm();
        fsm();
        fsm();

        instructions.add(new Pair<Integer, Integer>(1, 16 * 5));
        instructions.add(new Pair<Integer, Integer>(0, 16 * 1));
        instructions.add(new Pair<Integer, Integer>(1, 16 * 4));
        instructions.add(new Pair<Integer, Integer>(0, 16 * 2));
        instructions.add(new Pair<Integer, Integer>(0, 16 * 26));
        fsm();
        fsm();
        fsm();
        fsm();
        fsm();
        fsm();
        fsm();
        fsm();
        fsm();
        fsm();
        fsm();
        fsm();
        fsm();
        fsm();
        fsm();
        fsm();
        fsm();
        fsm();
        fsm();
        fsm();
        fsm();
        fsm();
        fsm();
        fsm();
        fsm();
        fsm();
        fsm();
        fsm();
        fsm();
        fsm();
        fsm();
        fsm();
        fsm();
        fsm();
        fsm();
        fsm();
        fsm();
        fsm();
        fsm();
        fsm();
    }

}

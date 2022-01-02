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
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

/**
 *
 * @author 15002
 */
public class VMPanel extends JLayeredPane {

    private final int PULL = 0;
    private final int PTECHECK = 1;
    private final int PTADDRF = 2;
    private final int PAGEFAULT = 3;
    private final int PTRCHECK = 4;
    private final int PTEVICT = 5;
    private final int RAMWRITEB = 6;
    private final int PTUPDATE_PF = 7;
    private final int DISKWB = 8;
    private final int RAMACCESS = 9;
    private final int RAMWRITE = 10;
    private final int PTUPDATE_NOTLB = 11;
    private final int PTREPLACEC = 12;

    private final int PULL_TLB = 0;
    private final int TLBCHECK = 11;
    private final int TLBADDRF = 13;
    private final int TLB_RAMWRITE = 10;
    private final int TLB_RAMACCESS = 9;
    private final int TLBUPDATE = 14;
    private final int TLBMISS = 1;
    private final int VPFOUND = 17;
    private final int TLBRCHECK = 15;
    private final int TLBREPLACEC = 20;
    private final int PTWB = 19;
    private final int TLBEVICT = 16;
    private final int TLBUPDATE_TLBMISS = 21;
    private final int TLBSYNC = 18;

    private final int PAGETABLE = 0;
    private final int TLB = 1;

    private VMJTable pageTable;
    private VMJTable tlbTable;
    private VMJTable ramTable;
    private VMJTable diskTable;
    private JTable vmAddrLine;
    private JTable pmAddrLine;
    private VMJTable clockTable;
    private JTextArea msgPane;
    private ImagePanel hwPane;
    private ImagePanel osPane;
    private ImagePanel colorPane;
    private VMJTable instruTable;
    private LinePainter topLayer;

    private boolean tlbEnabled;
    private String offset;
    private int offsetBits;
    private int pageSize;
    private int diskNumLength;
    private int ramNumLength;
    private JLabel output;
    private int state = 0;
    private int currState;
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
    private int clockHand_PT;
    private int clockHand_TLB = 0;
    private boolean ptClockTick;
    private int instruCount = 0;

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

    private Object[][] tlbData;
    private Object[][] ptData;
    private Object[][] ramData;
    private Object[][] vmData;
    private Object[][] ctData;

    public VMPanel(int tlbSize, int ramPageNum, int diskPageNum, int offsetSize, int ramSegNum, int diskSegNum, boolean tlbEn) {
        super();
        tlbCap = tlbSize;
        pageSize = offsetSize;
        ramCap = ramSegNum;
        diskCap = diskSegNum;
        tlbEnabled = tlbEn;
        uiSetUp(tlbSize, ramPageNum, diskPageNum, ramSegNum, diskSegNum);
        tableReset(tlbSize, ramPageNum, diskPageNum, offsetSize);
        tlbData = new Object[tlbCap][5];
        ptData = new Object[diskCap][5];
        ramData = new Object[ramCap][2];
        vmData = new Object[diskCap][2];
        ctData = new Object[ramCap][1];
        msgPane.setText("Press Play Button to start simulation\n");
//        test();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        VMPanel demo = new VMPanel(10, 17, 30, 16, 4, 15, false);
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
        currState = state;
        topLayer.clearLines();
        topLayer.setPTLine(-1);
        topLayer.setRAMLine(-1);
        topLayer.setTLBLine(-1);
        int[] xsOffset = {186, 680, 680, 663};
        int[] ysOffset = {45, 45, 290, 290};
        topLayer.addLine(xsOffset, ysOffset, 4);
        if (clockHand_PT >= ramCap) {
            clockHand_PT = 0;
        }
        if (clockHand_TLB >= tlbCap) {
            clockHand_TLB = 0;
        }
        topLayer.setTLBClockLine(clockHand_TLB);
        topLayer.setRAMClockLine(clockHand_PT);
        pmAddrLine.setBackground(Color.LIGHT_GRAY);
        switch (state) {
            case PULL_TLB: {
                pagefault = false;
                tlbMiss = false;
                msgPane.setText("Start a memory reference\n");
                topLayer.setLeftRect(true);
                output.setText("Output: ");
                if (!instructions.isEmpty()) {
                    Pair<Integer, Integer> instruPair = instructions.poll();
                    instru = instruPair.getK();
                    String addr = Integer.toBinaryString(instruPair.getV());
                    String addrHex = String.format("%X", instruPair.getV(), 16);

                    if (instruPair.getV() < pageSize) {
                        currVPN = 0;
                        vmAddrLine.getModel().setValueAt(addrHex, 0, 1);
                        vmAddrLine.getModel().setValueAt(addr, 1, 1);

                    } else {
                        currVPN = Integer.parseInt(addr.substring(0, addr.length() - offsetBits), 2);
                        if (currVPN >= diskCap) {
                            msgPane.setText("Invalid virtual address\n");
                            state = PULL_TLB;
                            instruTable.setColor(instruCount, Color.green);
                            instruCount++;
                            return;
                        }
                        String binaryOffset = addr.substring(addr.length() - offsetBits);
                        vmAddrLine.getModel().setValueAt(String.format("%X", Integer.parseInt(binaryOffset,
                                2), 16), 0, 1);
                        vmAddrLine.getModel().setValueAt(binaryOffset, 1, 1);
                    }
                    instruTable.setColor(instruCount, Color.yellow);
                    state = TLBCHECK;
                    vmAddrLine.getModel().setValueAt(String.format("%X", currVPN, 16), 0, 0);
                    vmAddrLine.getModel().setValueAt(Integer.toBinaryString(currVPN), 1, 0);
                    pmAddrLine.getModel().setValueAt(String.format("%X",
                            Integer.parseInt((String) vmAddrLine.getModel().getValueAt(0, 1), 16)), 0, 1);
                } else {
                    msgPane.setText("No more instruction\n");
                    done = true;
                }
                break;
            }
            case TLBCHECK: {
                instruTable.setColor(instruCount, Color.yellow);
                topLayer.setLeftRect(true);
                if (instru == 0) {
                    msgPane.setText("Read Instruction, access TLB, check if the virtual page is\nin physical memory using TLB");
                } else if (instru == 1) {
                    msgPane.setText("Write Instruction,access TLB, check if the virtual page is\nin physical memory using TLB");
                }

                boolean pageFound = false;
                state = TLBMISS;
                for (int i = 0; i < tlbCap; i++) {
                    if (tlbTable.getValueAt(i, 1).equals(1)) {
                        int tlbCurVPN = Integer.parseInt((String) tlbTable.getValueAt(i, 0), 16);
                        if (currVPN == tlbCurVPN) {
                            pageFound = true;
                            state = TLBADDRF;
                            currTLB = i;
                            break;
                        }
                    }
                }

                int[] xs = {27, 27, 70};
                int[] ys = {55 + 16, 136 + 16 * (tlbCap - 1), 136 + 16 * (tlbCap - 1)};
                topLayer.addLine(xs, ys, 3);
                for (int i = 0; i < tlbCap - 1; i++) {
                    int[] xs1 = {27, 70};
                    int[] ys1 = {136 + 16 * i, 136 + 16 * i};
                    topLayer.addLine(xs1, ys1, 2);
                }
                backupState();
                break;
            }
            case TLBADDRF: {
                msgPane.setText("Page found in TLB, form physical memory address");
                pmAddrLine.setBackground(Color.WHITE);
                int[] xs = {27, 27, 70};
                int[] ys = {55 + 16, 136 + 16 * (tlbCap - 1), 136 + 16 * (tlbCap - 1)};
                topLayer.addLine(xs, ys, 3);
                for (int i = 0; i < tlbCap - 1; i++) {
                    int[] xs1 = {27, 70};
                    int[] ys1 = {136 + 16 * i, 136 + 16 * i};
                    topLayer.addLine(xs1, ys1, 2);
                }
                int[] xs2 = {363, 420, 420, 455};
                int[] ys2 = {136 + currTLB * 16, 136 + currTLB * 16, 290, 290};
                topLayer.addLine(xs2, ys2, 4);

                currPPN = Integer.parseInt((String) tlbTable.getModel().getValueAt(currTLB, 4), 16);
                pmAddrLine.getModel().setValueAt(tlbTable.getModel().getValueAt(currTLB, 4), 0, 0);
                if (instru == 0) {
                    state = TLB_RAMACCESS;
                } else {
                    state = TLB_RAMWRITE;
                }
                break;
            }
            case RAMACCESS: {
                msgPane.setText("Access memory");
                pmAddrLine.setBackground(Color.WHITE);
                int[] xs = {453, 430, 430, 455};
                int[] ys = {307, 307, 410 + currPPN * 16, 410 + currPPN * 16};
                topLayer.addLine(xs, ys, 4);
                state = 14;
                break;
            }
            case RAMWRITE: {
                msgPane.setText("Write to memory");
                pmAddrLine.setBackground(Color.WHITE);
                int[] xs = {453, 430, 430, 455};
                int[] ys = {307, 307, 410 + currPPN * 16, 410 + currPPN * 16};
//                ramTable.setColor(currPPN, Color.pink);
                topLayer.addLine(xs, ys, 4);
                ramTable.getModel().setValueAt("M[" + pageTable.getModel().getValueAt(currVPN, 0) + "]", currPPN, 1);
                output.setText("Output:");
                state = 14;
                break;
            }
            case TLBUPDATE: {
                msgPane.setText("Update TLB");
                tlbTable.setModify(true);
                if (!tlbMiss) {
                    tlbTable.getModel().setValueAt(1, currTLB, 2);
                } else {
                    tlbTable.getModel().setValueAt(0, currTLB, 2);
                }
//                ramTable.setColor(currPPN, Color.cyan);
                if (instru == 1) {
                    tlbTable.getModel().setValueAt(1, currTLB, 3);
//                    diskTable.setColor(currVPN, Color.orange);
                } else {
                    output.setText("Output: " + ramTable.getValueAt(currPPN, 1));
                }
                tlbTable.setModify(false);
                state = 0;
                instruTable.setColor(instruCount, Color.green);
                instruCount++;
                break;
            }
            case TLBMISS: {
                tlbMiss = true;
                topLayer.setLeftRect(true);
                msgPane.setText("TLB Miss, check if the virtual page is in physical memory");
                int[] xs = {27, 27, 70};
                int[] ys = {55 + 16, 410 + currVPN * 16, 410 + currVPN * 16};
                topLayer.addLine(xs, ys, 3);
                if (pageTable.getModel().getValueAt(currVPN, 1).equals(0)) {
                    state = PAGEFAULT; //Page fault
                } else {
                    state = VPFOUND; //Page hit
                }
                break;
            }
            case VPFOUND: {
                msgPane.setText("Page found in memory,check if TLB is full");
                if (currTLBUse == tlbCap) {
                    state = TLBRCHECK;
                } else {
                    state = TLBUPDATE_TLBMISS;
                }
                break;
            }
            case TLBRCHECK: {
                int corrVPN = Integer.parseInt((String) tlbTable.getValueAt(clockHand_TLB, 0), 16);
                int ref = (Integer) tlbTable.getValueAt(clockHand_TLB, 2);
                topLayer.setTLBLine(clockHand_TLB);
                if (ref == 0) {
                    swapVPN = corrVPN;
                    swapTLB = clockHand_TLB;
                    state = TLBREPLACEC;
                    msgPane.setText("TLBE to replace found");
                } else {
                    tlbTable.setModify(true);
                    tlbTable.setValueAt(0, clockHand_TLB, 2);
                    tlbTable.setModify(false);
                    state = TLBRCHECK;
                    msgPane.setText("look for a new TLBE to replace");
                    clockHand_TLB++;
                }

                break;
            }
            case TLBREPLACEC: {
                currTLBUse--;
                topLayer.setTLBLine(swapTLB);
                topLayer.setPTLine(swapVPN);
                msgPane.setText("Replace TLB Entry containing\nvirtual page" + tlbTable.getValueAt(swapTLB, 0));
                if ((Integer) tlbTable.getValueAt(swapTLB, 3) == 1) {
                    state = PTWB;
                } else {
                    state = TLBUPDATE_TLBMISS;
                }
                break;
            }
            case PTWB: {
                msgPane.setText("Write TLB Entry #" + swapTLB
                        + "\nto Page Table Entry " + pageTable.getValueAt(swapVPN, 0));
                int[] xs = {70, 27, 27, 70};
                int[] ys = {136 + swapTLB * 16, 136 + swapTLB * 16, 410 + swapVPN * 16, 410 + swapVPN * 16};
                topLayer.addLine(xs, ys, 4);
                topLayer.setPTLine(swapVPN);
                topLayer.setTLBLine(swapTLB);

                if ((Integer) tlbTable.getValueAt(swapTLB, 2) == 1) {
                    pageTable.setModify(true);
                    pageTable.setValueAt(1, swapVPN, 2);
                    pageTable.setModify(false);
                }
                state = TLBUPDATE_TLBMISS;
                break;
            }
            case TLBEVICT: {
                msgPane.setText("Invalidate TLB Entry #" + swapTLB);
                topLayer.setPTLine(swapVPN);
                topLayer.setTLBLine(swapTLB);
                tlbTable.setModify(true);
                tlbTable.setValueAt(0, swapTLB, 1);
                tlbTable.setModify(false);
                currTLBUse--;
                state = TLBUPDATE_TLBMISS;
                break;
            }
            case TLBUPDATE_TLBMISS: {
                int[] xs = {70, 27, 27, 70};
                int[] ys = {410 + currVPN * 16, 410 + currVPN * 16, 136 + swapTLB * 16, 136 + swapTLB * 16};
                topLayer.addLine(xs, ys, 4);
                tlbTable.setModify(true);
                tlbTable.setValueAt(pageTable.getValueAt(currVPN, 0), swapTLB, 0);
                tlbTable.setValueAt(pageTable.getValueAt(currVPN, 4), swapTLB, 4);
                tlbTable.setValueAt(pageTable.getValueAt(currVPN, 3), swapTLB, 3);
                tlbTable.setValueAt(0, swapTLB, 2);
                tlbTable.setValueAt(1, swapTLB, 1);
                tlbTable.setModify(false);
                currTLBUse++;
                swapTLB++;
                pageTable.setModify(true);
                if (!pagefault) {
                    pageTable.setValueAt(1, currVPN, 2);
                } else {
                    pageTable.setValueAt(0, currVPN, 2);
                }
                pageTable.setModify(false);

                clockHand_TLB++;
                if (clockHand_TLB >= tlbCap) {
                    clockHand_TLB = 0;
                }
                topLayer.setTLBClockLine(clockHand_TLB);

                msgPane.setText("Update TLB Entry #" + swapTLB + " and page table, advance tlb\nclockhand, and retry access.");
                state = TLBCHECK;
                break;
            }
            case PAGEFAULT: {
                pagefault = true;
                msgPane.setText("Ppage fault, after page fault, OS will set up transfer");
                topLayer.setLeftRect(false);
                ptClockTick = true;
                if (currRamUse == ramCap) {
                    state = TLBSYNC;
                    ptClockTick = false;
                } else {
                    state = RAMWRITEB;
                }
                break;
            }
            case TLBSYNC: {
                msgPane.setText("Syncronize TLB and Page Table");
                for (int i = 0; i < tlbCap; i++) {
                    if (tlbTable.getValueAt(i, 1).equals(1)) {
                        int vpnTemp = Integer.parseInt((String) tlbTable.getValueAt(i, 0), 16);
                        int dirtyTemp = (Integer) tlbTable.getValueAt(i, 3);
                        int refTemp = (Integer) tlbTable.getValueAt(i, 2);
                        pageTable.setModify(true);
                        pageTable.setValueAt(dirtyTemp, vpnTemp, 3);
                        if (refTemp == 1) {
                            pageTable.setValueAt(refTemp, vpnTemp, 2);
                        }
                        pageTable.setModify(false);
                    }
                }
                state = PTRCHECK;
                break;
            }
            case PTRCHECK:
                String corrVPNRaw = (String) ramTable.getValueAt(clockHand_PT, 1);
                int corrVPN = Integer.parseInt(corrVPNRaw.substring(2, corrVPNRaw.length() - 1), 16);
                int ref = (Integer) pageTable.getValueAt(corrVPN, 2);
                topLayer.setPTLine(corrVPN);
                topLayer.setRAMLine(clockHand_PT);
                if (ref == 0) {
                    swapVPN = corrVPN;
                    swapPPN = clockHand_PT;
                    state = 12;
                    msgPane.setText("PTE to replace found");
                } else {
                    pageTable.setModify(true);
                    pageTable.setValueAt(0, corrVPN, 2);
                    pageTable.setModify(false);
                    state = 4;
                    msgPane.setText("look for a new PTE to replace");
                }
                clockHand_PT++;
                break;
            case PTREPLACEC:
                topLayer.setPTLine(swapVPN);
                topLayer.setRAMLine(swapPPN);
                msgPane.setText("Replace data from virtual page\n" + pageTable.getValueAt(swapVPN, 0));
                if ((Integer) pageTable.getValueAt(swapVPN, 3) == 1) {
                    state = 8;
                } else {
                    state = 5;
                }
                break;
            case DISKWB: {
                msgPane.setText("Write data " + ramTable.getValueAt(swapPPN, 1)
                        + "\nto virtual page " + pageTable.getValueAt(swapVPN, 0));
                int[] xs = {673, 700, 700, 752};
                int[] ys = {410 + swapPPN * 16, 410 + swapPPN * 16, 410 + currVPN * 16, 410 + currVPN * 16};
                topLayer.addLine(xs, ys, 4);
                topLayer.setPTLine(swapVPN);
                topLayer.setRAMLine(swapPPN);
                state = 5;
                break;
            }
            case PTEVICT:
                msgPane.setText("Invalidate data " + ramTable.getValueAt(swapPPN, 1));
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
                    int vpnTemp = Integer.parseInt((String) tlbTable.getValueAt(i, 0), 16);
                    if (vpnTemp == swapVPN) {
                        tlbTable.setValueAt(0, i, 1);
                        swapTLB = i;
                        currTLBUse--;
                        msgPane.setText("Invalidate data at " + ramTable.getValueAt(swapPPN, 1)
                                + "\nand TLB Entry with virtual page\n" + tlbTable.getValueAt(swapTLB, 0));
                    }
                }
                state = 6;
                break;
            case RAMWRITEB: {
                msgPane.setText("Write data from virtual page\n"
                        + pageTable.getValueAt(swapVPN, 0)
                        + "\ninto physical page\n"
                        + ((String) ramTable.getValueAt(swapPPN, 0)).
                                substring(0, ((String) ramTable.getValueAt(swapPPN, 0)).length() - 1));
//                ramTable.setColor(swapPPN, Color.pink);
                int[] xs = {752, 700, 700, 673};
                int[] ys = {410 + currVPN * 16, 410 + currVPN * 16, 410 + swapPPN * 16, 410 + swapPPN * 16};
                topLayer.addLine(xs, ys, 4);
                ramTable.getModel().setValueAt("M[" + pageTable.getModel().getValueAt(currVPN, 0) + "]", swapPPN, 1);
                state = 7;
                break;
            }
            case PTUPDATE_PF:
                pageTable.setModify(true);
                String ppAddr = (String) ramTable.getValueAt(swapPPN, 0);
                pageTable.getModel().setValueAt(ppAddr.substring(0, ppAddr.length() - 1), currVPN, 4);
                pageTable.getModel().setValueAt(1, currVPN, 1);
                pageTable.getModel().setValueAt(0, currVPN, 2);
                clockTable.setValueAt(pageTable.getValueAt(currVPN, 0), swapPPN, 0);
//                clockTable.setColor(swapPPN, Color.green);
                if (instru == 1) {
                    pageTable.getModel().setValueAt(1, currVPN, 3);
//                    ramTable.setColor(swapPPN, Color.orange);
//                    diskTable.setColor(currVPN, Color.orange);
                } else {
                    pageTable.getModel().setValueAt(0, currVPN, 3);
//                    ramTable.setColor(swapPPN, Color.cyan);
//                    diskTable.setColor(currVPN, Color.green);
                }
                pageTable.setModify(false);
                state = TLBMISS;
                currRamUse++;
                swapPPN++;
                if (ptClockTick) {
                    clockHand_PT++;
                    if (clockHand_PT >= ramCap) {
                        clockHand_PT = 0;
                    }
                    topLayer.setRAMClockLine(clockHand_PT);
                }
                msgPane.setText("Update physical page number at virtual page " + pageTable.getValueAt(currVPN, 0)
                        + "\nin page table, retry access.");
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
        int[] xsOffset = {186, 680, 680, 663};
        int[] ysOffset = {45, 45, 290, 290};
        topLayer.addLine(xsOffset, ysOffset, 4);
        pmAddrLine.setBackground(Color.LIGHT_GRAY);
        if (clockHand_PT >= ramCap) {
            clockHand_PT = 0;
        }
        topLayer.setRAMClockLine(clockHand_PT);
        switch (state) {
            case PULL: {
                pagefault = false;
                msgPane.setText("Start a memory reference\n");
                topLayer.setLeftRect(true);
                output.setText("Output: ");
                if (!instructions.isEmpty()) {
                    Pair<Integer, Integer> instruPair = instructions.poll();
                    instru = instruPair.getK();
                    String addr = Integer.toBinaryString(instruPair.getV());
                    String addrHex = String.format("%X", instruPair.getV(), 16);
                    if (instruPair.getV() < pageSize) {
                        currVPN = 0;
                        vmAddrLine.getModel().setValueAt(addrHex, 0, 1);
                        vmAddrLine.getModel().setValueAt(addr, 1, 1);
                    } else {
                        currVPN = Integer.parseInt(addr.substring(0, addr.length() - offsetBits), 2);
                        if (currVPN >= diskCap) {
                            msgPane.setText("Invalid virtual address\n");
                            state = 0;
                            instruTable.setColor(instruCount, Color.green);
                            instruCount++;
                            return;
                        }
                        String binaryOffset = addr.substring(addr.length() - offsetBits);
                        vmAddrLine.getModel().setValueAt(String.format("%X", Integer.parseInt(binaryOffset,
                                2), 16), 0, 1);
                        vmAddrLine.getModel().setValueAt(binaryOffset, 1, 1);
                    }
                    instruTable.setColor(instruCount, Color.yellow);
                    state = PTECHECK;
                    vmAddrLine.getModel().setValueAt(String.format("%X", currVPN, 16), 0, 0);
                    vmAddrLine.getModel().setValueAt(Integer.toBinaryString(currVPN), 1, 0);
                    pmAddrLine.getModel().setValueAt(String.format("%X",
                            Integer.parseInt((String) vmAddrLine.getModel().getValueAt(0, 1), 16)), 0, 1);
                } else {
                    msgPane.setText("No more instruction\n");
                    done = true;
                }
                break;
            }
            case PTECHECK: {
                instruTable.setColor(instruCount, Color.yellow);
                topLayer.setLeftRect(true);
                if (instru == 0) {
                    msgPane.setText("Read Instruction, access page table, check if the virtual\npage is in physical memory");
                } else if (instru == 1) {
                    msgPane.setText("Write Instruction, access page table, check if the virtual\npage is in physical memory");
                }
                int[] xs = {27, 27, 70};
                int[] ys = {55 + 16, 410 + currVPN * 16, 410 + currVPN * 16};
                topLayer.addLine(xs, ys, 3);
                if (pageTable.getModel().getValueAt(currVPN, 1).equals(0)) {
                    state = 3; //Page fault
                } else {
                    state = 2; //Page hit
                }
                backupState();
                break;
            }
            case PTADDRF: {
                pmAddrLine.setBackground(Color.WHITE);
                msgPane.setText("Page found in page table, form physical memory address");
                int[] xs = {27, 27, 70};
                int[] ys = {55 + 16, 410 + currVPN * 16, 410 + currVPN * 16};
                topLayer.addLine(xs, ys, 3);
                int[] xs1 = {363, 420, 420, 455};
                int[] ys1 = {410 + currVPN * 16, 410 + currVPN * 16, 290, 290};
                topLayer.addLine(xs1, ys1, 4);
                currPPN = Integer.parseInt((String) pageTable.getModel().getValueAt(currVPN, 4), 16);
                pmAddrLine.getModel().setValueAt(pageTable.getModel().getValueAt(currVPN, 4), 0, 0);

                if (instru == 0) {
                    state = 9;
                } else {
                    state = 10;
                }
                break;
            }
            case RAMACCESS: {
                pmAddrLine.setBackground(Color.WHITE);
                msgPane.setText("Access memory");
                int[] xs = {453, 430, 430, 455};
                int[] ys = {307, 307, 410 + currPPN * 16, 410 + currPPN * 16};
                topLayer.addLine(xs, ys, 4);
                state = 11;
                break;
            }
            case RAMWRITE: {
                pmAddrLine.setBackground(Color.WHITE);
                msgPane.setText("Write to memory");
                int[] xs = {453, 430, 430, 455};
                int[] ys = {307, 307, 410 + currPPN * 16, 410 + currPPN * 16};
//                ramTable.setColor(currPPN, Color.pink);
                topLayer.addLine(xs, ys, 4);
                ramTable.getModel().setValueAt("M[" + pageTable.getModel().getValueAt(currVPN, 0) + "]", currPPN, 1);
                output.setText("Output:");
                state = 11;
                break;
            }
            case PTUPDATE_NOTLB:
                msgPane.setText("Update page table");
                pageTable.setModify(true);
                if (!pagefault) {
                    pageTable.getModel().setValueAt(1, currVPN, 2);
                } else {
                    pageTable.getModel().setValueAt(0, currVPN, 2);
                }
//                ramTable.setColor(currPPN, Color.cyan);
                if (instru == 1) {
                    pageTable.getModel().setValueAt(1, currVPN, 3);
//                    diskTable.setColor(currVPN, Color.orange);
                } else {
                    output.setText("Output: " + ramTable.getValueAt(currPPN, 1));
                }
                pageTable.setModify(false);
                state = 0;
                instruTable.setColor(instruCount, Color.green);
                instruCount++;
                break;
            case PAGEFAULT: {
                pagefault = true;
                msgPane.setText("Ppage fault, after page fault, OS will set up transfer");
                topLayer.setLeftRect(false);
                int[] xs = {27, 27, 70};
                int[] ys = {55 + 16, 410 + currVPN * 16, 410 + currVPN * 16};
                topLayer.addLine(xs, ys, 3);
                ptClockTick = true;
                if (currRamUse == ramCap) {
                    state = 4;
                    ptClockTick = false;
                } else {
                    state = 6;
                }
                break;
            }
            case PTRCHECK:
                String corrVPNRaw = (String) ramTable.getValueAt(clockHand_PT, 1);
                int corrVPN = Integer.parseInt(corrVPNRaw.substring(2, corrVPNRaw.length() - 1), 16);
                int ref = (Integer) pageTable.getValueAt(corrVPN, 2);
                topLayer.setPTLine(corrVPN);
                topLayer.setRAMLine(clockHand_PT);
                if (ref == 0) {
                    swapVPN = corrVPN;
                    swapPPN = clockHand_PT;
                    state = 12;
                    msgPane.setText("PTE to replace found");
                } else {
                    pageTable.setModify(true);
                    pageTable.setValueAt(0, corrVPN, 2);
                    pageTable.setModify(false);
                    state = 4;
                    msgPane.setText("look for a new PTE to replace");
                }
                clockHand_PT++;
                break;
            case PTREPLACEC:
                topLayer.setPTLine(swapVPN);
                topLayer.setRAMLine(swapPPN);
                msgPane.setText("Replace data from virtual page\n" + pageTable.getValueAt(swapVPN, 0));
                if ((Integer) pageTable.getValueAt(swapVPN, 3) == 1) {
                    state = 8;
                } else {
                    state = 5;
                }
                break;
            case DISKWB: {
                msgPane.setText("Write data " + ramTable.getValueAt(swapPPN, 1)
                        + "\nto virtual page " + pageTable.getValueAt(swapVPN, 0));
                int[] xs = {673, 700, 700, 752};
                int[] ys = {410 + swapPPN * 16, 410 + swapPPN * 16, 410 + currVPN * 16, 410 + currVPN * 16};
                topLayer.addLine(xs, ys, 4);
                topLayer.setPTLine(swapVPN);
                topLayer.setRAMLine(swapPPN);
                state = 5;
                break;
            }
            case PTEVICT:
                msgPane.setText("Invalidate data " + ramTable.getValueAt(swapPPN, 1));
//                ramTable.setColor(swapPPN, Color.white);
//                diskTable.setColor(swapVPN, Color.white);
                topLayer.setPTLine(swapVPN);
                topLayer.setRAMLine(swapPPN);
                pageTable.setModify(true);
                pageTable.setValueAt(0, swapVPN, 1);
                pageTable.setModify(false);
//                clockTable.setColor(swapPPN, Color.white);
                currRamUse--;
                state = 6;
                break;
            case RAMWRITEB: {
                msgPane.setText("Write data from virtual page\n"
                        + pageTable.getValueAt(swapVPN, 0)
                        + "\ninto physical page\n"
                        + ((String) ramTable.getValueAt(swapPPN, 0)).
                                substring(0, ((String) ramTable.getValueAt(swapPPN, 0)).length() - 1));
//                ramTable.setColor(swapPPN, Color.pink);
                int[] xs = {752, 700, 700, 673};
                int[] ys = {410 + currVPN * 16, 410 + currVPN * 16, 410 + swapPPN * 16, 410 + swapPPN * 16};
                topLayer.addLine(xs, ys, 4);
                ramTable.getModel().setValueAt("M[" + pageTable.getModel().getValueAt(currVPN, 0) + "]", swapPPN, 1);
                state = 7;
                break;
            }
            case PTUPDATE_PF:
                pageTable.setModify(true);
                String ppAddr = (String) ramTable.getValueAt(swapPPN, 0);
                pageTable.getModel().setValueAt(ppAddr.substring(0, ppAddr.length() - 1), currVPN, 4);
                pageTable.getModel().setValueAt(1, currVPN, 1);
                pageTable.getModel().setValueAt(0, currVPN, 2);
                clockTable.setValueAt(pageTable.getValueAt(currVPN, 0), swapPPN, 0);
//                clockTable.setColor(swapPPN, Color.green);
                if (instru == 1) {
                    pageTable.getModel().setValueAt(1, currVPN, 3);
//                    ramTable.setColor(swapPPN, Color.orange);
//                    diskTable.setColor(currVPN, Color.orange);
                } else {
                    pageTable.getModel().setValueAt(0, currVPN, 3);
//                    ramTable.setColor(swapPPN, Color.cyan);
//                    diskTable.setColor(currVPN, Color.green);
                }
                pageTable.setModify(false);
                state = 1;
                currRamUse++;
                swapPPN++;
                if (ptClockTick) {
                    clockHand_PT++;
                    if (clockHand_PT >= ramCap) {
                        clockHand_PT = 0;
                    }
                    topLayer.setRAMClockLine(clockHand_PT);
                }
                msgPane.setText("Update physical page number at virtual page " + pageTable.getValueAt(currVPN, 0)
                        + "\nin page table, retry access.");
                break;
            default:
//                System.out.println("Something is wrong");
                break;
        }
//        System.out.println(state);
    }

    private void backupState() {
        currVPNBackup = currVPN;
        currPPNBackup = currPPN;
        currRamUseBackup = currRamUse;
        currTLBUseBackup = currTLBUse;
        swapVPNBackup = swapVPN;
        swapPPNBackup = swapPPN;
        currTLBUseBackup = currTLB;
        swapTLBBackup = swapTLB;
        pagefaultBackup = pagefault;
        tlbMissBackup = tlbMiss;
        clockHand_PTBackup = clockHand_PT;
        clockHand_TLBBackup = clockHand_TLB;
        ptClockTickBackup = ptClockTick;
        instruCountBackup = instruCount;
        for (int col = 0; col < 5; col++) {
            for (int row = 0; row < tlbCap; row++) {
                tlbData[row][col] = tlbTable.getValueAt(row, col);
            }
            for (int row = 0; row < ramCap; row++) {
                ptData[row][col] = pageTable.getValueAt(row, col);
            }
        }
        for (int col = 0; col < 2; col++) {
            for (int row = 0; row < tlbCap; row++) {
                ramData[row][col] = ramTable.getValueAt(row, col);
            }
            for (int row = 0; row < ramCap; row++) {
                vmData[row][col] = diskTable.getValueAt(row, col);
            }
        }
        for (int row = 0; row < ramCap; row++) {
            ctData[row][0] = clockTable.getValueAt(row, 0);
        }
        pmAddrBackup = pmAddrLine.getValueAt(0, 0);
    }

    public void recoverState() {
        if (currState != PULL_TLB) {
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
            for (int col = 0; col < 5; col++) {
                if (tlbEnabled) {
                    for (int row = 0; row < tlbCap; row++) {
                        tlbTable.setModify(true);
                        tlbTable.setValueAt(tlbData[row][col], row, col);
                        tlbTable.setModify(false);
                    }
                }
                pageTable.setModify(true);
                for (int row = 0; row < ramCap; row++) {
                    pageTable.setValueAt(ptData[row][col], row, col);
                }
                pageTable.setModify(false);
            }
            for (int col = 0; col < 2; col++) {
                ramTable.setModify(true);
                for (int row = 0; row < tlbCap; row++) {
                    ramTable.setValueAt(ramData[row][col], row, col);
                }
                ramTable.setModify(false);
                diskTable.setModify(true);
                for (int row = 0; row < ramCap; row++) {
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
            instruTable.setColor(instruCount, Color.yellow);
            if (tlbEnabled) {
                state = 11;
            } else {
                state = 1;
            }
            fsm();
            repaint();
        }
    }

    private void uiSetUp(int tlbSize, int ramPageNum, int diskPageNum, int ramSegNum, int diskSegNum) {
        tlbTable = new VMJTable(tlbSize, 5, TLB);
        tlbTable.getTableHeader().setReorderingAllowed(false);
        tlbTable.getColumnModel().getColumn(0).setPreferredWidth(85);
        tlbTable.getColumnModel().getColumn(1).setPreferredWidth(31);
        tlbTable.getColumnModel().getColumn(2).setPreferredWidth(31);
        tlbTable.getColumnModel().getColumn(3).setPreferredWidth(23);
        tlbTable.getColumnModel().getColumn(4).setPreferredWidth(85);

        tlbTable.getColumnModel().getColumn(0).setHeaderValue("Virtual Page#");
        tlbTable.getColumnModel().getColumn(1).setHeaderValue("V");
        tlbTable.getColumnModel().getColumn(2).setHeaderValue("R");
        tlbTable.getColumnModel().getColumn(3).setHeaderValue("D");
        tlbTable.getColumnModel().getColumn(4).setHeaderValue("Physical Page#");
        tlbTable.setMinimumSize(new Dimension(31 * 2 + 23 + 85 * 2, tlbSize * 16));
        tlbTable.setPreferredSize(new Dimension(31 * 2 + 23 + 85 * 2, tlbSize * 16));
        JScrollPane tlbPane = new JScrollPane(tlbTable);
        if (!tlbEnabled) {
            tlbTable.setBackground(Color.GRAY);
        }
        tlbTable.setEnabled(false);
        VMJTable.setTLBEnabled(true);
        if (!tlbEnabled) {
            tlbPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                    "Translation Lookaside Buffer (Disabled)", TitledBorder.CENTER, TitledBorder.TOP));
        } else {
            tlbPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                    "Translation Lookaside Buffer", TitledBorder.CENTER, TitledBorder.TOP));
        }
        pageTable = new VMJTable(diskPageNum, 5, PAGETABLE);
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
        pageTable.setEnabled(false);
        pagePane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "Page Table", TitledBorder.CENTER, TitledBorder.TOP));

        ramTable = new VMJTable(ramPageNum, 2, 2, ramSegNum);
        ramTable.getTableHeader().setReorderingAllowed(false);
        ramTable.getColumnModel().getColumn(0).setPreferredWidth(95);
        ramTable.getColumnModel().getColumn(1).setPreferredWidth(85);
        ramTable.getColumnModel().getColumn(0).setHeaderValue("Physical Page Addr");
        ramTable.getColumnModel().getColumn(1).setHeaderValue("Data");
        ramTable.setMinimumSize(new Dimension(85 + 95, ramPageNum * 16));
        ramTable.setPreferredSize(new Dimension(85 + 95, ramPageNum * 16));
        JScrollPane ramPane = new JScrollPane(ramTable);
        ramTable.setEnabled(false);
        ramPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "Physical Memory", TitledBorder.CENTER, TitledBorder.TOP));

        diskTable = new VMJTable(diskPageNum, 2, 2, diskSegNum);
        diskTable.getTableHeader().setReorderingAllowed(false);
        diskTable.getColumnModel().getColumn(0).setPreferredWidth(95);
        diskTable.getColumnModel().getColumn(1).setPreferredWidth(85);
        diskTable.getColumnModel().getColumn(0).setHeaderValue("Vistual Page Addr");
        diskTable.getColumnModel().getColumn(1).setHeaderValue("Data");
        diskTable.setMinimumSize(new Dimension(85 + 95, diskPageNum * 16));
        diskTable.setPreferredSize(new Dimension(85 + 95, diskPageNum * 16));
        JScrollPane diskPane = new JScrollPane(diskTable);
        diskTable.setEnabled(false);
        diskPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "Virtual Memory", TitledBorder.CENTER, TitledBorder.TOP));

        pmAddrLine = new JTable(1, 2);
        pmAddrLine.getTableHeader().setReorderingAllowed(false);
        pmAddrLine.getColumnModel().getColumn(0).setPreferredWidth(85);
        pmAddrLine.getColumnModel().getColumn(1).setPreferredWidth(85);
        pmAddrLine.getColumnModel().getColumn(0).setHeaderValue("Physical Page#");
        pmAddrLine.getColumnModel().getColumn(1).setHeaderValue("Offset");
        pmAddrLine.setMinimumSize(new Dimension(85 * 2, 16));
        pmAddrLine.setPreferredSize(new Dimension(85 * 2, 16));
        JScrollPane pmlPane = new JScrollPane(pmAddrLine);
        pmAddrLine.setEnabled(false);
        pmlPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "Physical Memory Address", TitledBorder.CENTER, TitledBorder.BOTTOM));
        pmAddrLine.setBackground(Color.LIGHT_GRAY);

        vmAddrLine = new JTable(2, 2);
        vmAddrLine.getTableHeader().setReorderingAllowed(false);
        vmAddrLine.getColumnModel().getColumn(0).setPreferredWidth(85);
        vmAddrLine.getColumnModel().getColumn(1).setPreferredWidth(31);
        vmAddrLine.getColumnModel().getColumn(0).setHeaderValue("Virtual Page#");
        vmAddrLine.getColumnModel().getColumn(1).setHeaderValue("Offset");
        vmAddrLine.setMinimumSize(new Dimension(85 + 31, 32));
        vmAddrLine.setPreferredSize(new Dimension(85 + 31, 32));
        JScrollPane vmlPane = new JScrollPane(vmAddrLine);
        vmAddrLine.setEnabled(false);
        vmlPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "Virtual Memory Address", TitledBorder.CENTER, TitledBorder.TOP));

        clockTable = new VMJTable(ramSegNum, 1, 2, ramSegNum);
        clockTable.getTableHeader().setReorderingAllowed(false);
        clockTable.getColumnModel().getColumn(0).setPreferredWidth(85);
        clockTable.getColumnModel().getColumn(0).setHeaderValue("Virtual Page#");
        clockTable.setMinimumSize(new Dimension(85, 16 * ramSegNum));
        JScrollPane clockPane = new JScrollPane(clockTable);
        clockTable.setEnabled(false);
        clockPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "Clock Table", TitledBorder.CENTER, TitledBorder.TOP));

//        hhdPane = new ImagePanel("images/hardDisk.png");
//        hhdPane.setPreferredSize(new Dimension(300, 200));
//        hhdPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
//                "Virtual Memory", TitledBorder.CENTER, TitledBorder.TOP));
        hwPane = new ImagePanel("images/hardware.png");
        hwPane.setPreferredSize(new Dimension(80, 80));

        osPane = new ImagePanel("images/os.png");
        osPane.setPreferredSize(new Dimension(80, 80));
        
        colorPane = new ImagePanel("images/colorGuide.png");
        colorPane.setPreferredSize(new Dimension(160, 120));

        topLayer = new LinePainter();
        topLayer.setPreferredSize(new Dimension(1150, 365 + (diskPageNum + 4) * 16));
        topLayer.setBackground(Color.blue);
        topLayer.setOpaque(false);

        output = new JLabel("Output:");
        output.setPreferredSize(new Dimension(200, 50));
        output.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "Data Output", TitledBorder.CENTER, TitledBorder.TOP));

        msgPane = new JTextArea("");
        msgPane.setPreferredSize(new Dimension(160, 50));
        msgPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "Information", TitledBorder.CENTER, TitledBorder.TOP));

        setLayout(null);

        add(vmlPane, JLayeredPane.DEFAULT_LAYER);
        add(tlbPane, JLayeredPane.DEFAULT_LAYER);
        add(pmlPane, JLayeredPane.DEFAULT_LAYER);
        add(pagePane, JLayeredPane.DEFAULT_LAYER);
        add(ramPane, JLayeredPane.DEFAULT_LAYER);
        add(diskPane, JLayeredPane.DEFAULT_LAYER);
        add(hwPane, JLayeredPane.DEFAULT_LAYER);
        add(osPane, JLayeredPane.DEFAULT_LAYER);
        add(colorPane, JLayeredPane.DEFAULT_LAYER);
        add(topLayer, JLayeredPane.PALETTE_LAYER);
        add(output, JLayeredPane.DEFAULT_LAYER);
        add(msgPane, JLayeredPane.DEFAULT_LAYER);
        add(clockPane, JLayeredPane.DEFAULT_LAYER);

        Insets insets = getInsets();

        Dimension size = vmAddrLine.getPreferredSize();
        vmlPane.setBounds(25 + insets.left, 0 + insets.top,
                size.width + 50, size.height + 45);

        size = tlbTable.getPreferredSize();
        tlbPane.setBounds(65 + insets.left, 90 + insets.top,
                size.width + 50, size.height + 45);

        size = pmAddrLine.getPreferredSize();
        pmlPane.setBounds(450 + insets.left, 255 + insets.top,
                size.width + 50, size.height + 45);

        size = pageTable.getPreferredSize();
        pagePane.setBounds(65 + insets.left, 365 + insets.top,
                size.width + 50, size.height + 45);

        size = ramTable.getPreferredSize();
        ramPane.setBounds(450 + insets.left, 365 + insets.top,
                size.width + 50, size.height + 45);

        size = diskTable.getPreferredSize();
        diskPane.setBounds(750 + insets.left, 365 + insets.top,
                size.width + 50, size.height + 45);

        size = clockTable.getPreferredSize();
        clockPane.setBounds(1000 + insets.left, 365 + insets.top,
                size.width + 50, size.height + 45);

        size = hwPane.getPreferredSize();
        hwPane.setBounds(720 + insets.left, -15 + insets.top,
                size.width + 30, size.height + 30);

        size = osPane.getPreferredSize();
        osPane.setBounds(820 + insets.left, -15 + insets.top,
                size.width + 30, size.height + 30);
        
        size = colorPane.getPreferredSize();
        colorPane.setBounds(430 + insets.left, 70 + insets.top,
                size.width + 30, size.height + 30);

        size = topLayer.getPreferredSize();
        topLayer.setBounds(insets.left, insets.top,
                size.width, size.height);

        size = output.getPreferredSize();
        output.setBounds(745 + insets.left, 120 + insets.top,
                size.width + 130, size.height + 20);

        size = msgPane.getPreferredSize();
        msgPane.setBackground(getBackground());
        msgPane.setBounds(745 + insets.left, 190 + insets.top,
                size.width + 170, size.height + 50);
        msgPane.setEditable(false);

        setPreferredSize(new Dimension(1150, 365 + (diskPageNum + 4) * 16));

    }

    private void tableReset(int tlbSize, int ramPageNum, int diskPageNum, int pageSize) {
        offset = String.format("%X", pageSize - 1);
        diskNumLength = Integer.toHexString(diskPageNum).length();
        ramNumLength = Integer.toHexString(ramPageNum).length();

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
            }
            tlbTable.setModify(false);
        }

        for (int i = 0; i < ramPageNum; i++) {
            ramTable.getModel().setValueAt(String.format("%0" + ramNumLength + "X", i) + offset, i, 0);
        }

        for (int i = 0; i < diskPageNum; i++) {
            diskTable.getModel().setValueAt(String.format("%0" + diskNumLength + "X", i) + offset, i, 0);
            diskTable.getModel().setValueAt(String.format("M[" + "%0" + diskNumLength + "X", i) + "]", i, 1);
        }

        offsetBits = Integer.toBinaryString(pageSize - 1).length();
    }

    private static class ImagePanel extends JPanel {

        private BufferedImage image;

        public ImagePanel(String addr) {
            super();
            try {
                image = ImageIO.read(getClass().getResource(addr));
            } catch (IOException e) {
            }
        }

        @Override
        public void paintComponent(Graphics g) {
            g.drawImage(image, 25, 25, this);
        }
    }

    public LinkedList<Pair<Integer, Integer>> getInstructions() {
        return instructions;
    }

    public void setInstructions(LinkedList<Pair<Integer, Integer>> instructions) {
        this.instructions = instructions;
        instruTable = new VMJTable(instructions.size(), 2, 2, instructions.size());
        instruTable.setEnabled(false);
        instruTable.getColumnModel().getColumn(0).setPreferredWidth(31);
        instruTable.getColumnModel().getColumn(1).setPreferredWidth(95);
        instruTable.getColumnModel().getColumn(0).setHeaderValue("r/w");
        instruTable.getColumnModel().getColumn(1).setHeaderValue("Virtual Address");
        instruTable.getTableHeader().setReorderingAllowed(false);
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

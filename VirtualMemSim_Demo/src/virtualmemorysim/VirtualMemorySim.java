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
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

/**
 *
 * @author 15002
 */
public class VirtualMemorySim {

    private final int PAGETABLE = 0;
    private final int TLB = 1;

    JScrollPane mainPane;
    private VMJTable pageTable;
    private VMJTable tlbTable;
    private JTable ramTable;
    private JTable vmAddrLine;
    private JTable pmAddrLine;
    private ImagePanel hhdPane;
    private ImagePanel hwPane;
    private ImagePanel osPane;
    private LinePainter topLayer;

    private String offset;
    private int offsetBits;
    private int pageSize;
    private int diskNumLength;
    private int ramNumLength;
    private JLabel output;
    private int state = 0;
    private LinkedList<Pair<Integer, Integer>> instructions = new LinkedList<Pair<Integer, Integer>>();

    private int currVPN;
    private int currPPN;
    private int instru;
    private int currRamUse;
    private int swapVPN;
    private int swapPPN;
    private int ramCap;
    private int clockHand = 1;

    public VirtualMemorySim(int tlbSize, int ramPageNum, int diskPageNum, int offsetSize) {
        pageSize = offsetSize;
        ramCap = ramPageNum;
        uiSetUp(tlbSize, ramPageNum, diskPageNum);
        tableReset(tlbSize, ramPageNum, diskPageNum, offsetSize);
        pageTable.setModify(true);
        pageTable.getModel().setValueAt(1, 0, 1);
        pageTable.getModel().setValueAt(1, 0, 2);
        pageTable.getModel().setValueAt(0, 0, 3);
        pageTable.getModel().setValueAt(0, 0, 4);
        ramTable.setValueAt("M[00]", 0, 1);

        pageTable.getModel().setValueAt(1, 1, 1);
        pageTable.getModel().setValueAt(1, 2, 1);
        pageTable.getModel().setValueAt(1, 3, 1);
        pageTable.getModel().setValueAt(1, 1, 2);
        pageTable.getModel().setValueAt(1, 2, 2);
        pageTable.getModel().setValueAt(1, 3, 2);
        pageTable.getModel().setValueAt(1, 1, 3);
        pageTable.getModel().setValueAt(1, 2, 3);
        pageTable.getModel().setValueAt(1, 3, 3);
        pageTable.getModel().setValueAt(3, 1, 4);
        pageTable.getModel().setValueAt(2, 2, 4);
        pageTable.getModel().setValueAt(1, 3, 4);
        ramTable.setValueAt("M[03]", 1, 1);
        ramTable.setValueAt("M[02]", 2, 1);
        ramTable.setValueAt("M[01]", 3, 1);
        currRamUse = 4;
        pageTable.setModify(false);
        instructions.add(new Pair<Integer, Integer>(0, 16 * 4));
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

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        VirtualMemorySim demo = new VirtualMemorySim(10, 4, 30, 16);
        JFrame frame = new JFrame("AbsoluteLayoutDemo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(demo.mainPane);
        frame.setVisible(true);
        frame.setSize(1200, 700);
    }

    private void fsm() {
        topLayer.clearLines();
        topLayer.setCircleLine(-1);
        if (state == 0) {
            //set information in msg board
            topLayer.setLeftRect(true);
            output.setText("Output: ");
            if (!instructions.isEmpty()) {
                Pair<Integer, Integer> instruPair = instructions.poll();
                instru = instruPair.getK();
                String addr = Integer.toBinaryString(instruPair.getV());
                if (instruPair.getV() < pageSize) {
                    currVPN = 0;
                    vmAddrLine.getModel().setValueAt(addr, 0, 1);

                } else {
                    currVPN = Integer.parseInt(addr.substring(0, addr.length() - offsetBits), 2);
                    vmAddrLine.getModel().setValueAt(addr.substring(addr.length() - offsetBits), 0, 1);
                }
                state = 1;
                vmAddrLine.getModel().setValueAt(Integer.toBinaryString(currVPN), 0, 0);

            } else {
                //set message no more instructions
            }
        } else if (state == 1) {
            if (instru == 0) {
                //Set msg "read from ram, see if the vpf is in ram"
            } else if (instru == 1) {
                //set msg "write to ram, see if the vpf is in ram"
            }
            int[] xs = {35, 35, 70};
            int[] ys = {53, 410 + currVPN * 16, 410 + currVPN * 16};

            topLayer.addLine(xs, ys, 3);
            if (pageTable.getModel().getValueAt(currVPN, 1).equals(0)) {
                state = 3; //Page fault
            } else {
                state = 2; //Page hit
            }
        } else if (state == 2) {
            //set msg: page found in memory, form addr
            int[] xs = {35, 35, 70};
            int[] ys = {53, 410 + currVPN * 16, 410 + currVPN * 16};
            topLayer.addLine(xs, ys, 3);
            int[] xs1 = {363, 420, 420, 455};
            int[] ys1 = {410 + currVPN * 16, 410 + currVPN * 16, 290, 290};
            topLayer.addLine(xs1, ys1, 4);
            int[] xs2 = {186, 680, 680, 663};
            int[] ys2 = {45, 45, 290, 290};
            topLayer.addLine(xs2, ys2, 4);
            currPPN = (Integer) pageTable.getModel().getValueAt(currVPN, 4);
            pmAddrLine.getModel().setValueAt(currPPN, 0, 0);
            pmAddrLine.getModel().setValueAt(
                    String.format("%X", Integer.parseInt((String) vmAddrLine.getModel().getValueAt(0, 1), 2)),
                    0, 1);
            if (instru == 0) {
                state = 9;
            } else {
                state = 10;
            }
        } else if (state == 9) {
            //set msg: Access Memory for data
            int[] xs = {453, 430, 430, 455};
            int[] ys = {307, 307, 410 + currPPN * 16, 410 + currPPN * 16};
            topLayer.addLine(xs, ys, 4);
            output.setText("Output: [M]" + currPPN);
            state = 11;
        } else if (state == 10) {
            //set msg: writing to memory
            int[] xs = {453, 430, 430, 455};
            int[] ys = {307, 307, 410 + currPPN * 16, 410 + currPPN * 16};
            topLayer.addLine(xs, ys, 4);
            ramTable.getModel().setValueAt("M[" + pageTable.getModel().getValueAt(currVPN, 0) + "]", currPPN, 1);
            output.setText("Output: [M] ");
            state = 11;
        } else if (state == 11) {
            //set msg: update page table
//            pmAddrLine.getModel().setValueAt(null, 0, 0);
//            pmAddrLine.getModel().setValueAt(null, 0, 1);
            pageTable.setModify(true);
            pageTable.getModel().setValueAt(1, currVPN, 2);
            if (instru == 1) {
                pageTable.getModel().setValueAt(1, currVPN, 3);
            }
            pageTable.setModify(false);
            state = 0;
        } else if (state == 3) {
            //set msg: page fault, leave control to os
            topLayer.setLeftRect(false);
            int[] xs = {35, 35, 70};
            int[] ys = {53, 410 + currVPN * 16, 410 + currVPN * 16};

            topLayer.addLine(xs, ys, 3);
            if (currRamUse == ramCap) {
                state = 4;
            } else if (instru == 0) {
                state = 6;
            } else if (instru == 1) {
                state = 12;
            }
        } else if (state == 4) {
            boolean found = false;
            String corrVPNRaw = (String) ramTable.getValueAt(clockHand, 1);
            int corrVPN = Integer.parseInt(corrVPNRaw.substring(2, corrVPNRaw.length() - 1), 16);
            int ref = (Integer) pageTable.getValueAt(corrVPN, 2);
            if (ref == 1) {
                pageTable.setModify(true);
                pageTable.setValueAt(0, corrVPN, 2);
                pageTable.setModify(false);
                clockHand++;
            }
            while (!found) {
                corrVPNRaw = (String) ramTable.getValueAt(clockHand, 1);
                corrVPN = Integer.parseInt(corrVPNRaw.substring(2, corrVPNRaw.length() - 1), 16);
                ref = (Integer) pageTable.getValueAt(corrVPN, 2);
                if (ref == 0) {
                    swapVPN = corrVPN;
                    swapPPN = clockHand;

                    //set ram space to white
                    found = true;
                    topLayer.setCircleLine(swapVPN);
                    System.out.println(corrVPN);
                }
                clockHand++;
                if (clockHand == ramCap) {
                    clockHand = 0;
                }
                //set msg: replace page xxx
            }
            if ((Integer) pageTable.getValueAt(swapVPN, 3) == 1) {
                state = 8;
            } else {
                state = 5;
            }
        } else if (state == 8) {
            //set msg : write to virtual memory with the new data
            int[] xs = {673, 700, 700, 752};
            int[] ys = {410 + swapPPN * 16, 410 + swapPPN * 16, 372, 372};
            topLayer.addLine(xs, ys, 4);
            topLayer.setCircleLine(swapVPN);
            state = 5;
        } else if (state == 5) {
            //set msg : evict data from ram
            //set ram line gray
            topLayer.setCircleLine(swapVPN);
            pageTable.setModify(true);
            pageTable.setValueAt(0, swapVPN, 1);
            pageTable.setModify(false);
            if (instru == 0) {
                state = 6;
            } else {
                state = 12;
            }
        } else if (state == 6) {
            //set msg : write new data into ram
            //set ram line red
            topLayer.setCircleLine(swapVPN);
            int[] xs = {752, 700, 700, 673};
            int[] ys = {372, 372, 410 + swapPPN * 16, 410 + swapPPN * 16};
            topLayer.addLine(xs, ys, 4);
            ramTable.getModel().setValueAt("M[" + pageTable.getModel().getValueAt(currVPN, 0) + "]", swapPPN, 1);
            state = 7;
        } else if (state == 12) {
            //set msg : write new data into ram
            //set ram line red
            topLayer.setCircleLine(swapVPN);
            ramTable.getModel().setValueAt("M[" + pageTable.getModel().getValueAt(currVPN, 0) + "]", swapPPN, 1);
            state = 7;
        } else if (state == 7) {
            //set msg: update page table
            pageTable.setModify(true);
            pageTable.getModel().setValueAt(ramTable.getValueAt(swapPPN, 0), currVPN, 4);
            pageTable.getModel().setValueAt(1, currVPN, 1);
            pageTable.getModel().setValueAt(0, currVPN, 2);
            if (instru == 1) {
                pageTable.getModel().setValueAt(1, currVPN, 3);
            } else {
                pageTable.getModel().setValueAt(0, currVPN, 3);
            }
            pageTable.setModify(false);
            state = 0;
        }
        System.out.println(state);
    }

    private void uiSetUp(int tlbSize, int ramPageNum, int diskPageNum) {
        JLayeredPane pane = new JLayeredPane();

        tlbTable = new VMJTable(tlbSize, 5, TLB);
        tlbTable.getTableHeader().setReorderingAllowed(false);
        tlbTable.getColumnModel().getColumn(0).setPreferredWidth(85);
        tlbTable.getColumnModel().getColumn(1).setPreferredWidth(31);
        tlbTable.getColumnModel().getColumn(2).setPreferredWidth(31);
        tlbTable.getColumnModel().getColumn(3).setPreferredWidth(23);
        tlbTable.getColumnModel().getColumn(4).setPreferredWidth(85);

        tlbTable.getColumnModel().getColumn(0).setHeaderValue("Virtual Page#");
        tlbTable.getColumnModel().getColumn(1).setHeaderValue("Valid");
        tlbTable.getColumnModel().getColumn(2).setHeaderValue("Ref");
        tlbTable.getColumnModel().getColumn(3).setHeaderValue("Dirty");
        tlbTable.getColumnModel().getColumn(4).setHeaderValue("Physical Page#");
        tlbTable.setMinimumSize(new Dimension(31 * 2 + 23 + 85 * 2, tlbSize * 16));
        tlbTable.setPreferredSize(new Dimension(31 * 2 + 23 + 85 * 2, tlbSize * 16));
        JScrollPane tlbPane = new JScrollPane(tlbTable);
        tlbTable.setBackground(Color.LIGHT_GRAY);
        tlbTable.setEnabled(false);
        tlbPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "Translation Lookaside Buffer (Disabled)", TitledBorder.CENTER, TitledBorder.TOP));

        pageTable = new VMJTable(diskPageNum, 5, PAGETABLE);
        pageTable.getTableHeader().setReorderingAllowed(false);
        pageTable.getColumnModel().getColumn(0).setPreferredWidth(85);
        pageTable.getColumnModel().getColumn(1).setPreferredWidth(31);
        pageTable.getColumnModel().getColumn(2).setPreferredWidth(31);
        pageTable.getColumnModel().getColumn(3).setPreferredWidth(23);
        pageTable.getColumnModel().getColumn(4).setPreferredWidth(85);
        pageTable.getColumnModel().getColumn(0).setHeaderValue("Virtual Page#");
        pageTable.getColumnModel().getColumn(1).setHeaderValue("Valid");
        pageTable.getColumnModel().getColumn(2).setHeaderValue("Ref");
        pageTable.getColumnModel().getColumn(3).setHeaderValue("Dirty");
        pageTable.getColumnModel().getColumn(4).setHeaderValue("Physical Page#");
        pageTable.setMinimumSize(new Dimension(31 * 2 + 23 + 85 * 2, diskPageNum * 16));
        pageTable.setPreferredSize(new Dimension(31 * 2 + 23 + 85 * 2, diskPageNum * 16));
        JScrollPane pagePane = new JScrollPane(pageTable);
        pageTable.setEnabled(false);
        pagePane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "Page Table", TitledBorder.CENTER, TitledBorder.TOP));

        ramTable = new JTable(ramPageNum, 2);
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

        vmAddrLine = new JTable(1, 2);
        vmAddrLine.getTableHeader().setReorderingAllowed(false);
        vmAddrLine.getColumnModel().getColumn(0).setPreferredWidth(85);
        vmAddrLine.getColumnModel().getColumn(1).setPreferredWidth(31);
        vmAddrLine.getColumnModel().getColumn(0).setHeaderValue("Virtual Page#");
        vmAddrLine.getColumnModel().getColumn(1).setHeaderValue("Offset");
        vmAddrLine.setMinimumSize(new Dimension(85 + 31, 16));
        vmAddrLine.setPreferredSize(new Dimension(85 + 31, 16));
        JScrollPane vmlPane = new JScrollPane(vmAddrLine);
        vmAddrLine.setEnabled(false);
        vmlPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "Virtual Memory Address", TitledBorder.CENTER, TitledBorder.TOP));

        hhdPane = new ImagePanel("images/hardDisk.png");
        hhdPane.setPreferredSize(new Dimension(300, 200));
        hhdPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "Virtual Memory", TitledBorder.CENTER, TitledBorder.TOP));

        hwPane = new ImagePanel("images/hardware.png");
        hwPane.setPreferredSize(new Dimension(80, 80));

        osPane = new ImagePanel("images/os.png");
        osPane.setPreferredSize(new Dimension(80, 80));

        topLayer = new LinePainter();
        topLayer.setPreferredSize(new Dimension(1100, 365 + (diskPageNum + 4) * 16));
        topLayer.setBackground(Color.blue);
        topLayer.setOpaque(false);

        output = new JLabel("Output:");
        output.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "Data Output", TitledBorder.CENTER, TitledBorder.TOP));

        pane.setLayout(null);

        pane.add(vmlPane, JLayeredPane.DEFAULT_LAYER);
        pane.add(tlbPane, JLayeredPane.DEFAULT_LAYER);
        pane.add(pmlPane, JLayeredPane.DEFAULT_LAYER);
        pane.add(pagePane, JLayeredPane.DEFAULT_LAYER);
        pane.add(ramPane, JLayeredPane.DEFAULT_LAYER);
        pane.add(hhdPane, JLayeredPane.DEFAULT_LAYER);
        pane.add(hwPane, JLayeredPane.DEFAULT_LAYER);
        pane.add(osPane, JLayeredPane.DEFAULT_LAYER);
        pane.add(topLayer, JLayeredPane.PALETTE_LAYER);
        pane.add(output, JLayeredPane.DEFAULT_LAYER);

        Insets insets = pane.getInsets();

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

        size = hhdPane.getPreferredSize();
        hhdPane.setBounds(750 + insets.left, 365 + insets.top,
                size.width, size.height);

        size = hwPane.getPreferredSize();
        hwPane.setBounds(720 + insets.left, -15 + insets.top,
                size.width + 30, size.height + 30);

        size = osPane.getPreferredSize();
        osPane.setBounds(820 + insets.left, -15 + insets.top,
                size.width + 30, size.height + 30);

        size = topLayer.getPreferredSize();
        topLayer.setBounds(insets.left, insets.top,
                size.width, size.height);

        size = output.getPreferredSize();
        output.setBounds(745 + insets.left, 120 + insets.top,
                size.width + 100, size.height + 20);

        pane.setPreferredSize(new Dimension(1100, 365 + (diskPageNum + 4) * 16));
        mainPane = new JScrollPane(pane);

        output.setText("Output: [M]FFFFFFFF");
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

        for (int i = 0; i < tlbSize; i++) {
            tlbTable.getModel().setValueAt(0, i, 1);
        }

        for (int i = 0; i < ramPageNum; i++) {
            ramTable.getModel().setValueAt(String.format("%0" + ramNumLength + "X", i) + offset, i, 0);
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
}

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
    private VMJTable instruTable;
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
    private int diskCap;
    private boolean pagefault = false;
    private int clockHand;
    private boolean clockTick;
    
    private boolean done = false;
    private int instruCount = 0;

    public VMPanel(int tlbSize, int ramPageNum, int diskPageNum, int offsetSize, int ramSegNum, int diskSegNum) {
        super();
        pageSize = offsetSize;
        ramCap = ramSegNum;
        diskCap = diskSegNum;
        uiSetUp(tlbSize, ramPageNum, diskPageNum, ramSegNum, diskSegNum);
        tableReset(tlbSize, ramPageNum, diskPageNum, offsetSize);
//        test();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        VMPanel demo = new VMPanel(10, 17, 30, 16, 4, 15);
        JScrollPane a = new JScrollPane(demo);
        JFrame frame = new JFrame("AbsoluteLayoutDemo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(a);
        frame.setVisible(true);
        frame.setSize(1200, 700);
    }

    public void fsm() {
        topLayer.clearLines();
        topLayer.setPTLine(-1);
        topLayer.setRAMLine(-1);
        if (clockHand >= ramCap) {
                clockHand = 0;
            }
        topLayer.setClockLine(clockHand);
        if (state == 0) {
            pagefault = false;
            msgPane.setText("Poll out an instruction\n");
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
                    if (currVPN >= diskCap) {
                        msgPane.setText("Invalid virtual address\n");
                        state = 0;
                        instruTable.setColor(instruCount, Color.green);
                        instruCount++;
                        return;
                    }
                    vmAddrLine.getModel().setValueAt(addr.substring(addr.length() - offsetBits), 0, 1);
                }
                instruTable.setColor(instruCount, Color.yellow);
                state = 1;
                vmAddrLine.getModel().setValueAt(Integer.toBinaryString(currVPN), 0, 0);

            } else {
                msgPane.setText("No more instruction\n");
                done = true;
            }
        } else if (state == 1) {
            topLayer.setLeftRect(true);
            if (instru == 0) {
                msgPane.setText("Read from ram,\ncheck if the virtual page is in\nphysical memory");
            } else if (instru == 1) {
                msgPane.setText("Write to ram,\ncheck if the virtual page is in\nphysical memory");
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
            msgPane.setText("Page found in page table,\nform physical memory address");
            int[] xs = {35, 35, 70};
            int[] ys = {53, 410 + currVPN * 16, 410 + currVPN * 16};
            topLayer.addLine(xs, ys, 3);
            int[] xs1 = {363, 420, 420, 455};
            int[] ys1 = {410 + currVPN * 16, 410 + currVPN * 16, 290, 290};
            topLayer.addLine(xs1, ys1, 4);
            int[] xs2 = {186, 680, 680, 663};
            int[] ys2 = {45, 45, 290, 290};
            topLayer.addLine(xs2, ys2, 4);
            currPPN = Integer.parseInt((String) pageTable.getModel().getValueAt(currVPN, 4), 16);
            pmAddrLine.getModel().setValueAt(pageTable.getModel().getValueAt(currVPN, 4), 0, 0);
            pmAddrLine.getModel().setValueAt(
                    String.format("%X", Integer.parseInt((String) vmAddrLine.getModel().getValueAt(0, 1), 2)),
                    0, 1);
            if (instru == 0) {
                state = 9;
            } else {
                state = 10;
            }
        } else if (state == 9) {
            msgPane.setText("Access memory");
            int[] xs = {453, 430, 430, 455};
            int[] ys = {307, 307, 410 + currPPN * 16, 410 + currPPN * 16};
            topLayer.addLine(xs, ys, 4);
            state = 11;
        } else if (state == 10) {
            msgPane.setText("Write to memory");
            int[] xs = {453, 430, 430, 455};
            int[] ys = {307, 307, 410 + currPPN * 16, 410 + currPPN * 16};
            ramTable.setColor(currPPN, Color.pink);
            topLayer.addLine(xs, ys, 4);
            ramTable.getModel().setValueAt("M[" + pageTable.getModel().getValueAt(currVPN, 0) + "]", currPPN, 1);
            output.setText("Output:");
            state = 11;
        } else if (state == 11) {
            msgPane.setText("Update page table");
            pageTable.setModify(true);
            if (!pagefault) {
                pageTable.getModel().setValueAt(1, currVPN, 2);
            } else {
                pageTable.getModel().setValueAt(0, currVPN, 2);
            }
            ramTable.setColor(currPPN, Color.cyan);
            if (instru == 1) {
                pageTable.getModel().setValueAt(1, currVPN, 3);
                diskTable.setColor(currVPN, Color.orange);
            } else {
                output.setText("Output: " + ramTable.getValueAt(currPPN, 1));
            }
            pageTable.setModify(false);
            state = 0;
            instruTable.setColor(instruCount, Color.green);
            instruCount++;
        } else if (state == 3) {
            pagefault = true;
            msgPane.setText("Page not found,\nraise page fault exception,\nOS is in charge");
            topLayer.setLeftRect(false);
            int[] xs = {35, 35, 70};
            int[] ys = {53, 410 + currVPN * 16, 410 + currVPN * 16};
            topLayer.addLine(xs, ys, 3);
            clockTick = true;
            if (currRamUse == ramCap) {
                state = 4;
                clockTick = false;
            } else if (instru == 0) {
                state = 6;
            } else if (instru == 1) {
                state = 7;
            }
        } else if (state == 4) {
            String corrVPNRaw = (String) ramTable.getValueAt(clockHand, 1);
            int corrVPN = Integer.parseInt(corrVPNRaw.substring(2, corrVPNRaw.length() - 1), 16);
            int ref = (Integer) pageTable.getValueAt(corrVPN, 2);
            topLayer.setPTLine(corrVPN);
            topLayer.setRAMLine(clockHand);
            if (ref == 0) {
                swapVPN = corrVPN;
                swapPPN = clockHand;
                state = 12;
                msgPane.setText("PTE to replace found");
            } else {
                pageTable.setModify(true);
                pageTable.setValueAt(0, corrVPN, 2);
                pageTable.setModify(false);
                state = 4;
                msgPane.setText("look for a new PTE to replace");
            }
            clockHand++;
        } else if (state == 12) {
            topLayer.setPTLine(swapVPN);
            topLayer.setRAMLine(swapPPN);
            msgPane.setText("Replace data from virtual page\n" + pageTable.getValueAt(swapVPN, 4));
            if ((Integer) pageTable.getValueAt(swapVPN, 3) == 1) {
                state = 8;
            } else {
                state = 5;
            }
        } else if (state == 8) {
            msgPane.setText("Write data " + ramTable.getValueAt(swapPPN, 1)
                    + "\nto virtual page " + pageTable.getValueAt(swapVPN, 0));
            int[] xs = {673, 700, 700, 752};
            int[] ys = {410 + swapPPN * 16, 410 + swapPPN * 16, 372, 372};
            topLayer.addLine(xs, ys, 4);
            topLayer.setPTLine(swapVPN);
            topLayer.setRAMLine(swapPPN);
            state = 5;
        } else if (state == 5) {
            msgPane.setText("Invalidate data at " + ramTable.getValueAt(swapPPN, 1));
            ramTable.setColor(swapPPN, Color.white);
            diskTable.setColor(swapVPN, Color.white);
            topLayer.setPTLine(swapVPN);
            topLayer.setRAMLine(swapPPN);
            pageTable.setModify(true);
            pageTable.setValueAt(0, swapVPN, 1);
            pageTable.setModify(false);
            clockTable.setColor(swapPPN, Color.white);
            currRamUse--;
            if (instru == 0) {
                state = 6;
            } else {
                state = 7;
            }
        } else if (state == 6) {
            msgPane.setText("Write data from virtual page\n"
                    + pageTable.getValueAt(swapVPN, 0)
                    + "\ninto physical page\n"
                    + ((String) ramTable.getValueAt(swapPPN, 0)).
                            substring(0, ((String) ramTable.getValueAt(swapPPN, 0)).length() - 1));
            ramTable.setColor(swapPPN, Color.pink);
            int[] xs = {752, 700, 700, 673};
            int[] ys = {372, 372, 410 + swapPPN * 16, 410 + swapPPN * 16};
            topLayer.addLine(xs, ys, 4);
            ramTable.getModel().setValueAt("M[" + pageTable.getModel().getValueAt(currVPN, 0) + "]", swapPPN, 1);
            state = 7;
            
        } else if (state == 7) {
            pageTable.setModify(true);
            String ppAddr = (String) ramTable.getValueAt(swapPPN, 0);
            pageTable.getModel().setValueAt(ppAddr.substring(0, ppAddr.length() - 1), currVPN, 4);
            pageTable.getModel().setValueAt(1, currVPN, 1);
            pageTable.getModel().setValueAt(0, currVPN, 2);
            clockTable.setValueAt(pageTable.getValueAt(currVPN, 0), swapPPN, 0);
            clockTable.setColor(swapPPN, Color.green);
            if (instru == 1) {
                pageTable.getModel().setValueAt(1, currVPN, 3);
                ramTable.setColor(swapPPN, Color.orange);
                diskTable.setColor(currVPN, Color.orange);
            } else {
                pageTable.getModel().setValueAt(0, currVPN, 3);
                ramTable.setColor(swapPPN, Color.cyan);
                diskTable.setColor(currVPN, Color.green);
            }
            pageTable.setModify(false);
            state = 1;
            currRamUse++;
            swapPPN++;
            if(clockTick) {
                clockHand++;
                topLayer.setClockLine(clockHand);
            }
            
            msgPane.setText("Update physical page number\nat virtual page " + pageTable.getValueAt(currVPN, 4)
                    + "\nin page table, retry access.");
        }
        System.out.println(state);
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
        tlbTable.getColumnModel().getColumn(1).setHeaderValue("Valid");
        tlbTable.getColumnModel().getColumn(2).setHeaderValue("Ref");
        tlbTable.getColumnModel().getColumn(3).setHeaderValue("Dirty");
        tlbTable.getColumnModel().getColumn(4).setHeaderValue("Physical Page#");
        tlbTable.setMinimumSize(new Dimension(31 * 2 + 23 + 85 * 2, tlbSize * 16));
        tlbTable.setPreferredSize(new Dimension(31 * 2 + 23 + 85 * 2, tlbSize * 16));
        JScrollPane tlbPane = new JScrollPane(tlbTable);
        tlbTable.setBackground(Color.GRAY);
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
        
        clockTable = new VMJTable (ramSegNum,1,2,ramSegNum);
        clockTable.getTableHeader().setReorderingAllowed(false);
        clockTable.getColumnModel().getColumn(0).setPreferredWidth(85);
        clockTable.getColumnModel().getColumn(0).setHeaderValue("Virtual Page#");
        clockTable.setMinimumSize(new Dimension(85, 16*ramSegNum));
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

        topLayer = new LinePainter();
        topLayer.setPreferredSize(new Dimension(1150, 365 + (diskPageNum + 4) * 16));
        topLayer.setBackground(Color.blue);
        topLayer.setOpaque(false);

        output = new JLabel("Output:");
        output.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "Data Output", TitledBorder.CENTER, TitledBorder.TOP));

        msgPane = new JTextArea("");
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

        for (int i = 0; i < tlbSize; i++) {
            tlbTable.getModel().setValueAt(0, i, 1);
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
    
    
    
    private void test(){          
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

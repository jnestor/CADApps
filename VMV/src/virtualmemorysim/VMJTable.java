/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package virtualmemorysim;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author 15002
 */
public class VMJTable extends JTable {

    private final int PAGETABLE = 0;
    private final int TLB = 1;
    private final int MEMTABLE = 2;
    public int mode;
    private int size;
    private boolean modify = true;
//    private static boolean TLBEnabled = true;
    private int accessibleSpace;
    private ArrayList<Color> colorTable = new ArrayList<Color>();
    private boolean referenceEnabled = false;

    private static int TLB_PM = 3;
    private static int TLB_D = 2;
    private static int TLB_R = -1;
    private static int TLB_V = 1;
    private static int TLB_VM = 0;
    


    public VMJTable(int r, int c, int m) {
        super(r, c);
        mode = m;
    }

    public VMJTable(int r, int c, int m, int a) {
        super(r, c);
        mode = m;
        size = r;
        accessibleSpace = a;
        for (int i = 0; i < accessibleSpace; i++) {
            colorTable.add(Color.white);
        }
        for (int i = accessibleSpace; i < size; i++) {
            colorTable.add(null);
        }
    }

    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
        Component comp = super.prepareRenderer(renderer, row, col);
        if (!modify) {
            if (mode == PAGETABLE) {
                if (!referenceEnabled && col == 2) {
                    comp.setBackground(Color.DARK_GRAY);
                } else if (getModel().getValueAt(row, 1).equals(0)) {
                    comp.setBackground(Color.red);
                } else if (getModel().getValueAt(row, 3).equals(1)) {
                    comp.setBackground(Color.orange);
                } else if (referenceEnabled && getModel().getValueAt(row, 2).equals(0)) {
                    comp.setBackground(Color.yellow);
                } else {
                    comp.setBackground(new Color(0, 204, 0));
                }
            } else if (mode == TLB) {
//                if (!referenceEnabled && col == TLB_R) {
//                    comp.setBackground(Color.DARK_GRAY);
//                } else 
                    if (getModel().getValueAt(row, TLB_V).equals(0)) {
                    comp.setBackground(Color.red);
                } else if (getModel().getValueAt(row, TLB_D).equals(1)) {
                    comp.setBackground(Color.orange);
                } 
//                else if (referenceEnabled && getModel().getValueAt(row, TLB_R).equals(0)) {
//                    comp.setBackground(Color.yellow);
//                } 
                else {
                    comp.setBackground(new Color(0, 204, 0));
                }
            }

        }
        if (mode == MEMTABLE) {
            if (colorTable.get(row) != null) {
                comp.setBackground(colorTable.get(row));
            } else {
                comp.setBackground(Color.gray);
            }
        }

        return comp;
    }

    public boolean isModify() {
        return modify;
    }

    public void setModify(boolean modify) {
        this.modify = modify;

    }

    public void setColor(int i, Color c) {
        colorTable.set(i, c);

    }

    public void addLine() {
        colorTable.add(colorTable.size(), Color.white);
    }

//    public static boolean isTLBEnabled() {
//        return TLBEnabled;
//    }
//    public static void setTLBEnabled(boolean TLBEnabled) {
//        VMJTable.TLBEnabled = TLBEnabled;
//    }
    @Override
    public boolean isCellEditable(int row, int column) {
        //all cells false
        return false;
    }

    public void setReferenceEnabled(boolean referenceEnabled) {
        this.referenceEnabled = referenceEnabled;
    }

}

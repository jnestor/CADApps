/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package virtualmemorysim;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author 15002
 */
public class VMJTable extends JTable {

    private final int PAGETABLE = 0;
    private final int TLB = 1;
    private int mode;
    private int size;
    private int currentCell;
    private boolean modify = true;
    private static boolean TLBEnabled;

    public VMJTable(int r, int c, int m) {
        super(r, c);
        mode = m;
    }

    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
        Component comp = super.prepareRenderer(renderer, row, col);
        if(!modify){
            if (mode == PAGETABLE) {
                
                if (getModel().getValueAt(row, 4) == null || getModel().getValueAt(row, 1).equals(0)) {
                    comp.setBackground(Color.red);
                } else if (getModel().getValueAt(row, 3).equals(1)) {
                    comp.setBackground(Color.orange);
                }else if (getModel().getValueAt(row, 2).equals(0)) {
                    comp.setBackground(Color.yellow);
                } else {
                    comp.setBackground(new Color(0, 204, 0));
                }
            } else if (mode == TLB&&TLBEnabled) {
                if (getModel().getValueAt(row, 0).equals(0)) {
                    comp.setBackground(Color.red);
                } else if (getModel().getValueAt(row, 1).equals(1)) {
                    comp.setBackground(Color.yellow);
                } else if (getModel().getValueAt(row, 4) == null) {
                    comp.setBackground(Color.white);
                } else {
                    comp.setBackground(Color.green);
                }
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
    
    
    
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package virtualmemorysim;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import javax.swing.JPanel;

/**
 *
 * @author 15002
 */
public class LinePainter extends JPanel {

    ArrayList<int[]> xLineDots = new ArrayList<int[]>();
    ArrayList<int[]> yLineDots = new ArrayList<int[]>();
    ArrayList<Integer> nums = new ArrayList<Integer>();
    boolean leftRect = true;
    int circleLine = -1;
    
    @Override
    public void paintComponent(Graphics g) {
        if (!xLineDots.isEmpty()) {
            for (int i = 0; i < xLineDots.size(); i++) {
                g.drawPolyline(xLineDots.get(i), yLineDots.get(i), nums.get(i));
                if(xLineDots.get(i)[xLineDots.get(i).length-2]<xLineDots.get(i)[xLineDots.get(i).length-1]){
                    int arrowX = xLineDots.get(i)[xLineDots.get(i).length-1]+1;
                    int arrowY = yLineDots.get(i)[yLineDots.get(i).length-1];
                    g.fillPolygon(new int[]{arrowX,arrowX-10,arrowX-10}, new int[]{arrowY,arrowY-5,arrowY+5}, 3);
                } else{
                    int arrowX = xLineDots.get(i)[xLineDots.get(i).length-1]-1;
                    int arrowY = yLineDots.get(i)[xLineDots.get(i).length-1];
                    g.fillPolygon(new int[]{arrowX,arrowX+10,arrowX+10}, new int[]{arrowY,arrowY-5,arrowY+5}, 3);
                }
            }
        }
        
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(5));
        g2.setColor(Color.CYAN);
        if(circleLine!=-1){
             g2.drawRect(69, 401+circleLine*16, 295, 17);
        }
        g2.setColor(Color.red);
        if (leftRect) {
            g2.drawRoundRect(745, 10, 80, 80,10,10);
        } else {
            g2.drawRoundRect(845, 10, 80, 80,10,10);
        }
    }

    public void addLine(int[] xs, int[] ys, int num) {
        xLineDots.add(xs);
        yLineDots.add(ys);
        nums.add(num);
    }

    public void clearLines() {
        xLineDots.clear();
        yLineDots.clear();
        nums.clear();
    }

    public void setLeftRect(boolean leftRect) {
        this.leftRect = leftRect;
    }

    public void setCircleLine(int circleLine) {
        this.circleLine = circleLine;
    }

    
}

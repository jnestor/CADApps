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
import javax.swing.JPanel;

/**
 *
 * @author 15002
 */
public class LinePainter extends JPanel {

    ArrayList<int[]> xLineDots = new ArrayList<int[]>();
    ArrayList<int[]> yLineDots = new ArrayList<int[]>();
    ArrayList<Integer> nums = new ArrayList<Integer>();
    private boolean leftRect = true;
    private int ptLine = -1;
    private int ramLine = -1;
    private int tlbLine = -1;
    private int clockLine_PT = -1;
    private int clockLine_TLB = -1;
    private int tableY;
    private int tlbY;
    private int lineThickX = 470;
    private int lineThickX2 = 673;
    private int lineThickX3 = 752;

    private int tlbRightX = 368 - 31;

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        if (!xLineDots.isEmpty()) {
            for (int i = 0; i < xLineDots.size(); i++) {
                if (xLineDots.get(i)[0] == lineThickX) {
                    g2.setStroke(new BasicStroke(3));
                } else if (xLineDots.get(i)[0] == lineThickX2 || xLineDots.get(i)[0] == lineThickX3) {
                    g2.setStroke(new BasicStroke(8));
                }
                g2.drawPolyline(xLineDots.get(i), yLineDots.get(i), nums.get(i));

                if (xLineDots.get(i)[xLineDots.get(i).length - 2] < xLineDots.get(i)[xLineDots.get(i).length - 1]) {
                    int arrowX = xLineDots.get(i)[xLineDots.get(i).length - 1] + 1;
                    if (xLineDots.get(i)[0] == lineThickX) {
                        arrowX += 2;
                    } else if (xLineDots.get(i)[0] == lineThickX2 || xLineDots.get(i)[0] == lineThickX3) {
                        arrowX += 8;
                    }
                    int arrowY = yLineDots.get(i)[yLineDots.get(i).length - 1];
                    g2.fillPolygon(new int[]{arrowX, arrowX - 10, arrowX - 10}, new int[]{arrowY, arrowY - 10, arrowY + 10}, 3);
                } else if (xLineDots.get(i)[xLineDots.get(i).length - 2] > xLineDots.get(i)[xLineDots.get(i).length - 1]) {
                    int arrowX = xLineDots.get(i)[xLineDots.get(i).length - 1] - 1;
                    if (xLineDots.get(i)[0] == lineThickX) {
                        arrowX -= 2;
                    } else if (xLineDots.get(i)[0] == lineThickX2 || xLineDots.get(i)[0] == lineThickX3) {
                        arrowX -= 8;
                    }
                    int arrowY = yLineDots.get(i)[xLineDots.get(i).length - 1];
                    g2.fillPolygon(new int[]{arrowX, arrowX + 10, arrowX + 10}, new int[]{arrowY, arrowY - 10, arrowY + 10}, 3);
                } else if (yLineDots.get(i)[xLineDots.get(i).length - 2] < yLineDots.get(i)[xLineDots.get(i).length - 1]) {
                    int arrowX = xLineDots.get(i)[xLineDots.get(i).length - 1];
                    int arrowY = yLineDots.get(i)[xLineDots.get(i).length - 1] + 1;
                    if (xLineDots.get(i)[0] == lineThickX) {
                        arrowY += 2;
                    } else if (xLineDots.get(i)[0] == lineThickX2 || xLineDots.get(i)[0] == lineThickX3) {
                        arrowY += 8;
                    }
                    g2.fillPolygon(new int[]{arrowX, arrowX + 10, arrowX - 10}, new int[]{arrowY, arrowY - 10, arrowY - 10}, 3);
                } else if (yLineDots.get(i)[xLineDots.get(i).length - 2] > yLineDots.get(i)[xLineDots.get(i).length - 1]) {
                    int arrowX = xLineDots.get(i)[xLineDots.get(i).length - 1];
                    int arrowY = yLineDots.get(i)[xLineDots.get(i).length - 1] - 1;
                    if (xLineDots.get(i)[0] == lineThickX) {
                        arrowY -= 2;
                    } else if (xLineDots.get(i)[0] == lineThickX2 || xLineDots.get(i)[0] == lineThickX3) {
                        arrowY -= 8;
                    }
                    g2.fillPolygon(new int[]{arrowX, arrowX + 10, arrowX - 10}, new int[]{arrowY, arrowY + 10, arrowY + 10}, 3);
                }
            }
        }

        g2.setStroke(new BasicStroke(1));
        g2.setColor(Color.red);
        int arrowX;
        int arrowY;
        if (clockLine_PT != -1) {
            arrowX = 1105;
            arrowY = tableY + 24 + clockLine_PT * 16;
            g.fillPolygon(new int[]{arrowX, arrowX + 10, arrowX + 10}, new int[]{arrowY, arrowY - 5, arrowY + 5}, 3);
            g2.setStroke(new BasicStroke(3));
            g2.drawLine(arrowX + 5, arrowY, arrowX + 20, arrowY);
        }
        if (clockLine_TLB != -1) {
            arrowX = tlbRightX;
            arrowY = tlbY + 45 + clockLine_TLB * 16;
            g.fillPolygon(new int[]{arrowX, arrowX + 10, arrowX + 10}, new int[]{arrowY, arrowY - 5, arrowY + 5}, 3);
            g2.drawLine(arrowX + 5, arrowY, arrowX + 20, arrowY);
        }

        g2.setStroke(new BasicStroke(5));
        g2.setColor(Color.CYAN);
        if (ptLine != -1) {
            g2.drawRect(69, tableY + ptLine * 16, 295, 17);
//            System.out.println("ptLine" + ptLine);
        }
        if (tlbLine != -1) {
            g2.drawRect(69, tlbY + 37 + tlbLine * 16, tlbRightX-73, 17);
//            System.out.println("tlbLine" + tlbLine);
        }
        g2.setColor(Color.red);
        if (ramLine != -1) {
            g2.drawRect(493, tableY + ramLine * 16, 182, 17);
//            System.out.println("ramLine" + ramLine);
        }
//        if (leftRect) {
//            g2.drawRoundRect(745, 10, 80, 80, 10, 10);
//        } else {
//            g2.drawRoundRect(845, 10, 80, 80, 10, 10);
//        }
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

//    public void setLeftRect(boolean leftRect) {
//        this.leftRect = leftRect;
//    }
    public void setPTLine(int circleLine) {
        this.ptLine = circleLine;
    }

    public void setRAMLine(int circleLine) {
        ramLine = circleLine;
    }

    public void setTLBLine(int circleLine) {
        tlbLine = circleLine;
    }

    public void setRAMClockLine(int line) {
        clockLine_PT = line;
    }

    public void setTLBClockLine(int line) {
        clockLine_TLB = line;
    }

    public void setTableY(int tableY) {
        this.tableY = tableY;
    }

    public void setTlbY(int tlbY) {
        this.tlbY = tlbY;
    }

    public void setTlbRightX(int tlbRightX) {
        this.tlbRightX = tlbRightX;
    }
    
    

}

package placement.ui;

import placement.anneal.*;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;

/**


 * Title:        PlacementApplet
 * Description:  Animation of VLSI Module Placement
 * Copyright:    Copyright (c) 2001
 * Company:      Lafayette College
 * @author John Nestor
 * @version 1.0
 */

 /** code to plot history information */
public class UITempHistory extends JPanel {
  private PAnnealer myAnnealer;

  public UITempHistory(PAnnealer p) {
    myAnnealer = p;
  }

  private static final int LEFT_OFFSET = 25;
  private static final int RIGHT_OFFSET = 15;
  private static final int TOP_OFFSET = 25;
  private static final int BOT_OFFSET = 20;
  private static final double LOG10_SCALE = Math.log(10.0);
  private double log10(double x) { return Math.log(x) / LOG10_SCALE; }
  private int xoffset;
  private int yoffset;
  private int curWidth;
  private int curHeight;
  private double logTempLeft;
  private double logTempRight;
  private double logCostTop;
  private double logCostBot;
  private double costScale;
  private double tempScale;
  private FontMetrics myFontMetrics;

  private int log10TempToX(double t) {
    return xoffset + (int)(tempScale * (t - logTempLeft));
  }

  private int log10CostToY(double c) {
    return yoffset - (int)(costScale * (c - logCostBot));
 }

  private void drawXLabel(Graphics g, int logt) {
    String xlabel = "1E" + Integer.toString(logt);
    int x = log10TempToX(logt) - myFontMetrics.stringWidth(xlabel)/2;
    int y = yoffset + myFontMetrics.getHeight() - myFontMetrics.getDescent();
    g.drawString(xlabel,x,y);
  }

  private void drawYLabel(Graphics g, int logC) {
    String ylabel = "1E" + Integer.toString(logC);
    int x = xoffset - myFontMetrics.stringWidth(ylabel) - 2;
    int y = log10CostToY(logC) /*+ myFontMetrics.getHeight()/2*/;
    g.drawString(ylabel,x,y);
  }

  private void drawLegend(Graphics g) {
    String legend = "Cost vs. Temperature";
    g.setColor(Color.black);
    int x = xoffset + curWidth/2 - myFontMetrics.stringWidth(legend)/2;
    int y = yoffset - curHeight - 5;
    g.drawString(legend,x,y);
  }

  public void paintComponent(Graphics g) {
      setBackground(Color.white);
      super.paintComponent(g);
    PTempHistory pt = myAnnealer.getTempHistory();
    if (pt.getMaxAverageCost() == 0) return; // nothing to scale yet
    if (pt.getOverallMaxTemperature() == 0) return; // nothing to scale yet
    if (pt.numTempPoints() == 0) return; // nothing to draw yet
    myFontMetrics = g.getFontMetrics();
    Insets insets = getInsets();
    curWidth = getWidth() - insets.left - insets.right - LEFT_OFFSET - RIGHT_OFFSET;
    curHeight = getHeight() - insets.top - insets.bottom - TOP_OFFSET - BOT_OFFSET;
    xoffset = insets.left + LEFT_OFFSET;
    yoffset = insets.top + curHeight + TOP_OFFSET;
    logCostTop = Math.ceil(log10(pt.getMaxAcceptedCost()));
    logCostBot = Math.floor(log10(pt.getOverallMinCost()));
    logTempRight = log10(pt.getOverallMaxTemperature());
    logTempLeft = Math.min(log10(pt.getCurrentTemp()),Math.ceil(logTempRight) - 3.0);
    costScale = curHeight / (logCostTop - logCostBot);
    tempScale = curWidth / (logTempRight - logTempLeft);
    PTempHistoryPoint curp = null;
    PTempHistoryPoint lastp = null;
    drawLegend(g);
    // draw horizontal tickmarks
    g.setColor(Color.gray);
    g.drawLine(xoffset, log10CostToY(logCostBot), xoffset + curWidth, log10CostToY(logCostBot));
    for (int j = (int)Math.ceil(logCostBot); j <= (int)Math.floor(logCostTop); j++) {
      g.drawLine(xoffset,
	         log10CostToY(j),
		 xoffset + curWidth,
		 log10CostToY(j));
      //g.drawString("1E" + Integer.toString(j),xoffset-LEFT_OFFSET,log10CostToY(j)+3);
      drawYLabel(g,j);
    }
    g.drawLine(xoffset, log10CostToY(logCostTop), xoffset + curWidth, log10CostToY(logCostTop));
    //g.drawString("1E"+ Integer.toString((int)Math.floor(logCostTop)),xoffset-LEFT_OFFSET,insets.top+TOP_OFFSET+3);
    // draw vertical tickmarks
    if (Math.ceil(logTempLeft) > logTempLeft)
        g.drawLine(log10TempToX(logTempLeft), yoffset,
                   log10TempToX(logTempLeft), insets.top + TOP_OFFSET);
    for (int k = (int)Math.ceil(logTempLeft); k <= (int)Math.floor(logTempRight); k++) {
        g.drawLine(log10TempToX(k),
	           yoffset,
		   log10TempToX(k),
		   insets.top + TOP_OFFSET);
        drawXLabel(g,k);
    }
    if (Math.floor(logTempRight) < logTempRight)
        g.drawLine(log10TempToX(logTempRight), yoffset,
                   log10TempToX(logTempRight), insets.top + TOP_OFFSET);
    // now draw annealing curve
    for (int i = 0; i < pt.numTempPoints(); i++) {
      curp = pt.getTempPoint(i);
      if (lastp != null) {
        // first draw Average Cost in black
        // now plot Max cost in red
        g.setColor(Color.red);
        g.drawLine(log10TempToX(log10(lastp.getTemperature())),
	           yoffset - (int)((log10(lastp.getMaxCost()) - logCostBot) * costScale),
		   log10TempToX(log10(curp.getTemperature())),
		   log10CostToY(log10(curp.getMaxCost())) );
        // now plot Min cost in green
        g.setColor(Color.green);
        g.drawLine(log10TempToX(log10(lastp.getTemperature())),
	           yoffset - (int)((log10(lastp.getMinCost()) - logCostBot) * costScale),
		   log10TempToX(log10(curp.getTemperature())),
		   yoffset - (int)((log10(curp.getMinCost()) - logCostBot) * costScale));
        g.setColor(Color.black);
        g.setColor(Color.black);
        // now plot Average cost in black
        g.drawLine(log10TempToX(log10(lastp.getTemperature())),
	           yoffset - (int)((log10(lastp.getAverageCost()) - logCostBot) * costScale),
		   log10TempToX(log10(curp.getTemperature())),
		   yoffset - (int)((log10(curp.getAverageCost()) - logCostBot) * costScale));
      }
      lastp = curp;
    }
  }
}

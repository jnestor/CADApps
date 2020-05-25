package placement.ui;

import placement.anneal.*;
import placement.moves.*;


import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;

/**


 * Title:        PlacementApplet
 * Description:  Animation of VLSI Module Placement
 * Copyright:    Copyright (c) 2001, 2003
 * Company:      Lafayette College
 * @author John Nestor
 * @version 1.0
 */

 /** code to plot MOVE history information - extended from UITempHistory */
public class UIMoveHistory extends JPanel {
    private PMoveHistory myMoveHistory = PMove.getHistory();
    
    
//    private static final int TPZD_WIDTH=10; /* marked for death */
    private static final int MOVE_DISPLAY_WIDTH=8;
    private static final int LEFT_OFFSET = 25;
    private static final int RIGHT_OFFSET = 15;
    private static final int TOP_OFFSET = 25;
    private static final int BOT_OFFSET = 20;
    private static final double LOG10_SCALE = Math.log(10.0);
    private static final int METRO_DISPLAY_HEIGHT = 40;
//    private static final int METRO_MARGIN = 1; /* marked for death */
    private static final int METRO_Y_MARGIN = 20;

    private static final Color brightOrange = new Color(0xff,0x66,0x33);
    private static final Color lightGreen = new Color(0x99,0xff,0x66);

    private static double log10(double x) { return Math.log(x) / LOG10_SCALE; }
    private int curWidth;
    private int curHeight;
    private int displayLeft;
    private int moveDisplayTop;
    private int moveDisplayBottom;
    private int moveDisplayHeight;
    private int metroDisplayTop;
    private int metroDisplayBottom;
    private double logCostTop;
    private double logCostBot;
    private double costScale;
    private double tempScale;
    private FontMetrics myFontMetrics;

    private boolean optMetropolisDisplay = false;

    public void setOptMetropolisDisplay(boolean o) { optMetropolisDisplay = o; }

    private int log10CostToY(double c) {
	return moveDisplayBottom - (int)(costScale * (c - logCostBot));
    }

    private void drawXLabel(Graphics g, int k) {
	String xlabel = Integer.toString(k);
	int x = displayLeft + k * (MOVE_DISPLAY_WIDTH) - myFontMetrics.stringWidth(xlabel)/2;
	int y = moveDisplayBottom + myFontMetrics.getHeight() - myFontMetrics.getDescent();
	g.drawString(xlabel,x,y);
    }
    
    private void drawYLabel(Graphics g, int logC) {
	String ylabel = "1E" + Integer.toString(logC);
	int x = displayLeft - myFontMetrics.stringWidth(ylabel) - 2;
	int y = log10CostToY(logC) /*+ myFontMetrics.getHeight()/2*/;
	g.drawString(ylabel,x,y);
    }
    
/*    private void drawLegend(Graphics g) {
	String legend = "Move Attempt History";
	g.setColor(Color.gray);
	int x = xoffset + curWidth/2 - myFontMetrics.stringWidth(legend)/2;
	int y = yoffset - curHeight - 5;
	g.drawString(legend,x,y);
	} */

    public void setHistoryLength(int samples) {
	Dimension myDimension = getSize();
	myDimension.width = LEFT_OFFSET + RIGHT_OFFSET + samples*(MOVE_DISPLAY_WIDTH);
	setPreferredSize(myDimension);
	
    }


    // coordinate arrays for bargraph trapezoids drawn by fillPoly
    private int drawTrapX[] = new int[4];
    private int drawTrapY[] = new int[4];
    private int fillTrapX[] = new int [4];
    private int fillTrapY[] = new int [4];

    public int moveLeft(int i) {
	return displayLeft + i*MOVE_DISPLAY_WIDTH;
    }

    public int moveRight(int i) {
	return displayLeft + (i+1)*MOVE_DISPLAY_WIDTH;
    }

    private Insets insets;

    public void paintComponent(Graphics g) {
	setBackground(Color.white);
	super.paintComponent(g);
	if (myMoveHistory.size() == 0) return; // nothing to scale yet
	myFontMetrics = g.getFontMetrics();
	insets = getInsets();
	curWidth = getWidth() - insets.left - insets.right - LEFT_OFFSET - RIGHT_OFFSET;
	curHeight = getHeight() - insets.top - insets.bottom - TOP_OFFSET - BOT_OFFSET;
	displayLeft = insets.left + LEFT_OFFSET;
	if (optMetropolisDisplay) {
	    moveDisplayHeight = curHeight - METRO_DISPLAY_HEIGHT - METRO_Y_MARGIN;
	    moveDisplayTop = insets.top + TOP_OFFSET; 
	    moveDisplayBottom = moveDisplayTop + moveDisplayHeight;
	    metroDisplayTop = moveDisplayBottom + METRO_Y_MARGIN;
	    metroDisplayBottom = metroDisplayTop + METRO_DISPLAY_HEIGHT;
	} else {
	    moveDisplayHeight = curHeight;
	    moveDisplayTop = insets.top + TOP_OFFSET; 
	    moveDisplayBottom = moveDisplayTop + moveDisplayHeight;
	}

	// calculate cost display range and scale
	logCostTop = Math.ceil(log10(myMoveHistory.getMaxCost()));
	logCostBot = Math.floor(log10(myMoveHistory.getMinCost()));
	costScale = moveDisplayHeight / (logCostTop - logCostBot);
	
	int curCost = myMoveHistory.getInitCost();

	for (int i=0; i < myMoveHistory.size(); i++) {
	    PMoveHistoryPoint hp = myMoveHistory.get(i);
	    if (optMetropolisDisplay) drawMetroBar(g,i, hp);
	    drawCostBar(g,i, curCost, hp);
	    if (hp.accepted()) curCost += hp.getDeltaCost();
	}

      // draw horizontal gridlines
      g.setColor(Color.gray);
      g.drawLine(displayLeft, log10CostToY(logCostBot), displayLeft + curWidth, log10CostToY(logCostBot));
      for (int j = (int)Math.ceil(logCostBot); j <= (int)Math.floor(logCostTop); j++) {
	  g.drawLine(displayLeft,
		     log10CostToY(j),
		     displayLeft + curWidth,
		     log10CostToY(j));
	  drawYLabel(g,j);
      }
      g.drawLine(displayLeft, log10CostToY(logCostTop), displayLeft + curWidth, log10CostToY(logCostTop));
      // draw vertical tickmarks
      for (int k = 0; k <= myMoveHistory.size(); k = k + 10) {
	  int lineX = moveLeft(k);
	  g.drawLine(lineX, moveDisplayBottom, lineX, moveDisplayTop);
	drawXLabel(g,k);
      }
    }
    
    private void drawMetroRandom(Graphics g, int move, double r, boolean accepted) {
	int tLeft = moveLeft(move) + 1;
	int tRight = moveRight(move) - 1;
	int ry = metroDisplayBottom - (int)(METRO_DISPLAY_HEIGHT * r);
	int offset = (MOVE_DISPLAY_WIDTH - 2) / 2;
	// border
	drawTrapX[0] = tLeft;
	drawTrapY[0] = ry;
	drawTrapX[1] = tLeft + offset;
	drawTrapY[1] = ry - offset;
	drawTrapX[2] = tRight;
	drawTrapY[2] = ry;
	drawTrapX[3] = tLeft + offset;
	drawTrapY[3] = ry + offset;
	if (accepted) g.setColor(lightGreen);
	else g.setColor(Color.white);
	g.fillPolygon(drawTrapX, drawTrapY, 4);
	g.setColor(Color.black);
	g.drawPolygon(drawTrapX, drawTrapY, 4);
	// fill
/*	if (accepted) g.setColor(lightGreen);
	else g.setColor(brightOrange);
	fillTrapX[0] = tLeft + 1;
	fillTrapY[0] = ry;
	fillTrapX[1] = tLeft + offset + 1;
	fillTrapY[1] = ry - offset + 1;
	fillTrapX[2] = tRight;
	fillTrapY[2] = ry;
	fillTrapX[3] = tLeft + offset;
	fillTrapY[3] = ry + offset;
	g.fillPolygon(fillTrapX, fillTrapY, 4); */

    }

    private void drawMetroBar(Graphics g, int move, PMoveHistoryPoint hp) {
	int xLeft = moveLeft(move);
	int xRight = moveRight(move);

	g.setColor(Color.black);
	g.drawRect(xLeft, metroDisplayTop, MOVE_DISPLAY_WIDTH, METRO_DISPLAY_HEIGHT); // draw the border
	if (hp.getProbability() > 1) System.out.println("AWK! Probability =" + hp.getProbability());
	int probHeight = (int)((METRO_DISPLAY_HEIGHT) * hp.getProbability());
	if (hp.acceptedUphill()) {               // draw the probability bar
	    g.setColor(brightOrange);
	    g.fillRect(xLeft + 1, metroDisplayBottom - probHeight, MOVE_DISPLAY_WIDTH-1, probHeight);
	} else if (hp.accepted()) {
	    g.setColor(lightGreen);
	    g.fillRect(xLeft + 1, metroDisplayTop + 1, MOVE_DISPLAY_WIDTH-1, METRO_DISPLAY_HEIGHT-1);
	} else {
	    g.setColor(Color.gray);
	    g.fillRect(xLeft + 1, metroDisplayBottom - probHeight, MOVE_DISPLAY_WIDTH-1, probHeight);
	}
	g.setColor(Color.black);      	      // draw random number "R"
	if (hp.acceptedUphill()) drawMetroRandom(g, move, hp.getR(), true);
	else if (hp.rejected()) drawMetroRandom(g, move, hp.getR(), false);
    }

    private void drawCostBar(Graphics g, int move, int curCost, PMoveHistoryPoint hp) {
	int xLeft = moveLeft(move);
	int xRight = moveRight(move);
	
	drawTrapX[0] = drawTrapX[1] = xLeft;
	drawTrapX[2] = drawTrapX[3] = xRight;
	drawTrapY[0] = drawTrapY[3] = moveDisplayBottom;
	
	fillTrapX[0] = fillTrapX[1] = xLeft;
	fillTrapX[2] = fillTrapX[3] = xRight;
	fillTrapY[0] = fillTrapY[3] = moveDisplayBottom;
	
	drawTrapY[1] = log10CostToY(log10(curCost));
	fillTrapY[1] = drawTrapY[1] + 1;
	
	if (hp.accepted()) {
	    curCost += hp.getDeltaCost();
	    drawTrapY[2] = log10CostToY(log10(curCost));
	    fillTrapY[2] = drawTrapY[2] + 1;
	    g.setColor(Color.black);
	    g.drawPolygon(drawTrapX, drawTrapY, 4);
	    if (hp.getDeltaCost() > 0) g.setColor(brightOrange);
	    else g.setColor(lightGreen);
	    g.fillPolygon(fillTrapX, fillTrapY, 4);
	} else {
	    drawTrapY[2] = drawTrapY[1];
	    g.setColor(Color.black);
	    g.drawPolygon(drawTrapX, drawTrapY, 4);
	    g.setColor(Color.white);
	    g.fillPolygon(fillTrapX, fillTrapY, 4);
	    // draw the "rejected" bar here
	    int barX1, barX2, barY;
	    barX1 = xLeft + 1;
	    barX2 = xRight - 1;
	    barY = log10CostToY(log10(curCost + hp.getDeltaCost()));
	    g.setColor(Color.red);
	    if (barY < (insets.top + TOP_OFFSET)) {  // thick line indicates value off the scale
		barY = insets.top + TOP_OFFSET ;
//		g.drawLine(barX1, barY+1, barX2, barY+1);
	    }
	    g.fillRect(barX1, barY-1, MOVE_DISPLAY_WIDTH-2, 3);
//	    g.drawLine(barX1, barY, barX2, barY);
	}
    }
    

/* this is the old paintComponent
  public void paintComponent(Graphics g) {
      super.paintComponent(g);
      if (myMoveHistory.size() == 0) return; // nothing to scale yet
      myFontMetrics = g.getFontMetrics();
      Insets insets = getInsets();
      curWidth = getWidth() - insets.left - insets.right - LEFT_OFFSET - RIGHT_OFFSET;
      curHeight = getHeight() - insets.top - insets.bottom - TOP_OFFSET - BOT_OFFSET;
      xoffset = insets.left + LEFT_OFFSET;
      ymoffset = yoffset = insets.top + curHeight + TOP_OFFSET;

      drawLegend(g);
      if (optMetropolisDisplay) {
	  yoffset = yoffset - METRO_DISPLAY_HEIGHT;
	  curHeight = curHeight - METRO_DISPLAY_HEIGHT;
      }
      logCostTop = Math.ceil(log10(myMoveHistory.getMaxCost()));
      logCostBot = Math.floor(log10(myMoveHistory.getMinCost()));
      costScale = curHeight / (logCostTop - logCostBot);

      // now draw the cost curve (draw axes after)
      
      int i;
      
      int curCost = myMoveHistory.getInitCost();
      
      for (i=0; i < myMoveHistory.size(); i++) {
	  int xLeft, xRight;
	  xLeft = xoffset + (i * (TPZD_WIDTH-1));
	  xRight = xLeft + TPZD_WIDTH-1;
	  PMoveHistoryPoint hp = myMoveHistory.get(i);

	  // draw the metropolis display
	  if (optMetropolisDisplay) {
	      g.setColor(Color.black);
	      g.drawRect(xLeft, ymoffset-METRO_DISPLAY_HEIGHT, (TPZD_WIDTH-1), METRO_DISPLAY_HEIGHT); // draw the frame
	      if (hp.getProbability() > 1) System.out.println("AWK! Probability =" + hp.getProbability());
	      int probHeight = (int)((METRO_DISPLAY_HEIGHT - 2*METRO_MARGIN) * hp.getProbability());
	      if (hp.acceptedUphill()) {               // draw the probability bar
		  g.setColor(brightOrange);
		  g.fillRect(xLeft + METRO_MARGIN, ymoffset - METRO_MARGIN - probHeight,
			     (TPZD_WIDTH-1)  - 2*METRO_MARGIN, probHeight);
	      } else if (hp.accepted()) {
		  g.setColor(lightGreen);
		  g.fillRect(xLeft + METRO_MARGIN, ymoffset - METRO_DISPLAY_HEIGHT - METRO_MARGIN,
			     (TPZD_WIDTH-1)  - 2*METRO_MARGIN, METRO_DISPLAY_HEIGHT - 2*METRO_MARGIN);
	      } else {
		  g.setColor(Color.gray);
		  g.fillRect(xLeft + METRO_MARGIN, ymoffset - METRO_MARGIN - probHeight,
			     (TPZD_WIDTH-1)  - 2*METRO_MARGIN , probHeight);
	      }
	      g.setColor(Color.black);      	      // draw random number "R"
	      if (hp.acceptedUphill() || hp.rejected()) {
		  int yr = ymoffset - METRO_MARGIN -  (int)((METRO_DISPLAY_HEIGHT - 2*METRO_MARGIN) * hp.getR());
		  g.drawLine(xLeft + 1, yr, xRight, yr);
	      }
	  }
	  
	  // draw the cost bargraph trapezoid
	  drawTrapX[0] = drawTrapX[1] = xLeft;
	  drawTrapX[2] = drawTrapX[3] = xRight;
	  drawTrapY[0] = drawTrapY[3] = yoffset;

	  fillTrapX[0] = fillTrapX[1] = xLeft + 1;
	  fillTrapX[2] = fillTrapX[3] = xRight;
	  fillTrapY[0] = fillTrapY[3] = yoffset;

	  drawTrapY[1] = log10CostToY(log10(curCost));
	  fillTrapY[1] = drawTrapY[1] + 1;

	  if (hp.accepted()) {
	      curCost += hp.getDeltaCost();
	      drawTrapY[2] = log10CostToY(log10(curCost));
	      fillTrapY[2] = drawTrapY[2] + 1;
	      g.setColor(Color.black);
	      g.drawPolygon(drawTrapX, drawTrapY, 4);
	      if (hp.getDeltaCost() > 0) g.setColor(brightOrange);
	      else g.setColor(lightGreen);
	      g.fillPolygon(fillTrapX, fillTrapY, 4);
	  } else {
	      drawTrapY[2] = log10CostToY(log10(curCost));
	      fillTrapY[2] = drawTrapY[2] -1;
	      g.setColor(Color.black);
	      g.drawPolygon(drawTrapX, drawTrapY, 4);
	      g.setColor(Color.white);
	      g.fillPolygon(fillTrapX, fillTrapY, 4);
	      // draw the "rejected" bar here
	      int barX1, barX2, barY;
	      barX1 = xoffset + (i * (TPZD_WIDTH-1));
	      barX2 = xoffset + ((i+1) * (TPZD_WIDTH-1));
	      barY = log10CostToY(log10(curCost + hp.getDeltaCost()));
	      g.setColor(Color.red);
	      if (barY < (insets.top + TOP_OFFSET)) {  // thick line indicates value off the scale
		  barY = insets.top + TOP_OFFSET ;
		  g.drawLine(barX1, barY+1, barX2, barY+1);
	      }
	      g.drawLine(barX1, barY, barX2, barY);
	  }
      }

      // draw horizontal tickmarks
      g.setColor(Color.gray);
      g.drawLine(xoffset, log10CostToY(logCostBot), xoffset + curWidth, log10CostToY(logCostBot));
      for (int j = (int)Math.ceil(logCostBot); j <= (int)Math.floor(logCostTop); j++) {
	  g.drawLine(xoffset,
		     log10CostToY(j),
		     xoffset + curWidth,
		     log10CostToY(j));
	  drawYLabel(g,j);
      }
      g.drawLine(xoffset, log10CostToY(logCostTop), xoffset + curWidth, log10CostToY(logCostTop));
      // draw vertical tickmarks
      for (int k = 0; k <= myMoveHistory.size(); k = k + 10) {
	  g.drawLine(xoffset + k * (TPZD_WIDTH-1), yoffset, xoffset + k * (TPZD_WIDTH-1), insets.top + TOP_OFFSET);
	drawXLabel(g,k);
      }
      
      
  }
    
*/
}

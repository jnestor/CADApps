package placement.ui;

import placement.PModule;
import placement.moves.PMove;
import placement.moves.PMoveFlipH;
import placement.moves.PMoveStatus;

import java.awt.Graphics;
import java.awt.Color;
import java.awt.Polygon;
import javax.swing.JComponent;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author
 * @version 1.0
 */

public class UIMoveFlipH extends UIMove {

  private static int [] leftArrowX =  { 0, 0,-9, 0, 0 };
  private static int [] leftArrowY =  { 0, 8, 0,-9, 0 };
  private static int [] rightArrowX = { 0, 0, 8, 0, 0 };
  private static int [] rightArrowY = { 0, 8, 0,-8, 0 };
  private static Polygon leftArrow = new Polygon(leftArrowX, leftArrowY, 5);
  private static Polygon rightArrow = new Polygon(rightArrowX, rightArrowY, 5);

  private static final int FLIPH_OFFSET = 5;

  private UIMoveFlipH() {
  }

  static UIMoveFlipH myInstance = new UIMoveFlipH();

  public static UIMove getInstance(PMove m) { myInstance.myMove = m; return myInstance; }

  public void drawMove(UILayout ul, Graphics g) {
    if (myMove.getMoveStatus() == PMoveStatus.MOVE_COMPLETE) return;
    PModule moveModule = myMove.getMoveModule();
    // int xlen = ul.scale(moveModule.width()) + FLIPH_OFFSET*2;
    int xlen = FLIPH_OFFSET * 2;
    int rx = ul.translateX( moveModule.getCenterX() ) - xlen/2;
    int ry = ul.translateY( moveModule.getCenterY() );

    g.setColor(MOVECOLOR);
    g.fillRect(rx, ry - LINEWIDTH/2, xlen, LINEWIDTH);
    leftArrow.translate(rx,ry);
    g.fillPolygon(leftArrow);
    leftArrow.translate(-rx,-ry);
    rightArrow.translate(rx+xlen,ry);
    g.fillPolygon(rightArrow);
    rightArrow.translate(-(rx+xlen),-ry);
    g.setColor(Color.black);
  }
}
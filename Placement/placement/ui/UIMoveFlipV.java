package placement.ui;

import placement.PModule;
import placement.moves.PMove;
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

public class UIMoveFlipV extends UIMove {
  private static int [] upArrowX = { 0, 9, 0, -9, 0 };
  private static int [] upArrowY =  { 0, 0, -9, 0, 0 };
  private static int [] downArrowX = { 0, 8,-1, -7, 0 };
  private static int [] downArrowY = { 0, 0, 8,  0, 0 };

  private static Polygon upArrow = new Polygon(upArrowX, upArrowY, 5);
  private static Polygon downArrow = new Polygon(downArrowX, downArrowY, 5);

  private static final int FLIPV_OFFSET = 5;

  private UIMoveFlipV() {
  }

  static UIMoveFlipV myInstance = new UIMoveFlipV();

  public static UIMove getInstance(PMove m) { myInstance.myMove = m; return myInstance; }

  public void drawMove(UILayout ul, Graphics g) {
    if (myMove.getMoveStatus() == PMoveStatus.MOVE_COMPLETE) return;
    PModule moveModule = myMove.getMoveModule();
    //int ylen = ul.scale(moveModule.height()) + FLIPV_OFFSET*2;
    int ylen = FLIPV_OFFSET*2;
    int rx = ul.translateX( moveModule.getCenterX() );
    int ry = ul.translateY( moveModule.getCenterY() ) - ylen/2;

    g.setColor(MOVECOLOR);
    g.fillRect(rx-LINEWIDTH/2, ry, LINEWIDTH, ylen);
    upArrow.translate(rx,ry);
    g.fillPolygon(upArrow);
    upArrow.translate(-rx,-ry);
    downArrow.translate(rx,ry+ylen);
    g.fillPolygon(downArrow);
    downArrow.translate(-rx,-(ry+ylen));
    g.setColor(Color.black);
  }
}

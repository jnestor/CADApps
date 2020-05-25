package placement.ui;

import placement.moves.PMove;
import placement.moves.PMoveStatus;
import placement.PModule;

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

public class UIMoveH extends UIMove {


  private static int [] leftArrowX =  { 0, 0, -9, 0, 0 };
  private static int [] leftArrowY = { 0, 9, 0, -9, 0 };
  private static int [] rightArrowX = { 0, 0, 8,  0, 0 };
  private static int [] rightArrowY = { 0, 8, 0, -8, 0 };
  private static Polygon leftArrow = new Polygon(leftArrowX, leftArrowY, 5);
  private static Polygon rightArrow = new Polygon(rightArrowX, rightArrowY, 5);

  private UIMoveH() {
  }

  static UIMoveH myInstance = new UIMoveH();

  public static UIMove getInstance(PMove m) { myInstance.myMove = m; return myInstance; }

  public void drawMove(UILayout ul, Graphics g) {
    if (myMove.getMoveStatus() == PMoveStatus.MOVE_COMPLETE) return;
    int dx = ul.scale(myMove.getOffset());
    PModule moveModule = myMove.getMoveModule();
    int rx = ul.translateX( moveModule.getCenterX() );
    int ry = ul.translateY( moveModule.getCenterY() );
    g.setColor(Color.white);
    if (myMove.getMoveStatus() == PMoveStatus.ACCEPT_PENDING ||
        myMove.getMoveStatus() == PMoveStatus.REJECT_PENDING) rx = rx-dx;
    g.setColor(MOVECOLOR);
    if (dx < 0) {
      if (-dx > 15) (dx = dx + 9); // adjust off arrowhead
      g.fillRect(rx+dx, ry-LINEWIDTH/2, -dx, LINEWIDTH);
      leftArrow.translate(rx+dx,ry);
      g.fillPolygon(leftArrow);
      leftArrow.translate(-(rx+dx),-(ry));
    } else {
      if (dx > 15) (dx = dx - 8); // adjust for arrowhead
      g.fillRect(rx, ry-LINEWIDTH/2, dx, LINEWIDTH);
      rightArrow.translate(rx+dx,(ry));
      g.fillPolygon(rightArrow);
      rightArrow.translate(-(rx+dx),-(ry));
    }
    g.setColor(Color.black);
  }
}
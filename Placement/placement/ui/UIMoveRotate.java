package placement.ui;

import placement.PModule;
import placement.moves.PMove;
import placement.moves.PMoveStatus;


import java.awt.Polygon;
import java.awt.Color;
import java.awt.Graphics;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author
 * @version 1.0
 */

public class UIMoveRotate extends UIMove {
  static private int [] rotX = {-7,-7, 7, 7, 14,  5,-4, 3, 3,-7 };
  static private int [] rotY = {-2,-7,-7, 3,  3, 12, 3, 3,-2,-2 };
  static private Polygon rotArrow = new Polygon(rotX,rotY,10);

  private UIMoveRotate() {
  }

  static UIMoveRotate myInstance = new UIMoveRotate();

  public static UIMove getInstance(PMove m) { myInstance.myMove = m; return myInstance; }

  public void drawMove(UILayout ul, Graphics g) {
    if (myMove.getMoveStatus() == PMoveStatus.MOVE_COMPLETE) return;
    PModule moveModule = myMove.getMoveModule();
    int rx = ul.translateX( moveModule.getCenterX() );
    int ry = ul.translateY( moveModule.getCenterY() );
    g.setColor(MOVECOLOR);
    rotArrow.translate(rx,ry);
    g.fillPolygon(rotArrow);
    rotArrow.translate(-rx,-ry);
    g.setColor(Color.black);
  }
}

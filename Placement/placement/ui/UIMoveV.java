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

public class UIMoveV extends UIMove {
  private static int [] upArrowX = { 0, 9, 0, -9, 0 };
  private static int [] upArrowY =  { 0, 0, -9, 0, 0 };
  private static int [] downArrowX = { 0, 8, 0, -8, 0 };
  private static int [] downArrowY = { 0, 0, 8,  0, 0 };
  private static Polygon upArrow = new Polygon(upArrowX, upArrowY, 5);
  private static Polygon downArrow = new Polygon(downArrowX, downArrowY, 5);

  private UIMoveV() {
  }

  static UIMoveV myInstance = new UIMoveV();
  private PMove myMove;

  public static UIMove getInstance(PMove m) { myInstance.myMove = m; return myInstance; }

  public void drawMove(UILayout ul, Graphics g) {
    if (myMove.getMoveStatus() == PMoveStatus.MOVE_COMPLETE) return;
    int dy = ul.scale(myMove.getOffset());
    PModule moveModule = myMove.getMoveModule();
    int rx = ul.translateX( moveModule.getCenterX() );
    int ry = ul.translateY( moveModule.getCenterY() );
    if (myMove.getMoveStatus() == PMoveStatus.ACCEPT_PENDING ||
        myMove.getMoveStatus() == PMoveStatus.REJECT_PENDING) ry = ry-dy;
    g.setColor(MOVECOLOR);
    if (dy < 0) {
      if (-dy > 15) dy = dy + 9; // adjust for arrowhead
      g.fillRect(rx-LINEWIDTH/2, ry+dy, LINEWIDTH, -dy);
      upArrow.translate(rx,ry+dy);
      g.fillPolygon(upArrow);
      upArrow.translate(-rx,-(ry+dy));
    } else {
      if (dy > 15) dy = dy - 9;  // adjust for arrowhead
      g.fillRect(rx-LINEWIDTH/2, ry, LINEWIDTH, dy);
      downArrow.translate(rx,ry+dy);
      g.fillPolygon(downArrow);
      downArrow.translate(-rx,-(ry+dy));
    }
    g.setColor(Color.black);
  }
}
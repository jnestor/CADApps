package placement.moves;
import placement.PModule;
import placement.ui.UIMove;
import placement.ui.UIMoveV;

/**
 * Title:        PlacementApplet
 * Description:  Animation of VLSI Module Placement
 * Copyright:    Copyright (c) 2001
 * Company:      Lafayette College
 * @author John Nestor
 * @version 1.0
 */

public class PMoveV extends PMove {
  private int dy;

  public PMoveV(double p) { super(p); }

  public void setup(PModule pm) {
    super.setup(pm);
    offset = (int)((Math.random() - 0.5) * genY());
    int newy = moveModule.getY() + offset;
    /* if (newy < 0)
      offset = -moveModule.getY();
    if (newy > moveModule.getMoveLimit())
      offset = moveModule.getMoveLimit() - moveModule.getY(); */
    if (offset == 0) {
      if (Math.random() < 0.5) offset = 1;
      else offset = -1;
    }
  }

  public void apply() {
    super.apply();
    deltaCost = moveModule.move(0,offset);
  }

  public void complete() {
    if (getMoveStatus() == PMoveStatus.REJECT_PENDING) moveModule.move(0,-offset);
    super.complete();
  }

  public String toString() { return moveString("Move-V (" + offset + ")"); }

  public UIMove getUIMove() { return UIMoveV.getInstance(this); }

}

package placement.moves;

import placement.PModule;
import placement.ui.UIMove;
import placement.ui.UIMoveRotate;

/**
 * Title:        PlacementApplet
 * Description:  Animation of VLSI Module Placement
 * Copyright:    Copyright (c) 2001
 * Company:      Lafayette College
 * @author John Nestor
 * @version 1.0
 */

public class PMoveRotate extends PMove {
  public PMoveRotate(double p) { super(p); }

  public void setup(PModule pm) { offset = 0; super.setup(pm);  }

  public void apply() {
    super.apply();
    deltaCost = moveModule.rotate();
  }

  public void complete() {
    if (getMoveStatus() == PMoveStatus.REJECT_PENDING) {
      moveModule.rotate();
      moveModule.rotate();
      moveModule.rotate(); // hack until we code a reverse-rotate
    }
    super.complete();
  }

  public String toString() { return moveString("Rotate"); }

  public UIMove getUIMove() { return UIMoveRotate.getInstance(this); }

}

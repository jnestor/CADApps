package placement.moves;

import placement.PModule;
import placement.ui.UIMove;
import placement.ui.UIMoveFlipH;

/**
 * Title:        PlacementApplet
 * Description:  Animation of VLSI Module Placement
 * Copyright:    Copyright (c) 2001
 * Company:      Lafayette College
 * @author John Nestor
 * @version 1.0
 */

public class PMoveFlipH extends PMove {
  public PMoveFlipH(double p) {
    super(p);
  }

  public void setup(PModule pm) {
    offset = 0;
    super.setup(pm);
  }

  public void apply() {
    super.apply();
    deltaCost = moveModule.flipHorizontal();
  }

  public void complete() {
    if (getMoveStatus() == PMoveStatus.REJECT_PENDING) moveModule.flipHorizontal();
    super.complete();
  }

  public String toString() { return moveString("Flip-H"); }

  public UIMove getUIMove() { return UIMoveFlipH.getInstance(this); }
}

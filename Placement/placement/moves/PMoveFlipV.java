package placement.moves;

import placement.PModule;
import placement.moves.PMove;
import placement.moves.PMoveStatus;
import placement.ui.UIMove;
import placement.ui.UIMoveFlipV;

/**
 * Title:        PlacementApplet
 * Description:  Animation of VLSI Module Placement
 * Copyright:    Copyright (c) 2001
 * Company:      Lafayette College
 * @author John Nestor
 * @version 1.0
 */

public class PMoveFlipV extends PMove {
  public PMoveFlipV(double p) {
    super(p);
  }

  public void setup(PModule pm) {
    offset = 0;
    super.setup(pm);
  }

  public void apply() {
    super.apply();
    deltaCost = moveModule.flipVertical();
  }

  public void complete() {
    if (getMoveStatus() == PMoveStatus.REJECT_PENDING) moveModule.flipVertical();
    super.complete();
  }

  public String toString() { return moveString("Flip-V "); }

  public UIMove getUIMove() { return UIMoveFlipV.getInstance(this); }


}

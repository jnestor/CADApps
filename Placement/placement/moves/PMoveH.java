package placement.moves;
import placement.PModule;
import placement.ui.UIMove;
import placement.ui.UIMoveH;
/**
 * Title:        PlacementApplet
 * Description:  Animation of VLSI Module Placement
 * Copyright:    Copyright (c) 2001
 * Company:      Lafayette College
 * @author John Nestor
 * @version 1.0
 */

public class PMoveH extends PMove {

  public PMoveH(double p) {
    super(p);
  }

  public void setup(PModule pm) {
    super.setup(pm);
    offset = (int)((Math.random() - 0.5) * genX());
    if (offset == 0) {
      if (Math.random() < 0.5) offset = 1;
      else offset = -1;
    }
  }

  public void apply() {
    super.apply();
    deltaCost = moveModule.move(offset,0);
  }

  public void complete() {
    if (getMoveStatus() == PMoveStatus.REJECT_PENDING) moveModule.move(-offset,0);
    super.complete();
  }

  public String toString() { return moveString("Move-H (" + offset + ")"); }

  public UIMove getUIMove() { return UIMoveH.getInstance(this); }

}

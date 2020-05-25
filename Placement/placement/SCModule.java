package placement;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author
 * @version 1.0
 */

public class SCModule extends PModule {

  public SCModule(String n, int xi, int yi, int wi, int hi) {
    super(n, xi, yi, wi, hi);
  }

  public int getRow() { return -1;/* not implemented yet! */ }

  public void setRowLocation(int nx, int row) { /* not implemented yet! */ }

  public void setLocation(int nx, int ny) { /* override to snap to row??? */ }
}
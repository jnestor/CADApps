package ChannelRouter;

import java.io.*;
//import ChannelRouter.Net;

public class Terminal {

  private int column;
  private boolean topOrBottom;
  private Net owner;

  /** Symbolic constant for terminals at top of channel */
  public static final boolean TOP = true;
  /** Symbolic constants for terminals at bottom of channel */
  public static final boolean BOTTOM = false;

  public Terminal(int col, boolean tOrB) {
    column = col;
    topOrBottom = tOrB;
  }

  public Terminal(String spec) throws IOException {
    switch (spec.charAt(spec.length() - 1)) {
      case 'u':
      case 'U': topOrBottom = Terminal.TOP;
                 break;
      case 'l':
      case 'L': topOrBottom = Terminal.BOTTOM;
                 break;
      default:  throw new IOException();
    }
    String s = spec.substring(0, spec.length()-1);
    column = Integer.parseInt(spec.substring(0, spec.length() - 1));
  }

  public int getColumn() { return column; }

  public boolean getTopOrBottom() { return topOrBottom; }

  public Net getNet() { return owner; }

  public void setNet(Net n) { owner = n; }

  public void writeTerminal(PrintStream os) {
    os.print(column);
    if (topOrBottom == Terminal.TOP) os.print("U ");
    else os.print("L ");
  }


}

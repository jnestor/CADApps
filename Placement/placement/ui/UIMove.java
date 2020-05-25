package placement.ui;

import java.awt.Graphics;
import java.awt.Color;
import javax.swing.JPanel;

import placement.moves.PMove;

/**
 * Title: UIMove
 * Description: Class for drawing moves in placement User Interface
 * Copyright:    Copyright (c) 2001
 * Company:      Lafayette College
 * @author
 * @version 1.0
 */

public abstract class UIMove {

  public abstract void drawMove(UILayout ul, Graphics g);

  protected static final int LINEWIDTH = 5;
  protected static final Color MOVECOLOR=Color.blue;

  protected PMove myMove;
}
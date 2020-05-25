package placement.ui;

import placement.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.JLabel;
import javax.swing.BorderFactory;
import javax.swing.SwingConstants;

/**
 * Title:        PlacementApplet
 * Description:  Animation of VLSI Module Placement
 * Copyright:    Copyright (c) 2001
 * Company:      Lafayette College
 * @author John Nestor
 * @version 1.0
 */

public class UIModule extends JLabel {

  private static DragListener dragger = new DragListener();

  private PModule myModule;

  public UIModule(PModule mod) {
    addMouseMotionListener(dragger);
    addMouseListener(dragger);
    myModule = mod;
    setBorder(BorderFactory.createLineBorder(Color.black,2));
    setText(mod.getName());
    setBackground(Color.yellow);
    setOpaque(true);
    setHorizontalAlignment(SwingConstants.CENTER);
    setSize(UILayout.scale(mod.width()),UILayout.scale(mod.height()));
    super.setLocation( UILayout.translateX(mod.getX()), UILayout.translateY(mod.getY()) );  // avoid overridden version
  }

  /** update UI location based on PModule object */
  public void updateLocation() {
    setSize(UILayout.scale(myModule.width()),UILayout.scale(myModule.height()));
    super.setLocation( UILayout.translateX(myModule.getX()), UILayout.translateY(myModule.getY()) );
  }

  public void setLocation(int x, int y) {
    super.setLocation(x,y);
    myModule.setLocation(UILayout.untranslateX(x),UILayout.untranslateY(y));
  }

  public void flipHorizontal() {
    myModule.flipHorizontal();
    getParent().getParent().repaint();
  }

  public void flipVertical() {
    myModule.flipVertical();
    getParent().getParent().repaint();
  }

  public void rotate() {
    myModule.rotate();
    getParent().getParent().repaint();
  }


  /* draw the terminals - the module should otherwise draw itself */
  public void paintComponent(Graphics g) {
    updateLocation();
    setBackground(myModule.getColor());
    super.paintComponent(g);
    for(int i = 0; i < myModule.numTerminals(); i++) {
      PTerminal pt = myModule.getTerminal(i);
      g.setColor(Color.black);
      int tx = Math.max( UILayout.scale(pt.getX())-2, 0);
      int ty = Math.max( UILayout.scale(pt.getY())-2, 0);
      tx = Math.min( tx, UILayout.scale(myModule.width()) - 5 );
      ty = Math.min( ty, UILayout.scale(myModule.height()) - 5 );
      g.fillRect(tx, ty, 5, 5);
    }
  }
}

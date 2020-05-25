package placement.ui;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.Component;
import java.awt.Point;

/**
 * Title:        TestDrag
 * Description:  Test of direct manipulation (dragging, stretching) of label components
 * Copyright:    Copyright (c) 2001
 * Company:      Lafayette College
 * @author John A. Nestor
 * @version 1.0
 */

public class DragListener implements MouseListener, MouseMotionListener {

  private int firstX = 0;
  private int firstY = 0;

  public DragListener() {
  }

  public void mouseClicked(MouseEvent e) {
    UIModule um = (UIModule)e.getComponent();
    if (e.isAltDown()) um.flipVertical();
    else if (e.isShiftDown()) um.flipHorizontal();
    else um.rotate();
  }

  public void mousePressed(MouseEvent e) {
    firstX = e.getX();
    firstY = e.getY();
  }
  public void mouseReleased(MouseEvent e) {
    firstX = 0;
    firstY = 0;
  }

  public void mouseEntered(MouseEvent e) {
  }

  public void mouseExited(MouseEvent e) {
  }

  public void mouseDragged(MouseEvent e) {
    int dx = e.getX() - firstX;
    int dy = e.getY() - firstY;
    //System.out.println("firstX=" + firstX + " firstY=" + firstY + " x=" + e.getX() + " y=" + e.getY() + " dx= " + dx + " dy=" + dy);
    Component c = e.getComponent();
    Point loc = c.getLocation();
    loc.x += dx;
    loc.y += dy;
    c.setLocation(loc);
    c.getParent().getParent().repaint();  // necessary to redraw nets!
  }

  public void mouseMoved(MouseEvent e) {
  }
}
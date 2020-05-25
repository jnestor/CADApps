package MazeRouter;

import java.io.*;
import java.awt.*;
import javax.swing.*;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Enumeration;


/**
 * Title:        MazeRouterApp
 * Description:  Animation of Lee Algorithm for maze routing.
 * Company:      Lafayette College
 * @author John A. Nestor
 * @version 1.0
 *
 * This program implements an animation of Lee's maze routing
 * algorithm.  Originally written as an applet in 2001; ported
 * to an application in April 2020.
 */

class MazeRouterApp {

// todo: parameterize size based on command line arguments?

private static void createAndShowGUI() {
    System.out.println("Hello, you old maze world!");
    System.out.println("and I mean it!");
    MazeRouterFrame mf = new MazeRouterFrame();
    mf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    mf.setMinimumSize(new Dimension(800,400));
    System.out.println("init?");
    mf.initMazeRouterFrame(1, false);
    mf.pack();
    mf.setVisible(true);
  }

  public static void main(String[] args) {
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        createAndShowGUI();
      }
    });
  }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author 15002
 */
import java.awt.*;
import javax.swing.*;

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

class HadlockApp {

// todo: parameterize size based on command line arguments?

private static void createAndShowGUI() {
    HadlockFrame mf = new HadlockFrame();
    mf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    mf.setMinimumSize(new Dimension(800,400));
    mf.initHadlockFrame(1);
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

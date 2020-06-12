// package placement.apps;
package placement.apps;
import placement.PLayout;
import placement.moves.PMoveHistory;
import placement.ui.UILayout;
import placement.ui.UIIterImprove;

import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import javax.swing.*;
import java.io.*;
import java.net.*;
import static placement.apps.FloorplannerApp.ua;
import placement.ui.UIAnnealer;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author
 * @version 1.0
 */

public class IterImproveApp {

  static private UIIterImprove ui;
  
 public static void main(String[] args) {
    System.out.println("starting up...");
    JFrame f = new JFrame();
    f.setMinimumSize(new Dimension(1200,600));
    try {
            BufferedReader in = new BufferedReader(
                 new FileReader("/Users/nestorj/java_projects/CADApps/Placement/placement/ntest10.in"));
           
             ui = new UIIterImprove(in);
             f.getContentPane().add(ui);
             f.add(ui,BorderLayout.CENTER);
             f.setVisible(true);
      } catch (IOException e) {
          System.out.println("IOException: "+e);
          e.printStackTrace();               
      }
  }
}

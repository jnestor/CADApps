//package placement.apps;
package placement.apps;

import placement.PLayout;
import placement.ui.UILayout;
import placement.ui.UIAnnealer;

import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import javax.swing.*;
import java.io.*;
import java.net.*;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author
 * @version 1.0
 */

public class FloorplannerApp {

  static UIAnnealer ua;
  
  public static void main(String[] args) {
      System.out.println("starting up...");
      JFrame f = new JFrame();
      f.setMinimumSize(new Dimension(1200,600));
      try {
          BufferedReader in = new BufferedReader(
                             new FileReader("/Users/nestorj/java_projects/CADApps/Placement/placement/ntest10.in"));
           ua = new UIAnnealer(in);
           f.add(ua,BorderLayout.CENTER);
           f.setVisible(true);
      } catch (IOException e) {
          System.out.println("IOException: "+e);
          e.printStackTrace();               
      }
  }
}

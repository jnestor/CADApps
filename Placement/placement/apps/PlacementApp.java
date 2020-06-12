package placement.apps;

import placement.PLayout;
import placement.ui.UILayout;

import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.StringTokenizer;
import placement.ui.UIIterImprove;

/**
 * Title:        PlacementApplet
 * Description:  Animation of VLSI Module Placement
 * Copyright:    Copyright (c) 2001
 * Company:      Lafayette College
 * @author John Nestor
 * @version 1.0
 */

public class PlacementApp {

    public static void main(String[] args) {
        System.out.println("starting up...");
        JFrame f = new JFrame();
        f.setMinimumSize(new Dimension(600,600));
        try {
            BufferedReader in = new BufferedReader(
                 new FileReader("/Users/nestorj/java_projects/CADApps/Placement/placement/ntest10.in"));
           
             PLayout pl = new PLayout(in);
             JPanel ul = new UILayout(pl); 
             f.getContentPane().add(ul);
             f.add(ul,BorderLayout.CENTER);
             f.setVisible(true);
      } catch (IOException e) {
          System.out.println("IOException: "+e);
          e.printStackTrace();               
      }
    }
}

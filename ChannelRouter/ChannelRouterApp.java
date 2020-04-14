package ChannelRouter;

import java.io.*;
import java.awt.*;
import javax.swing.*;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Enumeration;

/** This program implement's the "Left Edge Algorithm" for channel
  * routing.  It includes vertical constraints but cannot deal with cyclic
  * vertical constraints.  Orignally written circa 1997 as a Java Applet;
  * ported to run as an application using Swing in April 2020.
  */

class ChannelRouterApp {

private static void createAndShowGUI() {
    System.out.println("Hello, channleized world!");
    ChannelRouterFrame cf = new ChannelRouterFrame();
    cf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    cf.setPreferredSize(new Dimension(800,400));
    cf.initChannelRouterFrame();
    cf.pack();
    cf.setVisible(true);
  }

  public static void main(String[] args) {
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        createAndShowGUI();
      }
    });
  }
}

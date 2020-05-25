// package placement.apps;

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

public class FloorplannerApplet extends JApplet {
  boolean isStandalone = false;
  /**Get a parameter value*/
  public String getParameter(String key, String def) {
    return isStandalone ? System.getProperty(key, def) :
      (getParameter(key) != null ? getParameter(key) : def);
  }

  /**Construct the applet*/
  public FloorplannerApplet() {
  }
  /**Initialize the applet*/
  public void init() {
    try {
      String layoutString = getParameter("LAYOUT");
      URL layoutURL;
      if (layoutString != null)
        layoutURL = new URL(getDocumentBase(),layoutString);
      else layoutURL = new URL(getDocumentBase(),"ntest10.in");
      BufferedReader in = new BufferedReader(
                             new InputStreamReader(
                                    layoutURL.openStream() ) );
      ua = new UIAnnealer(in);
      this.getContentPane().add(ua);
    } catch(MalformedURLException e){
      System.out.println("URLException: "+e);
      e.printStackTrace();
    } catch(IOException e){
      System.out.println("IOException: "+e);
      e.printStackTrace();
    } catch(Exception e) {
      System.out.println("Exception: " + e);
      e.printStackTrace();
    }
  }
  /**Component initialization*/
  private void jbInit() throws Exception {
    // this.setSize(new Dimension(400,300));
  }
  /**Start the applet*/
  public void start() {
  }
  /**Stop the applet*/
  public void stop() {
    ua.stop();
  }
  /**Destroy the applet*/
  public void destroy() {
  }
  /**Get Applet information*/
  public String getAppletInfo() {
    return "Applet Information";
  }
  /**Get parameter info*/
  public String[][] getParameterInfo() {
    return null;
  }

  //static initializer for setting look & feel
  static {
    try {
      //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      //UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
    }
    catch(Exception e) {
    }
  }

  private UIAnnealer ua;
}

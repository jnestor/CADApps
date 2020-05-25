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

/**
 * Title:        PlacementApplet
 * Description:  Animation of VLSI Module Placement
 * Copyright:    Copyright (c) 2001
 * Company:      Lafayette College
 * @author John Nestor
 * @version 1.0
 */

public class PlacementApplet extends Applet {
  boolean isStandalone = false;
  /**Get a parameter value*/
  public String getParameter(String key, String def) {
    return isStandalone ? System.getProperty(key, def) :
      (getParameter(key) != null ? getParameter(key) : def);
  }

  /**Construct the applet*/
  public PlacementApplet() {
    System.out.println("PlacementApplet constructor called");
  }

  private JFrame frame;

  /**Initialize the applet*/
  public void init() {
    System.out.println("PlacementApplet: init");
    try{
       jbInit();
      setLayout(new BorderLayout());
      URL url = new URL( getDocumentBase(), "ntest10.in");
      BufferedReader in = new BufferedReader(
                             new InputStreamReader(
                                    url.openStream() ) );
      PLayout pl = new PLayout(in);
      JPanel ul = new UILayout(pl);
      add(ul,BorderLayout.CENTER);
    } catch(MalformedURLException e){
      System.out.println("URLException:"+e);
      e.printStackTrace();
    } catch(IOException e){
      System.out.println("IOException:"+e);
      e.printStackTrace();
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  /**Component initialization*/
  private void jbInit() throws Exception {
  }
  /**Start the applet*/
  public void start() {
  }
  /**Stop the applet*/
  public void stop() {
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
  /**Main method*/
  public static void main(String[] args) {
    PlacementApplet applet = new PlacementApplet();
    applet.isStandalone = true;
    Frame frame;
    frame = new Frame() {
      protected void processWindowEvent(WindowEvent e) {
        super.processWindowEvent(e);
        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
          System.exit(0);
        }
      }
      public synchronized void setTitle(String title) {
        super.setTitle(title);
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
      }
    };
    frame.setTitle("Applet Frame");
    frame.add(applet, BorderLayout.CENTER);
    applet.init();
    applet.start();
    frame.setSize(400,320);
    Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
    frame.setLocation((d.width - frame.getSize().width) / 2, (d.height - frame.getSize().height) / 2);
    frame.setVisible(true);
  }
}

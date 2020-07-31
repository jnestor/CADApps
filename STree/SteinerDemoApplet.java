/**
 * @Author: John Nestor <nestorj>
 * @Date:   2020-06-24T20:57:09-04:00
 * @Email:  nestorj@lafayette.edu
 * @Last modified by:   nestorj
 * @Last modified time: 2020-06-24T20:57:09-04:00
 */



import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import javax.swing.*;
import java.io.*;
import java.net.*;

/**
* Title:
* Description:
* Copyright:    Copyright (c) 2006
* Company:
* @author
* @version 1.0
*/

public class SteinerDemoApplet extends JApplet {

  UISteinerDemo us;

  public SteinerDemoApplet() {
  }

  /**Initialize the applet*/
  public void init() {
    us = new UISteinerDemo();
    this.getContentPane().add(us);
  }

  /**Start the applet*/
  public void start() {
    String graphString = getParameter("GRAPH");
    URL graphURL;
    if (graphString != null) {
      try {
        graphURL = new URL(getDocumentBase(),graphString);
        BufferedReader in = new BufferedReader( new InputStreamReader(
        graphURL.openStream() ) );
        us.readGraph(in);
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

    } else
    us.initRandom();
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

}

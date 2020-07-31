/**
* @Author: John Nestor <nestorj>
* @Date:   2020-06-24T11:05:41-04:00
* @Email:  nestorj@lafayette.edu
 * @Last modified by:   nestorj
 * @Last modified time: 2020-06-24T20:51:16-04:00
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

public class RMSTApp {

  static UIPrimMST upr;


  public static void main(String[] args) {
    System.out.println("starting up...");
    JFrame f = new JFrame();
    f.setMinimumSize(new Dimension(600,600));
    upr = new UIPrimMST();
    f.getContentPane().add(upr);
    f.setVisible(true);
    upr.initRandom();
  }


  /**Start the applet*/
  //   public void start() {
  // String graphString = getParameter("GRAPH");
  // URL graphURL;
  // if (graphString != null) {
  //     try {
  // 	graphURL = new URL(getDocumentBase(),graphString);
  // 	BufferedReader in = new BufferedReader( new InputStreamReader(
  // 						    graphURL.openStream() ) );
  // 	upr.readGraph(in);
  //     } catch(MalformedURLException e){
  // 	System.out.println("URLException: "+e);
  // 	e.printStackTrace();
  //     } catch(IOException e){
  // 	System.out.println("IOException: "+e);
  // 	e.printStackTrace();
  //     } catch(Exception e) {
  // 	System.out.println("Exception: " + e);
  // 	e.printStackTrace();
  //     }
  //
  // } else
  //     upr.initRandom();
  //   }


}

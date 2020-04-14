package ChannelRouter;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Enumeration;

public class ChannelRouterFrame extends JFrame implements ActionListener {

  private JTextField[] upperFields, lowerFields;
  private Netlist n;
  private ChannelRouterPanel cp;

  public void initChannelRouterFrame() {
    System.out.println("starting applet!!!");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    n = new Netlist();
    setLayout(new BorderLayout());
    JPanel up = new JPanel();
    JPanel lp = new JPanel();
    JPanel gp = new JPanel();
    JPanel bp = new JPanel();
    cp = new ChannelRouterPanel(n);
    setLayout(new BorderLayout());
    up.setLayout(new GridLayout(1, Netlist.NUM_COLS));
    lp.setLayout(new GridLayout(1,Netlist.NUM_COLS));
    gp.setLayout(new BorderLayout());
    bp.setLayout(new FlowLayout());
    Button cb = new Button("Clear Nets");
    Button rb = new Button("Route Nets");
    cb.addActionListener(this);
    rb.addActionListener(this);

    upperFields = new JTextField[Netlist.NUM_COLS];
    lowerFields = new JTextField[Netlist.NUM_COLS];
    for (int i = 0; i < Netlist.NUM_COLS; i++) {
      JTextField tf;
      upperFields[i] = tf = new JTextField("", 2);
      up.add(tf);
      lowerFields[i] = tf = new JTextField("", 2);
      lp.add("Center", tf);
    }
    gp.add("North", up);
    gp.add("South", lp);
    gp.add("Center", cp);
    bp.add(cb);
    bp.add(rb);
    getContentPane().add("South", bp);
    getContentPane().add("Center", gp);

  }

  /** Clear all net fields */
  private void clearNetFields() {
    for (int i = 0; i < Netlist.NUM_COLS; i++) {
      upperFields[i].setText("");
      lowerFields[i].setText("");
    }
  }

  /** Scan the terminal fields for each column and build up the netlist data structure */
  private void scanFieldsAndRoute() {
    n.clear();
    for (int i = 0; i < Netlist.NUM_COLS; i++) {
      String uname = upperFields[i].getText().trim();
      String lname = lowerFields[i].getText().trim();
      Terminal term;
      Net nt;
      // process upper terminal of column i
      if (!uname.equals("")) {
        nt = n.findNet(uname);
        if (nt == null) {
          nt = new Net(uname);
          n.addNet(nt);
          }
        term = n.getTerminal(i, Terminal.TOP);
        nt.addTerminal(term);
      }
      // now process lower terminal of column i
      if (!lname.equals("")) {
        nt = n.findNet(lname);
        if (nt == null) {
          nt = new Net(lname);
          n.addNet(nt);
        }
        term = n.getTerminal(i, Terminal.BOTTOM);
        nt.addTerminal(term);
      }
    }
    // n.write(System.out);
    n.leftEdgeAlgorithm();
    // n.write(System.out);
  }

  public void actionPerformed(ActionEvent e) {
    if (e.getActionCommand().equals("Route Nets")) {
      scanFieldsAndRoute();
      cp.repaint();
    } else if (e.getActionCommand().equals("Clear Nets")){
      clearNetFields();
      scanFieldsAndRoute();  // to clear the old nets away
      cp.repaint();
    }
  }

  // public boolean handleEvent(Event evt) {
  //   if (evt.id == Event.WINDOW_DESTROY) System.exit(0);
  //   return super.handleEvent(evt);
  // }
  //
  // public boolean action(Event evt, Object arg) {
  //   if (arg.equals("Route Nets")) {
  //     scanFieldsAndRoute();
  //     cp.repaint();
  //   } else if (arg.equals("Clear Nets")) {
  //     clearNetFields();
  //     scanFieldsAndRoute();  // to clear the old nets away
  //     cp.repaint();
  //   } else return super.action(evt, arg);
  //   return true;
  // }

}

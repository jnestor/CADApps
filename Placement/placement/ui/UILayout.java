package placement.ui;

import placement.*;
import placement.anneal.PAnnealInterface;
import placement.moves.PMove;


import java.awt.FlowLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.BoxLayout;
import javax.swing.Box;
import javax.swing.BorderFactory;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;
import java.util.ArrayList;


/**
 * Title:        PlacementApplet
 * Description:  Animation of VLSI Module Placement
 * Copyright:    Copyright (c) 2001
 * Company:      Lafayette College
 * @author John Nestor
 * @version 1.0
 */


public class UILayout extends JPanel implements ActionListener, PAnnealInterface {
    JPanel messagePanel;
    private List moduleList = new ArrayList();
//    private UIMetropolisGauge metroGauge;

    public UILayout(PLayout ml) {
	super();
	myLayout = ml;
	setBorder( BorderFactory.createEmptyBorder(2,2,2,2) );
	setLayout(new BorderLayout());
	placementPanel = new  UIPlacementPanel(myLayout);
	add(placementPanel,BorderLayout.CENTER);
	placementPanel.setVisible(true); // is this necessary?
	//placementPanel.setPreferredSize(200,200);
	placementPanel.setBorder( BorderFactory.createLineBorder(Color.black) );
	add(placementPanel,BorderLayout.CENTER);
	statusPanel = new JPanel();
	statusPanel.setLayout(new GridLayout(1,4));
	//  statusPanel.setPreferredSize(new Dimension(200,30));
	areaDisplay = new UIValDisplay("Area",0);
	statusPanel.add(areaDisplay);
	olapDisplay = new UIValDisplay("Overlap",0);
	statusPanel.add(olapDisplay);
	wireDisplay = new UIValDisplay("Wirelength",0);
	statusPanel.add(wireDisplay);
	costDisplay = new UIValDisplay("Cost",0);
	statusPanel.add(costDisplay);
	add(statusPanel,BorderLayout.NORTH);
	messagePanel = new JPanel();
	messagePanel.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
//	messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.X_AXIS));
	messagePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
	fitButton = new JButton("FIT");
	fitButton.addActionListener(this);
	messagePanel.add(fitButton);
//	metroGauge  = new UIMetropolisGauge(-1.0,-1.0);
//	messagePanel.add(metroGauge);
	messageLabel = new JLabel("This Space for Rent");
//	messageLabel.setMinimumSize(new Dimension(100,20));
	messagePanel.add(messageLabel);
//	messagePanel.add(Box.createHorizontalGlue());
	add(messagePanel,BorderLayout.SOUTH);
	addModules();
	myLayout.calcCost();
    }
    
  private void addModules() {
    for (int i = 0; i < myLayout.numModules(); i++ ) {
      PModule pm = myLayout.getModule(i);
      UIModule um = new UIModule(pm);
      moduleList.add(um);
      placementPanel.getContentPane().add(um);
    }
  }

  public void setMessage(String m) {
    messageLabel.setText(m);
  }

    public void setMoveMessage() {
	PMove lastMove = myLayout.getLastMove();
	if (lastMove != null) {
	    setMessage(lastMove.toString());
//	    metroGauge.setValues(lastMove.getAcceptRandom(), lastMove.getAcceptProbability());
	}
	else {
	    (setMessage(""));
//	    metroGauge.setValues(-1.0,-1.0);
	}
    }

  public void setCostDisplay() {
    areaDisplay.setValue(myLayout.currentArea());
    olapDisplay.setValue(myLayout.currentOverlap());
    wireDisplay.setValue(myLayout.currentWirelength());
    costDisplay.setValue(myLayout.currentCost());
    statusPanel.repaint();
 }

  public void paintComponent(Graphics g) {
    if (updateAllModules) {
      for (int i=0; i<moduleList.size(); i++) {
        UIModule um = (UIModule)moduleList.get(i);
        um.updateLocation();
        updateAllModules = false;
      }
    }
    int mm = myLayout.getMoveModuleNumber();
    if (mm >= 0) {
      // this will break if we ever add more than UIModules to UILayout!
      UIModule moveUIModule = (UIModule)moduleList.get(mm);
      moveUIModule.updateLocation();
      // check if it's off-screen, and re-zoom if so
      int x = moveUIModule.getX();
      int y = moveUIModule.getY();
      // need to adjust for insets, too?
      if (x + moveUIModule.getWidth() < 0 || y + moveUIModule.getHeight() < 0 ||
          x > placementPanel.getWidth() || y > placementPanel.getHeight() )
          autoZoom(1.2);
    }
    setCostDisplay();
    setMoveMessage();
    super.paintComponent(g);
  }

  /** convert placement coordinate to screen coordinate */
  private static int offsetX = 0; // offset from screen "origin" in layout coordinates
  private static int offsetY = 0;

  public static int scale(int i) { return (int)((double)i * zoom); }

  public static int translateX(int x) { return (int)((offsetX + x) * zoom); }

  public static int translateY(int y) { return (int)((offsetY + y) * zoom); }

  public static int untranslateX(int lx) { return (int)(lx / zoom - offsetX); }

  public static int untranslateY(int ly) { return (int)(ly / zoom - offsetY); }

  /** convert screen coordinate to placement coordinate */
  public static int unscale(int i) { return (int)((double)(i) / zoom); }

  public static void setZoom(double s) { zoom = s; }

  private static double zoom = 2.0;

  public void autoZoom() { autoZoom(1.1); }

  public void autoZoom(double bloat) {
    if (bloat < 1.0) bloat = 1.0;
    double margin = (bloat - 1.0)/2.0;
    offsetX = - (myLayout.currentLeftEdge() - (int)(myLayout.currentWidth()*margin));
    offsetY = - (myLayout.currentTopEdge() - (int)(myLayout.currentHeight()*margin));
    double vscale = (double)placementPanel.getWidth() /
        ((double)myLayout.currentWidth()*bloat);
    double hscale = (double)placementPanel.getHeight() /
        ((double)myLayout.currentHeight()*bloat);
    zoom = Math.min(vscale,hscale);
    updateAllModules = true;
    repaint();
  }

  public synchronized void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();
    if (cmd == "FIT") autoZoom(1.2);
  }

  private PLayout myLayout;
  private JRootPane placementPanel;
  private JPanel statusPanel;
  private UIValDisplay areaDisplay;
  private UIValDisplay olapDisplay;
  private UIValDisplay wireDisplay;
  private UIValDisplay costDisplay;
  private JLabel messageLabel;
  private JButton fitButton;
  private boolean updateAllModules = false;

    /** interesting event: move selected but not applied yet */
  public void showSelectMove() throws InterruptedException {
    repaint();
  }

  /** interesting event: move applied and about to be accepted */
  public void showAcceptMove() throws InterruptedException  {
    repaint();
  }

  /** interesting event: move applied and about to be rejected */
  public void showRejectMove() throws InterruptedException {
    repaint();
  }
  /** interesting event: move complete */
  public void showCompleteMove() throws InterruptedException {
    repaint();
  }

  /** interesting event: end of temperature, about to update */
  public void showUpdateTemperature() throws InterruptedException  {
    autoZoom(1.2);
    repaint();
  }

  public static void main(String [] args) {
    PLayout test = new PLayout("ntest10.in");
    JFrame jf = new JFrame("UILayout Test");
    UILayout uil = new UILayout(test);
    uil.setSize(200,200);
    jf.setSize(300,300);
    //jf.pack();
    jf.getContentPane().add(uil);
    jf.setVisible(true);
    jf.addWindowListener( new WindowAdapter()
      {
	    public void windowClosing(WindowEvent e) { System.exit(0); }
      } );
    uil.autoZoom(3.0);
  }
}

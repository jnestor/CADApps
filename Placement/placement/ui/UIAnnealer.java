package placement.ui;

/* import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JCheckBox;
*/
import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.Color;
import java.awt.Insets;
import java.awt.BorderLayout;
import javax.swing.BoxLayout;
import javax.swing.Box;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Graphics;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.lang.Thread;
import java.lang.Runnable;

/**
 * Title:        PlacementApplet
 * Description:  Animation of VLSI Module Placement
 * Copyright:    Copyright (c) 2001
 * Company:      Lafayette College
 * @author John Nestor
 * @version 1.0
 *
 */

import placement.*;
import placement.anneal.*;
import placement.moves.PMove;


/** provides user-interface for annealing */
public class UIAnnealer extends JPanel implements Runnable, ActionListener, PAnnealInterface {
  private PAnnealer myAnnealer;
  private PLayout myLayout;
  private UILayout myUILayout;
  private static final int STOP=0;
  private static final int RUN=1;
  private static final int PAUSE = 2;
  private static final int STEP = 3;
  private int state = STOP;
  private boolean animate = true;
  private Thread runner = null;
  private UIOptionsDialog myDialog;

  // options
  private double optT0=0.0001;
  private boolean optT0Adaptive = true;
  private boolean optAnimateMoves = true;
  private int optMovesPerTemp = 0;
  private boolean optMovesPerTempPerModule = false;
  private double optCoolRate = 0.95;
  private int optSpeed = 50;

  public void setOptT0Adaptive(boolean b) { optT0Adaptive = b; }

  public void setOptInitialTemperature(double t0) { optT0 = t0; }

  public void setOptAnimateMoves(boolean b) { optAnimateMoves = b; }

  public void setOptMovesPerTemp(int m) { optMovesPerTemp = m; }
  public int getOptMovesPerTemp() { return optMovesPerTemp; }

  public void setOptMovesPerTempPerModule(boolean b) { optMovesPerTempPerModule = b; }
  public boolean getOptMovesPerTempPerModule() { return optMovesPerTempPerModule; }

  public void setOptCoolRate(double d) { optCoolRate = d; }

  public double getOptCoolRate() { return optCoolRate; }

  public void setOptSpeed(int s) { optSpeed = s; }

  public int getOptSpeed() { return optSpeed; }

  // user-interface elements
    private JPanel annealerPanel;
    private JPanel historyPanel;
    private JPanel tempHistoryPanel;
    private UIMoveHistory moveHistoryPanel;
    private JScrollPane moveHistoryScrollPane;
    private JTabbedPane historyTabbedPane;
    private JPanel statusPanel;
    private JPanel controlPanel;
    private JToggleButton stopButton = new JToggleButton(
	new ImageIcon(getClass().getResource("images/stop.gif")),true);
    // hack to allow reading of image files from JAR file (Just Java 4e p. 338)
    private JToggleButton runButton = new JToggleButton(
	new ImageIcon(getClass().getResource("images/start.gif")));
    private JToggleButton pauseButton = new JToggleButton(
	new ImageIcon(getClass().getResource("images/pause.gif")));
    private JToggleButton stepButton = new JToggleButton(
	new ImageIcon(getClass().getResource("images/step.gif")));
  private ButtonGroup bg = new ButtonGroup();
    private JButton optionsButton = new JButton("OPTIONS");
    private JLabel tempLabel = new JLabel("T=");
    private JTextField tempField = new JTextField(8);
    private UIValDisplay tempDisplay = new UIValDisplay("Temperature",0);
    private UIValDisplay attemptsDisplay = new UIValDisplay("Attempts",0);
    private UIValDisplay acceptedDisplay = new UIValDisplay("Accepted",0);
    private UIValDisplay percentDisplay = new UIValDisplay("%",0);
    
    public UIAnnealer(BufferedReader in) throws IOException {
	// create top-level objects
	myLayout = new PLayout(in);
	myUILayout = new UILayout(myLayout);
	myAnnealer = new PAnnealer(myLayout, this);
	// now do user-interface
	annealerPanel = new JPanel(new BorderLayout());
	this.setLayout(new GridLayout(1,2));
	this.add(myUILayout);
	annealerPanel.setBorder( BorderFactory.createEmptyBorder(2,2,2,2) );
	statusPanel = new UIAnnealStatusPanel();
	statusPanel.setLayout(new GridLayout(1,5));
	statusPanel.add(tempDisplay);
	statusPanel.add(attemptsDisplay);
	statusPanel.add(acceptedDisplay);
	statusPanel.add(percentDisplay);
	JPanel controlPanel = new JPanel();
	controlPanel.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
	controlPanel.setLayout(new BoxLayout(controlPanel,BoxLayout.X_AXIS));
	bg.add(stopButton);
	bg.add(runButton);
	bg.add(pauseButton);
	bg.add(stepButton);
	stopButton.setActionCommand("STOP");
	runButton.setActionCommand("RUN");
	pauseButton.setActionCommand("PAUSE");
	stepButton.setActionCommand("STEP");
	optionsButton.setActionCommand("OPTIONS");
	stopButton.addActionListener(this);
	runButton.addActionListener(this);
	pauseButton.addActionListener(this);
	stepButton.addActionListener(this);
	optionsButton.addActionListener(this);
	
	controlPanel.add(Box.createHorizontalGlue());
	controlPanel.add(stopButton);
	controlPanel.add(runButton);
	controlPanel.add(pauseButton);
	controlPanel.add(stepButton);
	controlPanel.add(Box.createHorizontalGlue());
	controlPanel.add(optionsButton);
	controlPanel.add(Box.createHorizontalGlue());
	annealerPanel.add(statusPanel,BorderLayout.NORTH);
	annealerPanel.add(controlPanel,BorderLayout.SOUTH);
	
	myDialog = new UIOptionsDialog(this);  // do this first to set up options
	myDialog.pack();
	myDialog.setVisible(false);
	myDialog.repaint();

	moveHistoryPanel = new UIMoveHistory();
	moveHistoryPanel.setOptMetropolisDisplay(true);
	if (optMovesPerTempPerModule) moveHistoryPanel.setHistoryLength(optMovesPerTemp * myLayout.numModules());
	else moveHistoryPanel.setHistoryLength(optMovesPerTemp);
	moveHistoryScrollPane = new JScrollPane(moveHistoryPanel);
	moveHistoryScrollPane.setBorder(BorderFactory.createLineBorder(Color.black));

	tempHistoryPanel = new UITempHistory(myAnnealer);
	historyTabbedPane = new JTabbedPane();
	historyTabbedPane.addTab("Move History", null, moveHistoryScrollPane, "Detailed move history");
	historyTabbedPane.addTab("Cooling Schedule",null,tempHistoryPanel,"Cost-vs-temperature");

	historyTabbedPane.setBorder(BorderFactory.createLineBorder(Color.black));
	annealerPanel.add(historyTabbedPane, BorderLayout.CENTER);
	this.add(annealerPanel);
  }

  public synchronized void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();
    if (cmd == "STOP") {
      runButton.enable();
      pauseButton.disable();
      stepButton.disable();
      state = STOP;
      if (runner != null) {
        runner.interrupt();
        runner = null;
      }
    } else if (cmd == "RUN") {
      pauseButton.enable();
      stepButton.disable();
      if (state == STOP) {
        if (runner == null) {
          runner = new Thread(this); // fire 'em up!
          runner.start();
          state = RUN;
      } else System.out.println("trying to start but non-null thread");
      } else if (state == PAUSE) {
        state = RUN;
        notifyAll();
      }
    } else if (cmd == "PAUSE") {
      if (state == STOP) {
        stopButton.setSelected(true);
      } else {
        stepButton.enable();
        state = PAUSE;
      }
    } else if (cmd == "STEP") {
      if (state == STOP) {
        stopButton.setSelected(true);
      } else {
        state = STEP;
        notifyAll();
      }
    } else if (cmd == "OPTIONS") {
      if (!myDialog.isVisible()) {
        myDialog.setVisible(true);
        myDialog.toFront();
      } else myDialog.setVisible(false);
    }
  }

  public void run() {
    try {
      if (optT0Adaptive) myAnnealer.findT0();
      else myAnnealer.setT0(optT0);
      myAnnealer.anneal();
    } catch (InterruptedException e) {
      // do nothing except quit
    }
    state = STOP;
    stopButton.setSelected(true);
    runner = null;
  }

  /** use to stop annealing when applet stop() method is called */
  public synchronized void stop() {
    runButton.enable();
    pauseButton.disable();
    stepButton.disable();
    state = STOP;
    if (runner != null) {
      runner.interrupt();
      runner = null;
    }
  }

  private static int ANIMATE_DELAY = 4000;



  /** use to update the Placement Layout */
  public synchronized void animateDelay() throws InterruptedException {
    Thread.sleep(ANIMATE_DELAY / optSpeed);
    if (state == STEP) {
      state = PAUSE;
      pauseButton.setSelected(true);
    }
    while (state == PAUSE) wait();
  }

 // "interesting events" from PAnnealInterface

  public void showSelectMove() throws InterruptedException {
    if (optAnimateMoves) {
      myUILayout.showSelectMove();
      animateDelay();
    }
  }

  public void showAcceptMove()  throws InterruptedException {
    statusPanel.repaint();
    if (optAnimateMoves) {
      myUILayout.showAcceptMove();
      animateDelay();
    }
  }

  public void showRejectMove() throws InterruptedException  {
    statusPanel.repaint();
    if (optAnimateMoves) {
      myUILayout.showRejectMove();
      animateDelay();
    }
  }

  public void showCompleteMove() throws InterruptedException {
    statusPanel.repaint();
    if (optAnimateMoves) {
	myUILayout.showCompleteMove();
	if (historyTabbedPane.getSelectedComponent() == moveHistoryScrollPane) {

	    moveHistoryScrollPane.repaint();
	}
    }
  }

  public void showUpdateTemperature() throws InterruptedException {
    myUILayout.showUpdateTemperature();
    repaint();
    animateDelay();
  }

  public static void main(String [] args) {
    JFrame jf = new JFrame("Floorplanner - Standalone Application");
    jf.getContentPane().setLayout(new BorderLayout());
    jf.setSize(600,300);
    //URL url = new URL( getDocumentBase(), "ntest10.in");
    try {
      BufferedReader in = new BufferedReader(
                             new FileReader(
                                    "ntest10.in" ) );
      UIAnnealer ua = new UIAnnealer(in);
      jf.getContentPane().add(ua, BorderLayout.CENTER);
      jf.setVisible(true);
      jf.addWindowListener( new WindowAdapter()
        {
	  public void windowClosing(WindowEvent e) { System.exit(0); }
        } );
      // uil.autoZoom();
    } catch (FileNotFoundException e) {
      System.out.println("Could not open file " + e);
    } catch (Exception e) {
      System.out.println("Exception: " + e);
      e.printStackTrace();
    }
  }

  class UIAnnealStatusPanel extends JPanel {
    public void paintComponent(Graphics g) {
      PTempHistory pt = myAnnealer.getTempHistory();
      PTempHistoryPoint pth = pt.getCurrentTempPoint();
      if (pth != null) {
        tempDisplay.setValue(pth.getTemperature());
        attemptsDisplay.setValue(pth.getAttemptCount());
        acceptedDisplay.setValue(pth.getAcceptCount());
        percentDisplay.setValue(pth.getAcceptPercent());
      }
      super.paintComponent(g);
    }
  }

 }













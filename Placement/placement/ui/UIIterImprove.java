package placement.ui;

import placement.moves.*;

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


/** provides user-interface for Iterative Improvement  */
public class UIIterImprove extends JPanel implements Runnable, ActionListener, PAnnealInterface {
  private PIterImprove myIterImprove;
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
    private boolean optAnimateMoves = true;
    private int optMoves = 50;
    private int optSpeed = 50;
    private int optIterations = 100;

    public void setOptAnimateMoves(boolean b) { optAnimateMoves = b; }

    public void setOptIterations(int i) { optIterations = i; }

    public void setOptMoves(int m) { optMoves = m; }
    public int getOptMoves() { return optMoves; }

    public void setOptSpeed(int s) { optSpeed = s; }
    public int getOptSpeed() { return optSpeed; }

    // user-interface elements
    private JPanel improverPanel;
    private JScrollPane historyScrollPane;
    private UIMoveHistory  historyPanel;
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
    private UIValDisplay attemptsDisplay = new UIValDisplay("Attempts",0);
    private UIValDisplay acceptedDisplay = new UIValDisplay("Accepted",0);
    private UIValDisplay percentDisplay = new UIValDisplay("%",0);
    
    public UIIterImprove(BufferedReader in) throws IOException {
	// create top-level objects
	myLayout = new PLayout(in);
	myUILayout = new UILayout(myLayout);
	myIterImprove = new PIterImprove(myLayout, this);
	// now do user-interface
	improverPanel = new JPanel(new BorderLayout());
	this.setLayout(new GridLayout(1,2));
	this.add(myUILayout);
	improverPanel.setBorder( BorderFactory.createEmptyBorder(2,2,2,2) );
	statusPanel = new UIIterImproveStatusPanel();
	statusPanel.setLayout(new GridLayout(1,5));
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
	improverPanel.add(statusPanel,BorderLayout.NORTH);
	improverPanel.add(controlPanel,BorderLayout.SOUTH);

	
	historyPanel = new UIMoveHistory();
	historyPanel.setOptMetropolisDisplay(false);
	historyPanel.setHistoryLength(optIterations);
	historyScrollPane = new JScrollPane(historyPanel);
	historyScrollPane.setBorder(BorderFactory.createLineBorder(Color.black));
	improverPanel.add(historyScrollPane, BorderLayout.CENTER);
	this.add(improverPanel);
//	myDialog = new UIOptionsDialog(this);   /* NEED TO REFROB OPTIONS FOR ITERIMPROVE! */
//	myDialog.pack();
//	myDialog.setVisible(false);
//	myDialog.repaint();
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
	    myIterImprove.improve(optIterations);
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
	    //historyPanel.repaint();
	    repaint();
	    animateDelay();
	}
    }
    
    public void showRejectMove() throws InterruptedException  {
	statusPanel.repaint();
	if (optAnimateMoves) {
	    myUILayout.showRejectMove();
	    //historyPanel.repaint();
	    repaint();
	    animateDelay();
	}
    }
    
    public void showCompleteMove() throws InterruptedException {
	//statusPanel.repaint();
	//historyPanel.repaint();
	repaint();
	if (optAnimateMoves) myUILayout.showCompleteMove();
    }
    
    public void showUpdateTemperature() throws InterruptedException {
	myUILayout.showUpdateTemperature();
	repaint();
	animateDelay();
    }
    
    public static void main(String [] args) {
	JFrame jf = new JFrame("Iterative Improvement Floorplanner - Standalone Application");
	jf.getContentPane().setLayout(new BorderLayout());
	jf.setSize(600,300);
	//URL url = new URL( getDocumentBase(), "ntest10.in");
	try {
	    BufferedReader in = new BufferedReader(
		new FileReader(
		    "ntest10.in" ) );
	    UIIterImprove ui = new UIIterImprove(in);
	    jf.getContentPane().add(ui, BorderLayout.CENTER);
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
    
    class UIIterImproveStatusPanel extends JPanel {
	public void paintComponent(Graphics g) {
	    PMoveHistory ph = PMove.getHistory();
	    if (ph != null) {
		attemptsDisplay.setValue(ph.getAttemptCount());
		acceptedDisplay.setValue(ph.getAcceptCount());
		percentDisplay.setValue(ph.getAcceptPercent());
	    }
	    super.paintComponent(g);
	}
    }
    
}

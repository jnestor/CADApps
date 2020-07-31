import java.lang.Thread;
import java.lang.Runnable;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.ImageIcon;
import javax.swing.BorderFactory;

import java.io.*;
//import javax.swing.*;
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

/**
 * Title:        UIAnimationController
 * Description:  Controls animation with a VCR -style control panel (stop/start/pause/step).
 * Starts and runs the animated algorithm a separate thread.  Algorithm is called using the
 * runAnimation method of the UIAnimated interface.  The algorithm then calls back to its user
 * interface at interesting events and the UI calls animateDelay() to wait for the next update.
 * PAUSE and STEP commands are realized by waiting in animateDelay().  STOP is realized by
 * killing the thread containing the animation.
 *
 * Copyright:    Copyright (c) 2006
 * Company:      Lafayette College
 * @author John Nestor
 * @version 1.0
 *
 */


public class UIAnimationController extends JPanel implements Runnable, ActionListener {

    // state variables for control panel
    public static final int STOP=0;
    public static final int RUN=1;
    public static final int PAUSE = 2;
    public static final int STEP = 3;

    private int state = STOP;
    private boolean animate = true;
    private Thread runner = null;

    UIAnimated ua;

    public void SetAnimated(UIAnimated a) { ua = a; }

    public int getState() { return state; }

    // options
    private int optSpeed = 50;
    
    public void setOptSpeed(int s) { optSpeed = s; }
    
    public int getOptSpeed() { return optSpeed; }
    
    // user-interface elements
    
    // hack to allow reading of image files from JAR file (Just Java 4e p. 338)
    private JToggleButton stopButton;
    private JToggleButton runButton;
    private JToggleButton pauseButton;
    private JToggleButton stepButton;
    private ButtonGroup bg = new ButtonGroup();

    public static final long serialVersionUID = 1L;  // to shut up serialization warning


    public UIAnimationController(UIAnimated a) {
	ua = a;
	// initialize the button images
	stopButton = new JToggleButton(new ImageIcon(getClass().getResource("images/stop.gif")),true);
	runButton = new JToggleButton(new ImageIcon(getClass().getResource("images/start.gif")));
	pauseButton = new JToggleButton(new ImageIcon(getClass().getResource("images/pause.gif")));
	stepButton = new JToggleButton(new ImageIcon(getClass().getResource("images/step.gif"))); 

/*	stopButton = new JToggleButton(ImageIconExtractor.getStopIcon(), true);
	runButton = new JToggleButton(ImageIconExtractor.getStartIcon());
	pauseButton = new JToggleButton(ImageIconExtractor.getPauseIcon());
	stepButton = new JToggleButton(ImageIconExtractor.getStepIcon());
*/

	stopButton.setToolTipText("Stop");
	runButton.setToolTipText("Start");
	pauseButton.setToolTipText("Pause");
	stepButton.setToolTipText("Single Step");


	setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
	setLayout(new FlowLayout(FlowLayout.LEFT));
	bg.add(stopButton);
	bg.add(runButton);
	bg.add(pauseButton);
	bg.add(stepButton);
	stopButton.setActionCommand("STOP");
	runButton.setActionCommand("RUN");
	pauseButton.setActionCommand("PAUSE");
	stepButton.setActionCommand("STEP");
	stopButton.addActionListener(this);
	runButton.addActionListener(this);
	pauseButton.addActionListener(this);
	stepButton.addActionListener(this);
	
	add(Box.createHorizontalGlue());
	add(stopButton);
	add(runButton);
	add(pauseButton);
	add(stepButton);
	add(Box.createHorizontalGlue());
    }

    public void interruptAnimation() {
	state = STOP;
	if (runner != null) {
	    runner.interrupt();
	    runner = null;
	}
    }

    public void enableAnimation() {
	stopButton.setSelected(true);
	stopButton.setEnabled(true);
	runButton.setEnabled(true);
	pauseButton.setEnabled(false);
	stepButton.setEnabled(true);
    }

    public void disableAnimation() {
	interruptAnimation();
	stopButton.setEnabled(false);
	runButton.setEnabled(false);
	pauseButton.setEnabled(false);
	stepButton.setEnabled(false);
    }

    public synchronized void actionPerformed(ActionEvent e) {
	String cmd = e.getActionCommand();
	// System.out.println("command = " + cmd);
	if (cmd == "STOP") {
	    stopButton.setSelected(true);
	    runButton.setEnabled(true);
	    pauseButton.setEnabled(false);
	    stepButton.setEnabled(true);
	    interruptAnimation();
	} else if (cmd == "RUN") {
	    pauseButton.setEnabled(true);
	    stepButton.setEnabled(false);
	    if (state == STOP) {
		if (runner == null) {
		    runner = new Thread(this); // fire 'em up!
		    runner.start();
		    state = RUN; 
		    } else System.out.println("trying to start but non-null thread");
		state = RUN;
	    } else if (state == PAUSE) {
		state = RUN;
		notifyAll();
	    }
	} else if (cmd == "PAUSE") {
	    if (state == STOP) {
		stopButton.setSelected(true);
	    } else {
		stepButton.setEnabled(true);
		state = PAUSE;
	    }
	} else if (cmd == "STEP") {
	    if (state == STOP) {
		pauseButton.setEnabled(true);
		runner = new Thread(this); // fire 'em up!
		runner.start();
		state = STEP; 
	    } else {
		state = STEP;
		notifyAll();
	    }
	}
    }


    /** set to stop state & set button enables accordingly */
    public void setStop() {
	state = STOP;
	stopButton.setSelected(true);
	runButton.setEnabled(true);
	pauseButton.setEnabled(false);
	stepButton.setEnabled(true);
    }


    /** run() method for "algorithm" thread */
    public void run() {
	try {
	    ua.runAnimation();
	} catch (InterruptedException e) {
	    // do nothing except quit
	}
	setStop();
	runner = null;
    }
    
    /** use to stop annealing when applet stop() method is called */
    public synchronized void stop() {
	runButton.setEnabled(true);
	pauseButton.setEnabled(false);
	stepButton.setEnabled(false);
	state = STOP;
	ua.stopAnimation();
	if (runner != null) {
	    runner.interrupt();
	    runner = null;
	}
	
    }
    
    private static int ANIMATE_DELAY = 12000;
    
    /** delay before updating display during animation */
    
    public synchronized void animateDelay(int delayCount) throws InterruptedException {
	Thread.sleep(delayCount * ANIMATE_DELAY / optSpeed);
	if (state == STEP) {
	    state = PAUSE;
	    pauseButton.setSelected(true);
	}
	while (state == PAUSE)
	    wait();
    }
    
    public synchronized void animateDelay() throws InterruptedException { animateDelay(1); }

    public static void main(String [] args) {
	JFrame jf = new JFrame("Control Panel Test");
	jf.setSize(300,300);
	jf.setVisible(true);
	UIAnimationController test = new UIAnimationController(null);
	jf.add(test);
	
	for (int i=1; i < 1000; i++) {
	    try{
		Thread.sleep(600);
		System.out.println("State = " + test.state);
	    } catch (InterruptedException e) {}
	}

    }

}













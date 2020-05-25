package edu.lafayette.vcache.visual;

import java.awt.Color;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.stream.events.StartDocument;

import edu.lafayette.vcache.core.Cache;
import edu.lafayette.vcache.core.Updater;
import edu.lafayette.vcache.visual.abst.VPanel;

public class VCacheAnimation extends Updater {
	private JButton playButton, pauseButton, nextStepButton;
	private JSlider delaySlider;
	private AnimationPanel panel;
	
	public VCacheAnimation(Cache cache, int unitAnimationDelay, Map<Integer, AnimationInterval> animationIntervalMap) {
		super(cache, unitAnimationDelay, animationIntervalMap);
		panel = new AnimationPanel(cache, this);
		
		setControlStatus(Updater.PAUSE);
	}
	
	public VPanel<?> getPanel() {
		return panel;
	}

	private Image getImage(String path) {
		if (VCache.getInstance().getMode() == VCache.APPLICATION)
			return Toolkit.getDefaultToolkit().getImage(path);
		else if (VCache.getInstance().getMode() == VCache.APPLET) {
			VCacheApplet applet = VCacheApplet.getInstance();
			return applet.getImage(applet.getCodeBase(), path);
//			return null;
		}
		return null;
	}
	
	public void setControlStatus(int status) {
		if (status == PLAY) {
			playButton.setIcon(new ImageIcon(getImage("images/start_inv.gif")));
			playButton.setBackground(Color.BLACK);
		} else {
			playButton.setIcon(new ImageIcon(getImage("images/start.gif")));
			playButton.setBackground(Color.WHITE);
		}
		if (status == PAUSE) {
			pauseButton.setIcon(new ImageIcon(getImage("images/pause_inv.gif")));
			pauseButton.setBackground(Color.BLACK);
		} else {
			pauseButton.setIcon(new ImageIcon(getImage("images/pause.gif")));
			pauseButton.setBackground(Color.WHITE);
		}
		if (status == NEXT_STEP) {
			nextStepButton.setIcon(new ImageIcon(getImage("images/step_inv.gif")));
			nextStepButton.setBackground(Color.BLACK);
		} else {
			nextStepButton.setIcon(new ImageIcon(getImage("images/step.gif")));
			nextStepButton.setBackground(Color.WHITE);
		}
		super.setControlStatus(status);
	}
	
	private class AnimationPanel extends VPanel<Updater> implements ActionListener, ChangeListener {
		public AnimationPanel(Cache c, Updater updater) {
			super(c, updater);

			playButton = new JButton();
			playButton.addActionListener(this);
			add(playButton);
			pauseButton = new JButton();
			pauseButton.addActionListener(this);
			add(pauseButton);
			nextStepButton = new JButton();
			nextStepButton.addActionListener(this);
//			nextStepButton.setEnabled(false);
			add(nextStepButton);
			
			add(new JLabel("Animation delay:"));
			delaySlider = new JSlider(0, 5000, getUnitAnimationDelay());
			delaySlider.addChangeListener(this);
			delaySlider.setPaintLabels(true);
			Hashtable<Integer, JLabel> table = new Hashtable<Integer, JLabel>();
			for (int i = 0; i <= 5; i++)
				table.put(i * 1000, new JLabel("" + i));
			delaySlider.setLabelTable(table);
			add(delaySlider);
			
			setControlStatus(getControlStatus()); //so that correct image is displayed
		}


		public void actionPerformed(ActionEvent e) {
			if (e.getSource().equals(playButton)) {
				setControlStatus(PLAY);
			} else if (e.getSource().equals(pauseButton)) {
				setControlStatus(PAUSE);
			} else if (e.getSource().equals(nextStepButton)) {
				setControlStatus(NEXT_STEP);
			}
		}
		
		public void stateChanged(ChangeEvent e) {
			new Thread(new Runnable() {
				public void run() {
					setUnitAnimationDelay(delaySlider.getValue());
				}
			}).start();
		}
	}
	
}

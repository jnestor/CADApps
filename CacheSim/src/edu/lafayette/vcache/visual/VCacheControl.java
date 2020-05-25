package edu.lafayette.vcache.visual;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import edu.lafayette.vcache.core.Cache;
import edu.lafayette.vcache.core.CacheAccess;
import edu.lafayette.vcache.core.Updater;
import edu.lafayette.vcache.core.view.CacheView;
import edu.lafayette.vcache.visual.abst.VPanel;

public class VCacheControl extends VPanel<Updater> implements CacheView, ActionListener {
	private JLabel refCountLabel;
	private JLabel hitCountLabel;
	private JLabel replaceCountLabel;
	private JLabel readCountLabel;
	private JLabel writeCountLabel;
	private JTextField addressEntryField;
	private JButton readButton;
	private JButton writeButton;
	
	public VCacheControl(Cache cache, VCacheAnimation animation) {
		super(cache, animation);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS) );

		add(animation.getPanel());
		
		JPanel statPanel = new JPanel();
		JPanel statPanel2 = new JPanel();
		refCountLabel = new JLabel();
		hitCountLabel = new JLabel();
		replaceCountLabel = new JLabel();
		readCountLabel = new JLabel();
		writeCountLabel = new JLabel();
		statPanel.add(refCountLabel);
		statPanel.add(hitCountLabel);
		statPanel.add(replaceCountLabel);
		statPanel2.add(readCountLabel);
		statPanel2.add(writeCountLabel);
		fillStatLabels();
		add(statPanel);
		add(statPanel2);
//		setBorder(BorderFactory.createLoweredBevelBorder());
		JPanel entryPanel = new JPanel();
		JLabel addressEntryLabel = new JLabel("Enter Address:");
		entryPanel.add(addressEntryLabel);
		addressEntryField = new JTextField(8);
		entryPanel.add(addressEntryField);
		addressEntryField.addActionListener(this); // allow update on return
		readButton = new JButton("Read");
		readButton.addActionListener(this);
		entryPanel.add(readButton);
		writeButton = new JButton("Write");
		writeButton.addActionListener(this);
		entryPanel.add(writeButton);
		//Removed Clear button from here, and put it to the frame to take a more global effect.
//		clearButton = new JButton("Clear");
//		clearButton.addActionListener(this);
//		entryPanel.add(clearButton);
		add(entryPanel);
	}
	
	public void actionPerformed(ActionEvent e) {

		int addr;
		String command = e.getActionCommand();
		if (command.equals("Clear")) {
			cache.clearCache();
		}
		else if (e.getSource().equals(readButton) || e.getSource().equals(writeButton) || e.getSource().equals(addressEntryField)) {
			if ((cache.getCurrentAccess() == null) || 
					(updater.getAccessStatus() == CacheAccess.COMPLETE_HIT) ||
					(updater.getAccessStatus() == CacheAccess.COMPLETE_MISS) ||
					(updater.getAccessStatus() == CacheAccess.NONE)) {
				try {
					addr = Integer.valueOf(addressEntryField.getText(), 16);
					addressEntryField.setText("");
				} catch (NumberFormatException ex) {
					addressEntryField.setBackground(Color.RED);
					Toolkit.getDefaultToolkit().beep();
					addressEntryField.setText("");
					addressEntryField.setBackground(Color.WHITE);
					return;
				}
				cache.newAccess(e.getSource().equals(writeButton) ? CacheAccess.DATA_WRITE : CacheAccess.DATA_READ, addr);
				updater.completeAccess();
			}
		}
	}

	public void updateLater() {
		fillStatLabels();
	}
	
	private void fillStatLabels() {
		refCountLabel.setText(String.format("References: %d", cache == null ? 0 : cache.getRefCount()));
		hitCountLabel.setText(String.format("| Hits: %d (%5.2f%%)", cache == null ? 0 : cache.getHitCount(), cache == null ? 0d : cache.getHitRate()));
		replaceCountLabel.setText(String.format("| Replaced: %d", cache == null ? 0 : cache.getReplaceCount()));
		readCountLabel.setText(String.format("Reads: %d", cache == null ? 0 : cache.getReadCount()));
		writeCountLabel.setText(String.format("| Writes: %d", cache == null ? 0 : cache.getWriteCount()));
	}
}

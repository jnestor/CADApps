package edu.lafayette.vcache.visual;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import edu.lafayette.vcache.visual.abst.VCacheLine;

public class VCompactCacheLineLabels extends JPanel {
	public VCompactCacheLineLabels(VCompactCacheLine compactCacheLine) {
		SpringLayout layout = new SpringLayout();
		setLayout(layout);
		JLabel validLabel = new JLabel("V");
		add(validLabel);
		validLabel.setToolTipText("Valid");
		
		JLabel tagLabel = new JLabel("T");
		add(tagLabel);
		tagLabel.setToolTipText("Tag");
		
		JLabel dirtyLabel = null;
		if (compactCacheLine.isDirtyFieldSupported()) {
			dirtyLabel = new JLabel("D");
			add(dirtyLabel);
//			dirtyLabel.setLocation(compactCacheLine.getLocation(VCacheLine.DIRTY_FIELD));
			dirtyLabel.setToolTipText("Dirty");
		}
		
		JLabel dataLabel = new JLabel("Data");
		add(dataLabel);
//		dataLabel.setLocation(compactCacheLine.getLocation(VCacheLine.DATA_PANEL));
		dataLabel.setToolTipText("Data");
	
		layout.putConstraint(SpringLayout.WEST, validLabel, compactCacheLine.getLocation(VCacheLine.VALID_FIELD).x, SpringLayout.WEST, this);
		System.out.println(compactCacheLine.getLocation(VCacheLine.TAG_FIELD));
		layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, tagLabel, compactCacheLine.getLocation(VCacheLine.TAG_FIELD).x + compactCacheLine.getWidth(VCacheLine.TAG_FIELD) / 2, SpringLayout.WEST, this);
		if (dirtyLabel != null)
			layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, dirtyLabel, compactCacheLine.getLocation(VCacheLine.DIRTY_FIELD).x + compactCacheLine.getWidth(VCacheLine.DIRTY_FIELD) / 2, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, dataLabel, compactCacheLine.getLocation(VCacheLine.DATA_PANEL).x + compactCacheLine.getWidth(VCacheLine.DATA_PANEL) / 2, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.SOUTH, this, 0, SpringLayout.SOUTH, validLabel);
	}
}

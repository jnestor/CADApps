package edu.lafayette.vcache.visual;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import edu.lafayette.vcache.core.Cache;
import edu.lafayette.vcache.core.CacheAccess;
import edu.lafayette.vcache.core.CacheLine;
import edu.lafayette.vcache.core.Updater;
import edu.lafayette.vcache.core.policy.write.WriteBack;
import edu.lafayette.vcache.visual.abst.VCacheLine;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Point;
import java.util.ArrayList;

public class VFieldedCacheLine extends VCacheLine {
	private JTextField lineField;
	private JTextField validField;
	private JTextField tagField;
	private JTextField dirtyField;
	private JPanel dataPanel;
	private ArrayList<JTextField> dataFields;

	public VFieldedCacheLine(Cache c, Updater updater, int position, CacheLine... l) {
		super(c, updater, position, l);
		JTextField jtf;
		FlowLayout layout = new FlowLayout();
		layout.setVgap(3);
		setLayout(layout);
		lineField = new JTextField();
		lineField.setColumns(4);
		lineField.setFocusable(false);
		lineField.setText(String.format("%d", position));
		add(lineField);
		validField = new JTextField();
		validField.setColumns(1);
		validField.setFocusable(false); // user can't change!
		add(validField);
		tagField = new JTextField();
		tagField.setColumns(c.getTagBits()/4);
		tagField.setFocusable(false); // user can't change!
		add(tagField);
		if (c.getWritePolicy() instanceof WriteBack) {
			dirtyField = new JTextField("D");
			dirtyField.setColumns(1);
			dirtyField.setFocusable(false); // user can't change!
			add(dirtyField);
		}
		FlowLayout dataPanelLayout = new FlowLayout();
		dataPanelLayout.setVgap(0);
		dataPanel = new JPanel(dataPanelLayout);
		dataPanel.setOpaque(false);
		dataFields = new ArrayList<JTextField>(c.getWordCount());
		for (int i = 0; i < c.getWordCount(); i++) {
			jtf = new JTextField();
			jtf.setColumns(11);
			jtf.setFocusable(false);
			dataFields.add(jtf);
			dataPanel.add(jtf);
		}
		add(dataPanel);
	}

	public String getDataFieldContent(int index) {
		return dataFields.get(index).getText();
	}

	private void setDataFieldContents(int base, Color c) {
		JTextField df;
		for (int i = 0; i < updater.getOwner().getWordCount(); i++ ) {
			df = dataFields.get(i);
			df.setText(String.format("M[%x]", base + i*4));
			df.setBackground(c);
		}
	}

	private void setDataFieldEmpty(Color c) {
		JTextField df;
		for (int i = 0; i < updater.getOwner().getWordCount(); i++ ) {
			df = dataFields.get(i);
			df.setText("");
			df.setBackground(c);
		}
	}

	public void setStatus(int status) { // set graphical and values based on state of cache line &current access
		int address = updater.getBaseAddress();
		CacheAccess ca = updater.getCurrentAccess();
		
		if ((status & ACCESSED) > 0) {
				setBackground(Color.WHITE);
				validField.setText("1");
				validField.setBackground(Color.WHITE);
				tagField.setText(String.format("%x", updater.getTag()));
				tagField.setBackground(Color.WHITE);
				setDataFieldContents(address, Color.WHITE);
		} else if (status == NORMAL) { // not current / not valid - nothing to see here!
				setBackground(Color.GRAY);
				validField.setText("0");
				validField.setBackground(Color.WHITE);
				tagField.setText("");
				tagField.setBackground(Color.WHITE);
				setDataFieldEmpty(Color.WHITE);
		} else if ((status & HIT) > 0) {
			setBackground(Color.YELLOW);  // highlight current access
			validField.setText("1");  // it's a hit, so it must be valid!
			validField.setBackground(Color.YELLOW);
			tagField.setText(String.format("%x", updater.getTag()));
			tagField.setBackground(Color.YELLOW);
			setDataFieldContents(address, Color.YELLOW);
		} else if ((status & LOOKUP) > 0) {
			setBackground(Color.YELLOW);  // highlight current access
			if ((status & NOT_VALID) > 0) {
				validField.setText("0");
				validField.setBackground(Color.RED);
				tagField.setText("");
				tagField.setBackground(Color.WHITE);
				setDataFieldEmpty(Color.WHITE);
			} else {
				validField.setText("1");
				validField.setBackground(Color.WHITE);
				tagField.setText(String.format("%x", updater.getTag()));
				tagField.setBackground(Color.RED); // tag mismatch!
				setDataFieldContents(address, Color.WHITE);
			}
		} else if ((status & COMPLETE) > 0) {
			setBackground(Color.YELLOW);  // highlight current access
			Color fc = ((status & REPLACED) > 0 ? Color.GREEN : Color.YELLOW);
			validField.setText("1");
			validField.setBackground(fc);
			tagField.setText(String.format("%x", updater.getTag()));
			tagField.setBackground(fc);
			setDataFieldContents(address, fc);
		}
		if (cache.getWritePolicy() instanceof WriteBack) {
			WriteBack writeBack = (WriteBack) cache.getWritePolicy();
			dirtyField.setText(writeBack.isDirty(updater) ? "1" : "0");
		}
	}

	public JComponent getComp(int component) {
		if (component >= DATA_FIELD) 
			return dataFields.get(component - DATA_FIELD);
		switch(component) {
		case LINE_FIELD:
			return lineField;
		case VALID_FIELD:
			return validField;
		case TAG_FIELD:
			return tagField;
		case DATA_PANEL:
			return dataPanel;
		default:
			return null;
		}
	}

	public Point getLocation(int component) {
		JComponent comp = getComp(component);
		if(comp == null)
			return null;
		return comp.getLocation();
	}

	public int getWidth(int component) {
		JComponent comp = getComp(component);
		if(comp == null)
			return 0;
		return comp.getWidth();
	}
	
	public int getHeight(int component) {
		JComponent comp = getComp(component);
		if(comp == null)
			return 0;
		return comp.getHeight();
	}

}


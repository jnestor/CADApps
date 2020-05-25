package edu.lafayette.vcache.visual;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;

import edu.lafayette.vcache.core.Cache;

import java.lang.Math;

public class VAddress extends JPanel {

    private int tagBits;
    private int lineIndexBits;
    private int wordIndexBits;
    private final int BYTE_INDEX_BITS = 2;

    private JTextField tagField;
    private JTextField lineIndexField;
    private JTextField wordIndexField;
    private JTextField byteIndexField;

    private int log16f(int i) {
	return (int)Math.ceil(Math.log((double)i)/Math.log(16.0));
    }

    private void buildGUI() {
	JPanel tagPanel = new JPanel();
	tagPanel.setLayout(new BoxLayout(tagPanel, BoxLayout.Y_AXIS));
	tagField = new JTextField();
	tagField.setColumns(tagBits/4 + 1);
	tagField.setFocusable(false);
	tagPanel.add(new JLabel("Tag"), JLabel.CENTER_ALIGNMENT);
	tagPanel.add(tagField);
	add(tagPanel);
	JPanel lineIndexPanel = new JPanel();
	lineIndexPanel.setLayout(new BoxLayout(lineIndexPanel, BoxLayout.Y_AXIS));
	lineIndexField = new JTextField();
	lineIndexField.setColumns(lineIndexBits/4 + 1);
	lineIndexField.setFocusable(false);
	lineIndexPanel.add(new JLabel("L"), JLabel.CENTER_ALIGNMENT);
	lineIndexPanel.add (lineIndexField);
	add(lineIndexPanel);
	if (wordIndexBits > 0) {
	    JPanel wordIndexPanel = new JPanel();
	    wordIndexPanel.setLayout(new BoxLayout(wordIndexPanel, BoxLayout.Y_AXIS));
	    wordIndexPanel.add(new JLabel("W"), JLabel.CENTER_ALIGNMENT);
	    wordIndexField = new JTextField();
	    wordIndexField.setColumns(wordIndexBits/4 + 1);
	    wordIndexField.setFocusable(false);
	    wordIndexPanel.add(wordIndexField);
	    add(wordIndexPanel);
	}
	JPanel byteIndexPanel = new JPanel();
	byteIndexPanel.setLayout(new BoxLayout(byteIndexPanel, BoxLayout.Y_AXIS));
	byteIndexPanel.add(new JLabel("B"), JLabel.CENTER_ALIGNMENT);
	byteIndexField = new JTextField("0");
	byteIndexField.setColumns(1);
	byteIndexField.setFocusable(false);
	byteIndexPanel.	add(byteIndexField);
	add(byteIndexPanel);
    }

    public VAddress(int tb, int lb, int wb) {
	tagBits = tb;
	lineIndexBits = lb;
	wordIndexBits = wb;
	buildGUI();
    }

    public VAddress(Cache c) {
	this(c.getTagBits(), c.getLineIndexBits(), c.getWordIndexBits());
    }

    public void setAddress(int tag, int li, int wi) {
	tagField.setText(String.format("%x", tag));
	lineIndexField.setText(String.format("%x", li));
	if (wordIndexBits > 0) {
	    wordIndexField.setText(String.format("%x", wi));
	}
	byteIndexField.setText("0");
    }
	
    public static void main(String [] args) {
	JFrame f = new JFrame("VAddress Test");
	VAddress tv = new VAddress(27, 3, 2);
	f.getContentPane().add(tv);
	f.pack();
	f.setVisible(true);
	tv.setAddress(0x4040, 0x7, 0x0);
    }	

}

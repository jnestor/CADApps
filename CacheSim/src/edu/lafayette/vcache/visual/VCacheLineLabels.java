package edu.lafayette.vcache.visual;
import javax.swing.JPanel;
import javax.swing.JTextField;

import edu.lafayette.vcache.core.Cache;
import edu.lafayette.vcache.core.policy.write.WriteBack;

import java.awt.FlowLayout;
import java.util.ArrayList;

public class VCacheLineLabels extends JPanel {
	private JTextField lineField;
	private JTextField validField;
	private JTextField tagField;
	private ArrayList<JTextField> dataFields;

	public VCacheLineLabels(Cache c) {
		JTextField jtf;
		setLayout(new FlowLayout());
		lineField = new JTextField("Line");
		lineField.setColumns(4);
		lineField.setFocusable(false);
		add(lineField);
		validField = new JTextField("V");
		validField.setColumns(1);
		validField.setFocusable(false); // user can't change!
		add(validField);
		tagField = new JTextField("Tag");
		tagField.setColumns(c.getTagBits()/4);
		tagField.setFocusable(false); // user can't change!
		add(tagField);
		if (c.getWritePolicy() instanceof WriteBack) {
			JTextField dirtyField = new JTextField("D");
			dirtyField.setColumns(1);
			dirtyField.setFocusable(false); // user can't change!
			add(dirtyField);
		}
		FlowLayout dataPanelLayout = new FlowLayout();
		dataPanelLayout.setVgap(0);
		JPanel dataPanel = new JPanel(dataPanelLayout);
		dataPanel.setOpaque(false);
		dataFields = new ArrayList<JTextField>(c.getWordCount());
		for (int i = 0; i < c.getWordCount(); i++) {
			jtf = new JTextField(String.format("Word %d",i));
			jtf.setHorizontalAlignment(JTextField.CENTER);
			jtf.setColumns(11);
			jtf.setFocusable(false);
			dataFields.add(jtf);
			dataPanel.add(jtf);
		}
		add(dataPanel);
	}
}


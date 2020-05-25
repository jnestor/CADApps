package edu.lafayette.vcache.visual.settings;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class Setting<T> implements ActionListener, ItemListener{
	public static final int BUTTON = 1, FILE_OPEN = 2, FILE_SAVE = 3, COMBO_BOX = 4, FIELD = 5, SLIDER = 6, CHECK_BOX = 7, INT_FIELD = 8;
	public static final String COMBO_BOX_ITEMS = "combo_box_items", VALIDATOR = "validator";
	private T value, defaultValue;
	private String name; 
	private int type;
	private Map<String, ?> constraints;
//	private SettingsSupporter panel;
	private Component guiComp;
	private Settings container;
	private JComponent actionComponent;

	public Setting(String name, int type, Map<String, ?> constraints, T defaultValue) {
		this.name = name;
		this.type = type;
		if (constraints == null)
			this.constraints = new HashMap<String, Object>();
		else
			this.constraints = constraints;
		value = defaultValue;
		this.defaultValue = defaultValue;
	}
	
	public Component getGraphicalComponent() {
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.LEFT));
		if (type == COMBO_BOX) {
			panel.add(new JLabel(name));
			Object[] items = (Object[]) constraints.get(COMBO_BOX_ITEMS);
			JComboBox comboBox = new JComboBox(items);
			comboBox.setSelectedItem(value);
			comboBox.addItemListener(this);
			panel.add(comboBox);
			guiComp = comboBox;
		} else if (type == CHECK_BOX) {
			JCheckBox checkBox = new JCheckBox(name, (Boolean) value);
			checkBox.addActionListener(this);
			actionComponent = checkBox;
			panel.add(checkBox);
			guiComp = checkBox;
		} else if (type == FIELD || type == INT_FIELD) {
			panel.add(new JLabel(name));
			JTextField field = new JTextField(value.toString(), 7);
			actionComponent = field;
			field.addActionListener(this);
			panel.add(field);
			guiComp = field;
		}
		
		return panel;
	}
	
	@SuppressWarnings("unchecked")
	public void checkValue() {
		if (type == CHECK_BOX) {
			value = (T) (Object) ((JCheckBox) actionComponent).isSelected();
			container.getParent().settingChanged(this);
		} else if (type == FIELD) {
			value = (T) (Object) ((JTextField) actionComponent).getText();
		} else if (type == INT_FIELD) {
			try {
				if (constraints.get(VALIDATOR) != null && 
						!((FieldValidator) constraints.get(VALIDATOR)).validate(((JTextField) actionComponent).getText()))
					throw new Exception();
				value = (T) (Object) Integer.parseInt(((JTextField) actionComponent).getText());
			} catch (Exception ex) {
				JTextField field = (JTextField) actionComponent;
				field.setBackground(Color.RED);
				Toolkit.getDefaultToolkit().beep();
				value = defaultValue;
				field.setText(defaultValue.toString());
			}
		}
	}
	
	public void setValue(T value) {
		if (type == CHECK_BOX)
			((JCheckBox) guiComp).setSelected((Boolean) value);
		this.value = value;
	}

	public T getValue() {
		return value;
	}

	public String getName() {
		return name;
	}

	public int getType() {
		return type;
	}

	public Map<String, ?> getConstraints() {
		return constraints;
	}
	
	protected void setContainer(Settings container) {
		this.container = container;
	}
	
	public Settings getContainer() {
		return container;
	}

	
	public void actionPerformed(ActionEvent e) {
		actionComponent = (JComponent) e.getSource();
		checkValue();
	}

	@SuppressWarnings("unchecked")
	public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
			if (type == COMBO_BOX) {
				value = (T) e.getItem();
				container.getParent().settingChanged(this);
			}
		}
	}
	
	public interface FieldValidator {
		public boolean validate(String input);
	}
}
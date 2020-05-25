package edu.lafayette.vcache.visual.settings;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import edu.lafayette.vcache.visual.abst.VPanel;

public class Settings {
	private static final String CLOSE = "Close";
	private String name;
	private List<Setting<?>> settingList;
	private JFrame frame;
	private SettingsSupporter parent;
	private String closeButtonText;
	
	public Settings(String name, SettingsSupporter parent, String closeButtonText) {
		this.name = name;
		this.parent = parent;
		this.closeButtonText = closeButtonText;
		settingList = new ArrayList<Setting<?>>();
	}
	
	public Settings(String name, SettingsSupporter parent) {
		this(name, parent, CLOSE);
	}
	
	public void addSetting(Setting<?> setting) {
		setting.setContainer(this);
		settingList.add(setting);
	}
	
	public String getName() {
		return name;
	}
	
	public List<Setting<?>> getSettingList() {
		return settingList;
	}
	
	public void setFrame(JFrame frame) {
		this.frame = frame;
	}
	
	public JFrame getFrame() {
		return frame;
	}
	
	public SettingsSupporter getParent() {
		return parent;
	}

	public String getCloseButtonText() {
		return closeButtonText;
	}
}

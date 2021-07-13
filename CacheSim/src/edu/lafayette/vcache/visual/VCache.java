package edu.lafayette.vcache.visual;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import edu.lafayette.vcache.core.Cache;
import edu.lafayette.vcache.core.Updater;
import edu.lafayette.vcache.core.policy.setassocrepl.LRUReplacementPolicy;
import edu.lafayette.vcache.core.policy.setassocrepl.RandomReplacementPolicy;
import edu.lafayette.vcache.core.policy.setassocrepl.RoundRobinReplacementPolicy;
import edu.lafayette.vcache.core.policy.setassocrepl.SetAssociativeReplacementPolicy;
import edu.lafayette.vcache.core.policy.write.WriteBack;
import edu.lafayette.vcache.core.policy.write.WriteThrough;
import edu.lafayette.vcache.visual.abst.EntryPoint;
import edu.lafayette.vcache.visual.abst.VPanel;
import edu.lafayette.vcache.visual.settings.Setting;
import edu.lafayette.vcache.visual.settings.Settings;
import edu.lafayette.vcache.visual.settings.SettingsSupporter;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import java.io.*;

/** This class implements the cache visualization  */
/*------------------------------------------------------------*/
/*                                                            */
/*   Address words look like this:                            */
/*                                                            */
/*   +----------+------------+------------+------------+      */
/*   |  tag     | line index | word index | byte index |      */
/*   +----------+------------+------------+------------+      */
/*                                                            */
/*------------------------------------------------------------*/
public class VCache extends VPanel<VCacheAnimation> implements SettingsSupporter, ActionListener {
	public static final int APPLICATION = 1, APPLET = 2;
	private static final String SOURCE = "src", SOURCE_PANEL = "src_panel", LINE = "Line Count", WORD = "Word Count", ASSOC = "Associative Sets",
								LRU = "LRU", ROUND_ROBIN = "Round Robin", RANDOM = "Random", REPL_POLICY = "Set Associative Replacement Policy",
								WRITE_POLICY = "Write Policy", WRITE_THROUGH = "Write-through", WRITE_BACK = "Write-back";
	private static VCache vCache;
	private Settings viewSettings, cacheSettings;
	private List<Settings> settingsList;
	
	private VCacheDiagram diagram;
	private int mode;
//	private VCacheAnimation upd;
	private JInternalFrame diagramFrame, traceFrame, controlFrame, statFrame;
	
	private JMenuBar menuBar;
	private JDesktopPane dPane;
	private JMenuItem clearCacheMenuItem;	

	public void update() {
		
	}

	public VCache(Cache c, int mode) {
		this(c, mode, null);
	}
	
	//oldVCache is to copy the location and sizes of the previous windows, leave it null for new initialization with defaults
	public VCache(Cache c, int mode, VCache oldVCache) {
		super(c);
		this.mode = mode;
		vCache = this;
		//TODO: make a set method instead of add
		HashMap<Integer, Updater.AnimationInterval> animationIntervalMap = new HashMap<Integer, Updater.AnimationInterval>();
		animationIntervalMap.put(Updater.INITIAL, new Updater.AnimationInterval(2, 100));
		animationIntervalMap.put(Updater.LOOKUP_HIT, new Updater.AnimationInterval(1, 100));
		animationIntervalMap.put(Updater.LOOKUP_MISS, new Updater.AnimationInterval(1, 100));
		animationIntervalMap.put(Updater.COMPLETE_HIT, new Updater.AnimationInterval(1, 1));
		animationIntervalMap.put(Updater.COMPLETE_MISS, new Updater.AnimationInterval(1, 1));
		animationIntervalMap.put(Updater.NONE, new Updater.AnimationInterval(0, 1));
		animationIntervalMap.put(Updater.HIGHER_LEVEL_READ, new Updater.AnimationInterval(3, 1));
		animationIntervalMap.put(Updater.HIGHER_LEVEL_WRITE, new Updater.AnimationInterval(3, 1));
		updater = new VCacheAnimation(c, 2000, animationIntervalMap);
		VCacheTrace trace = new VCacheTrace(cache, (Updater)updater);
		((Updater)updater).setTrace(trace.getTrace());
//		VPanel.setVCache(this);
		
		
//		JFrame applicationFrame = new JFrame("VCache Application");
//		applicationFrame.add(getSettingsEnabledPanel(), BorderLayout.CENTER);
//		applicationFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		applicationFrame.setResizable(true);
		
		menuBar = new JMenuBar();
		JMenu cacheMenu = new JMenu("Cache");
		JMenu viewMenu = new JMenu("View");
		menuBar.add(cacheMenu);
		menuBar.add(viewMenu);
		JMenuItem showViewMenuItem = new JMenuItem("Show View");
		
		viewMenu.add(showViewMenuItem);
		
		clearCacheMenuItem = new JMenuItem("Clear");
		cacheMenu.add(clearCacheMenuItem);
		JMenuItem cacheSettingsMenuItem = new JMenuItem("Cache Settings");
		cacheMenu.add(cacheSettingsMenuItem);
		clearCacheMenuItem.addActionListener(this);
		if (mode == APPLICATION) {
			JMenuItem exitMenuItem = new JMenuItem("Exit");
			exitMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					System.exit(0);
				}
			});
			cacheMenu.add(exitMenuItem);
		}
		
		dPane = new JDesktopPane();
		dPane.setBackground(Color.GRAY);
		
		diagramFrame = new JInternalFrame("Cache Diagram");
		dPane.add(diagramFrame);
		diagram = oldVCache == null ? new VCacheDiagram(cache, (Updater)updater, diagramFrame) : new VCacheDiagram(cache, (Updater)updater, diagramFrame, oldVCache.diagram);
		JPanel temp = diagram.getSettingsEnabledPanel();
		diagramFrame.add(new JScrollPane(temp));
//		temp.setSize(temp.getPreferredSize());
		diagramFrame.setClosable(true);
		diagramFrame.setResizable(true);
		diagramFrame.pack();
		diagramFrame.addInternalFrameListener(diagram);
		diagramFrame.setDefaultCloseOperation(JInternalFrame.HIDE_ON_CLOSE);
		if (oldVCache != null) 
			copySizeAndLocation(diagramFrame, oldVCache.diagramFrame);
		diagramFrame.setVisible(true);
		
		traceFrame = new JInternalFrame("Trace");
		dPane.add(traceFrame);
		
		traceFrame.add(trace.getSettingsEnabledPanel());
		traceFrame.setClosable(true);
		traceFrame.setResizable(true);
		traceFrame.pack();
		traceFrame.addInternalFrameListener(trace);
		traceFrame.setDefaultCloseOperation(JInternalFrame.HIDE_ON_CLOSE);
		if (oldVCache != null) 
			copySizeAndLocation(traceFrame, oldVCache.traceFrame);
		else {
			traceFrame.setSize(150, diagramFrame.getHeight());
			traceFrame.setLocation(diagramFrame.getWidth(), 0);
		}
		traceFrame.setVisible(true);
		
		controlFrame = new JInternalFrame("Control");
		dPane.add(controlFrame);
		VCacheControl control = new VCacheControl(cache, (VCacheAnimation)updater);
		controlFrame.add(control.getSettingsEnabledPanel());
		controlFrame.setClosable(true);
		controlFrame.pack();
		if (oldVCache != null) 
			copySizeAndLocation(controlFrame, oldVCache.controlFrame);
		else
			controlFrame.setLocation(0, diagramFrame.getHeight());
		controlFrame.addInternalFrameListener(control);
		controlFrame.setDefaultCloseOperation(JInternalFrame.HIDE_ON_CLOSE);
		controlFrame.setVisible(true);
		
		statFrame = new JInternalFrame("Graph");
		dPane.add(statFrame);
		VCacheStatistics stat = new VCacheStatistics(cache, (Updater)updater);
		statFrame.add(stat.getSettingsEnabledPanel());
		statFrame.setClosable(true);
		statFrame.setResizable(true);
		if (oldVCache != null) 
			copySizeAndLocation(statFrame, oldVCache.statFrame);
		else {
			statFrame.setSize(diagramFrame.getWidth() + traceFrame.getWidth() - controlFrame.getWidth(), controlFrame.getHeight());
			statFrame.setLocation(controlFrame.getWidth(), diagramFrame.getHeight());
		}
		statFrame.addInternalFrameListener(stat);
		statFrame.setDefaultCloseOperation(JInternalFrame.HIDE_ON_CLOSE);
		statFrame.setVisible(true);
		
		dPane.setPreferredSize(new Dimension(traceFrame.getLocation().x + traceFrame.getWidth(), 
				controlFrame.getLocation().y + controlFrame.getHeight()));
//		applicationFrame.pack();
		
		viewSettings = new Settings("Show View", this);
		Map<String, Object> srcMap = new HashMap<String, Object>();
		srcMap.put(SOURCE, diagramFrame);
		srcMap.put(SOURCE_PANEL, diagram);
		viewSettings.addSetting(new Setting<Boolean>("Show Cache Diagram", Setting.CHECK_BOX, srcMap, true));
		srcMap = new HashMap<String, Object>();
		srcMap.put(SOURCE, traceFrame);
		srcMap.put(SOURCE_PANEL, trace);
		viewSettings.addSetting(new Setting<Boolean>("Show Trace", Setting.CHECK_BOX, srcMap, true));
		srcMap = new HashMap<String, Object>();
		srcMap.put(SOURCE, controlFrame);
		srcMap.put(SOURCE_PANEL, control);
		viewSettings.addSetting(new Setting<Boolean>("Show Control", Setting.CHECK_BOX, srcMap, true));
		srcMap = new HashMap<String, Object>();
		srcMap.put(SOURCE, statFrame);
		srcMap.put(SOURCE_PANEL, stat);
		viewSettings.addSetting(new Setting<Boolean>("Show Graph", Setting.CHECK_BOX, srcMap, true));
		
		Map<String, Object> valMap = new HashMap<String, Object>();
		valMap.put(Setting.VALIDATOR, new Setting.FieldValidator() {
			public boolean validate(String input) {
				try {
					int i = Integer.parseInt(input);
					if (i < 1)
						return false;
					if (Cache.checkPower2(i) < 0)
						return false;
				} catch (Exception e) {	
					return false;
				}
				return true;
			}
		});		
		cacheSettings = new Settings("Cache Settings", this, "OK");
		cacheSettings.addSetting(new Setting<Integer>(LINE, Setting.INT_FIELD, valMap, c.getLineCount()));
		cacheSettings.addSetting(new Setting<Integer>(WORD, Setting.INT_FIELD, valMap, c.getWordCount()));
		cacheSettings.addSetting(new Setting<Integer>(ASSOC, Setting.INT_FIELD, valMap, c.getAssociativeSetCount()));
		String[] writePolicies = {WRITE_THROUGH, WRITE_BACK};
		Map<String, String[]> comboBoxItems = new HashMap<String, String[]>();
		comboBoxItems.put(Setting.COMBO_BOX_ITEMS, writePolicies);
		cacheSettings.addSetting(new Setting<String>(WRITE_POLICY, Setting.COMBO_BOX, comboBoxItems, c.getWritePolicy() instanceof WriteThrough ? WRITE_THROUGH : WRITE_BACK));
		
		String[] replPolicies = {LRU, ROUND_ROBIN, RANDOM};
		comboBoxItems = new HashMap<String, String[]>();
		comboBoxItems.put(Setting.COMBO_BOX_ITEMS, replPolicies);
		String defaultPolicy = LRU;
		if (c.getReplacementPolicy() instanceof RoundRobinReplacementPolicy)
			defaultPolicy = ROUND_ROBIN;
		else if (c.getReplacementPolicy() instanceof RandomReplacementPolicy)
			defaultPolicy = RANDOM;
		cacheSettings.addSetting(new Setting<String>(REPL_POLICY, Setting.COMBO_BOX, comboBoxItems, defaultPolicy));
		
		settingsList = new ArrayList<Settings>();
		settingsList.add(viewSettings);
		settingsList.add(cacheSettings);
		
		constructSettingsFrame();
		
		showViewMenuItem.addActionListener(getNewSettingsActionListener(viewSettings));
		cacheSettingsMenuItem.addActionListener(getNewSettingsActionListener(cacheSettings));
	}
	
	private static void copySizeAndLocation(JInternalFrame newFrame, JInternalFrame oldFrame) {
		newFrame.setSize(oldFrame.getSize());
		newFrame.setLocation(oldFrame.getLocation());
	}
	
	public Container getContentPane() {
		return dPane;
	}
	
	public JMenuBar getJMenuBar() {
		return menuBar;
	}
	
	public static VCache getInstance() {
		return vCache;
	}
	
	public List<Settings> getSettingsList() {
		
		return settingsList;
	}
	
	public int getMode() {
		return mode;
	}
	
	public void childClosed(VPanel<?> child) {
		for (Setting<?> setting : viewSettings.getSettingList()) {
			if (setting.getConstraints().get(SOURCE_PANEL).equals(child)) {
				((Setting<Boolean>) setting).setValue(false);
				break;
			}
		}
	}
	
	public void settingChanged(Setting<?> affectedSetting) {
		if (affectedSetting.getContainer().equals(viewSettings)) {
			Setting<Boolean> set = (Setting<Boolean>) affectedSetting;
			JInternalFrame frame = (JInternalFrame) set.getConstraints().get(SOURCE);
			VPanel<?> panel = (VPanel<?>) set.getConstraints().get(SOURCE_PANEL);
			try {
				if (set.getValue()) {
					frame.setClosed(false);
					frame.setVisible(true);
					panel.registerViews();
				} else {
					frame.setVisible(false);
					panel.unregisterViews();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private EntryPoint getEntryPoint() {
		if (mode == APPLICATION)
			return VCacheApplication.getInstance();
//		else if (mode == APPLET)
//			return VCacheApplet.getInstance();
		return null;
	}

	public void settingsChanged(Settings settings) {
		if (settings.equals(cacheSettings)) {
			EntryPoint e = getEntryPoint();
			int l = 8, w = 4, a = 1;
			String repPol = LRU, wtePol = WRITE_THROUGH;
			for (Setting<?> setting : cacheSettings.getSettingList()) {
				if (setting.getName().equals(LINE))
					l = (Integer) setting.getValue();
				else if (setting.getName().equals(WORD))
					w = (Integer) setting.getValue();
				else if (setting.getName().equals(ASSOC))
					a = (Integer) setting.getValue();
				else if (setting.getName().equals(WRITE_POLICY))
					wtePol = (String) setting.getValue();
				else if (setting.getName().equals(REPL_POLICY))
					repPol = (String) setting.getValue();
			}
			SetAssociativeReplacementPolicy replPolicy = null;
			if (repPol.equals(LRU))
				replPolicy = new LRUReplacementPolicy();
			else if (repPol.equals(ROUND_ROBIN))
				replPolicy = new RoundRobinReplacementPolicy(l);
			else if (repPol.equals(RANDOM))
				replPolicy = new RandomReplacementPolicy();
			e.buildGUI(new Cache(l, w, a, wtePol.equals(WRITE_THROUGH) ? new WriteThrough() : new WriteBack(l), replPolicy), this);
		}
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(clearCacheMenuItem)) {
			((Updater)updater).stopTimer();
			hideAllSettingsFrames();
			if (mode == APPLICATION)
				VCacheApplication.getInstance().buildGUI(cache.clone(), this);
//			else if (mode == APPLET)
//				VCacheApplet.getInstance().buildGUI(cache.clone(), this);
		}
	}
}

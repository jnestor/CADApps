package edu.lafayette.vcache.visual.abst;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import edu.lafayette.vcache.core.Cache;
import edu.lafayette.vcache.core.Updater;
import edu.lafayette.vcache.core.view.CacheView;
import edu.lafayette.vcache.core.view.ViewUpdater;
import edu.lafayette.vcache.visual.VCache;
import edu.lafayette.vcache.visual.settings.Setting;
import edu.lafayette.vcache.visual.settings.Settings;
import edu.lafayette.vcache.visual.settings.SettingsSupporter;

public abstract class VPanel<T extends ViewUpdater> extends JPanel implements CacheView, WindowListener, InternalFrameListener {
	protected static final int DISABLED = 0, LEFT = 1, UP = 2, RIGHT = 3, DOWN = 4;
	protected JPanel outerPanel, settingsPanel;
//	protected List<JButton> settingsButtonList;
	protected List<VPanel<?>> panelList;
	protected T updater;
	protected List<T> updaterList;
	protected Cache cache;
//	protected int settingsLocation;
//	protected Map<String, JFrame> settingsFrameMap;
//	protected Map<JButton, JFrame> settingsCloseButtonMap;
//	protected String settingsTitle;
	protected static VCache vCache;
	protected int level;

	
	//the first updater is the primary one and will be stored as updater. if the first updater appears again, it will be ignored. 
	public VPanel(Cache c, int level, T... updaters) {
		cache = c;
		updaterList = new ArrayList<T>(updaters != null ? updaters.length : 1);
		this.level = level;
		if (updaters != null) {
			for (T updater : updaters) {
				if (this.updater == null) {
					this.updater = updater;
					updater.addView(this, level);
				} else if (updater != this.updater) {
					updaterList.add(updater);
					updater.addView(this, level);
				}
			}
		}
		panelList = new LinkedList<VPanel<?>>();
		vCache = null;
	}
	
	public VPanel(Cache c, T... updaters) {
		this(c, Updater.STEP, updaters);
	}
	
	public void constructSettingsFrame() {
		if (!(this instanceof SettingsSupporter))
			return;
//		settingsFrameMap = new HashMap<String, JFrame>();
		SettingsSupporter supporter = (SettingsSupporter) this;
		for (Settings settings : supporter.getSettingsList()) {
			JFrame settingsFrame = new JFrame(settings.getName());
//			settingsFrameMap.put(settings.getName(), settingsFrame);
			settings.setFrame(settingsFrame);
			JPanel settingsFramePanel = new JPanel();
			settingsFramePanel.setLayout(new BoxLayout(settingsFramePanel, BoxLayout.Y_AXIS));
			for (Setting<?> setting : settings.getSettingList()) {
				settingsFramePanel.add(setting.getGraphicalComponent());
			}
			JButton settingsCloseButton = new JButton(settings.getCloseButtonText());
			settingsCloseButton.addActionListener(new SettingsActionListener(SettingsActionListener.CLOSE, settings));
			JPanel settingsCloseButtonPanel = new JPanel();
			settingsCloseButtonPanel.add(settingsCloseButton);
			settingsFramePanel.add(settingsCloseButtonPanel);
			settingsFrame.add(settingsFramePanel);
			settingsFrame.pack();
		}
	}
	
	public void hideAllSettingsFrames() {
		if (this instanceof SettingsSupporter) {
			SettingsSupporter supporter = (SettingsSupporter) this;
			for (Settings settings : supporter.getSettingsList())
				settings.getFrame().setVisible(false);
		}
		for (VPanel<?> child : panelList) {
			child.hideAllSettingsFrames();
		}
	}
	
	public JPanel getSettingsEnabledPanel() {
		if (!(this instanceof SettingsSupporter))
			return this;
		SettingsSupporter supporter = (SettingsSupporter) this;
		if (supporter.getSettingsList().get(0).getFrame() == null)
			constructSettingsFrame();
		outerPanel = new JPanel();
		settingsPanel = new JPanel();
		SpringLayout settingsLayout = new SpringLayout();
		settingsPanel.setLayout(settingsLayout);
		List<JButton> settingsButtonList = new ArrayList<JButton>();
		for (Settings settings : supporter.getSettingsList()) {
			JButton settingsButton = new JButton(settings.getName());
			settingsButtonList.add(settingsButton);
			SettingsActionListener settingsActionListener = new SettingsActionListener(settings);
		
			settingsButton.addActionListener(settingsActionListener);
			settingsPanel.add(settingsButton);
		}
		
		settingsLayout.putConstraint(SpringLayout.NORTH, settingsButtonList.get(0), 2, SpringLayout.NORTH, settingsPanel);
		settingsLayout.putConstraint(SpringLayout.SOUTH, settingsPanel, 2, SpringLayout.SOUTH, settingsButtonList.get(0));
		settingsLayout.putConstraint(SpringLayout.WEST, settingsButtonList.get(0), 10, SpringLayout.WEST, settingsPanel);
		settingsLayout.putConstraint(SpringLayout.EAST, settingsPanel, 10, SpringLayout.EAST, settingsButtonList.get(settingsButtonList.size() - 1));
		for (int i = 1; i < supporter.getSettingsList().size(); i++) {
			settingsLayout.putConstraint(SpringLayout.WEST, settingsButtonList.get(i), 5, SpringLayout.EAST, settingsButtonList.get(i - 1));
		}
		outerPanel.add(this);
		outerPanel.add(settingsPanel);
		SpringLayout outerLayout = new SpringLayout();
		outerPanel.setLayout(outerLayout);
		outerLayout.putConstraint(SpringLayout.NORTH, this, 0, SpringLayout.NORTH, outerPanel);
		outerLayout.putConstraint(SpringLayout.NORTH, settingsPanel, 0, SpringLayout.SOUTH, this);
		outerLayout.putConstraint(SpringLayout.SOUTH, outerPanel, 0, SpringLayout.SOUTH, settingsPanel);
		outerLayout.putConstraint(SpringLayout.EAST, outerPanel, 0, SpringLayout.EAST, this);
		
		return outerPanel;
	}
	
	public void add(VPanel<?> child) {
		super.add(child);
		panelList.add(child);
	}
	
	public static void setVCache(VCache vCache) {
		VPanel.vCache = vCache;
	}
	
	public void unregisterViews() {
		updater.removeView(this);
		for (VPanel<?> child : panelList)
			child.unregisterViews();
	}
	
	public void registerViews() {
		updater.addView(this, level);
		for (VPanel<?> child : panelList)
			child.registerViews();
	}
	
	public void update() {
		if (!SwingUtilities.isEventDispatchThread()) {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						updateLater();
					}
				});
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} else
			updateLater();
	}
	
	protected void updateLater() {
		
	}
	
	//WindowListener events
	public void windowClosing(WindowEvent arg0) {
		unregisterViews();
	}
	
	public void windowActivated(WindowEvent arg0) {}
	public void windowClosed(WindowEvent arg0) {}
	public void windowDeactivated(WindowEvent arg0) {}
	public void windowDeiconified(WindowEvent arg0) {}
	public void windowIconified(WindowEvent arg0) {}
	public void windowOpened(WindowEvent arg0) {}
	
	public void internalFrameClosing(InternalFrameEvent e) {
		VCache.getInstance().childClosed(this);
		unregisterViews();
	}
	
	public void internalFrameActivated(InternalFrameEvent e) {}
	public void internalFrameClosed(InternalFrameEvent e) {}
	public void internalFrameDeactivated(InternalFrameEvent e) {}
	public void internalFrameDeiconified(InternalFrameEvent e) {}
	public void internalFrameIconified(InternalFrameEvent e) {}
	public void internalFrameOpened(InternalFrameEvent e) {}

	public SettingsActionListener getNewSettingsActionListener(Settings settings) {
		return new SettingsActionListener(settings);
	}

	protected class SettingsActionListener implements ActionListener {
		private static final boolean OPEN = true, CLOSE = false;
		private boolean open;
		private Settings settings;
		
		public SettingsActionListener(Settings settings) {
			this(OPEN, settings);
		}
		
		public SettingsActionListener(boolean open, Settings settings) {
			this.open = open;
			this.settings = settings;
		}
		
		public void actionPerformed(ActionEvent arg0) {
			settings.getFrame().setVisible(open);
			if (open == CLOSE) {
				for (Setting<?> setting : settings.getSettingList())
					setting.checkValue();
				settings.getParent().settingsChanged(settings);
			}
		}
	}
}

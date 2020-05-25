package edu.lafayette.vcache.visual;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import edu.lafayette.vcache.core.Cache;
import edu.lafayette.vcache.core.CacheAccess;
import edu.lafayette.vcache.core.Updater;
import edu.lafayette.vcache.visual.abst.VPanel;
import edu.lafayette.vcache.visual.settings.Setting;
import edu.lafayette.vcache.visual.settings.Settings;
import edu.lafayette.vcache.visual.settings.SettingsSupporter;

public class VCacheStatistics extends VPanel<Updater> implements SettingsSupporter {
	private static final int GRAPH = 1, PIE_CHART = 2, MOVING_AVERAGE_RANGE = 20;
	private Data[] statisticalDataArray;
	private Data naiveData;
	private StatisticalDisplay display;
	private JComboBox dataSelector;
	private boolean vertical;
	private int refreshDist;
	private List<Settings> settingsList;
	
	public VCacheStatistics(Cache cache, Updater updater) {
		super(cache, updater);
		
		statisticalDataArray = new Data[4];
		naiveData = new Data("Naive") {
			float calculate(float datum, LinkedList<Float> data, Data naiveData) {
				return datum;
			}
		};
		statisticalDataArray[0] = naiveData;
		statisticalDataArray[1] = new Data("Cumulative Average") {
			float calculate(float datum, LinkedList<Float> data, Data naiveData) {
				if (data.size() == 0)
					return datum;
				return (datum + data.size() * data.getLast()) / (data.size() + 1);
			}
		};
		statisticalDataArray[2] = new Data("Moving Average") {
			float calculate(float datum, LinkedList<Float> data, Data naiveData) {
				LinkedList<Float> naiveDataList = naiveData.getData();
				if (data.size() <= MOVING_AVERAGE_RANGE) {
					float sum = 0;
					for (int i = 0; i < naiveDataList.size(); i++) {
						sum += naiveDataList.get(i);
					}
					return sum / naiveDataList.size();
				}
				return data.getLast() + datum / MOVING_AVERAGE_RANGE - naiveDataList.get(data.size() - MOVING_AVERAGE_RANGE) / MOVING_AVERAGE_RANGE;
			}
		};
		statisticalDataArray[3] = new Data("Weighted Moving Average") {
			float calculate(float datum, LinkedList<Float> data, Data naiveData) {
				LinkedList<Float> naiveDataList = naiveData.getData();
				ListIterator<Float> naiveIt = naiveDataList.listIterator(naiveDataList.size());
				int range = MOVING_AVERAGE_RANGE;
				if (data.size() <= MOVING_AVERAGE_RANGE) {
					range = data.size();
				}
				float sum = 0, iSum = 0;
				for (int i = range; i >= 0; i--) {
					sum += naiveIt.previous() * i;
					iSum += i;
				}
				return sum / iSum;
			}
		};
		Settings settings = new Settings("Graph Settings", this);
		Map<String, Data[]> constraints = new HashMap<String, Data[]>();
		constraints.put(Setting.COMBO_BOX_ITEMS, statisticalDataArray);
		Setting<Data> setting = new Setting<Data>("Mode", Setting.COMBO_BOX, constraints, statisticalDataArray[3]);
		settings.addSetting(setting);
		settings.addSetting(new Setting<Boolean>("Vertical Presentation", Setting.CHECK_BOX, null, false));
		settingsList = new ArrayList<Settings>();
		settingsList.add(settings);
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		display = new StatisticalDisplay(statisticalDataArray[3], GRAPH);
		display.setPreferredSize(new Dimension(700, 150));
		add(display);
		
		vertical = false;
		refreshDist = 50;
	}
	
	
	//TODO -- we need a new update scheme from scratch
	public void updateLater() {
		CacheAccess currentAccess = cache.getCurrentAccess();
		if (currentAccess != null && (updater.getAccessStatus() == CacheAccess.COMPLETE_HIT || updater.getAccessStatus() == CacheAccess.COMPLETE_MISS)) {
			for (Data data : statisticalDataArray) {
				data.addDatum(updater.getAccessStatus() == CacheAccess.COMPLETE_HIT ? 1 : 0, null);
			}
			display.update();
		}
	}
	
	private class StatisticalDisplay extends JPanel {
		private static final int LEFT_GAP = 10, RIGHT_GAP = 2, UP_GAP = 2, DOWN_GAP = 10;
		private int type, xOffset;
		private Data sourceData;
		private float prevDatum;
		
		public StatisticalDisplay(Data sourceData, int type) {
			this.sourceData = sourceData;
			this.type = type;
			setBackground(Color.WHITE);
			xOffset = 0;
		}
		
		public void update() {
			int x = sourceData.getData().size() - 1;
			if (x > xOffset)
				drawLine(x - 1, prevDatum, x, sourceData.getData().getLast(), getGraphics());
			prevDatum = sourceData.getData().getLast();
		}
		
		public void setSourceData(Data data) {
			sourceData = data;
			repaint();
		}
		
		public void paint(Graphics g) {
			super.paint(g);
			
			int r = getWidth() - RIGHT_GAP;
			int d = getHeight() - DOWN_GAP;
			g.drawLine(LEFT_GAP, UP_GAP, r, UP_GAP);
			g.drawLine(LEFT_GAP, UP_GAP, LEFT_GAP, d);
			g.drawLine(LEFT_GAP, d, r, d);
			g.drawLine(r, UP_GAP, r, d);
			
			List<Float> dataList = sourceData.getData();
			ListIterator<Float> dataIt = dataList.listIterator(xOffset);
			int x = xOffset;
			while (dataIt.hasNext()) {
				Float datum = dataIt.next();  //<-- FOUND THE ERROR: ConcurrentModificationException
				if (x > xOffset)
					drawLine(x - 1, prevDatum, x, datum, g);
				x++;
				prevDatum = datum;
			}
		}
		
		private void drawLine(int x1, float y1, int x2, float y2, Graphics g) {
			int r = getWidth() - RIGHT_GAP - 1;
			int l = LEFT_GAP + 1;
			int d = getHeight() - DOWN_GAP - 1;
			int u = UP_GAP + 1;
			int tStart, tEnd, h;
			if (vertical) {
				tStart = u;
				tEnd = d;
				h = r - l;
			} else {
				tStart = l;
				tEnd = r;
				h = d - u;
			}
			if (tStart + x2 - xOffset >= tEnd) {
				xOffset += refreshDist;
				repaint();
			} else { 
				g.setColor(Color.BLACK);
				if (vertical)
					g.drawLine(l + (int) (y1 * h), u + x1 - xOffset, l + (int) (y2 * h), u + x2 - xOffset);
				else
					g.drawLine(l + x1 - xOffset, d - (int) (y1 * h), l + x2 - xOffset, d - (int) (y2 * h));
			}
		}
	}
	
	private abstract class Data {
		private String name;
		private int preferredRepresentation; //pie chart, graph etc.
		private LinkedList<Float> data;
		private Map<Integer, String> labels;
		
		public Data(String name) {
			this.name = name;
			data = new LinkedList<Float>();
			labels = new HashMap<Integer, String>();
		}
		
		abstract float calculate(float datum, LinkedList<Float> data, Data naiveData);
		
		public LinkedList<Float> getData() {
			return data;
		}
		
		public String getName() {
			return name;
		}
		
		public void addDatum(float datum, String label) {
			int index = data.size();
			data.add(calculate(datum, data, naiveData));
			if (label != null)
				labels.put(index, label);
		}
		
		public String getLabel(int index) {
			return labels.get(index);
		}
		
		public String toString() {
			return name;
		}
	}

	public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
			display.setSourceData((Data) e.getItem());
		}
	}

	public List<Settings> getSettingsList() {
		
		return settingsList;
	}

	public void settingChanged(Setting<?> affectedSetting) {
		if (affectedSetting.getName().equals("Mode"))
			display.setSourceData((Data) affectedSetting.getValue()); 
		else if (affectedSetting.getName().equals("Vertical Presentation")) {
			vertical = (Boolean) affectedSetting.getValue();
			display.repaint();
		}
	}

	public void settingsChanged(Settings affectedSettings) {

	}
}

package edu.lafayette.vcache.visual;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.*;

import edu.lafayette.vcache.core.Cache;
import edu.lafayette.vcache.core.CacheAccess;
import edu.lafayette.vcache.core.CacheLine;
import edu.lafayette.vcache.core.Updater;
import edu.lafayette.vcache.util.Debug;
import edu.lafayette.vcache.visual.abst.Locatable;
import edu.lafayette.vcache.visual.abst.VCacheLine;
import edu.lafayette.vcache.visual.abst.VPanel;
import edu.lafayette.vcache.visual.settings.Setting;
import edu.lafayette.vcache.visual.settings.Settings;
import edu.lafayette.vcache.visual.settings.SettingsSupporter;

public class VCacheDiagram extends VPanel<Updater> implements SettingsSupporter {
	protected static final int MULTIPLEXER_WIDTH = 40, MULTIPLEXER_DIST_FROM_CACHE = 50, MULTIPLEXER_2ND_WIDTH = 30, MULTIPLEXER_HEIGHT = 20,
							COMPARATOR_RADIUS = 10, COMPARATOR_DIST_FROM_CACHE = 22, AND_DIST_FROM_CACHE = 50, AND_WIDTH = 20, AND_HEIGHT = 12,
							WORD_BUS_DIST_FROM_CACHE = 5, MULTI_CACHE_EXTRA_TAG_BUS_DISTANCE_FROM_COMP = 55, MULTI_CACHE_TAG_BUS_DIST_FROM_CACHE = 7,
							MULTI_CACHE_TAG_BUS_DIST_FROM_V = 10, PARALLEL_LINES_GAP = 5, MULTI_CACHE_MUX_DISTANCE_FROM_BLOCK_MUX = 70,
							MULTI_CACHE_OR_DISTANCE_FROM_AND = 60, MULTI_CACHE_MUX_KNEE_START = 30, MULTI_CACHE_MUX_KNEE_RANGE = 30,
							MULTI_CACHE_OR_KNEE_START = 0, MULTI_CACHE_OR_KNEE_RANGE = 30, OR_END_CIRCLE_DIAMETER = 30,
							TRUE = 1, FALSE = 2, BUS = 3, INACTIVE = 4, REPAINT = 5, NO_LINE = 6,
							ANIMATION = 0, NO_ANIMATION_WITH_COLORS = 1, NO_ANIMATION_WITHOUT_COLORS = 2
							;
	protected static final String NORMAL = "Normal", COMPACT = "Compact";
	protected static final float ANIMATION_STAGE_1 = 0, ANIMATION_STAGE_2 = 0.2f, ANIMATION_STAGE_3 = 0.7f;
	private VCurrentAccess vca;
	private ArrayList<JPanel> cacheRAMPanels; 
	private ArrayList<VCacheLine> lines;
	private Graphics drawLineGraphics;
	private SpringLayout layout;
	private JTextField dataField, statusField;
	private Line tagBus, wordBus;
	private List<Line> animatedLines, oldAnimatedLines;
	//type to control the abstraction level of the visualization (NORMAL & COMPACT)
	private String visualizationType;
	private List<Settings> settingsList;
	private JComponent frame;
	private int compactPixels;
	//animation simplification modes, to speed up...
	private int animationMode;
	
	public VCacheDiagram(Cache cache, Updater updater, JComponent frame) {
		this(cache, updater, frame, NORMAL, 1);		
	}
	
	public VCacheDiagram(Cache cache, Updater updater, JComponent frame, VCacheDiagram oldDiagram) {
		this(cache, updater, frame, oldDiagram.visualizationType, oldDiagram.compactPixels);
	}
	
	public VCacheDiagram(Cache cache, Updater updater, JComponent frame, String visualizationType, int compactPixels) {	
		super(cache, Updater.ANIMATION, updater);
		this.frame = frame;
		this.visualizationType = visualizationType;
		this.compactPixels = compactPixels;
		animationMode = ANIMATION;
		buildGUI();
	}
	
	private void buildGUI() {
//		visualizationType = NORMAL;
		boolean compact = visualizationType.equals(COMPACT);
		vca = new VCurrentAccess(cache, updater);
		add(vca);
		cacheRAMPanels = new ArrayList<JPanel>();
		CacheLine[] tempLineArr = new CacheLine[cache.getAssociativeSetCount()];
		for (int j = 0; j < cache.getAssociativeSetCount(); j++) {
			JPanel cacheRAMPanel = new JPanel();
			BoxLayout cacheRAMLayout = new BoxLayout(cacheRAMPanel, BoxLayout.Y_AXIS);
			cacheRAMPanel.setLayout(cacheRAMLayout);
			if (compact) {
				VCompactCacheLine dummyLine = new VCompactCacheLine(cache, compactPixels, cache.getLine(0, j));
				cacheRAMPanel.add(new VCompactCacheLineLabels(dummyLine));
				cacheRAMPanel.add(dummyLine);
			} else
				cacheRAMPanel.add(new VCacheLineLabels(cache));
			lines = new ArrayList<VCacheLine>(cache.getLineCount());
			for (int i = 0; i < cache.getLineCount(); i++) {
			    CacheLine line = cache.getLine(i, j);
			    VCacheLine vLine;
			    tempLineArr[0] = line;
			    for (int k = 0, l = 1; l < cache.getAssociativeSetCount(); k++, l++)
			    	tempLineArr[l] = cache.getLine(k == i ? ++k : k, j);
			    if (compact)
			    	vLine = new VCompactCacheLine(cache, updater, i, compactPixels, tempLineArr);
			    else
			    	vLine = new VFieldedCacheLine(cache, updater, i, tempLineArr);
			    lines.add(vLine);
			    cacheRAMPanel.add(vLine);
			    vLine.update();
			}
			if (compact) 
				cacheRAMPanel.add(new VCompactCacheLine(cache, compactPixels, cache.getLine(0, j)));
			cacheRAMPanel.setMaximumSize(cacheRAMPanel.getPreferredSize());
			add(cacheRAMPanel);
			cacheRAMPanels.add(cacheRAMPanel);
		}
		
		dataField = new JTextField(11);
		dataField.setFocusable(false);
		add(dataField);
		
		statusField = new JTextField();
		statusField.setColumns(9);
		statusField.setFocusable(false);
		add(statusField);
				
		//SpringLayout 
		layout = new SpringLayout();

		setLayout(layout);
		layout.putConstraint(SpringLayout.NORTH, vca, 0, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.WEST, vca, 0, SpringLayout.WEST, this);
		for (int i = 0; i < cacheRAMPanels.size(); i++) {
			JPanel cacheRAMPanel = cacheRAMPanels.get(i);
			layout.putConstraint(SpringLayout.NORTH, cacheRAMPanel, 10, SpringLayout.SOUTH, vca);
			if (i == 0)
				layout.putConstraint(SpringLayout.WEST, cacheRAMPanel, -10, SpringLayout.EAST, vca);
			else
				layout.putConstraint(SpringLayout.WEST, cacheRAMPanel, 20, SpringLayout.EAST, cacheRAMPanels.get(i - 1));
			
			
		}
		JPanel firstCacheRAMPanel = cacheRAMPanels.get(0), lastCacheRAMPanel = cacheRAMPanels.get(cacheRAMPanels.size() - 1);
		layout.putConstraint(SpringLayout.EAST, this, 20, SpringLayout.EAST, lastCacheRAMPanel);
		int bottomHeight = cacheRAMPanels.size() > 1 ? 200 : 120;
		layout.putConstraint(SpringLayout.SOUTH, this, bottomHeight, SpringLayout.SOUTH, lastCacheRAMPanel);
		layout.putConstraint(SpringLayout.SOUTH, statusField, -3, SpringLayout.SOUTH, this);
		layout.putConstraint(SpringLayout.WEST, statusField, 5, SpringLayout.WEST, this);
		
		layout.putConstraint(SpringLayout.SOUTH, dataField, -3, SpringLayout.SOUTH, this);
		layout.putConstraint(SpringLayout.EAST, dataField, 0, SpringLayout.EAST, lastCacheRAMPanel);
		vca.setLocation(new Point(10, 10));
	}
	
	//TODO - clean this method up
	public void updateLater() {
		if (Debug.debug)
			System.out.println("VCacheDigram update");
		Graphics g = getGraphics();
		CacheAccess currentAccess = cache.getCurrentAccess();
		if (currentAccess != null && updater.getStatus() == 0) {

			oldAnimatedLines = animatedLines;
			
			//redraw everything at the start of the new access
			if (animationMode == ANIMATION) {
				animatedLines = new LinkedList<Line>();
				drawComponents(g, true);
				
			}
			int status = updater.getAccessStatus();
			if (status == CacheAccess.INITIAL) {
				statusField.setText("LOOKUP");
			} else if (status == CacheAccess.LOOKUP_HIT || status == CacheAccess.LOOKUP_MISS) {
				statusField.setText("LOOKUP");
			} else if (status == Updater.HIGHER_LEVEL_READ) {
				statusField.setText("MISS (fetching)");
			} else if (status == Updater.HIGHER_LEVEL_WRITE) {
				statusField.setText("MISS (writing)");
			} else if (status == CacheAccess.COMPLETE_HIT) {
				statusField.setText("HIT");
			} else if (status == CacheAccess.COMPLETE_MISS) {
				statusField.setText("MISS (complete)");
			} else if (status == CacheAccess.NONE) {
				statusField.setText("");
			} 

		}
		else {
			if (animationMode == ANIMATION)
				drawLines(g);
		}
		
//		if (currentAccess != null && currentAccess.getStatus() == CacheAccess.LOOKUP_MISS && updater.getStatus() >= 0.25f)
//			statusField.setText("MISS (fetching)");
		if (currentAccess != null && (updater.getAccessStatus() == CacheAccess.COMPLETE_HIT || 
				updater.getAccessStatus() == CacheAccess.COMPLETE_MISS || updater.getAccessStatus() == Updater.HIGHER_LEVEL_READ || updater.getAccessStatus() == Updater.HIGHER_LEVEL_WRITE
//				(updater.getAccessStatus() == CacheAccess.LOOKUP_MISS && updater.getStatus() >= 0.25f)
				)) {
			dataField.setText(new String(lines.get(currentAccess.getLineIndex()).getDataFieldContent(currentAccess.getWordIndex())));
			
		}
		else
			dataField.setText("");

			
	}
	
	public void animateAccess() {
		vca.animateAccess();
	}
	
	private void drawComponents(Graphics g, boolean repaintLines) {
		g.setColor(Color.BLACK);
		drawLineBus(g);
		for (int i = 0; i < cache.getAssociativeSetCount(); i++) {
			drawTagBus(g, i);
			drawWordBus(g, i);
			drawMultiplexer(g, i);
			drawHitLogic(g, i);
			if (cache.getAssociativeSetCount() > 1)
				drawMultiCacheLogic(g);
		}
		if (repaintLines)
			repaintLines(g);
		drawLines(g);
	}
	
	public void paint(Graphics g) {
		super.paint(g);
		animatedLines = new LinkedList<Line>();
		drawComponents(g, false);
	}
	
	private void drawLineBus(Graphics g) {
		if(cache.getCurrentAccess() == null)
			return;
		int x, y;
		VCacheLine line = lines.get(cache.getCurrentAccess().getLineIndex());
		Line l = new Line(CacheAccess.INITIAL, 0, 1, INACTIVE, BUS);
		l.addPoint(x = getHorizontalCenter(vca, VCurrentAccess.LINE_INDEX_PANEL), 
				vca.getLocation().y + vca.getLocation(VCurrentAccess.LINE_INDEX_PANEL).y + vca.getHeight(VCurrentAccess.LINE_INDEX_PANEL));
		l.addPoint(x, y = cacheRAMPanels.get(0).getLocation().y + line.getLocation().y + line.getHeight() / 2);
		l.addPoint(cacheRAMPanels.get(0).getLocation().x - 1, y);
		animatedLines.add(l);
//		animatedLines.add(new Line(CacheAccess.LOOKUP_MISS, l.getAnimationStart(), l.getAnimationEnd(), INACTIVE, BUS, l.getPoints()));
		animatedLines.add(new Line(CacheAccess.LOOKUP_HIT, BUS, l.getPoints()));
		animatedLines.add(new Line(CacheAccess.LOOKUP_MISS, BUS, l.getPoints()));
		animatedLines.add(new Line(CacheAccess.COMPLETE_HIT, BUS, l.getPoints()));
		animatedLines.add(new Line(CacheAccess.COMPLETE_MISS, BUS, l.getPoints()));
		animatedLines.add(new Line(Updater.HIGHER_LEVEL_READ, BUS, l.getPoints()));
		animatedLines.add(new Line(Updater.HIGHER_LEVEL_WRITE, BUS, l.getPoints()));
		animatedLines.add(new Line(CacheAccess.NONE, INACTIVE, l.getPoints()));
//		animatedLines.add(new Line(CacheAccess.INITIAL, INACTIVE, l.getPoints()));
	}
	
	private void drawTagBus(Graphics g, int assInd) {
		int x, y;
		tagBus = new Line(CacheAccess.INITIAL, ANIMATION_STAGE_1, ANIMATION_STAGE_2, INACTIVE, BUS);
		tagBus.addPoint(x = getHorizontalCenter(vca, VCurrentAccess.TAG_PANEL), 
				vca.getLocation().y + vca.getLocation(VCurrentAccess.TAG_PANEL).y + vca.getHeight(VCurrentAccess.TAG_PANEL));
		tagBus.addPoint(x, y = cacheRAMPanels.get(assInd).getLocation().y + cacheRAMPanels.get(assInd).getHeight() + 
				(cache.getAssociativeSetCount() > 1 ? MULTI_CACHE_TAG_BUS_DIST_FROM_CACHE : COMPARATOR_DIST_FROM_CACHE));
		animatedLines.add(tagBus);
//		animatedLines.add(new Line(CacheAccess.LOOKUP_MISS, tagBus.getAnimationStart(), tagBus.getAnimationEnd(), INACTIVE, BUS, tagBus.getPoints()));
		animatedLines.add(new Line(CacheAccess.LOOKUP_HIT, BUS, tagBus.getPoints()));
		animatedLines.add(new Line(CacheAccess.LOOKUP_MISS, BUS, tagBus.getPoints()));
		animatedLines.add(new Line(CacheAccess.COMPLETE_HIT, BUS, tagBus.getPoints()));
		animatedLines.add(new Line(CacheAccess.COMPLETE_MISS, BUS, tagBus.getPoints()));
		animatedLines.add(new Line(Updater.HIGHER_LEVEL_READ, BUS, tagBus.getPoints()));
		animatedLines.add(new Line(Updater.HIGHER_LEVEL_WRITE, BUS, tagBus.getPoints()));
		animatedLines.add(new Line(CacheAccess.NONE, INACTIVE, tagBus.getPoints()));
//		animatedLines.add(new Line(CacheAccess.INITIAL, INACTIVE, tagBus.getPoints()));
	}
	
	private void drawWordBus(Graphics g, int assInd) {
		if(cache.getWordCount() == 1)
			return;
		int x, y;
		wordBus = new Line(CacheAccess.INITIAL, ANIMATION_STAGE_1, ANIMATION_STAGE_2, INACTIVE, BUS);
		wordBus.addPoint(x = getHorizontalCenter(vca, VCurrentAccess.WORD_INDEX_PANEL), 
				y = vca.getLocation().y + vca.getLocation(VCurrentAccess.WORD_INDEX_PANEL).y + vca.getHeight(VCurrentAccess.WORD_INDEX_PANEL));
		wordBus.addPoint(x, y = cacheRAMPanels.get(assInd).getLocation().y - WORD_BUS_DIST_FROM_CACHE);
		wordBus.addPoint(x = cacheRAMPanels.get(assInd).getLocation().x + cacheRAMPanels.get(assInd).getWidth() + WORD_BUS_DIST_FROM_CACHE, y);
		wordBus.addPoint(x, y = cacheRAMPanels.get(assInd).getLocation().y + cacheRAMPanels.get(assInd).getHeight() + MULTIPLEXER_DIST_FROM_CACHE + MULTIPLEXER_HEIGHT / 2);
		animatedLines.add(wordBus);
//		animatedLines.add(new Line(CacheAccess.LOOKUP_MISS, wordBus.getAnimationStart(), wordBus.getAnimationEnd(), INACTIVE, BUS, wordBus.getPoints()));
		animatedLines.add(new Line(CacheAccess.LOOKUP_HIT, BUS, wordBus.getPoints()));
		animatedLines.add(new Line(CacheAccess.LOOKUP_MISS, BUS, wordBus.getPoints()));
		animatedLines.add(new Line(CacheAccess.COMPLETE_HIT, BUS, wordBus.getPoints()));
		animatedLines.add(new Line(CacheAccess.COMPLETE_MISS, BUS, wordBus.getPoints()));
		animatedLines.add(new Line(Updater.HIGHER_LEVEL_READ, BUS, wordBus.getPoints()));
		animatedLines.add(new Line(Updater.HIGHER_LEVEL_WRITE, BUS, wordBus.getPoints()));
		animatedLines.add(new Line(CacheAccess.NONE, INACTIVE, wordBus.getPoints()));
//		animatedLines.add(new Line(CacheAccess.INITIAL, INACTIVE, wordBus.getPoints()));
	}
	
	private void drawMultiplexer(Graphics g, int assInd) {
		int midX = getHorizontalCenter(lines.get(0), VCacheLine.DATA_PANEL) + cacheRAMPanels.get(assInd).getLocation().x;
		int x1, y1, x2, y2;
		int cacheRAMPanelBottom = cacheRAMPanels.get(assInd).getLocation().y + cacheRAMPanels.get(assInd).getHeight();
		
		if (cache.getWordCount() == 1) {
			if (cache.getAssociativeSetCount() != 1)
				return;
			Line l = new Line(CacheAccess.LOOKUP_HIT, 0, 1, INACTIVE, BUS);
			l.addPoint(x1 = midX, y1 = cacheRAMPanelBottom);
			l.addPoint(x1, y1 = (y1 + dataField.getLocation().y) / 2);
			l.addPoint(x1 = dataField.getLocation().x + dataField.getWidth() / 2, y1);
			l.addPoint(x1, dataField.getLocation().y);
			animatedLines.add(new Line(CacheAccess.LOOKUP_MISS, 0, 1, INACTIVE, BUS, l.getPoints()));
			animatedLines.add(new Line(CacheAccess.COMPLETE_HIT, BUS, l.getPoints()));
			animatedLines.add(new Line(CacheAccess.COMPLETE_MISS, BUS, l.getPoints()));
			animatedLines.add(new Line(CacheAccess.NONE, INACTIVE, l.getPoints()));
			animatedLines.add(new Line(CacheAccess.INITIAL, INACTIVE, l.getPoints()));
			return;
		}
		//W to MUX -- wordBus
		wordBus.addPoint(midX + (MULTIPLEXER_WIDTH + MULTIPLEXER_2ND_WIDTH) / 4, 
				cacheRAMPanels.get(assInd).getLocation().y + cacheRAMPanels.get(assInd).getHeight() + MULTIPLEXER_DIST_FROM_CACHE + MULTIPLEXER_HEIGHT / 2);
		
		//MUX
		drawLine(x1 = midX - MULTIPLEXER_WIDTH / 2, y1 = cacheRAMPanels.get(assInd).getLocation().y + cacheRAMPanels.get(assInd).getHeight() + MULTIPLEXER_DIST_FROM_CACHE,
					x1 + MULTIPLEXER_WIDTH, y1, g);
		drawLine(x1, y1, x1 = midX - MULTIPLEXER_2ND_WIDTH / 2, y2 = y1 + MULTIPLEXER_HEIGHT);
		drawLine(midX + MULTIPLEXER_WIDTH / 2, y1, x2 = x1 + MULTIPLEXER_2ND_WIDTH, y2);
		drawLine(x1, y2, x2, y2);
		
		
		//Data Outputs from Cache
		for (int i = 0; i < cache.getWordCount() / 2; i++) {
			Line l1 = new Line(CacheAccess.LOOKUP_HIT, 0, 0.5f, INACTIVE, BUS);
			Line l2 = new Line(CacheAccess.LOOKUP_HIT, l1.getAnimationStart(), l1.getAnimationEnd(), INACTIVE, BUS);
			l1.addPoint(x1 = lines.get(0).getLocation(VCacheLine.DATA_PANEL).x + getHorizontalCenter(lines.get(0), VCacheLine.DATA_FIELD + i) + cacheRAMPanels.get(assInd).getLocation().x, cacheRAMPanelBottom);
			l1.addPoint(x1, y1 = cacheRAMPanelBottom + MULTIPLEXER_DIST_FROM_CACHE - (MULTIPLEXER_DIST_FROM_CACHE - 
					(cache.getAssociativeSetCount() > 1 ? MULTI_CACHE_TAG_BUS_DIST_FROM_CACHE : 0)) * (i + 1) / (cache.getWordCount() / 2 + 1));
			l2.addPoint(x2 = lines.get(0).getLocation(VCacheLine.DATA_PANEL).x + getHorizontalCenter(lines.get(0), VCacheLine.DATA_FIELD + cache.getWordCount() - 1 - i) + cacheRAMPanels.get(assInd).getLocation().x, cacheRAMPanelBottom);
			l2.addPoint(x2, y1);
			l1.addPoint(x1 = midX - MULTIPLEXER_WIDTH / 2 + MULTIPLEXER_WIDTH * (i + 1) / (cache.getWordCount() + 1), y1);
			l2.addPoint(x2 = 2 * midX - x1, y1);
			l1.addPoint(x1, y2 = cacheRAMPanelBottom + MULTIPLEXER_DIST_FROM_CACHE);
			l2.addPoint(x2, y2);
			animatedLines.add(l1);
			animatedLines.add(l2);
			animatedLines.add(new Line(CacheAccess.LOOKUP_MISS, l1.animationStart, l1.animationEnd, INACTIVE, BUS, l1.getPoints()));
			animatedLines.add(new Line(CacheAccess.LOOKUP_MISS, l2.animationStart, l2.animationEnd, INACTIVE, BUS, l2.getPoints()));
			animatedLines.add(new Line(CacheAccess.COMPLETE_HIT, BUS, l1.getPoints()));
			animatedLines.add(new Line(CacheAccess.COMPLETE_MISS, BUS, l1.getPoints()));
			animatedLines.add(new Line(Updater.HIGHER_LEVEL_READ, BUS, l1.getPoints()));
			animatedLines.add(new Line(Updater.HIGHER_LEVEL_WRITE, BUS, l1.getPoints()));
			animatedLines.add(new Line(CacheAccess.NONE, INACTIVE, l1.getPoints()));
			animatedLines.add(new Line(CacheAccess.INITIAL, INACTIVE, l1.getPoints()));
			animatedLines.add(new Line(CacheAccess.COMPLETE_HIT, BUS, l2.getPoints()));
			animatedLines.add(new Line(CacheAccess.COMPLETE_MISS, BUS, l2.getPoints()));
			animatedLines.add(new Line(Updater.HIGHER_LEVEL_READ, BUS, l2.getPoints()));
			animatedLines.add(new Line(Updater.HIGHER_LEVEL_WRITE, BUS, l2.getPoints()));
			animatedLines.add(new Line(CacheAccess.NONE, INACTIVE, l2.getPoints()));
			animatedLines.add(new Line(CacheAccess.INITIAL, INACTIVE, l2.getPoints()));
		}
		
		//Connection inside the MUX
		if (cache.getCurrentAccess() != null) {
			Line l = new Line(CacheAccess.LOOKUP_HIT, 0.5f, 0.5f, NO_LINE, BUS);
			int wordIndex = cache.getCurrentAccess().getWordIndex();
			l.addPoint(x1 = midX - MULTIPLEXER_WIDTH / 2 + MULTIPLEXER_WIDTH * (wordIndex + 1) / (cache.getWordCount() + 1), y1 = cacheRAMPanelBottom + MULTIPLEXER_DIST_FROM_CACHE);
			l.addPoint(x1, y1 += MULTIPLEXER_HEIGHT / 2);
			l.addPoint(midX, y1);
			l.addPoint(midX, cacheRAMPanelBottom + MULTIPLEXER_DIST_FROM_CACHE + MULTIPLEXER_HEIGHT);
			animatedLines.add(l);
			animatedLines.add(new Line(CacheAccess.LOOKUP_MISS, l.animationStart, l.animationEnd, NO_LINE, BUS, l.getPoints()));
			animatedLines.add(new Line(CacheAccess.COMPLETE_HIT, BUS, l.getPoints()));
			animatedLines.add(new Line(CacheAccess.COMPLETE_MISS, BUS, l.getPoints()));
			animatedLines.add(new Line(Updater.HIGHER_LEVEL_READ, BUS, l.getPoints()));
			animatedLines.add(new Line(Updater.HIGHER_LEVEL_WRITE, BUS, l.getPoints()));
		}
		
		
		//MUX to dataField
		if (cache.getAssociativeSetCount() == 1) {
			Line l = new Line(CacheAccess.LOOKUP_HIT, 0.5f, 1, INACTIVE, BUS);
			l.addPoint(midX, y1 = cacheRAMPanelBottom + MULTIPLEXER_DIST_FROM_CACHE + MULTIPLEXER_HEIGHT);
			l.addPoint(midX, y1 = (y1 + dataField.getLocation().y) / 2);
			l.addPoint(x1 = dataField.getLocation().x + dataField.getWidth() / 2, y1);
			l.addPoint(x1, dataField.getLocation().y);
			animatedLines.add(l);
			animatedLines.add(new Line(CacheAccess.LOOKUP_MISS, l.animationStart, l.animationEnd, INACTIVE, BUS, l.getPoints()));
			animatedLines.add(new Line(CacheAccess.COMPLETE_HIT, BUS, l.getPoints()));
			animatedLines.add(new Line(CacheAccess.COMPLETE_MISS, BUS, l.getPoints()));
			animatedLines.add(new Line(Updater.HIGHER_LEVEL_READ, BUS, l.getPoints()));
			animatedLines.add(new Line(Updater.HIGHER_LEVEL_WRITE, BUS, l.getPoints()));
			animatedLines.add(new Line(CacheAccess.NONE, INACTIVE, l.getPoints()));
			animatedLines.add(new Line(CacheAccess.INITIAL, INACTIVE, l.getPoints()));
		}
	}
	
	private void drawHitLogic(Graphics g, int assInd) {
		int tagX = getHorizontalCenter(lines.get(0), VCacheLine.TAG_FIELD) + cacheRAMPanels.get(assInd).getLocation().x;
		int validX = getHorizontalCenter(lines.get(0), VCacheLine.VALID_FIELD) + cacheRAMPanels.get(assInd).getLocation().x;
		int cacheRAMPanelBottom = cacheRAMPanels.get(assInd).getLocation().y + cacheRAMPanels.get(assInd).getHeight();
		CacheLine line = null;
		if (cache.getCurrentAccess() != null)
			line = cache.getLine(cache.getCurrentAccess().getLineIndex(), assInd);
		
		int x1, x2, y1, y2;
		
		//Edge of CacheRAMPanel to COMPARATOR -- tagBus
		if (cache.getAssociativeSetCount() == 1)
			tagBus.addPoint(tagX - COMPARATOR_RADIUS, cacheRAMPanelBottom + COMPARATOR_DIST_FROM_CACHE);
		else {
			tagBus.addPoint(x1 = validX - MULTI_CACHE_TAG_BUS_DIST_FROM_V, cacheRAMPanelBottom + MULTI_CACHE_TAG_BUS_DIST_FROM_CACHE);
			tagBus.addPoint(x1, y1 = cacheRAMPanelBottom + COMPARATOR_DIST_FROM_CACHE);
			tagBus.addPoint(tagX - COMPARATOR_RADIUS, y1);
		}
		
		//COMPARATOR
		g.drawOval(tagX - COMPARATOR_RADIUS, cacheRAMPanelBottom + COMPARATOR_DIST_FROM_CACHE - COMPARATOR_RADIUS, COMPARATOR_RADIUS * 2, COMPARATOR_RADIUS * 2);
		drawLine(x1 = tagX - 1, y1 = cacheRAMPanelBottom + COMPARATOR_DIST_FROM_CACHE - 2, x1 - 6, y1, g, Color.BLACK, 1);
		drawLine(x1 += 2, y1, x1 + 6, y1);
		drawLine(x1 -= 2, y1 += 4, x1 - 6, y1);
		drawLine(x1 += 2, y1, x1 + 6, y1);
		
		//Tag Output
		Line l = new Line(CacheAccess.LOOKUP_HIT, 0, 0.25f, INACTIVE, BUS);
		l.addPoint(tagX, cacheRAMPanelBottom);
		l.addPoint(tagX, cacheRAMPanelBottom + COMPARATOR_DIST_FROM_CACHE - COMPARATOR_RADIUS);
		animatedLines.add(l);
		animatedLines.add(new Line(CacheAccess.LOOKUP_MISS, l.animationStart, l.animationEnd, INACTIVE, BUS, l.getPoints()));
		animatedLines.add(new Line(CacheAccess.COMPLETE_HIT, BUS, l.getPoints()));
		animatedLines.add(new Line(CacheAccess.COMPLETE_MISS, BUS, l.getPoints()));
		animatedLines.add(new Line(Updater.HIGHER_LEVEL_READ, BUS, l.getPoints()));
		animatedLines.add(new Line(Updater.HIGHER_LEVEL_WRITE, BUS, l.getPoints()));
		animatedLines.add(new Line(CacheAccess.NONE, INACTIVE, l.getPoints()));
		animatedLines.add(new Line(CacheAccess.INITIAL, INACTIVE, l.getPoints()));
		
		//V output
		if (cache.getCurrentAccess() != null)
			l = new Line(CacheAccess.LOOKUP_HIT, 0, 0.5f, INACTIVE, line.getValid() ? TRUE : FALSE);
		else
			l = new Line(CacheAccess.LOOKUP_HIT, 0, 0.2f, INACTIVE, INACTIVE);
		l.addPoint(validX, cacheRAMPanelBottom);
		l.addPoint(validX, cacheRAMPanelBottom + AND_DIST_FROM_CACHE);
		animatedLines.add(l);
		animatedLines.add(new Line(CacheAccess.LOOKUP_MISS, l.animationStart, l.animationEnd, l.getStartType(), l.getType(), l.getPoints()));
		animatedLines.add(new Line(CacheAccess.COMPLETE_HIT, l.getType(), l.getPoints()));
		animatedLines.add(new Line(CacheAccess.COMPLETE_MISS, l.getType(), l.getPoints()));
		animatedLines.add(new Line(Updater.HIGHER_LEVEL_READ, l.getType(), l.getPoints()));
		animatedLines.add(new Line(Updater.HIGHER_LEVEL_WRITE, l.getType(), l.getPoints()));
		animatedLines.add(new Line(CacheAccess.NONE, INACTIVE, l.getPoints()));
		animatedLines.add(new Line(CacheAccess.INITIAL, INACTIVE, l.getPoints()));
		
		//AND GATE
		drawLine(x1 = validX - AND_WIDTH / 3, y1 = cacheRAMPanelBottom + AND_DIST_FROM_CACHE, x2 = x1 + AND_WIDTH, y1);
		drawLine(x1, y1, x1, y1 + AND_HEIGHT);
		drawLine(x2, y1, x2, y1 + AND_HEIGHT);
		g.drawArc(x1, y1 + AND_HEIGHT - AND_WIDTH / 2, AND_WIDTH, AND_WIDTH, 0, -180);
		
		//Connection from COMPARATOR to AND GATE
		if (cache.getCurrentAccess() != null)
			l = new Line(CacheAccess.LOOKUP_HIT, 0.25f, 0.5f, INACTIVE, cache.getCurrentAccess().getTag() == line.getTag() ? TRUE : FALSE);
		else
			l = new Line(CacheAccess.LOOKUP_HIT, 0.2f, 0.7f, INACTIVE, INACTIVE);
		l.addPoint(tagX, y1 = cacheRAMPanelBottom + COMPARATOR_DIST_FROM_CACHE + COMPARATOR_RADIUS);
		l.addPoint(tagX, y1 = (y1 + cacheRAMPanelBottom + AND_DIST_FROM_CACHE) / 2);
		l.addPoint(x1 = validX + AND_WIDTH / 3, y1);
		l.addPoint(x1, cacheRAMPanelBottom + AND_DIST_FROM_CACHE);
		animatedLines.add(l);
		animatedLines.add(new Line(CacheAccess.LOOKUP_MISS, l.animationStart, l.animationEnd, l.getStartType(), l.getType(), l.getPoints()));
		animatedLines.add(new Line(CacheAccess.COMPLETE_HIT, l.getType(), l.getPoints()));
		animatedLines.add(new Line(CacheAccess.COMPLETE_MISS, l.getType(), l.getPoints()));
		animatedLines.add(new Line(Updater.HIGHER_LEVEL_READ, l.getType(), l.getPoints()));
		animatedLines.add(new Line(Updater.HIGHER_LEVEL_WRITE, l.getType(), l.getPoints()));
		animatedLines.add(new Line(CacheAccess.NONE, INACTIVE, l.getPoints()));
		animatedLines.add(new Line(CacheAccess.INITIAL, INACTIVE, l.getPoints()));
		
		//Connection from AND GATE to statusField
		if (cache.getAssociativeSetCount() == 1) {
			if (cache.getCurrentAccess() != null)
				l = new Line(CacheAccess.LOOKUP_HIT, 0.5f, 1, INACTIVE, (cache.getCurrentAccess().getTag() == line.getTag()) && line.getValid() ? TRUE : FALSE);
			else
				l = new Line(CacheAccess.LOOKUP_HIT, ANIMATION_STAGE_3, 1.0f, INACTIVE, INACTIVE);
			l.addPoint(x1 = validX + AND_WIDTH / 6, y1 = cacheRAMPanelBottom + AND_DIST_FROM_CACHE + AND_HEIGHT + AND_WIDTH / 2);
			l.addPoint(x1, y1 = (y1 + statusField.getLocation().y) / 2);
			l.addPoint(x1 = statusField.getLocation().x + statusField.getWidth() / 2, y1);
			l.addPoint(x1, statusField.getLocation().y);
			animatedLines.add(l);
			animatedLines.add(new Line(CacheAccess.LOOKUP_MISS, l.animationStart, l.animationEnd, l.getStartType(), l.getType(), l.getPoints()));
			animatedLines.add(new Line(CacheAccess.COMPLETE_HIT, l.getType(), l.getPoints()));
			animatedLines.add(new Line(CacheAccess.COMPLETE_MISS, l.getType(), l.getPoints()));
			animatedLines.add(new Line(Updater.HIGHER_LEVEL_READ, l.getType(), l.getPoints()));
			animatedLines.add(new Line(Updater.HIGHER_LEVEL_WRITE, l.getType(), l.getPoints()));
			animatedLines.add(new Line(CacheAccess.NONE, INACTIVE, l.getPoints()));
			animatedLines.add(new Line(CacheAccess.INITIAL, INACTIVE, l.getPoints()));
		}
	}
	
	/**
	 * Draws the set associative MUX and OR gates. Precondition: cache must be set assoc.
	 * @param g Graphics obj
	 */
	private void drawMultiCacheLogic(Graphics g) {
		int associativeSets = cache.getAssociativeSetCount();
		int cacheRAMPanelBottom = cacheRAMPanels.get(0).getLocation().y + cacheRAMPanels.get(0).getHeight();
		int andBottom = cacheRAMPanelBottom + AND_DIST_FROM_CACHE + AND_HEIGHT + AND_WIDTH / 2;
		int muxBottom = cache.getWordCount() == 1 ? andBottom : cacheRAMPanelBottom + MULTIPLEXER_DIST_FROM_CACHE + MULTIPLEXER_HEIGHT;
		int lineStartY = cache.getWordCount() == 1 ? cacheRAMPanelBottom : muxBottom;
		int muxMid = dataField.getLocation().x + dataField.getWidth() / 2;
		ArrayList<CacheLine> lineArray = new ArrayList<CacheLine>(cache.getAssociativeSetCount());
		if (cache.getCurrentAccess() != null)
			for (int i = 0; i < cache.getAssociativeSetCount(); i++)
				lineArray.add(cache.getLine(cache.getCurrentAccess().getLineIndex(), i));
		int x1, y1, x2, y2;
		for (int i = 0; i < associativeSets / 2; i++) {
			int midX1 = getHorizontalCenter(lines.get(0), VCacheLine.DATA_PANEL) + cacheRAMPanels.get(i).getLocation().x;
			int midX2 = getHorizontalCenter(lines.get(0), VCacheLine.DATA_PANEL) + cacheRAMPanels.get(associativeSets - i - 1).getLocation().x;
			
			Line l1 = new Line(CacheAccess.LOOKUP_HIT, 0.5f, 0.75f, INACTIVE, BUS);
			Line l2 = new Line(CacheAccess.LOOKUP_HIT, l1.getAnimationStart(), l1.getAnimationEnd(), INACTIVE, BUS);
			l1.addPoint(midX1, lineStartY); //cache output
			l2.addPoint(midX2, lineStartY);
			l1.addPoint(midX1, y1 = muxBottom + MULTI_CACHE_MUX_KNEE_START + MULTI_CACHE_MUX_KNEE_RANGE - MULTI_CACHE_MUX_KNEE_RANGE * (i + 1) / (associativeSets + 1));
			l2.addPoint(midX2, y2 = muxBottom + MULTI_CACHE_MUX_KNEE_START + MULTI_CACHE_MUX_KNEE_RANGE - MULTI_CACHE_MUX_KNEE_RANGE * (associativeSets - i) / (associativeSets + 1));
			l1.addPoint(x1 = muxMid - MULTIPLEXER_WIDTH / 2 + MULTIPLEXER_WIDTH * (i + 1) / (associativeSets + 1), y1);
			l2.addPoint(x2 = 2 * muxMid - x1, y2);
			l1.addPoint(x1, y2 = muxBottom + MULTI_CACHE_MUX_DISTANCE_FROM_BLOCK_MUX);
			l2.addPoint(x2, y2);
			animatedLines.add(l1);
			animatedLines.add(l1);
			animatedLines.add(new Line(CacheAccess.LOOKUP_MISS, l1.animationStart, l1.animationEnd, INACTIVE, BUS, l1.getPoints()));
			animatedLines.add(new Line(CacheAccess.COMPLETE_HIT, BUS, l1.getPoints()));
			animatedLines.add(new Line(CacheAccess.COMPLETE_MISS, BUS, l1.getPoints()));
			animatedLines.add(new Line(Updater.HIGHER_LEVEL_READ, BUS, l1.getPoints()));
			animatedLines.add(new Line(Updater.HIGHER_LEVEL_WRITE, BUS, l1.getPoints()));
			animatedLines.add(new Line(CacheAccess.NONE, INACTIVE, l1.getPoints()));
			animatedLines.add(new Line(CacheAccess.INITIAL, INACTIVE, l1.getPoints()));
			animatedLines.add(new Line(CacheAccess.LOOKUP_MISS, l2.animationStart, l2.animationEnd, INACTIVE, BUS, l2.getPoints()));
			animatedLines.add(new Line(CacheAccess.COMPLETE_HIT, BUS, l2.getPoints()));
			animatedLines.add(new Line(CacheAccess.COMPLETE_MISS, BUS, l2.getPoints()));
			animatedLines.add(new Line(Updater.HIGHER_LEVEL_READ, BUS, l2.getPoints()));
			animatedLines.add(new Line(Updater.HIGHER_LEVEL_WRITE, BUS, l2.getPoints()));
			animatedLines.add(new Line(CacheAccess.NONE, INACTIVE, l2.getPoints()));
			animatedLines.add(new Line(CacheAccess.INITIAL, INACTIVE, l2.getPoints()));
		}
		
		//MUX
		drawLine(x1 = muxMid - MULTIPLEXER_WIDTH / 2, y1 = muxBottom + MULTI_CACHE_MUX_DISTANCE_FROM_BLOCK_MUX,
					x1 + MULTIPLEXER_WIDTH, y1, g);
		drawLine(x1, y1, x1 = muxMid - MULTIPLEXER_2ND_WIDTH / 2, y2 = y1 + MULTIPLEXER_HEIGHT);
		drawLine(muxMid + MULTIPLEXER_WIDTH / 2, y1, x2 = x1 + MULTIPLEXER_2ND_WIDTH, y2);
		drawLine(x1, y2, x2, y2);
		
		//MUX to dataField
		Line l = new Line(CacheAccess.LOOKUP_HIT, 0.75f, 1, INACTIVE, BUS);
		l.addPoint(muxMid, y1 = muxBottom + MULTI_CACHE_MUX_DISTANCE_FROM_BLOCK_MUX + MULTIPLEXER_HEIGHT);
		l.addPoint(muxMid, dataField.getLocation().y);
		animatedLines.add(l);
		animatedLines.add(new Line(CacheAccess.LOOKUP_MISS, l.animationStart, l.animationEnd, INACTIVE, BUS, l.getPoints()));
		animatedLines.add(new Line(CacheAccess.COMPLETE_HIT, BUS, l.getPoints()));
		animatedLines.add(new Line(CacheAccess.COMPLETE_MISS, BUS, l.getPoints()));
		animatedLines.add(new Line(Updater.HIGHER_LEVEL_READ, BUS, l.getPoints()));
		animatedLines.add(new Line(Updater.HIGHER_LEVEL_WRITE, BUS, l.getPoints()));
		animatedLines.add(new Line(CacheAccess.NONE, INACTIVE, l.getPoints()));
		animatedLines.add(new Line(CacheAccess.INITIAL, INACTIVE, l.getPoints()));
		
		
		int orMid = statusField.getLocation().x + statusField.getWidth() / 2;
		
		for (int i = 0; i < associativeSets / 2; i++) {
			int andX1 = getHorizontalCenter(lines.get(0), VCacheLine.VALID_FIELD) + cacheRAMPanels.get(i).getLocation().x + AND_WIDTH / 6;
			int andX2 = getHorizontalCenter(lines.get(0), VCacheLine.VALID_FIELD) + cacheRAMPanels.get(associativeSets - i - 1).getLocation().x + AND_WIDTH / 6;
			
			Line l1, l2;
			if (cache.getCurrentAccess() != null) {
				CacheLine line1 = lineArray.get(i);
				CacheLine line2 = lineArray.get(associativeSets - i - 1);
				l1 = new Line(CacheAccess.LOOKUP_HIT, 0.5f, 0.75f, INACTIVE, (cache.getCurrentAccess().getTag() == line1.getTag()) && line1.getValid() ? TRUE : FALSE);
				l2 = new Line(CacheAccess.LOOKUP_HIT, 0.5f, 0.75f, INACTIVE, (cache.getCurrentAccess().getTag() == line2.getTag()) && line2.getValid() ? TRUE : FALSE);
			} else {
				l1 = new Line(CacheAccess.LOOKUP_HIT, ANIMATION_STAGE_3, 0.85f, INACTIVE, INACTIVE);
				l2 = new Line(CacheAccess.LOOKUP_HIT, ANIMATION_STAGE_3, 0.85f, INACTIVE, INACTIVE);
			}
			l1.addPoint(andX1, andBottom); //cache output
			l2.addPoint(andX2, andBottom);
			l1.addPoint(andX1, y1 = andBottom + MULTI_CACHE_OR_KNEE_START + MULTI_CACHE_OR_KNEE_RANGE - MULTI_CACHE_OR_KNEE_RANGE * (associativeSets - i) / (associativeSets + 1));
			l2.addPoint(andX2, y2 = andBottom + MULTI_CACHE_OR_KNEE_START + MULTI_CACHE_OR_KNEE_RANGE - MULTI_CACHE_OR_KNEE_RANGE * (i + 1) / (associativeSets + 1));
			l1.addPoint(x1 = orMid - AND_WIDTH / 2 + AND_WIDTH * (i + 1) / (associativeSets + 1), y1);
			l2.addPoint(x2 = 2 * orMid - x1, y2);
			l1.addPoint(x1, y2 = andBottom + MULTI_CACHE_OR_DISTANCE_FROM_AND - (int) Math.round(Math.abs(x1 - orMid) * Math.tan(Math.asin((double) Math.abs(x1 - orMid) / OR_END_CIRCLE_DIAMETER / 2))));
			l2.addPoint(x2, y2);
			animatedLines.add(l1);
			animatedLines.add(l1);
			animatedLines.add(new Line(CacheAccess.LOOKUP_MISS, l1.animationStart, l1.animationEnd, l1.getStartType(), l1.getType(), l1.getPoints()));
			animatedLines.add(new Line(CacheAccess.COMPLETE_HIT, l1.getType(), l1.getPoints()));
			animatedLines.add(new Line(CacheAccess.COMPLETE_MISS, l1.getType(), l1.getPoints()));
			animatedLines.add(new Line(Updater.HIGHER_LEVEL_READ, l1.getType(), l1.getPoints()));
			animatedLines.add(new Line(Updater.HIGHER_LEVEL_WRITE, l1.getType(), l1.getPoints()));
			animatedLines.add(new Line(CacheAccess.NONE, INACTIVE, l1.getPoints()));
			animatedLines.add(new Line(CacheAccess.INITIAL, INACTIVE, l1.getPoints()));
			animatedLines.add(new Line(CacheAccess.LOOKUP_MISS, l2.animationStart, l2.animationEnd, l2.getStartType(), l2.getType(), l2.getPoints()));
			animatedLines.add(new Line(CacheAccess.COMPLETE_HIT, l2.getType(), l2.getPoints()));
			animatedLines.add(new Line(CacheAccess.COMPLETE_MISS, l2.getType(), l2.getPoints()));
			animatedLines.add(new Line(Updater.HIGHER_LEVEL_READ, l2.getType(), l2.getPoints()));
			animatedLines.add(new Line(Updater.HIGHER_LEVEL_WRITE, l2.getType(), l2.getPoints()));
			animatedLines.add(new Line(CacheAccess.NONE, INACTIVE, l2.getPoints()));
			animatedLines.add(new Line(CacheAccess.INITIAL, INACTIVE, l2.getPoints()));
		}

		//OR gate
		double halfAngle = Math.toDegrees(Math.asin((double) AND_WIDTH / OR_END_CIRCLE_DIAMETER));
		g.drawArc(orMid - OR_END_CIRCLE_DIAMETER / 2, andBottom + MULTI_CACHE_OR_DISTANCE_FROM_AND - OR_END_CIRCLE_DIAMETER, OR_END_CIRCLE_DIAMETER, 
				OR_END_CIRCLE_DIAMETER, (int) Math.round(-90 - halfAngle), (int) Math.round(2 * halfAngle));
		drawLine(x1 = orMid - AND_WIDTH / 2, y1 = andBottom + MULTI_CACHE_OR_DISTANCE_FROM_AND - (int) Math.round(OR_END_CIRCLE_DIAMETER * (1 - Math.cos(Math.toRadians(halfAngle))) / 2),
				x1, y1 + AND_HEIGHT);
		drawLine(x2 = x1 + AND_WIDTH, y1, x2, y1 + AND_HEIGHT);
		int orBottom = y1 + AND_HEIGHT + (int) Math.round(AND_WIDTH * Math.sin(Math.acos(0.5)));
		int ang = (int) Math.round(Math.toDegrees(Math.acos(0.5)));
		g.drawArc(x1, y1 += AND_HEIGHT - AND_WIDTH, AND_WIDTH * 2, AND_WIDTH * 2, 180, ang);
		g.drawArc(x1 - AND_WIDTH, y1, AND_WIDTH * 2, AND_WIDTH * 2, -ang, ang);

		boolean orOut = false;
		for (CacheLine line : lineArray)
			if ((cache.getCurrentAccess().getTag() == line.getTag()) && line.getValid()) {
				orOut = true;
				break;
			}
		if (cache.getCurrentAccess() != null)
			l = new Line(CacheAccess.LOOKUP_HIT, 0.75f, 1, INACTIVE, orOut ? TRUE : FALSE);
		else
			l = new Line(CacheAccess.LOOKUP_HIT, 0.85f, 1.0f, INACTIVE, INACTIVE);
		l.addPoint(orMid, orBottom);
		l.addPoint(orMid, statusField.getLocation().y);
		animatedLines.add(l);
		animatedLines.add(new Line(CacheAccess.LOOKUP_MISS, l.animationStart, l.animationEnd, l.getStartType(), l.getType(), l.getPoints()));
		animatedLines.add(new Line(CacheAccess.COMPLETE_HIT, l.getType(), l.getPoints()));
		animatedLines.add(new Line(CacheAccess.COMPLETE_MISS, l.getType(), l.getPoints()));
		animatedLines.add(new Line(Updater.HIGHER_LEVEL_READ, l.getType(), l.getPoints()));
		animatedLines.add(new Line(Updater.HIGHER_LEVEL_WRITE, l.getType(), l.getPoints()));
		animatedLines.add(new Line(CacheAccess.NONE, INACTIVE, l.getPoints()));
		animatedLines.add(new Line(CacheAccess.INITIAL, INACTIVE, l.getPoints()));
	}
	
	private void drawLines(Graphics g) {
		float animStatus = 0;
		int status = CacheAccess.NONE;
		if (cache.getCurrentAccess() != null) {
			animStatus = updater.getStatus();
			status = updater.getAccessStatus();
		}
		for (Line l : animatedLines) {   //ConcurrentModificationException here!
			if (l.getAnimationStatus() == status)
				drawLine(l, g, status, animStatus);
		}
	}
	
	private void repaintLines(Graphics g) {
		for (Line l : oldAnimatedLines) {
			Point p1 = null;
			for (Point p2 : l.getPoints()) {
				if (p1 == null) {
					p1 = p2;
					continue;
				}
				drawLine(g, p1.x, p1.y, p2.x, p2.y, setGraphicsWithType(g, REPAINT), true);
				p1 = p2;
			}
		}
	}
	
	private int getVerticalCenter(Locatable parent, int component) {
		return (int) (parent.getLocation(component).getY() + parent.getHeight(component) / 2 + parent.getLocation().getY());
	}
	
	private int getHorizontalCenter(Locatable parent, int component) {
		return parent.getLocation(component).x + parent.getWidth(component) / 2 + parent.getLocation().x;
	}
	
	private void drawLine(int x1, int y1, int x2, int y2, Graphics g) {
		drawLine(x1, y1, x2, y2, g, Color.BLACK, 1);
	}
	
	private void drawLine(Line line, Graphics g, int status, float animStatus) {		
		ArrayList<Point> points = line.getPoints();
		int lineChangeIndex = -1;
		Point changePoint = null;
		if (line.isAnimated() && (animStatus > line.getAnimationStart()) && (animStatus <= line.getAnimationEnd())) {
			Object[] anP = findAnimationPoint(points, animStatus, line.getAnimationStart(), line.getAnimationEnd());
			lineChangeIndex = (Integer)anP[0];
			changePoint = (Point)anP[1];
		}
		int thickness = setGraphicsWithType(g, line.getType());
		for (int i = 0; i < points.size() - 1; i++) {
			if (!line.isAnimated())
				drawLine(g, points.get(i).x, points.get(i).y, points.get(i + 1).x, points.get(i + 1).y, thickness);
			else {
				if (i == lineChangeIndex) {
					thickness = setGraphicsWithType(g, line.getType());
					drawLine(g, points.get(i).x, points.get(i).y, changePoint.x, changePoint.y, thickness);
					thickness = setGraphicsWithType(g, line.getStartType());
					drawLine(g, changePoint.x, changePoint.y, points.get(i + 1).x, points.get(i + 1).y, thickness);
				}
				else if (lineChangeIndex != -1) {
					thickness = setGraphicsWithType(g, i < lineChangeIndex ? line.getType() : line.getStartType());
					drawLine(g, points.get(i).x, points.get(i).y, points.get(i + 1).x, points.get(i + 1).y, thickness);
				}
				else {
					thickness = setGraphicsWithType(g, animStatus > line.getAnimationStart() ? line.getType() : line.getStartType());
					drawLine(g, points.get(i).x, points.get(i).y, points.get(i + 1).x, points.get(i + 1).y, thickness);
				}
			}
		}
	}
	
	private void drawLine(Graphics g, int x1, int y1, int x2, int y2, int thickness) {
		drawLine(g, x1, y1, x2, y2, thickness, false);
	}
	
	//this is the newest method for drawing lines so far
	private void drawLine(Graphics g, int x1, int y1, int x2, int y2, int thickness, boolean noShade) {
		if (thickness > 1) {
			Color c = null;
			if (!noShade) {
				c = g.getColor();
				int c1 = 150;
				int c2 = 255 - c1;
				g.setColor(new Color(c.getRed() > c2 ? 255 : c.getRed() + c1, c.getGreen() > c2 ? 255 : c.getGreen() + c1, c.getBlue() > c2 ? 255 : c.getBlue() + c1));
			}
			boolean v = x1 == x2;
			int x = v ? x1 - 1 : (x1 < x2 ? x1 : x2);
			int y = v ? (y1 < y2 ? y1 : y2) : y1 - 1;
			g.fillRect(x, y, v ? thickness + 1 : Math.abs(x2 - x1), v ? Math.abs(y2 - y1) : thickness + 1);
			if (!noShade)
				g.setColor(c);	
		}
		g.drawLine(x1, y1, x2, y2);
	}
	
	//returns line thickness
	private int setGraphicsWithType(Graphics g, int type) {
//		((Graphics2D)g).setStroke(new BasicStroke(2.0f));
		if (type == TRUE)
			g.setColor(new Color(0, 150, 0)); //green
		else if (type == FALSE) 
			g.setColor(Color.RED);
		else if (type == BUS)
			g.setColor(Color.BLUE);
		else if (type == INACTIVE) {
			g.setColor(Color.BLACK);
			return 1;
//			((Graphics2D)g).setStroke(new BasicStroke(1.0f));
		}
		else if (type == NO_LINE) {
			g.setColor(getBackground());
			return 1;
//			((Graphics2D)g).setStroke(new BasicStroke(1.0f));
		}
		else if (type == REPAINT)
			g.setColor(getBackground());
		return 2;
	}
	
	//returns the array of the index of insertion and the point
	private Object[] findAnimationPoint(ArrayList<Point> points, float status, float animationStart, float animationEnd) {
		ArrayList<Double> lengthList = new ArrayList<Double>(points.size() - 1);
		double totLength = 0, len;
		Point p2 = null;
		for (Point p1 : points) {
			if (p2 != null) {
				lengthList.add(len = p1.distance(p2));
				totLength += len;
			}
			p2 = p1;
		}
		len = (status - animationStart) * totLength / (animationEnd - animationStart);
		double len2 = 0;
		Point changePoint = null;
		int lineChangeIndex;
		for (lineChangeIndex = 0; lineChangeIndex < lengthList.size(); lineChangeIndex++) { 
			//		(double lineSeg : lengthList) {
			if (len2 + lengthList.get(lineChangeIndex) > len) {
				double reqLen = len - len2;
				double changeX = points.get(lineChangeIndex + 1).getX() - points.get(lineChangeIndex).getX();
				double changeY = points.get(lineChangeIndex + 1).getY() - points.get(lineChangeIndex).getY();
				double segRatio = reqLen / lengthList.get(lineChangeIndex);
				changePoint = new Point((int)(points.get(lineChangeIndex).getX() + segRatio * changeX), (int)(points.get(lineChangeIndex).getY() + segRatio * changeY));
				break;
			}
			else
				len2 += lengthList.get(lineChangeIndex);
		}
		Object[] ret = {lineChangeIndex, changePoint};
		return ret;
	}
	
	private void drawLine(int x1, int y1, int x2, int y2, Graphics g, int animId) {
		float status = CacheAccess.NONE;
		Color c = Color.BLUE;
		if ((animId & (1 << 3)) > 0)
			c = new Color(0, 150, 0); //green
		else if ((animId & (1 << 4)) > 0)
			c = Color.RED;
		animId &= 0x07;
		if (cache.getCurrentAccess() != null)
			status = updater.getStatus();
		if (animId == 1) {
			if (status > 0 && status < CacheAccess.NONE) {
//					status == CacheAccess.LOOKUP || status == CacheAccess.TAG_VALID_COMPARE || 
//					status == CacheAccess.LOOKUP_HIT || status == CacheAccess.LOOKUP_MISS) {
				drawLine(x1, y1, x2, y2, g, c, 2);
				return;
			}
		}
		else if (animId == 2) {
			if (status > 0.5f && status < CacheAccess.NONE) {
//					status == CacheAccess.TAG_VALID_COMPARE || 
//				status == CacheAccess.LOOKUP_HIT || status == CacheAccess.LOOKUP_MISS) {
				drawLine(x1, y1, x2, y2, g, c, 2);
				return;
			}
		}
		else if (animId == 3) {
			if (status >= 1 && status < CacheAccess.NONE) {
				drawLine(x1, y1, x2, y2, g, c, 2);
				return;
			}
		}
		c = Color.BLACK;
		drawLine(x1, y1, x2, y2, g, c, 1);
	}
	
	private void drawLine(int x1, int y1, int x2, int y2) {
		if(drawLineGraphics == null)
			return;
		drawLine(x1, y1, x2, y2, drawLineGraphics, Color.BLACK, 1);
	}
	
	private void drawLine(int x1, int y1, int x2, int y2, Graphics g, Color color, float thickness) {
		drawLineGraphics = g;
		g.setColor(color);
		((Graphics2D)g).setStroke(new BasicStroke(thickness));
		g.drawLine(x1, y1, x2, y2);
		g.setColor(Color.BLACK);
		((Graphics2D)g).setStroke(new BasicStroke(1.0f));
	}
	
	private class Line {
		private ArrayList<Point> points;
		private float animationStart, animationEnd;
		private int startType, type, animationStatus;
		private boolean animated;
		
		public Line(int animationStatus, float animationStart, float animationEnd, int startType, int type) {
			this(animationStatus, animationStart, animationEnd, startType, type, new ArrayList<Point>());
		}
		
		public Line(int animationStatus, float animationStart, float animationEnd, int startType, int type, ArrayList<Point> points) {
			this.points = points;
			this.animationStart = animationStart;
			this.animationEnd = animationEnd;
			this.startType = startType;
			this.type = type;
			this.animationStatus = animationStatus;
			animated = true;
		}
		
		public Line(int animationStatus, int type, ArrayList<Point> points) {
			this.points = points;
			this.type = type;
			this.animationStatus = animationStatus;
			animated = false;
		}

		public boolean isAnimated() {
			return animated;
		}
		
		public ArrayList<Point> getPoints() {
			return points;
		}

		public float getAnimationStart() {
			return animationStart;
		}
		
		public float getAnimationEnd() {
			return animationEnd;
		}
		
		//TRUE, FALSE, or BUS
		public int getType() {
			return type;
		}
		
		public int getStartType() {
			return startType;
		}
		
		public int getAnimationStatus() {
			return animationStatus;
		}
		
		public void setPoints(ArrayList<Point> points) {
			this.points = points;
		}

		public void addPoint(Point p) {
			points.add(p);
		}
		
		public void addPoint(int x, int y) {
			addPoint(new Point(x, y));
		}
	}

	public List<Settings> getSettingsList() {
		if (settingsList != null)
			return settingsList;
		Settings settings = new Settings("Cache Diagram Settings", this);
		Map<String, String[]> constraints = new HashMap<String, String[]>();
		String[] types = {NORMAL, COMPACT};
		constraints.put(Setting.COMBO_BOX_ITEMS, types);
		Setting<String> setting = new Setting<String>("Representation type", Setting.COMBO_BOX, constraints, visualizationType);
		Setting<Integer> setting2 = new Setting<Integer>("Compact px/bit", Setting.INT_FIELD, null, compactPixels);
		settings.addSetting(setting);
		settings.addSetting(setting2);
		settingsList = new ArrayList<Settings>();
		settingsList.add(settings);
		return settingsList;
	}

	public void settingChanged(Setting<?> affectedSetting) {
		// TODO Auto-generated method stub
		
	}

	public void settingsChanged(Settings settings) {
		for (Setting setting : settings.getSettingList()) {
			if (setting.getName().equals("Representation type") && !setting.getValue().equals(visualizationType)) {
				visualizationType = (String) setting.getValue();
				removeAll();
				buildGUI();
				repaint();
				frame.setSize(frame.getSize().height + 1, frame.getSize().width + 1);
				frame.setSize(frame.getSize().height - 1, frame.getSize().width - 1);
//				repaint();
			} else if (setting.getName().equals("Compact px/bit") && !setting.getValue().equals(compactPixels)) {
				compactPixels = (Integer) setting.getValue();
				removeAll();
				buildGUI();
				repaint();
				frame.setSize(frame.getSize().height + 1, frame.getSize().width + 1);
				frame.setSize(frame.getSize().height - 1, frame.getSize().width - 1);
			}
		}
	}
}

package edu.lafayette.vcache.visual;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.Timer;

import edu.lafayette.vcache.core.Cache;
import edu.lafayette.vcache.core.CacheAccess;
import edu.lafayette.vcache.core.Updater;
import edu.lafayette.vcache.core.view.CacheView;
import edu.lafayette.vcache.util.Debug;
import edu.lafayette.vcache.visual.abst.Locatable;
import edu.lafayette.vcache.visual.abst.VPanel;

import java.lang.Math;

public class VCurrentAccess extends VPanel<Updater> implements Locatable
//, ActionListener 
{
	public static final int TAG_PANEL = 0, STATUS_PANEL = 1, LINE_INDEX_PANEL = 2, BYTE_INDEX_PANEL = 3,
							WORD_INDEX_PANEL = 4
//							, STATUS_FIELD = 5
//							, DATA_FIELD = 6
							;



	private JTextField tagField;
	private JTextField lineIndexField;
	private JTextField wordIndexField;
	private JTextField byteIndexField;
	private JTextField statusField;
	private JPanel tagPanel;
	private JPanel statusPanel;
	private JPanel lineIndexPanel;
	private JPanel byteIndexPanel;
	private JPanel wordIndexPanel;
//	private static final int ANIM_DELAY = 2000;
//	private Timer accessTimer;
	
	public VCurrentAccess(Cache c, Updater updater) {
		super(c, updater);
		buildGUI();
	}
	
	private JComponent getComp(int component) {
		switch(component) {
		case TAG_PANEL:
			return tagPanel;
		case STATUS_PANEL:
			return statusPanel;
		case LINE_INDEX_PANEL:
			return lineIndexPanel;
		case BYTE_INDEX_PANEL:
			return byteIndexPanel;
		case WORD_INDEX_PANEL:
			return wordIndexPanel;
//		case STATUS_FIELD:
//			return statusField;
//		case DATA_FIELD:
//			return dataField;
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
	
	private int log16f(int i) {
		return (int)Math.ceil(Math.log((double)i)/Math.log(16.0));
	}

	private void buildGUI() {
		CacheAccess currentAccess = cache.getCurrentAccess();
		tagPanel = new JPanel();
		tagPanel.setLayout(new BoxLayout(tagPanel, BoxLayout.Y_AXIS));
		tagField = new JTextField();
		tagField.setColumns(cache.getTagBits()/4 + 1);
		tagField.setFocusable(false);
		tagPanel.add(new JLabel("Tag"), JLabel.CENTER_ALIGNMENT);
		tagPanel.add(tagField);
		add(tagPanel);
		lineIndexPanel = new JPanel();
		lineIndexPanel.setLayout(new BoxLayout(lineIndexPanel, BoxLayout.Y_AXIS));
		lineIndexField = new JTextField();
		lineIndexField.setColumns(cache.getLineIndexBits()/4 + 1);
		lineIndexField.setFocusable(false);
		lineIndexPanel.add(new JLabel("L"), JLabel.CENTER_ALIGNMENT);
		lineIndexPanel.add (lineIndexField);
		add(lineIndexPanel);
		if (cache.getWordIndexBits() > 0) {
			wordIndexPanel = new JPanel();
			wordIndexPanel.setLayout(new BoxLayout(wordIndexPanel, BoxLayout.Y_AXIS));
			wordIndexPanel.add(new JLabel("W"), JLabel.CENTER_ALIGNMENT);
			wordIndexField = new JTextField();
			wordIndexField.setColumns(cache.getWordIndexBits()/4 + 1);
			wordIndexField.setFocusable(false);
			wordIndexPanel.add(wordIndexField);
			add(wordIndexPanel);
		}
		byteIndexPanel = new JPanel();
		byteIndexPanel.setLayout(new BoxLayout(byteIndexPanel, BoxLayout.Y_AXIS));
		byteIndexPanel.add(new JLabel("B"), JLabel.CENTER_ALIGNMENT);
		byteIndexField = new JTextField("0");
		byteIndexField.setColumns(1);
		byteIndexField.setFocusable(false);
		byteIndexPanel.	add(byteIndexField);
		add(byteIndexPanel);
		statusPanel = new JPanel();
		statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.Y_AXIS));
		statusPanel.add(new JLabel("Status"), JLabel.CENTER_ALIGNMENT);
		statusField = new JTextField();
		statusField.setColumns(9);
		statusField.setFocusable(false);
		statusPanel.add(statusField);
		//	add(statusPanel);
		
		
		
//		accessTimer = new Timer(ANIM_DELAY, this);
//		accessTimer.setInitialDelay(0);
	}

	

	public void animateAccess() {
		CacheAccess currentAccess = cache.getCurrentAccess();
		if (Debug.debug)
			System.out.println("starting animateAccess: " + currentAccess);
		if (updater.getAccessStatus() != CacheAccess.INITIAL)
			if (Debug.debug)
				System.out.println("VCacheAccess.animateAccess() - not starting in initial state: " + updater.getAccessStatus());
		else {
//			accessTimer.start();  // timer callbacks should do the rest!
		}
	}
	
	public JComponent getStatusField() {
		return statusField;
	}

//	public JComponent getDataField() {
//		return dataField;
//	}
	
	public int getLastAccessedLine() {
		return cache.getCurrentAccess().getLineIndex();
	}

	/** used for callbacks from Timer to do animation.  Note that nextStep() indirectly calls for updates
     through the Cache, CacheView, and CacheAccess classes */
//	public void actionPerformed(ActionEvent e) {     
//		if ((cache.getCurrentAccess() != null) && !cache.getCurrentAccess().nextStep()) 
//			accessTimer.stop();
//	}
	


	/** fill in fields and highlight based on status */
	public void updateLater() {
		CacheAccess currentAccess = cache.getCurrentAccess();
		if (Debug.debug)
			System.out.println("VCacheAccess update");
		float status;
		if (currentAccess == null) {
			status = CacheAccess.NONE;
		} else {
			tagField.setText(String.format("%x", currentAccess.getTag()));
			tagField.setBackground(Color.WHITE);
			lineIndexField.setText(String.format("%x", currentAccess.getLineIndex()));
			lineIndexField.setBackground(Color.WHITE);
			if (cache.getWordIndexBits() > 0) {
				wordIndexField.setText(String.format("%x",currentAccess.getWordIndex()));
				wordIndexField.setBackground(Color.WHITE);
			}
			byteIndexField.setText(String.format("%x",currentAccess.getByteIndex()));
			byteIndexField.setBackground(Color.WHITE);
			statusField.setBackground(Color.WHITE);
			status = updater.getAccessStatus();
//			if((int)status >= currentAccess.getStatus() + 1) {
//				System.out.println("VCurrentAccess calling nextStep()");
//				currentAccess.nextStep();
////				if(!currentAccess.nextStep()) {
////					System.out.println("VCurrentAccess animation marking as done");
////					animation.markAsDone();
////				}
//				return;
//			}
		}

		if ((int)status == CacheAccess.INITIAL) {
			tagField.setBackground(Color.WHITE);
			statusField.setText("START");
		}

		//			tagField.setBackground(Color.YELLOW);
		//			statusField.setText("START");
		//			break;
		else if ((int)status == CacheAccess.LOOKUP_HIT) {
			tagField.setBackground(Color.YELLOW);
			statusField.setText("HIT");
		} else if ((int)status == CacheAccess.LOOKUP_MISS) {
			tagField.setBackground(Color.RED);
			statusField.setText("MISS");
		} else if ((int)status == CacheAccess.COMPLETE_HIT) {
			statusField.setText("HIT (Complete)");
		} else if ((int)status == CacheAccess.COMPLETE_MISS) {
			statusField.setText("MISS (Complete)");
		} else if ((int)status == CacheAccess.NONE) {
			tagField.setText("");
			tagField.setBackground(Color.WHITE);
			lineIndexField.setText("");
			wordIndexField.setText("");
			lineIndexField.setText("");
			byteIndexField.setText("");
			statusField.setText("");
		} else {
			statusField.setText("ERROR");
		}
	}
	

	/*    public static void main(String [] args) {
	JFrame f = new JFrame("VAdrdess Test");
	VAddress tv = new VAddress(27, 3, 2);
	f.getContentPane().add(tv);
	f.pack();
	f.setVisible(true);
	tv.setAddress(0x4040, 0x7, 0x0);
    }	
	 */

}

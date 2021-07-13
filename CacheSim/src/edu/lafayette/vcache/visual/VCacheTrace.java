package edu.lafayette.vcache.visual;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import edu.lafayette.vcache.core.Cache;
import edu.lafayette.vcache.core.CacheAccess;
import edu.lafayette.vcache.core.CacheTrace;
import edu.lafayette.vcache.core.Updater;
import edu.lafayette.vcache.util.Debug;
import edu.lafayette.vcache.visual.abst.VPanel;

//TODO: JList has serious performence issues especially with dynamic data updates, find an alternative implementation
public class VCacheTrace extends VPanel<Updater> implements ActionListener, ListSelectionListener {
	private CacheTrace<Vector<CacheAccess>> trace;
//	private Cache cache;
//	private VCacheAnimation animation;
	private DefaultListModel listModel;
	private JScrollPane scrollPane;
	private JButton openButton;
	private JFileChooser fileChooser;
	private int selectedIndex;
	private JList list;
	private boolean windowClosed;
	private static final Font SELECTED = new Font(Font.SANS_SERIF, 12, Font.BOLD),
								NOT_SELECTED = new Font(Font.SANS_SERIF, 12, Font.PLAIN);
	
	public VCacheTrace(Cache cache, Updater updater) {
		super(cache, updater);
//		this.animation = animation;
		trace = new CacheTrace<Vector<CacheAccess>>(cache, new Vector<CacheAccess>());
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		list = new JList();
		list.setCellRenderer(new VCacheTraceCell());
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.addListSelectionListener(this);
		list.setModel(listModel = new DefaultListModel());
		scrollPane = new JScrollPane(list);
//		scrollPane.setPreferredSize(new Dimension(200, height));
		add(scrollPane);
		
		openButton = new JButton("Open");
		openButton.addActionListener(this);
		add(openButton);
		
		fileChooser = null;
		selectedIndex = -1;
		
		windowClosed = false;
	}
	
	public CacheTrace<?> getTrace() {
		return trace;
	}
	
	
	public void updateLater() {
		
		if (Debug.debug)
			System.out.println("VCacheTrace update");
		if (list.getSelectedIndex() == -1)
			list.setSelectedIndex(trace.getCurrentIndex());
		if ((cache.getCurrentAccess() != null && (((Updater)updater).getAccessStatus() == CacheAccess.COMPLETE_HIT || ((Updater)updater).getAccessStatus() == CacheAccess.COMPLETE_MISS)
				&& ((Updater)updater).getStatus() == 1) || cache.getCurrentAccess() == null) {
			if (!trace.hasMoreAccess()) {
				list.clearSelection();
				((Updater)updater).setControlStatus(VCacheAnimation.PAUSE);
			}
			else if (!windowClosed)
				list.setSelectedIndex(trace.getCurrentIndex() + 1);
			if (((Updater)updater).getControlStatus() != VCacheAnimation.PAUSE && trace.hasMoreAccess()) {
				//trace.nextAccess();
				if (((Updater)updater).getControlStatus() == VCacheAnimation.NEXT_STEP) 
					((Updater)updater).setControlStatus(VCacheAnimation.PAUSE);
				
				if (!windowClosed) {
					if (trace.isTraceModified()) {
						list.setListData(trace.getTrace());
					}
					int index = trace.getCurrentIndex();
					list.ensureIndexIsVisible(index + 10);
					list.ensureIndexIsVisible(index - 10);
					list.setSelectedIndex(index);
				}
			}
		} 
//		else if (animation.getStatus() == 1 && !windowClosed) {
////			list.repaint();
//			list.setSelectedIndex(trace.getCurrentIndex());
//		}
	}
	
	public void unregisterViews() {
		windowClosed = true;
	}
	
	public synchronized void registerViews() {
		windowClosed = false;
		list.setListData(trace.getTrace());
		list.ensureIndexIsVisible(trace.getCurrentIndex() + 10);
		list.ensureIndexIsVisible(trace.getCurrentIndex() - 10);
		
		list.setSelectedIndex(trace.getCurrentIndex());
	}
	
	public void setFile(File file) {
		try {
			trace.setTrace(new FileInputStream(file));
			list.setListData(trace.getTrace());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(openButton)) {
			if (VCache.getInstance().getMode() == VCache.APPLICATION) {
				if (fileChooser == null)
					fileChooser = new JFileChooser();
			    int returnVal = fileChooser.showOpenDialog(this);
			    if (returnVal == JFileChooser.APPROVE_OPTION) {
			       setFile(fileChooser.getSelectedFile());
			    }
			} 
//                        else {
//				try {
//					URL traceURL = new URL(VCacheApplet.getInstance().getCodeBase(), "trace_files/cc1.din");
//					trace.setTrace(traceURL.openStream());
//					list.setListData(trace.getTrace());
//				} catch (Exception ex) {
//					ex.printStackTrace();
//				}
//			}
		}
	}
	
	private class VCacheTraceCell extends JLabel implements ListCellRenderer {

		public VCacheTraceCell() {
			setOpaque(true);
		}
		
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			if (value == null) {
				System.out.println("null value");
				return this;
			}
			CacheAccess access = (CacheAccess) value;
			setText("" + access.getType() + " " + Integer.toHexString(access.getAddress()));
			Color c = Color.WHITE;
			setForeground(Color.BLACK);
			if (access.getStatus() == CacheAccess.COMPLETE_HIT)
				c = Color.YELLOW;
			else if (access.getStatus() == CacheAccess.COMPLETE_MISS)
				c = Color.GREEN;
			if (isSelected)
				setFont(getFont().deriveFont(Font.BOLD));
			else
				setFont(getFont().deriveFont(Font.PLAIN));
			setBackground(c);
			return this;
			
		}
		
	}

	public synchronized void valueChanged(ListSelectionEvent e) {
		if (((Updater)updater).getControlStatus() == VCacheAnimation.PAUSE) {
			if (Debug.debug)
				System.out.println("VCacheTrace valueChange value setting " + list.getSelectedIndex());
			trace.setNextAccessIndex(list.getSelectedIndex());
		}
	}
}

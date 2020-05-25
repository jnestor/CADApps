package edu.lafayette.vcache.visual;
//import javax.swing.JFrame;
//import java.awt.BorderLayout;
//import java.awt.Container;
import javax.swing.*;

import edu.lafayette.vcache.core.Cache;
import edu.lafayette.vcache.visual.abst.EntryPoint;

public class VCacheApplication extends JFrame implements EntryPoint {
	private static VCacheApplication app;
	
	public VCacheApplication() {
		super("VCache Application");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setResizable(true);
		buildGUI();
		pack();
		
		setVisible(true);
		app = this;
	}
	
	public void buildGUI() {
		buildGUI(new Cache(16, 4, 1, null), null);
	}
	
	public void buildGUI(Cache c, VCache oldVC) {
		VCache vc = new VCache(c, VCache.APPLICATION, oldVC);
		setJMenuBar(vc.getJMenuBar());
		setContentPane(new JScrollPane(vc.getContentPane()));
		setSize(getSize().width + 1, getSize().height + 1);
		setSize(getSize().width - 1, getSize().height - 1);
	}
	
	public static VCacheApplication getInstance() {
		return app;
	}

    public static void main(String [] args) {
		new VCacheApplication();
    }
}

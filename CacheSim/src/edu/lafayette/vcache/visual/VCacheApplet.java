package edu.lafayette.vcache.visual;

import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JApplet;

import edu.lafayette.vcache.core.Cache;
import edu.lafayette.vcache.visual.abst.EntryPoint;

public class VCacheApplet extends JApplet implements EntryPoint {
	private static VCacheApplet vCacheApplet;
	
	public VCacheApplet() {
		vCacheApplet = this;
	}
	
	public void init() {
		buildGUI();
	}
	
	public void buildGUI() {
		buildGUI(new Cache(8, 4, null), null);
	}
	
	public void buildGUI(Cache c, VCache oldVC) {
		VCache vc = new VCache(c, VCache.APPLET, oldVC);
		setJMenuBar(vc.getJMenuBar());
		setContentPane(vc.getContentPane());
	}
	
	public static VCacheApplet getInstance() {
		return vCacheApplet;
	}
}

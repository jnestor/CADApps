package edu.lafayette.vcache.visual.abst;

import edu.lafayette.vcache.core.Cache;
import edu.lafayette.vcache.visual.VCache;

public interface EntryPoint {
	public void buildGUI();
	public void buildGUI(Cache c, VCache vc);
}

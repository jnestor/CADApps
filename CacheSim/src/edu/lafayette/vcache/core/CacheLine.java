package edu.lafayette.vcache.core;
import java.util.ArrayList;
import java.util.ListIterator;

import edu.lafayette.vcache.core.view.CacheView;
import edu.lafayette.vcache.core.view.ViewUpdater;
import edu.lafayette.vcache.util.Debug;
import edu.lafayette.vcache.util.Timer;

/** Used to simuate cache line contents */

public class CacheLine implements ViewUpdater {
	private boolean valid;
	//    private boolean dirty;  // for future use?
	private int tag;
	//    private int[] contents;
	private Cache owner;
	private int baseAddress; // base address of current line
	private ArrayList<CacheView> views;
	private int lastAccess;
	private int lineIndex, associativeSetIndex;

	public CacheLine(Cache o, int lineIndex, int associativeSetIndex) {
		owner = o;
		views = new ArrayList<CacheView>();
		valid = false;
		tag = 0;
		baseAddress = 0; // arbitrary
		lastAccess = -1;
		this.lineIndex = lineIndex;
		this.associativeSetIndex = associativeSetIndex;
		//	contents = new int[o.getWordCount()];
		//	for (int i = 0; i < o.getWordCount(); i++) contents[i] = 0;
	}

	public void setLine(boolean v, int t, int base) {
		valid = v;
		tag = t;
		baseAddress = base;
	}

	public void clearLine() {
		valid = false;
		tag = 0;
		baseAddress = 0;
	}

	public boolean getValid() { return valid; }

	public int getTag() { return tag; }

	public boolean tagMatch(int t) { return ((tag == t) && valid); }

	public boolean tagMatch(CacheAccess ca) { return tagMatch(ca.getTag()); }

	public int getBaseAddress() { return baseAddress; }

	/*    public void setWord(int ix, int val) {
	contents[ix] = val;
	}*

	public int getWord(int ix) { return contents[ix]; } */

	public Cache getOwner() { return owner; }
	
	public boolean isOnLineOfCurrentAccess() {
		CacheAccess ca = owner.getCurrentAccess();
		if (ca == null) 
			return false;
		int curLineIndex = ca.getLineIndex();
		for (int i = 0; i < owner.getAssociativeSetCount(); i++)
			if ((owner.getLine(curLineIndex, i) != null) &&	(owner.getLine(curLineIndex, i) == this)) 
				return true;
		return false;
	}

	public boolean isCurrentAccess() {
		CacheAccess ca = owner.getCurrentAccess();
		if (ca == null) 
			return false;
		int curLineIndex = ca.getLineIndex();
		int curSetIndex = ca.getSetIndex();
		if (curSetIndex < 0)
			return false;
		if ((owner.getLine(curLineIndex, curSetIndex) != null) &&
				(owner.getLine(curLineIndex, curSetIndex) == this)) 
			return true;
		else return false;
	}

	public CacheAccess getCurrentAccess() { return owner.getCurrentAccess(); }
	
	public int getLastAccess() {
		return lastAccess;
	}

	public void setLastAccess(int lastAccess) {
		this.lastAccess = lastAccess;
	}
	
	public int getLineIndex() {
		return lineIndex;
	}

	public int getAssociativeSetIndex() {
		return associativeSetIndex;
	}

	public void addView(CacheView clv, int dummy) { views.add(clv); }
	
	public void removeView(CacheView view) {
		views.remove(view);
	}

	public void updateViews() {
		ListIterator<CacheView> ci = views.listIterator();
		while (ci.hasNext()) {
			CacheView v = ci.next();
			if (Debug.debug) {
				long t = System.nanoTime();
				System.out.println("CacheLine updating (time between = " + (t - Timer.time) / 1000000 + "ms)");
				v.update();
				System.out.println("CacheLine updated (" + ((Timer.time = System.nanoTime()) - t) / 1000000 + "ms) " + v);
			} else
				v.update();
		}
	}


}

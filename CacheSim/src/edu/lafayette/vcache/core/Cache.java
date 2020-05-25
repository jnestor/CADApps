package edu.lafayette.vcache.core;
import java.util.ArrayList;
import java.util.ListIterator;

import edu.lafayette.vcache.core.policy.setassocrepl.LRUReplacementPolicy;
import edu.lafayette.vcache.core.policy.setassocrepl.SetAssociativeReplacementPolicy;
import edu.lafayette.vcache.core.policy.write.WritePolicy;
import edu.lafayette.vcache.core.policy.write.WriteThrough;
import edu.lafayette.vcache.core.view.CacheView;
import edu.lafayette.vcache.util.Debug;
import edu.lafayette.vcache.util.Timer;

//import java.io.*;

/** This class implements the cache simulation                */
/*------------------------------------------------------------*/
/*                                                            */
/*   Address words look like this:                            */
/*                                                            */
/*   +----------+------------+------------+------------+      */
/*   |  tag     | line index | word index | byte index |      */
/*   +----------+------------+------------+------------+      */
/*                                                            */
/*------------------------------------------------------------*/
public class Cache {
	private int lineCount;  // number of lines in cache (must be pwr of 2
	private int associativeSetCount; //number of associative sets
	private int lineIndexBits;   // number of line bits in addr word (log2(lineCount))
	private int associativeSetIndexBits;
	private int lineWordCount; // number of words in each line
	private int wordIndexBits;
	private int byteIndexBits;
	private CacheLine lines [];
	private int refCount = 0;
	private int hitCount = 0;
	private int replaceCount = 0;
	private int writeCount = 0;
	private int readCount = 0;
	private ArrayList<CacheView> views;
	private CacheAccess currentAccess;
	private CacheAccess lastAccess;
	private SetAssociativeReplacementPolicy replacementPolicy;
	private WritePolicy writePolicy;
	private Updater updater;
	

	public Cache(int lc, int wc, WritePolicy writePolicy) {
		this(lc, wc, 1, writePolicy);
	}
	
	public Cache(int lc, int wc, int ac, WritePolicy writePolicy) {
		this(lc, wc, ac, writePolicy, null);
	}
	
	public Cache(int lc, int wc, int ac, WritePolicy writePolicy, SetAssociativeReplacementPolicy policy) {
		setLineCount(lc, ac);
		setWordCount(wc);
		byteIndexBits = 2; // change here for non-32-bit words
		views = new ArrayList<CacheView>();
		currentAccess = null;
		lastAccess = null;
		if (policy == null)
			replacementPolicy = new LRUReplacementPolicy();
		else
			replacementPolicy = policy;
		if (writePolicy == null)
			this.writePolicy = new WriteThrough();
		else
			this.writePolicy = writePolicy;
	}
	
	/** check if input is a power of 2.  Return log2 if it is; otherwise return -floor(log2) */
	public static int checkPower2(int n) {
		int nshift = n;
		int l2 = -1;
		if (nshift <= 0) return -1;
		while (nshift > 0) {
			nshift = nshift >> 1;
			l2++;
		}
		if ((1<<l2) != n) return -l2;
		else return l2;
	}

	public void setLineCount(int l, int a) {
		int l2 = checkPower2(l);
		int a2 = checkPower2(a);
		if (l2 >= 0) {
			associativeSetCount = a;
			associativeSetIndexBits = a2;
			lineCount = l;
			lineIndexBits = l2;
		} else {
			lineCount = (1<<(-l2));
			lineIndexBits = -l2;
			System.out.println("Cache.setLineCount(" + l + "): must be power of 2 - using: "
					+ lineCount);
		}
		lines = new CacheLine[lineCount * associativeSetCount];  // use lazy population to add lines
	}

	public int getLineCount() { return lineCount; }

	public int getLineIndexBits() { return lineIndexBits; }

	public void setWordCount(int w) {
		int w2 = checkPower2(w);
		if (w2 >= 0) {
			lineWordCount = w;
			wordIndexBits = w2;
		} else {
			lineWordCount = (1<<(-w2));
			wordIndexBits = -w2;
			System.out.println("Cache.setWordCount(" + w + "): must be power of 2 - using: "
					+ lineWordCount);
		}
	}
	
	public int getAssociativeSetCount() {
		return associativeSetCount;
	}

	public int getAssociativeSetIndexBits() {
		return associativeSetIndexBits;
	}

	public int getWordCount() { 
		return lineWordCount; 
	}

	public int getWordIndexBits() { 
		return wordIndexBits;
	}

	public int getByteIndexBits() { 
		return byteIndexBits; 
	}

	public CacheAccess getCurrentAccess() { 
		return currentAccess; 
	}

	public WritePolicy getWritePolicy() {
		return writePolicy;
	}

	public void setWritePolicy(WritePolicy writePolicy) {
		this.writePolicy = writePolicy;
	}
	
	void setUpdater(Updater updater) {
		this.updater = updater;
	}
	
	Updater getUpdater() {
		return updater;
	}

	public void newAccess(int type, int addr) {
		newAccess(new CacheAccess(this, type, addr));
	}
	
	public void newAccess(CacheAccess access) {
		lastAccess = currentAccess;
		currentAccess = access;
		if (lastAccess != null) {
//			CacheLine lastLine = getLine(lastAccess.getLineIndex(), lastAccess.getSetIndex());  // get the line from the LAST access to refresh after changing
//			lastLine.updateViews(); // update old line
			lastAccess.updateAllViews();
		}
		updateViews();
	}

	public int getTagBits() { 
		return 32 - lineIndexBits - wordIndexBits - 2; 
	}

	public CacheLine getLine(int i) {
		return getLine(i, 0);
	}
	
	public CacheLine getLine(int i, int set) {
		int lineInd = associativeSetCount * i + set;
		if (lines[lineInd] == null)
			lines[lineInd] = new CacheLine(this, i, set);
		return lines[lineInd];
	}

	public String toString() {
		return ("Cache - tag bits: " + (32-lineIndexBits-wordIndexBits-2) + " line index bits: "
				+ lineIndexBits + " word index bits: " + wordIndexBits + " byte index bits: 2");
	}

	public int getRefCount() { 
		return refCount; 
	}

	public int getHitCount() { 
		return hitCount; 
	}

	public int getReplaceCount() { 
		return replaceCount; 
	}
	
	public int getWriteCount() {
		return writeCount;
	}
	
	public int getReadCount() {
		return readCount;
	}

	public double getHitRate() {
		if (refCount == 0) return 0.0;
		else return (double)hitCount/(double)refCount * 100;
	}
	
	public SetAssociativeReplacementPolicy getReplacementPolicy() {
		return replacementPolicy;
	}

	/** clear the cache! */
	public void clearCache() {
		currentAccess = null;
		refCount = 0;
		hitCount = 0;
		for (int i = 0; i < lines.length; i++) {
			lines[i].clearLine();
			lines[i].updateViews();
		}
		updateViews();
	}
	
	public void writeToHigherLevel(CacheLine line) {
		writeCount++;
		System.out.println("Write to higher level. Count = " + writeCount);
		updater.write(line);
	}
	
	public void readFromHigherLevel(CacheLine line) {
		readCount++;
		System.out.println("Read from higher level. Count = " + readCount);
		updater.read(line);
	}

	public void incrHitCount() { hitCount++; }

	public void incrRefCount() { refCount++; }

	public void incrReplaceCount() { replaceCount++; }

	public void addView(CacheView cv) { views.add(cv); }
	
	/**
	 * Returns an empty copy of this cache, useful for reseting the cache.
	 */
	public Cache clone() {
		return new Cache(getLineCount(), getWordCount(),getAssociativeSetCount(), getWritePolicy(), getReplacementPolicy());
	}
	
	public void removeView(CacheView view) {
		views.remove(view);
	}

	public void updateViews() {
		ListIterator<CacheView> ci = views.listIterator();
		while (ci.hasNext()) {
			CacheView v = ci.next();
			if(v != null) {
				if (Debug.debug) {
					long t = System.nanoTime();
					System.out.println("Cache updating (time between = " + (t - Timer.time) / 1000000 + "ms)");
					v.update();
					System.out.println("Cache updated (" + ((Timer.time = System.nanoTime()) - t) / 1000000 + "ms) " + v);
				} else
					v.update();
			}
		}
	}

}

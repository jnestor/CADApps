package edu.lafayette.vcache.core;

import edu.lafayette.vcache.util.Debug;

/** Cache Access and associated methods */

public class CacheAccess {
	private Cache cache;
	private int addr, byteIndex, lineIndex, wordIndex, setIndex, tag;
	private boolean hit;
	private boolean replaced;
	private int status;
	private int type;
	public static final int INITIAL = 0;
//	public static final int LOOKUP = 1;
//	public static final int TAG_VALID_COMPARE = 2;
	public static final int LOOKUP_HIT = 1;
	public static final int LOOKUP_MISS = LOOKUP_HIT + 1;
	public static final int COMPLETE_HIT = LOOKUP_MISS + 1;
	public static final int COMPLETE_MISS = COMPLETE_HIT + 1;
	public static final int NONE = COMPLETE_MISS + 1;  // must explicitly set access address
	 // access types
    public static final int DATA_READ = 0;
    public static final int DATA_WRITE = 1;
    public static final int INSTR_FETCH = 2;
    public static final int MISC_ACCESS = 3;
    public static final int COPY_BACK = 4;
    public static final int INVALIDATE_BLOCK = 5;

    public CacheAccess(Cache c, int t, int a) {
    	cache = c;
    	status = NONE;
    	type = t;
    	addr = a;
    	hit = false;
    	replaced = false;
    	status = INITIAL;
    	if (c.getAssociativeSetCount() == 1)
    		setIndex = 0;
    	else
    		setIndex = -1;
    	calculateByteIndex();
    	calculateLineIndex();
    	calculateTag();
    	calculateWordIndex();
    }

	public Cache getCache() { 
		return cache; 
	}

	public int getAddress() { 
		return addr; 
	}

	public int getByteIndex() {
		return byteIndex;
	}

	public int getLineIndex() {
		return lineIndex;
	}

	public int getWordIndex() {
		return wordIndex;
	}

	public int getSetIndex() {
		return setIndex;
	}

	public int getTag() {
		return tag;
	}
	
	public boolean isWrite() {
		return type == D4Trace.DATA_WRITE;
	}

	private void calculateTag() {
		int lbits = cache.getLineIndexBits();
		int wbits = cache.getWordIndexBits();
		int bbits = cache.getByteIndexBits();
		tag = (addr >>> (lbits + wbits + bbits));  // no sign-extend on shift!
	}

	private void calculateWordIndex() {
		int wbits = cache.getWordIndexBits();
		int bbits = cache.getByteIndexBits();
		int mask = ~(0xffffffff << wbits);
		wordIndex = ((addr >>> bbits) & mask);
	}

	private void calculateLineIndex() {
		int lbits = cache.getLineIndexBits();
		int wbits = cache.getWordIndexBits();
		int bbits = cache.getByteIndexBits();
		int mask = ~(0xffffffff << lbits);

		lineIndex = ((addr >>> (wbits + bbits)) & mask);
	}

	private void calculateByteIndex() {
		int bbits = cache.getByteIndexBits();
		int mask = ~(0xffffffff << bbits);
		byteIndex = (addr & mask);
	}

	public int getStatus() { return status; }

	/** sequence through the states of the access; return true until complete */
	public boolean nextStep() {
		switch(status) {
		case INITIAL:
//		case LOOKUP:
//			status++;
//			updateAllViews();
//			return true;
//		case TAG_VALID_COMPARE:
			lookupAccess();
			return true;
		case LOOKUP_MISS:
			completeMiss();
			return true;
		case LOOKUP_HIT:
			completeHit();
			return true;
		case COMPLETE_MISS:
		case COMPLETE_HIT:
			return false;
			// do nothing - wait until state reset by setAccess()
		default:
			if (Debug.debug)
				System.out.println("CacheAccess.nextStep: unknown status: " + status);
		return false;
		}
	}

	/** evaluate the cache access */
	public boolean lookupAccess() {
		cache.incrRefCount();
		hit = false;
		status = LOOKUP_MISS;
		CacheLine[] cl = new CacheLine[cache.getAssociativeSetCount()];
		for (int setIndex = 0; setIndex < cache.getAssociativeSetCount(); setIndex++) {
			cl[setIndex] = cache.getLine(getLineIndex(), setIndex);
			if (cl[setIndex].tagMatch(getTag())) {
				hit = true;
				status = LOOKUP_HIT;
				this.setIndex = setIndex;
				cache.incrHitCount();
			} 
		}
		updateAllViews(getLineIndex());
		return hit;
	}

	public void completeMiss() {
		CacheLine cl = cache.getLine(getLineIndex()); 
		setIndex = 0;
		if (status == LOOKUP_MISS) {
			cl = cache.getLine(getLineIndex(), setIndex = cache.getReplacementPolicy().determineSet(cache, getLineIndex()));
			
			if (cl.getValid()) {
				cache.incrReplaceCount();
				replaced = true;
			}
			int baseAddr = addr & (0xffffffff << (cache.getWordIndexBits() + cache.getByteIndexBits()));
			if (isWrite())
				cache.getWritePolicy().write(cache, this, cl, getTag(), baseAddr);
			else
				cache.getWritePolicy().read(cache, this, cl, getTag(), baseAddr);
		}
		cl.setLastAccess(cache.getRefCount());
		status = COMPLETE_MISS;
		updateAllViews(getLineIndex());
	}

	public void completeHit() {
		CacheLine cl = cache.getLine(getLineIndex(), getSetIndex());
		if (isWrite())
			cache.getWritePolicy().write(cache, this, cl, getTag(), addr & (0xffffffff << (cache.getWordIndexBits() + cache.getByteIndexBits())));
		cl.setLastAccess(cache.getRefCount());
		status = COMPLETE_HIT;
		updateAllViews(getLineIndex());
	}
	
	public int getType() {
		return type;
	}
	
	public boolean getHitStatus() { return hit; }

	public boolean getReplacedStatus() { return replaced; }
	
	public String toString() {
		return "" + type + " " + Integer.toHexString(addr);
	}
	
	protected void reset() {
		status = INITIAL;
	}
	
	private void updateAllViews(int lineIndex) {
		CacheLine[] cl = new CacheLine[cache.getAssociativeSetCount()];
		for (int setIndex = 0; setIndex < cache.getAssociativeSetCount(); setIndex++) {
			cl[setIndex] = cache.getLine(getLineIndex(), setIndex);
		}
		updateAllViews(cl);
	}
	
	private void updateAllViews(CacheLine... lines) {
		for (CacheLine cl : lines) 
			cl.updateViews();
//		updateViews();
		cache.updateViews();
	}
	
	//overload with no args
	public void updateAllViews() {
		updateAllViews(getLineIndex());
	}
}

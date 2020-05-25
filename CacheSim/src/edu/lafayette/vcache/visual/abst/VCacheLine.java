package edu.lafayette.vcache.visual.abst;

import java.awt.Color;
import java.awt.Point;

import edu.lafayette.vcache.core.Cache;
import edu.lafayette.vcache.core.CacheAccess;
import edu.lafayette.vcache.core.CacheLine;
import edu.lafayette.vcache.core.Updater;
import edu.lafayette.vcache.core.policy.write.WriteBack;
import edu.lafayette.vcache.util.Debug;
import edu.lafayette.vcache.visual.VCacheAnimation;

public abstract class VCacheLine extends VPanel<CacheLine> implements Locatable {
	public static final int LINE_FIELD = 0, VALID_FIELD = 1, TAG_FIELD = 2, DIRTY_FIELD = 3, DATA_PANEL = 4, 
							DATA_FIELD = 10,
							NORMAL = 0x0, ACCESSED = 0x10, ILLEGAL = 0x20, HIT = 0x1, 
							LOOKUP = 0x40, 
							COMPLETE = 0x80, NOT_VALID = 0x2, WRONG_TAG = 0x4, REPLACED = 0x8;
	//when a certain field is not available, the getLocation returns this (particularly for the dirty field)
	public static final Point UNAVAILABLE = new Point(-1, -1);
	protected Updater animation;
	protected int position;
	
	public VCacheLine(Cache cache, Updater updater, int position, CacheLine... l) {
		super(cache, l);
		animation = updater;
		this.position = position;
	}
	
	public String getDataFieldContent(int index) {
		return String.format("M[%x]", updater.getBaseAddress() + index * 4);
	}
	
	protected abstract void setStatus(int status);
	
	public void update() { // set graphical and values based on state of cache line &current access
		CacheAccess ca = updater.getCurrentAccess();
		int status = 0;
		if (!updater.isOnLineOfCurrentAccess() ||
				(animation.getStatus() == CacheAccess.INITIAL) ||
				(animation.getStatus() == CacheAccess.NONE) || 
				(!updater.isCurrentAccess() && !(animation.getAccessStatus() == CacheAccess.LOOKUP_HIT || animation.getAccessStatus() == CacheAccess.LOOKUP_MISS))) {
			if (updater.getValid()) {
				status |= ACCESSED;
			} else { // not current / not valid - nothing to see here!
				status |= NORMAL;
			}
		} else { // isCurrentAccess && status != INITIAL
			setBackground(Color.YELLOW);  // highlight current access
			if ((animation.getAccessStatus() == CacheAccess.LOOKUP_HIT || animation.getAccessStatus() == CacheAccess.COMPLETE_HIT) && updater.isCurrentAccess()) {
				status |= HIT;
				if (animation.getAccessStatus() == CacheAccess.LOOKUP_HIT)
					status |= LOOKUP;
				else
					status |= COMPLETE;
			} else if (animation.getAccessStatus() == CacheAccess.LOOKUP_MISS || !updater.isCurrentAccess()) {  // show the missing fields
				status |= LOOKUP;
				if (!updater.getValid()) 
					status |= NOT_VALID;
				if (updater.getTag() != ca.getTag())
					status |= WRONG_TAG;
			} else if (animation.getAccessStatus() == CacheAccess.COMPLETE_MISS) {
				status |= COMPLETE;
				if (ca.getReplacedStatus())
					status |= REPLACED;
			} else {
				status |= ILLEGAL;
				if (Debug.debug)
					System.out.println("VCacheLine.update() - unexpected status: " + animation.getAccessStatus());
			}
		}
		setStatus(status);
	}
	
	public boolean isDirtyFieldSupported() {
		return cache.getWritePolicy() instanceof WriteBack;
	}
}

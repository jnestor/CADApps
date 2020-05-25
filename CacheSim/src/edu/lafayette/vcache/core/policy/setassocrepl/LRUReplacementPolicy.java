package edu.lafayette.vcache.core.policy.setassocrepl;

import edu.lafayette.vcache.core.Cache;
import edu.lafayette.vcache.core.CacheLine;

public class LRUReplacementPolicy implements SetAssociativeReplacementPolicy {

	public int determineSet(Cache cache, int lineIndex) {
		CacheLine cl = cache.getLine(lineIndex); 
		int setIndex = 0;
		for (int i = 1; i < cache.getAssociativeSetCount(); i++) {
			CacheLine line = cache.getLine(lineIndex, i);
			if (line.getLastAccess() < cl.getLastAccess()) {
				cl = line;
				setIndex = i;
			}
		}
		return setIndex;
	}

}

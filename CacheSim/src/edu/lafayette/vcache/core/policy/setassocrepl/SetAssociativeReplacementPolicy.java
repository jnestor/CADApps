package edu.lafayette.vcache.core.policy.setassocrepl;

import edu.lafayette.vcache.core.Cache;

public interface SetAssociativeReplacementPolicy {
	public int determineSet(Cache cache, int lineIndex);
}

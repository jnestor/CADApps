package edu.lafayette.vcache.core.policy.setassocrepl;

import edu.lafayette.vcache.core.Cache;

public class RoundRobinReplacementPolicy implements	SetAssociativeReplacementPolicy {
	private int[] cacheRotState;
	
	public RoundRobinReplacementPolicy(int cacheLines) {
		cacheRotState = new int[cacheLines];
	}

	public int determineSet(Cache cache, int lineIndex) {
		int set = cacheRotState[lineIndex];
		cacheRotState[lineIndex] = (cacheRotState[lineIndex] + 1) % cache.getAssociativeSetCount();
		return set;
	}

}

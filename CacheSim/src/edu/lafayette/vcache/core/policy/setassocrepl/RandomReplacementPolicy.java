package edu.lafayette.vcache.core.policy.setassocrepl;

import java.util.Random;

import edu.lafayette.vcache.core.Cache;

public class RandomReplacementPolicy implements SetAssociativeReplacementPolicy {
	private Random random;
	
	public RandomReplacementPolicy(long seed) {
		random = new Random(seed);
	}
	
	public RandomReplacementPolicy() {
		random = new Random();
	}
	
	public int determineSet(Cache cache, int lineIndex) {
		return random.nextInt(cache.getAssociativeSetCount());
	}

}

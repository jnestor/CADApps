package edu.lafayette.vcache.core.policy.write;

import edu.lafayette.vcache.core.Cache;
import edu.lafayette.vcache.core.CacheAccess;
import edu.lafayette.vcache.core.CacheLine;

public interface WritePolicy {
	void write(Cache cache, CacheAccess access, CacheLine line, int newTag, int newAddress);
	void read(Cache cache, CacheAccess access, CacheLine line, int newTag, int newAddress);
}

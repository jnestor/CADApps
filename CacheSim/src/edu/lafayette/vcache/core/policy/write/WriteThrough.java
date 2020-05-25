package edu.lafayette.vcache.core.policy.write;

import edu.lafayette.vcache.core.Cache;
import edu.lafayette.vcache.core.CacheAccess;
import edu.lafayette.vcache.core.CacheLine;

public class WriteThrough implements WritePolicy {

	public void write(Cache cache, CacheAccess access, CacheLine line, int newTag, int newAddress) {
		line.setLine(true, newTag, newAddress);
		cache.writeToHigherLevel(line);
	}

	public void read(Cache cache, CacheAccess access, CacheLine line, int newTag, int newAddress) {
		
		cache.readFromHigherLevel(line);
		line.setLine(true, newTag, newAddress);
	}

}

package edu.lafayette.vcache.core.policy.write;

import java.util.ArrayList;
import java.util.HashSet;

import edu.lafayette.vcache.core.Cache;
import edu.lafayette.vcache.core.CacheAccess;
import edu.lafayette.vcache.core.CacheLine;

public class WriteBack implements WritePolicy {
	ArrayList<HashSet<Integer>> dirtyList;
	
	public WriteBack(int cacheLines) {
		dirtyList = new ArrayList<HashSet<Integer>>(cacheLines);
		for (int i = 0; i < cacheLines; i++)
			dirtyList.add(new HashSet<Integer>());
	}
	
	public boolean isDirty(CacheLine line) {
		return dirtyList.get(line.getLineIndex()).contains(line.getAssociativeSetIndex());
	}

	public void write(Cache cache, CacheAccess access, CacheLine line, int newTag, int newAddress) {
		if (!line.tagMatch(newTag)) {
			if (dirtyList.get(line.getLineIndex()).contains(line.getAssociativeSetIndex())) { //if the old line is dirty, write back
				writeBack(cache, line);
			}
			line.setLine(true, newTag, newAddress);
		}
		dirtyList.get(line.getLineIndex()).add(line.getAssociativeSetIndex()); //would replace the data in this line if we actually stored the data
	}

	public void read(Cache cache, CacheAccess access, CacheLine line, int newTag, int newAddress) {
		if (!line.tagMatch(newTag)) {
			if (dirtyList.get(line.getLineIndex()).contains(line.getAssociativeSetIndex())) { //if the old line is dirty, write back	
				writeBack(cache, line);
			}
			cache.readFromHigherLevel(line);
			line.setLine(true, newTag, newAddress);
		}	
	}
	
	private void writeBack(Cache cache, CacheLine line) {
		dirtyList.get(line.getLineIndex()).remove(line.getAssociativeSetIndex());
		cache.writeToHigherLevel(line);
	}

}

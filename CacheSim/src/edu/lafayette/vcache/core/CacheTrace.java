package edu.lafayette.vcache.core;

import java.io.InputStream;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

import edu.lafayette.vcache.util.Debug;

public class CacheTrace<T extends List<CacheAccess>> {
	private static final int ENSURED_SCANNED = 50, SCAN_NUMBER = 500;
	private int traceFormat;
	private T accessList;
	private int index, updatedIndex, nextAccessIndex;
	private Cache cache;
	private Scanner scanner;
	private boolean traceModified;
	
	public CacheTrace(Cache cache, T accessList) {
		this.accessList = accessList;
		this.cache = cache;
		reset();
	}
	
	public boolean hasMoreAccess() {
		return hasMoreAccess(nextAccessIndex);
	}
	
	public boolean hasMoreAccess(int index) {
		if (hasMoreAccessInList(index)) 
			return true;
		advanceScanned(index);
		return hasMoreAccessInList(index);
	}
	
	private boolean hasMoreAccessInList(int index) {
		return accessList.size() > index && updatedIndex >= index;
	}
	
	public void nextAccess() {
		if (!hasMoreAccessInList(nextAccessIndex + ENSURED_SCANNED))
			advanceScanned(nextAccessIndex + SCAN_NUMBER);
		if (!hasMoreAccessInList(nextAccessIndex))
			return;
		if (accessList.get(index = nextAccessIndex).getStatus() != CacheAccess.INITIAL)
			accessList.get(index).reset();
		cache.newAccess(accessList.get(index));
		nextAccessIndex++;
	}
	
	public void reset() {
		accessList.clear();
		nextAccessIndex = 0;
		index = -1;
		updatedIndex = -1;
	}

	public void setTrace(String traceStr) {
		scanner = new Scanner(traceStr);
		setTrace();
	}
	
	public void setTrace(InputStream in) {
		scanner = new Scanner(in);
		setTrace();
	}
	
	private void setTrace() {
		reset();
		advanceScanned(SCAN_NUMBER);
	}
	
	public List<CacheAccess> getTrace(int startIndex, int endIndex) {
		if (accessList == null)
			return null;
		advanceScanned(endIndex);
		return accessList.subList(startIndex, endIndex);
	}
	
	public T getTrace() {
		return accessList;
	}
	
	public Object[] getTraceArray() {
		return accessList.subList(index > 100 ? index - 100 : 0, updatedIndex).toArray();
	}

	public int getTraceFormat() {
		return traceFormat;
	}

	public void setTraceFormat(int traceFormat) {
		this.traceFormat = traceFormat;
	}

	public synchronized List<CacheAccess> getAccessList() {
		return accessList;
	}
	
	public synchronized int getCurrentIndex() {
		if (Debug.debug)
			System.out.println("CacheTrace getCurrentIndex " + index);
		return index;
	}
	
	public int getNextAccessIndex() {
		return nextAccessIndex;
	}
	
	public void setNextAccessIndex(int nextAccessIndex) {
		this.nextAccessIndex = nextAccessIndex;
	}
	
	public boolean isTraceModified() {
		if (traceModified) {
			traceModified = false;
			return true;
		}
		return false;
	}
	
	private void advanceScanned(int targetIndex) {
		while (updatedIndex < targetIndex) {
			if (scanner == null || !scanner.hasNextLine())
				return;
			try {
				accessList.add(new CacheAccess(cache, scanner.nextInt(), Integer.valueOf(scanner.next(), 16)));
				updatedIndex++;
				traceModified = true;
			} catch (NoSuchElementException e) {
				return;
			}
		}
	}
}

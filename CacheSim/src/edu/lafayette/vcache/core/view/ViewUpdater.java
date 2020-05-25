package edu.lafayette.vcache.core.view;

public interface ViewUpdater {
	public void addView(CacheView view, int level);
	public void removeView(CacheView view);
}

package edu.lafayette.vcache.visual.abst;

import java.awt.Point;

public interface Locatable {
	public Point getLocation(int component);
	public Point getLocation();
	public int getWidth(int component);
	public int getHeight(int component);
}

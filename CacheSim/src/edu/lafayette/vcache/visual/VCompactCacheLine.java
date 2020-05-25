package edu.lafayette.vcache.visual;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;

import edu.lafayette.vcache.core.Cache;
import edu.lafayette.vcache.core.CacheLine;
import edu.lafayette.vcache.core.Updater;
import edu.lafayette.vcache.core.policy.write.WriteBack;
import edu.lafayette.vcache.visual.abst.VCacheLine;

public class VCompactCacheLine extends VCacheLine {
	private static final Color 	BORDER = new Color(0, 0, 255), 
								INNER_BORDER = new Color(150, 150, 255),
								BIT_0 = new Color(200, 200, 100),
								BIT_1 = new Color(150, 150, 100),
								ACTIVE = new Color(50, 50, 150),
								VALID = new Color(20, 15, 15);
	private static final int WORD_WIDTH = 16;
	private int vLoc, tLoc, dirtyLoc, dLoc, pixels, width, vWidth, tWidth, dirtyWidth, dWidth;
	
	//pixels is with how many pixels we are going to represent one bit (pixel X pixel square)
	public VCompactCacheLine(Cache c, Updater updater, int position, int pixels, CacheLine... l) {
		super(c, updater, position, position == -1 ? null : l);
		vLoc = 1;
		vWidth = pixels;
		tLoc = vLoc + vWidth + 1;
		tWidth = l[0].getOwner().getTagBits() * pixels;
		dirtyLoc = tLoc + tWidth + 1;
		dirtyWidth = c.getWritePolicy() instanceof WriteBack ? pixels : -1;
		dLoc = dirtyLoc + dirtyWidth + 1;
		dWidth = WORD_WIDTH * pixels * l[0].getOwner().getWordCount() + l[0].getOwner().getWordCount() - 1;
		this.pixels = pixels;
		setPreferredSize(new Dimension(width = dLoc + dWidth + 1, position == -1 ? 1 : pixels));
	}
	
	//creates a dummy line, for the top and the bottom borders.
	public VCompactCacheLine(Cache c, int pixels, CacheLine l) {
		this(c, null, -1, pixels, l);
	}
	
	public void paint(Graphics g) {
		
		if (updater == null) {
			g.setColor(BORDER);
			g.drawLine(0, 0, width, 0);
			return;
		}
		g.setColor(BORDER);
		g.fillRect(0, 0, 1, pixels);
		g.fillRect(vLoc + pixels, 0, 1, pixels);
		if (cache.getWritePolicy() instanceof WriteBack)
			g.fillRect(dirtyLoc - 1, 0, 1, pixels);
		g.fillRect(dLoc - 1, 0, 1, pixels);
		g.setColor(INNER_BORDER);
		for (int i = 1; i < updater.getOwner().getWordCount(); i++)
			g.fillRect(dLoc + i * pixels * WORD_WIDTH + i - 1, 0, 1, pixels);
		g.setColor(BORDER);
		g.fillRect(dLoc + WORD_WIDTH * pixels * updater.getOwner().getWordCount() + updater.getOwner().getWordCount() - 1, 0, 1, pixels);
		paintData(g);
	}

	private void paintData(Graphics g) {
		int dirty = -1;
		if (cache.getWritePolicy() instanceof WriteBack) {
			dirty = ((WriteBack) cache.getWritePolicy()).isDirty(updater) ? 1 : 0;
		}
		setToolTipText("L: " + position + ", V: " + (updater.getValid() ? 1 : 0) + ", T: " + updater.getTag()
				+ (dirty == -1 ? "" : ", D: " + dirty));
		Color b1 = getModifiedColor(BIT_1);
		Color b0 = getModifiedColor(BIT_0);
		if (updater.getValid())
			g.setColor(b1);
		else
			g.setColor(b0);
		g.fillRect(vLoc, 0, pixels, pixels);
		paintDataBits(g, updater.getOwner().getTagBits(), updater.getTag(), tLoc);
		if (dirty != -1)
			paintDataBits(g, 1, dirty, dirtyLoc);
		for (int i = 0; i < updater.getOwner().getWordCount(); i++)
			paintDataBits(g, WORD_WIDTH, 0, dLoc + WORD_WIDTH * pixels * i + i); 
	}
	
	private void paintDataBits(Graphics g, int bitNumber, int value, int startX) {
		Color b1 = getModifiedColor(BIT_1);
		Color b0 = getModifiedColor(BIT_0);
		for (int i = 0, j = 1 << (bitNumber - 1); i < bitNumber; i++, j >>>= 1) {
			if ((value & j) > 0)
				g.setColor(b1);
			else
				g.setColor(b0);
			g.fillRect(startX + i * pixels, 0, pixels, pixels);
		}
	}
	
	private Color getModifiedColor(Color c) {
		return updater.getCurrentAccess() != null && updater.getCurrentAccess().getLineIndex() == position ? addColors(c, ACTIVE) : (updater.getValid() ? addColors(c, VALID) : c);
		//return updater.getCurrentAccess() != null && updater.getCurrentAccess().getLineIndex() == position ? subtractColors(VColor.ACTIVE, c) : (updater.getValid() ? (updater.)) : subtractColors(VColor.NOT_VALID, c));
	}
	
	private static Color addColors(Color c1, Color c2) {
		int r = c1.getRed() + c2.getRed();
		int g = c1.getGreen() + c2.getGreen();
		int b = c1.getBlue() + c2.getBlue();
		return new Color(r > 255 ? 255 : r, g > 255 ? 255 : g, b > 255 ? 255 : b);
	}
	
	//c1 - c2
	private static Color subtractColors(Color c1, Color c2) {
		int r = c1.getRed() - c2.getRed();
		int g = c1.getGreen() - c2.getGreen();
		int b = c1.getBlue() - c2.getBlue();
		return new Color(r >= 0 ? r : 0, g >= 0 ? g : 0, b >= 0 ? b : 0);
	}
	
	public void update() {
		repaint();
	}

	public int getHeight(int component) {
		return pixels;
	}

	public Point getLocation(int component) {
		if (component == VALID_FIELD)
			return new Point(vLoc, 0);
		if (component == TAG_FIELD)
			return new Point(tLoc, 0);
		if (component == DIRTY_FIELD)
			return cache.getWritePolicy() instanceof WriteBack ? new Point(dirtyLoc, 0) : UNAVAILABLE;
		if (component == DATA_PANEL)
			return new Point(dLoc, 0);
		if (component >= DATA_FIELD)
			return new Point(WORD_WIDTH * (component - DATA_FIELD) * pixels + (component - DATA_FIELD), 0);
		return new Point(0, 0);
	}

	public int getWidth(int component) {
		if (component == VALID_FIELD)
			return vWidth;
		if (component == TAG_FIELD)
			return tWidth;
		if (component == DIRTY_FIELD)
			return dirtyWidth;
		if (component == DATA_PANEL)
			return dWidth;
		if (component >= DATA_FIELD)
			return WORD_WIDTH * pixels;
		return 0;
	}

	@Override
	protected void setStatus(int status) {
		// TODO Auto-generated method stub
		
	}
}

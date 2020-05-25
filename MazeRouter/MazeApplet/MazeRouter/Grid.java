package MazeRouter;

import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;


// Grid - panel to display grid for maze routing
//
// Layout:
//		+--------------------------+
//		| Layer 1                  |
//		+--------------------------+
//		|   |   |  |   |   |   |   |
//		+--------------------------+
//		|   |   |  |   |   |   |   |
//		+--------------------------+
//		| Layer 2                  |
//		+--------------------------+
//		|   |   |  |   |   |   |   |
//		+--------------------------+
//		|   |   |  |   |   |   |   |
//		+--------------------------+
//			(additional layers)
//		+--------------------------+
//		| Message Area             |
//		+--------------------------+
//
// 4/16/20: need to convert to JPanel, use paintComponent instead of paintComponent
// also need to figure out what I was doing with double-buffering ???
//
class Grid extends Panel {

  private String msg = null;

  private boolean displayParallelMode = false;

  public void setParallelExpand() { displayParallelMode = true; }
  public void setSerialExpand() { displayParallelMode = false; }

  Image myOffScreenImage = null;
  Graphics myOffScreenGraphics = null;

  public Grid(int w, int h, int d) {
    gridArray = new GridPoint[w][h][d];
    for (int i = 0; i < w; i++ )
      for (int j = 0; j < h; j++)
        for (int k = 0; k < d; k++)
          gridArray[i][j][k] = new GridPoint(i,j,k);

    GridPoint.myGrid = this;
    setSize(pixelWidth(), pixelHeight());
    addMouseListener(new MouseAdapter()
    { public void mouseClicked(MouseEvent m) {
        //System.out.println("mouseEvent: " + m);
        handleMouseClick(m.getX(), m.getY());
      }
    } );

  }

  public void setMessage(String s) {
    msg = s;
    redrawGrid();
  }

  public void flash(GridPoint gp) throws InterruptedException {
    for (int i=0;i<3;i++) {
      gp.highlight(true);
      redrawGrid();
      gridDelay(4);
      gp.highlight(false);
      redrawGrid();
      gridDelay(4);
    }
  }

  public void redrawGrid() {
    if (myOffScreenImage == null) { // this doesn't work in constructor
      myOffScreenImage = createImage(getSize().width, getSize().height );
      myOffScreenGraphics = myOffScreenImage.getGraphics();
     }
    repaint();
  }

  public int width() { return gridArray.length; }

  public int height() { return gridArray[0].length; }

  public int depth() { return gridArray[0][0].length; }

  public int pixelWidth() { return width() * Grid.GRIDSIZE; }

  public int pixelHeight() { return depth() * (height() + 1) * Grid.GRIDSIZE; }

  public static int calculateCols(int pixelWidth) { return (pixelWidth - 2) / GRIDSIZE; }

  public static int calculateRows(int pixelHeight, int nLayers) {
    int rawCols = ((pixelHeight - 30) / GRIDSIZE) - 2; // adjust for msg, clr btn
    return rawCols / nLayers - 1; // adjust  -1 for layer labels
  }

  public GridPoint gridPointAt(int x, int y, int z) {
    if (x < 0 || x >= width() ) return null;
    if (y < 0 || y >= height() ) return null;
    if (z < 0 || z >= depth() ) return null;
    return gridArray[x][y][z];
  }

  public void reset() {
    for (int i = 0; i < width(); i++ )
      for (int j = 0; j < height(); j++)
        for (int k = 0; k < depth(); k++) {
          GridPoint gp = gridPointAt(i,j,k);
          if (!gp.isObstacle()) gp.reset();
        }
  }

  public void clear() {
    for (int i = 0; i < width(); i++ )
      for (int j = 0; j < height(); j++)
        for (int k = 0; k < depth(); k++) {
          GridPoint gp = gridPointAt(i,j,k);
          gp.reset();
        }
  }
    private class GridLink {
      public GridLink(GridPoint gp) {
        wot = gp;
        next = null;
      }
      private GridLink next;
      private GridPoint wot;
    }

  private GridLink gridPointHead;
  private GridLink gridPointTail;

  void printGridPointQueue() { // for debugging - package visible
    for (GridLink gl = gridPointHead; gl != null; gl = gl.next) {
      System.out.print(gl.wot + " ");
    }
    System.out.println();
  }

  public void enqueueGridPoint(GridPoint gp) throws InterruptedException {
    // if (gp.isEnqueued()) return;  // already there!
    if (gridPointHead == null) gridPointHead = gridPointTail = new GridLink(gp);
    else {
      GridLink gl = new GridLink(gp);
      gridPointTail.next = gl;
      gridPointTail = gl;
    }
    gp.setEnqueued(true);
   if (!displayParallelMode) {
      redrawGrid();
      gridDelay();
    }
  }

  public GridPoint dequeueGridPoint() {
    if (gridPointHead == null) return null;
    else {
      GridPoint gp = gridPointHead.wot;
      gridPointHead = gridPointHead.next;
      gp.setEnqueued(false);
      // debug
//      System.out.println("GridPoint.dequeuePoint - " + gp);
      return gp;
    }
  }

  public void clearQueue() {
    gridPointHead = null;
  }

  private static final int DELAY = 100; // 10ms = 0.1s

  public static void gridDelay(int d) throws InterruptedException {
      Thread.sleep(d * DELAY);
  }

  public static void gridDelay() throws InterruptedException {
    gridDelay(1);
  }

  public int expansion() throws InterruptedException {
    GridPoint gp;
    int actualLength;
    int curVal = 0;
    setMessage("Expansion phase");
    gridDelay(3);
    if (src != null && tgt != null ) {
      src.initExpand();
      if ((actualLength = src.expand()) > 0) {
        clearQueue();
        return actualLength; // found it right away!
      }
      while ((gp = dequeueGridPoint()) != null) {
        if (displayParallelMode && (gp.getVal() > curVal)) {
          curVal = gp.getVal();
          redrawGrid();
          gridDelay(2);
        }
        if ((actualLength = gp.expand()) > 0) {
          clearQueue();
          return actualLength;  // found it!
        }
      }
    }
    return -1;
  }

  public void traceBack() throws InterruptedException {
    // start at target, then work back
    GridPoint current = tgt;
    while (!current.isSource()) {
      GridPoint next;
      int curval = current.getVal();
      setMessage("Traceback: distance = " + curval);
      current.setRouted();
      redrawGrid();
      gridDelay(3);
      next = current.westNeighbor();
      if (next != null && !next.isObstacle() && next.getVal() < curval) {
        current = next;
        continue;
      }
      next = current.eastNeighbor();
      if (next != null && !next.isObstacle() && next.getVal() < curval) {
        current = next;
        continue;
      }
      next = current.southNeighbor();
      if (next != null && !next.isObstacle() && next.getVal() < curval) {
        current = next;
        continue;
      }
      next = current.northNeighbor();
      if (next != null && !next.isObstacle() && next.getVal() < curval) {
        current = next;
        continue;
      }
      next = current.upNeighbor();
      if (next != null && !next.isObstacle() && next.getVal() < curval) {
        current = next;
        continue;
      }
      next = current.downNeighbor();
      if (next != null && !next.isObstacle() && next.getVal() < curval) {
        current = next;
        continue;
      }
      System.out.println("AWK! can't trace back! current= " + current);
      break;
    }
    if (current.isSource()) {
      setMessage("Traceback complete");
      flash(current);
      current.setRouted();
    } else System.out.println("Warning: traceBack failed!");
  }

   // the following are used in drawing into the grid panel and are package visible

  static final int GRIDSIZE = 19;
  static final int CHARXOFFSET = 6;
  static final int CHARYOFFSET = 14;

  int gridPanelX(int i, int j, int k) { return i * GRIDSIZE;}

  int gridPanelY(int i, int j, int k) {
    return(k * (GRIDSIZE * (height() + 1) )) + ((j+1) * GRIDSIZE);
  }

  int gridLyrY(int layer) { return (GRIDSIZE * layer * (height()+1) + CHARYOFFSET); }

  int gridMsgY() { return (GRIDSIZE * depth() * (height() + 1)) + CHARYOFFSET; }

  private GridPoint clickedPoint = null;
  private boolean clearPending = false;

  public synchronized void handleMouseClick(int x, int y) {
    clickedPoint = mouseToGridPoint(x, y);
    notifyAll();
    //System.out.println("handleMouseClick: " + clickedPoint);
  }

  public synchronized void requestClear() {
    clearPending = true;
    notifyAll();
  }

  GridPoint mouseToGridPoint(int x, int y) {
    int i, j, k;
    i = x / GRIDSIZE;
    for (k = 0; k < depth(); k++) {
      if (y >= gridPanelY(0,0,k) && y <= gridPanelY(0,0,k+1)-GRIDSIZE) {
        j = (y % ((height() + 1) * GRIDSIZE) / GRIDSIZE) - 1;
        //System.out.println("mouseToGridPoint: found GridPoint at ("
         //    + i + "," + j + "," + k + ") " + gridPointAt(i,j,k));
        if (i >= 0 && i < width() && j >= 0 && j < height() && k >= 0 && k < depth())
          return gridPointAt(i,j,k);
        else return null;
      }
    }
    return null;
  }


  private static final int WAITFORSRC = 0;
  private static final int WAITFORTGT = 1;

  private synchronized void waitForInput() throws InterruptedException {
    while (!clearPending && clickedPoint == null) wait();
  }

  public void run() throws InterruptedException {
    clear();
    int state = WAITFORSRC;
    setMessage("Click on Source");
    while (true) {
      waitForInput();
      if (state == WAITFORSRC) {
        if (clearPending) {
          clear();
          setMessage("Grid cleared!");
          gridDelay(5);
          setMessage("Click on Source");
          clearPending = false;
        } else if (clickedPoint != null) {
          if (!clickedPoint.isRouted()) {
            setSource(clickedPoint);
            System.out.println("Setting source: " + clickedPoint);
            state = WAITFORTGT;
            setMessage("Click on Target");
          } else {
            setMessage("Already routed!");
            gridDelay(5);
            setMessage("Click on Source");
          }
          clickedPoint = null;
        }
      } else if (state == WAITFORTGT) {
        if (clickedPoint != null) {
          setTarget(clickedPoint);
          System.out.println("Setting target: " + clickedPoint);
          clickedPoint = null;
          setMessage("Ready to Route!");
          gridDelay(5);
          redrawGrid();
          route();
          src = null;
          tgt = null;
          state = WAITFORSRC;
          setMessage("Click on Source");
          redrawGrid();
        }
        clearPending = false;
      }
    }
  }

  public void paint(Graphics g) {
    g.setColor(getBackground());
    g.fillRect(0,0,getSize().width,getSize().height);
    for (int k = 0; k < depth(); k++ ) {
      g.setColor(Color.black);
      g.drawString("Layer " + (k+1), CHARXOFFSET, gridLyrY(k));
      for (int j = 0; j < height(); j++)
        for (int i = 0; i < width(); i++)
          gridArray[i][j][k].paintGridPoint(g);
    }
    if (msg != null) {
      g.setColor(Color.black);
      g.drawString(msg, CHARXOFFSET, gridMsgY());
    }
  }

  public void update(Graphics g) {
    paint(myOffScreenGraphics);
    g.drawImage(myOffScreenImage,0,0,this);
  }

  public void setSource(int x, int y, int z) {
    setSource(gridArray[x][y][z]);
  }

  public void setSource(GridPoint s) {
    src = s;
  }

  public void setTarget(int x, int y, int z) {
    tgt = gridArray[x][y][z];
  }

  public void setTarget(GridPoint t) { tgt = t; }

  public GridPoint getSource() { return src; }

  public GridPoint getTarget() { return tgt; }

  public int route() throws InterruptedException {
    if (src == null || tgt == null) return -1;
    GridPoint.nextRouteColor();
    reset();
    if (src == tgt) {  // trivial case
      src.setRouted();
      return 0;
    } else {
      int actualLength = expansion();
      clearQueue();
      redrawGrid();
      if (actualLength > 0) {
        setMessage("Target Found!");
        flash(tgt);
        traceBack();
      } else setMessage("Target not found!");
      reset();
      redrawGrid();
      return actualLength;
    }
  }

  public int route(GridPoint s, GridPoint t) throws InterruptedException {
    setSource(s);
    setTarget(t);
    return route();
  }

  public int route(int sx, int sy, int sz, int dx, int dy, int dz) throws InterruptedException {
    return route(gridPointAt(sx,sy,sz),gridPointAt(dx,dy,dz));
  }

  GridPoint [][][] gridArray;
  GridPoint src;
  GridPoint tgt;

}


import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

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
class Grid extends JPanel {
    
    

    private String msg = null;

    private final Router router = new Router(this);

    private boolean displayParallelMode = false;

    public boolean getMode() {
        return displayParallelMode;
    }

    public void setParallelExpand() {
        displayParallelMode = true;
    }

    public void setSerialExpand() {
        displayParallelMode = false;
    }

    Image myOffScreenImage = null;
    Graphics myOffScreenGraphics = null;

    public Grid(int w, int h, int d) {
        gridArray = new GridPoint[w][h][d];
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                for (int k = 0; k < d; k++) {
                    gridArray[i][j][k] = new GridPoint(i, j, k);
                }
            }
        }

        GridPoint.myGrid = this;
        setSize(pixelWidth(), pixelHeight());
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent m) {
                //System.out.println("mouseEvent: " + m);
                handleMouseClick(m.getX(), m.getY());
            }
        });

    }

    public void setMessage(String s) {
        msg = s;
        redrawGrid();
    }

    public void flash(GridPoint gp) throws InterruptedException {
        for (int i = 0; i < 3; i++) {
            gp.highlight(true);
            redrawGrid();
            gridDelay(4);
            gp.highlight(false);
            redrawGrid();
            gridDelay(4);
        }
    }

    public void redrawGrid() {
        /*if (myOffScreenImage == null) { // this doesn't work in constructor
      myOffScreenImage = createImage(getSize().width, getSize().height );
      myOffScreenGraphics = myOffScreenImage.getGraphics();
     }*/
        repaint();
    }

    public int width() {
        return gridArray.length;
    }

    public int height() {
        return gridArray[0].length;
    }

    public int depth() {
        return gridArray[0][0].length;
    }

    public int pixelWidth() {
        return width() * Grid.GRIDSIZE;
    }

    public int pixelHeight() {
        return depth() * (height() + 1) * Grid.GRIDSIZE;
    }

    public static int calculateCols(int pixelWidth) {
        return (pixelWidth - 2) / GRIDSIZE;
    }

    public static int calculateRows(int pixelHeight, int nLayers) {
        int rawCols = ((pixelHeight - 30) / GRIDSIZE) - 2; // adjust for msg, clr btn
        return rawCols / nLayers - 1; // adjust  -1 for layer labels
    }

    public GridPoint gridPointAt(int x, int y, int z) {
        if (x < 0 || x >= width()) {
            return null;
        }
        if (y < 0 || y >= height()) {
            return null;
        }
        if (z < 0 || z >= depth()) {
            return null;
        }
        return gridArray[x][y][z];
    }

    public void reset() {
        paused = false;
        for (int i = 0; i < width(); i++) {
            for (int j = 0; j < height(); j++) {
                for (int k = 0; k < depth(); k++) {
                    GridPoint gp = gridPointAt(i, j, k);
                    if (!gp.isObstacle()) {
                        gp.reset();
                    }
                }
            }
        }
    }

    public void clear() {
        for (int i = 0; i < width(); i++) {
            for (int j = 0; j < height(); j++) {
                for (int k = 0; k < depth(); k++) {
                    GridPoint gp = gridPointAt(i, j, k);
                    gp.reset();
                }
            }
        }
    }

    private final Queue<GridPoint> gridPointList = new LinkedList<GridPoint>();
    private GridPoint gridPointTail;

    public GridPoint getTail() {
        return gridPointTail;
    }

    void printGridPointQueue() { // for debugging - package visible
        System.out.println(gridPointList.toString());
    }

    public void enqueueGridPoint(GridPoint gp) throws InterruptedException {
        gridPointList.add(gp);
        gp.setEnqueued(true);
        gridPointTail = gp;
        if (!displayParallelMode) {
            redrawGrid();
            gridDelay();
        }
    }

    public GridPoint dequeueGridPoint() {
        GridPoint gp = gridPointList.poll();
        if (gp == null) {
            return null;
        } else {
            gp.setEnqueued(false);
            // debug
//      System.out.println("GridPoint.dequeuePoint - " + gp);
            return gp;
        }
    }

    public void clearQueue() {
        gridPointList.clear();
    }

    private static final int DELAY = 100; // 10ms = 0.1s

    public static void gridDelay(int d) throws InterruptedException {
        Thread.sleep(d * DELAY);
    }

    public static void gridDelay() throws InterruptedException {
        gridDelay(1);
    }
    private boolean paused = false;
    
    public void stopRouter(){
        router.stop();
    }
    
    public boolean pauseResume() {
        paused = !paused;
        if (paused) {
            msg += " Pause";
        } else {
            msg = msg.substring(0, msg.length() - 6);
            synchronized (router) {
                router.notify();
            }
        }
        return paused;
    }

    public boolean isPaused() {
        return paused;
    }

    // the following are used in drawing into the grid panel and are package visible
    static final int GRIDSIZE = 19;
    static final int CHARXOFFSET = 6;
    static final int CHARYOFFSET = 14;

    int gridPanelX(int i, int j, int k) {
        return i * GRIDSIZE;
    }

    int gridPanelY(int i, int j, int k) {
        return (k * (GRIDSIZE * (height() + 1))) + ((j + 1) * GRIDSIZE);
    }

    int gridLyrY(int layer) {
        return (GRIDSIZE * layer * (height() + 1) + CHARYOFFSET);
    }

    int gridMsgY() {
        return (GRIDSIZE * depth() * (height() + 1)) + CHARYOFFSET;
    }

    private GridPoint clickedPoint = null;
    private boolean clearPending = false;

    public synchronized void handleMouseClick(int x, int y) {
        if (!paused) {
            clickedPoint = mouseToGridPoint(x, y);
            notifyAll();
            //System.out.println("handleMouseClick: " + clickedPoint);
        }
    }

    public synchronized void requestClear() {
        clearPending = true;
        notifyAll();
    }

    GridPoint mouseToGridPoint(int x, int y) {
        int i, j, k;
        i = x / GRIDSIZE;
        for (k = 0; k < depth(); k++) {
            if (y >= gridPanelY(0, 0, k) && y <= gridPanelY(0, 0, k + 1) - GRIDSIZE) {
                j = (y % ((height() + 1) * GRIDSIZE) / GRIDSIZE) - 1;
                //System.out.println("mouseToGridPoint: found GridPoint at ("
                //    + i + "," + j + "," + k + ") " + gridPointAt(i,j,k));
                if (i >= 0 && i < width() && j >= 0 && j < height() && k >= 0 && k < depth()) {
                    return gridPointAt(i, j, k);
                } else {
                    return null;
                }
            }
        }
        return null;
    }

    private static final int WAITFORSRC = 0;
    private static final int WAITFORTGT = 1;
    
    
    private int state = 0;
    
    public void setState(int i){
        state = i;
    }
    
    public int getState(){
        return state;
    }

    private synchronized void waitForInput() throws InterruptedException {
        while (!clearPending && clickedPoint == null) {
            wait();
        }
    }

    public void run() throws InterruptedException {
        clear();
        state = WAITFORSRC;
        System.out.println(state);
        setMessage("Click on Source");
        while (true) {
            waitForInput();
            if (state == WAITFORSRC) {
                System.out.println("Looking for source");
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
                    router.route();              
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

    @Override
    public void paintComponent(Graphics g) {
        g.setColor(getBackground());
        g.fillRect(0, 0, getSize().width, getSize().height);
        for (int k = 0; k < depth(); k++) {
            g.setColor(Color.black);
            g.drawString("Layer " + (k + 1), CHARXOFFSET, gridLyrY(k));
            for (int j = 0; j < height(); j++) {
                for (int i = 0; i < width(); i++) {
                    gridArray[i][j][k].paintGridPoint(g);
                }
            }

        }
    }

    public String getMSG() {
        return msg;
    }

    /* public void update(Graphics g) {
    paint(myOffScreenGraphics);
    g.drawImage(myOffScreenImage,0,0,this);
  }*/
    public void setSource(int x, int y, int z) {
        setSource(gridArray[x][y][z]);
    }

    public void setSource(GridPoint s) {
        src = s;
    }

    public void setTarget(int x, int y, int z) {
        tgt = gridArray[x][y][z];
    }

    public void setTarget(GridPoint t) {
        tgt = t;
    }

    public GridPoint getSource() {
        return src;
    }

    public GridPoint getTarget() {
        return tgt;
    }

    GridPoint[][][] gridArray;
    private GridPoint src;
    private GridPoint tgt;

    public GridPoint getTGT() {
        return tgt;
    }

    public GridPoint getSRC() {
        return src;
    }

    public Router getRouter() {
        return router;
    }
}

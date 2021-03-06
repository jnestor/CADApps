
/**
 * @Author: John Nestor <nestorj>
 * @Date: 2020-06-24T21:04:11-04:00
 * @Email: nestorj@lafayette.edu
 * @Last modified by: nestorj
 * @Last modified time: 2020-06-24T21:04:11-04:00
 */
import java.awt.BasicStroke;
import java.awt.Point;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import javax.swing.JFrame;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;

/**
 * STHananGraph extends UIGraph by adding "Hanan Mode", in which it displays the
 * Hanan Grid, and the editing operations are changed to create/delete Steiner
 * points on the Hanan Grid
 */
public class UIHananGraph extends UIGraph implements MouseListener, MouseMotionListener {

    private boolean hananMode = false;
    private STNEPair selectedNEPair = null;
    private Color c = Color.gray;

    private boolean changable = true;

    public UIHananGraph(STGraph g, UIGraphChangeListener cl) {
        super(g, cl);
    }

    public UIHananGraph(STGraph g) {
        this(g, null);
    }

    public void setHananMode(boolean m) {
        hananMode = m;
    }

    public boolean getHananMode() {
        return hananMode;
    }

    public void selectNEPair(STNEPair ne) {
        selectedNEPair = ne;
    }

    /*----------------------------------------------------------------------*/
 /*           MouseListener Methods                                      */
 /*----------------------------------------------------------------------*/
    @Override
    public void mouseClicked(MouseEvent e) {
    }

    /**
     * mousePressed - if pressed over a node, select for dragging - otherwise,
     * add node
     */
    @Override
    public void mousePressed(MouseEvent e) {
        if (!hananMode && changable) {
            super.mousePressed(e);
        } else {
            Point mouseLoc = e.getPoint();
            selNode = gr.findNode(mouseLoc, TERM_SIZE); 	    // see if mouse is over an existing terminal
            if (selNode != null) {
                if (selNode.isTerminal()) {
                    return; // ignore normal terminals now
                }
                if (e.isControlDown()) { // ctrl-click to remove a Steiner node
                    gr.removeNode(selNode);
                    selNode = null;
                }
            } else {   // see if the mouse was clicked over a point on Hanan Grid
                Point steinerLoc = gr.findSteiner(mouseLoc, TERM_SIZE);
                if (steinerLoc == null) {
                    return; // nothing to see here, folks! move along!
                }
                selNode = new STNode(steinerLoc, false);
                gr.addNode(selNode);
            }
            if (changeListener != null) {
                changeListener.graphChanged(false);
            }
            getParent().repaint();

        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (!hananMode) {
            super.mouseReleased(e);
        } else {
            selNode = null;
        }
        getParent().repaint();
        /*	else {
      // need to snap to the nearest unoccupied gridpoint
      selNode = null;
      if (changeListener != null) changeListener.graphChanged();
      getParent().repaint();
    } */
    }

    @Override
    public void mouseEntered(MouseEvent e) { // do nothing
    }

    @Override
    public void mouseExited(MouseEvent e) { // do nothing
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (selNode != null && selNode.isTerminal() && changable) {
            setPastLength(gr.edgeLength());
            //if (!hananMode) {
            gr.removeNonTerminalNodes();
            super.mouseDragged(e);
        }
        //}
        /*	else if (selNode != null) {
    Point newLoc = gr.snapSteiner(e.getPoint());
    selNode.setLocation(newLoc);
    if (changeListener != null) changeListener.graphChanged();
    getParent().repaint();
  }*/
    }

    @Override
    public void mouseMoved(MouseEvent e) { // do nothing
    }

    /*----------------------------------------------------------------------*/
 /*           Graphics                                                   */
 /*----------------------------------------------------------------------*/
    public void setColor(Color color) {
        c = color;
    }

    public void drawBBox(Graphics g, Point p1, Point p2) {
        int minx = Math.min(p1.x, p2.x);
        int maxx = Math.max(p1.x, p2.x);
        int miny = Math.min(p1.y, p2.y);
        int maxy = Math.max(p1.y, p2.y);
        Graphics2D g2 = (Graphics2D) g;
        double thickness = 2;
        g2.setStroke(new BasicStroke((float) thickness));
        g2.drawRect(minx, miny, maxx - minx, maxy - miny);
    }

    public void drawNEPair(Graphics g, STNEPair p) {
        STEdge e = p.getEdge();
        g.setColor(c);
        drawBBox(g, e.getP1().getLocation(), e.getP2().getLocation());
        if (p.getNode() == null || p.getSteiner() == null) {
            return;
        }
        Point nloc = p.getNode().getLocation();
        Graphics2D g2 = (Graphics2D) g;
        double thickness = 2;
        g2.setStroke(new BasicStroke((float) thickness));
        g2.drawLine(nloc.x, nloc.y, p.getSteiner().x, p.getSteiner().y);
    }

    public void drawHananGrid(Graphics g) {
        int i;
        STNode n;
        int minx = Integer.MAX_VALUE;
        int maxx = 0;
        int miny = Integer.MAX_VALUE;
        int maxy = 0;
        for (i = 0; i < gr.numNodes(); i++) {
            n = gr.getNode(i);
            minx = Math.min(minx, n.getLocation().x);
            maxx = Math.max(maxx, n.getLocation().x);
            miny = Math.min(miny, n.getLocation().y);
            maxy = Math.max(maxy, n.getLocation().y);
        }
        for (i = 0; i < gr.numNodes(); i++) {
            n = gr.getNode(i);
            if (n.isTerminal()) {     // don't draw grid for Steiner points
                int x = n.getLocation().x;
                int y = n.getLocation().y;
                g.setColor(Color.lightGray);
                g.drawLine(x, miny, x, maxy); // vertical gridline
                g.drawLine(minx, y, maxx, y); // horitzontal gridline
            }
        }
    }

    public void paintComponent(Graphics g) {
        if (!hananMode) {
            super.paintComponent(g);
        } else {
            drawHananGrid(g);
            drawEdges(g);
            drawNodes(g);
        }
        if (selectedNEPair != null) {
            drawNEPair(g, selectedNEPair);
        }
    }

    public void enableModification(boolean b) {
        changable = b;
    }

    public static void main(String[] args) {
        STGraph g = new STGraph();
        UIHananGraph ug = new UIHananGraph(g);
        ug.setHananMode(true);
        JFrame f = new JFrame();
        f.add(ug);
        f.setSize(4000, 400);
        f.setVisible(true);
        g.addRandomNodes(10, 300, 200);
        f.repaint();
    }
}

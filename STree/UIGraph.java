
/**
 * @Author: John Nestor <nestorj>
 * @Date: 2020-06-24T21:04:38-04:00
 * @Email: nestorj@lafayette.edu
 * @Last modified by: nestorj
 * @Last modified time: 2020-06-24T21:04:38-04:00
 */
import java.awt.Point;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;
import javax.swing.JFrame;

public class UIGraph extends JPanel implements MouseListener, MouseMotionListener {

    protected STGraph gr;

    public static final long serialVersionUID = 1L; // to shut up warning

    protected UIGraphChangeListener changeListener;

    private int pastLength = 0;

    public UIGraph(STGraph g, UIGraphChangeListener cl) {
        gr = g;
        changeListener = cl;
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    public UIGraph(STGraph g) {
        this(g, null);
    }

    protected static final int TERM_SIZE = 15;

    private void drawNode(Graphics g, STNode n, boolean isSelected) {
        Color fillColor;
        Point loc = n.getLocation();
        int orig_x = loc.x - TERM_SIZE / 2;
        int orig_y = loc.y - TERM_SIZE / 2;
        if (isSelected) {
            fillColor = Color.yellow;
        } else if (n.isVisited()) {
            fillColor = new Color(2, 199, 54);
        } else {
            fillColor = Color.blue;
        }
        g.setColor(fillColor);
        if (n.isTerminal()) {
            g.fillRect(orig_x, orig_y, TERM_SIZE, TERM_SIZE);
            g.setColor(Color.black);
            g.drawRect(orig_x, orig_y, TERM_SIZE, TERM_SIZE);
            g.drawString("n" + n.getID(), orig_x + TERM_SIZE + 4, orig_y + TERM_SIZE - 2);
        } else {
            g.fillOval(orig_x, orig_y, TERM_SIZE, TERM_SIZE);
            g.setColor(Color.black);
            g.drawOval(orig_x, orig_y, TERM_SIZE, TERM_SIZE);
            g.drawString("s" + n.getID(), orig_x + TERM_SIZE + 4, orig_y + TERM_SIZE - 2);
        }

    }

    private void drawEdge(Graphics g, STEdge e) {
        if (e.getDeleteMark()) {
            return; // nothing to see here, folks!
        }
        Point loc1 = e.getP1().getLocation();
        Point loc2 = e.getP2().getLocation();
        if (e == selEdge) {
            g.setColor(Color.blue);
        } else {
            g.setColor(new Color(255, 0, 204));
        }
        //	System.out.println("drawing edge from " + loc1 + " to " + loc2);
        g.drawLine(loc1.x, loc1.y, loc2.x, loc2.y);
        // label it
        int dx, dy, midx, midy, offx, offy;
        dx = loc2.x - loc1.x;
        dy = loc2.y - loc1.y;
        midx = loc1.x + (dx / 2);
        midy = loc1.y + (dy / 2);
        offx = 5;
        offy = 5;
        if (dx != 0) {  // hack to space edges - could probably do better
            double slope = (double) (dy) / (double) (dx);
            if (slope >= 0 && slope < 1) {
                offy = -5;
            } else if (slope < 0 && slope > -1) {
                offy = 10;
            }
        }
        g.drawString("e" + e.getID(), midx + offx, midy + offy);
        g.setColor(Color.black);
    }

    protected void drawEdges(Graphics g) {
        // paint edges
        for (int i = 0; i < gr.numEdges(); i++) {
            STEdge e = gr.getEdge(i);
            drawEdge(g, e);
        }
    }

    protected void drawNodes(Graphics g) {
        for (int i = 0; i < gr.numNodes(); i++) {
            STNode n = gr.getNode(i);
            drawNode(g, n, (n == selNode));
        }

    }

    protected STNode selNode = null;

    public void selectNode(STNode n) {
        selNode = n;
    }

    protected STEdge selEdge = null;

    public void selectEdge(STEdge e) {
        selEdge = e;
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawEdges(g);
        drawNodes(g);
    }

    public void mouseClicked(MouseEvent e) {
    }

    /**
     * mousePressed - if pressed over a node, select for dragging - otherwise,
     * add node
     */
    public void mousePressed(MouseEvent e) {
        Point mouseLoc = e.getPoint();
        selNode = gr.findNode(mouseLoc, TERM_SIZE);
        boolean modified = false;
        if (selNode == null) {
            selNode = new STNode(mouseLoc);
            gr.addNode(selNode);
            modified = true;
        } else if (e.isControlDown()) {  // ctrl-click removes a node
            gr.removeNode(selNode);
            if (selNode.isTerminal()) {
                gr.removeNonTerminalNodes();
                modified = true;
            }
            selNode = null;
        }
        if (changeListener != null) {
            changeListener.graphChanged(modified);
            if (modified) {
                pastLength = gr.edgeLength();
            }
        }
        getParent().repaint();
    }

    public void mouseReleased(MouseEvent e) {
        selNode = null;
        if (changeListener != null) {
            changeListener.graphChanged(false);
        }
        getParent().repaint();
    }

    public void mouseEntered(MouseEvent e) { // do nothing
    }

    public void mouseExited(MouseEvent e) { // do nothing
    }

    public void mouseDragged(MouseEvent e) {
        if (selNode != null) {
            selNode.setLocation(e.getPoint());
            if (changeListener != null) {
                changeListener.graphChanged(true);
            }
            getParent().repaint();
        }
    }

    public void mouseMoved(MouseEvent e) { // do nothing
    }

    public void clear() {
        gr.clearGraph();
        repaint();
    }

    public static void main(String[] args) {
        STGraph g = new STGraph();
        UIGraph ug = new UIGraph(g);
        JFrame f = new JFrame();
        f.add(ug);
        f.setSize(250, 250);
        f.setVisible(true);
        STNode n1, n2, n3;
        n1 = new STNode(5, 5);
        n2 = new STNode(100, 100);
        n3 = new STNode(150, 250);
        g.addNode(n1);
        g.addNode(n2);
        g.addNode(n3);
        f.repaint();
    }

    public int getPastLength() {
        return pastLength;
    }

    public void setPastLength(int i) {
        pastLength = i;
    }
}

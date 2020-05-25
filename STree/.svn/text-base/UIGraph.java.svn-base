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

    public UIGraph(STGraph g, UIGraphChangeListener cl) {
	gr = g;
	changeListener = cl;
	addMouseListener(this);
	addMouseMotionListener(this);
    }

    public UIGraph(STGraph g) {
	this(g, null);
    }

    protected static final int TERM_SIZE = 8;

    private void drawNode(Graphics g, STNode n, boolean isSelected) {
	Color fillColor;
	Point loc = n.getLocation();
	int orig_x = loc.x - TERM_SIZE/2;
	int orig_y = loc.y - TERM_SIZE/2;
	if ( isSelected ) fillColor = Color.blue;
	else if ( n.isVisited() ) fillColor = Color.green;
	else fillColor = Color.blue;
	g.setColor(fillColor);
	if ( n.isTerminal() ) {
	    g.fillRect(orig_x, orig_y,TERM_SIZE, TERM_SIZE);
	    g.setColor(Color.black);
	    g.drawRect(orig_x, orig_y,TERM_SIZE, TERM_SIZE);
	} else {
	    g.fillOval(orig_x, orig_y,TERM_SIZE, TERM_SIZE);
	    g.setColor(Color.black);
	    g.drawOval(orig_x, orig_y,TERM_SIZE, TERM_SIZE);
	}
    }

    private void drawEdge(Graphics g, STEdge e) {
	if (e.getDeleteMark()) return; // nothing to see here, folks!
	Point loc1 = e.getP1().getLocation();
	Point loc2 = e.getP2().getLocation();
	if (e == selEdge) g.setColor(Color.blue);
	else g.setColor(Color.red);
//	System.out.println("drawing edge from " + loc1 + " to " + loc2);
	g.drawLine(loc1.x, loc1.y, loc2.x, loc2.y);
	g.setColor(Color.black);
    }

    protected void drawEdges(Graphics g) {
	// paint edges
	for(int i = 0; i < gr.numEdges(); i++) {
	    STEdge e = gr.getEdge(i);
	    drawEdge(g,e);
	}
    }

    protected void drawNodes(Graphics g) {
	for(int i = 0; i < gr.numNodes(); i++) {
	    STNode n = gr.getNode(i);
	    drawNode(g, n, (n==selNode));
	}

    }

    protected STNode selNode = null;

    public void selectNode(STNode n) { selNode = n; }

    protected STEdge selEdge = null;

    public void selectEdge(STEdge e) { selEdge = e; }

    public void paintComponent(Graphics g) {
	int i;
	super.paintComponent(g);
	drawEdges(g);
	drawNodes(g);
    }



    public void mouseClicked(MouseEvent e) {
    }

    /** mousePressed - if pressed over a node, select for dragging - otherwise, add node */
    public void mousePressed(MouseEvent e) {
	Point mouseLoc = e.getPoint();
	selNode = gr.findNode(mouseLoc,TERM_SIZE);
	if (selNode == null) {
	    selNode = new STNode(mouseLoc);
	    gr.addNode(selNode);
	} else if (e.isControlDown()) {  // ctrl-click removes a node
	    gr.removeNode(selNode);
	    selNode = null;
	}
	if (changeListener != null) changeListener.graphChanged();
	getParent().repaint();
   }
    
    public void mouseReleased(MouseEvent e) {
	selNode = null;
	if (changeListener != null) changeListener.graphChanged();
	getParent().repaint();
    }
	
    public void mouseEntered(MouseEvent e) { // do nothing
    }

    public void mouseExited(MouseEvent e) { // do nothing
    }

    public void mouseDragged(MouseEvent e) {
	if (selNode != null) {
	    selNode.setLocation(e.getPoint());
	    if (changeListener != null) changeListener.graphChanged();
	    getParent().repaint();
	}
    }

    public void mouseMoved(MouseEvent e) { // do nothing
    }

    public static void main(String [] args) {
	STGraph g = new STGraph();
	UIGraph ug = new UIGraph(g);
	JFrame f = new JFrame();
	f.add(ug);
	f.setSize(250,250);
	f.setVisible(true);
	STNode n1, n2, n3;
	n1 = new STNode(5,5);
	n2 = new STNode(100,100);
	n3 = new STNode(150,250);
	g.addNode(n1);
	g.addNode(n2);
	g.addNode(n3);
	f.repaint();
    }
}


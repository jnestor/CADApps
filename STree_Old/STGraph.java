import java.awt.Point;
import java.util.Vector;
import java.io.*;

public class STGraph  {

    private Vector/*<STNode>*/ nodes = new Vector/*<STNode>*/();
    private Vector/*<STEdge>*/ edges = new Vector/*<STEdge>*/();

    public void addNode(STNode n) {
	// check for membership first?
	nodes.add(n);
    }

    public void addRandomNodes(int numNodes, int xrange, int yrange) {
	int x, y, xoff, yoff;
	xrange = xrange - 10;  // kluge to make keep it away from the edges
	yrange = yrange - 10;
	xoff = 5;
	yoff = 5;
	for (int i = 0; i < numNodes; i++) {
	    x = xoff + (int)(Math.random() * (double)xrange);
	    y = yoff + (int)(Math.random() * (double)yrange);
	    addNode(new STNode(new Point(x,y)));
	}
    }
	    
    public void removeNode(STNode n) {
	nodes.remove(n);
	for (int i=numEdges()-1; i>=0; i--) { // go backwards so removed elements don't throw us off
	    STEdge e = getEdge(i);
	    if ((e.getP1() == n) || (e.getP2() == n)) {
		e.disconnect();
		edges.remove(e);
	    }
	}
    }

    public void removeNonTerminalNodes() {
	for (int i = numNodes()-1; i>=0; i--) {
	    STNode n = getNode(i);
	    if (!n.isTerminal()) removeNode(n);
	}
    }

    public STNode getNode(int i) {
	return (STNode)nodes.elementAt(i);
    }

    /** find a node that "touches" a point within window */
    public STNode findNode(Point searchLoc, int window) {
	int i;
	STNode n;
	for (i = 0; i < numNodes(); i++) {
	    n = getNode(i);
	    if (n.nodeFound(searchLoc,window)) return n;
	}
	return null;
    }

    /** find the closest Steiner point to p */
    public Point snapSteiner(Point p) {
	int closestX = -1;
	int minDistX = Integer.MAX_VALUE;
	int closestY = -1;
	int minDistY = Integer.MAX_VALUE;
	int i;
	STNode n;
	for (i = 0; i < numNodes(); i++) {
	    n = getNode(i);
	    int distX = Math.abs(p.x - n.getLocation().x);
	    if (distX < minDistX) {
		minDistX = distX;
		closestX = n.getLocation().x;
	    }
	    int distY = Math.abs(p.y - n.getLocation().y);
	    if (distY < minDistY) {
		minDistY = distY;
		closestY = n.getLocation().y;
	    }
	}
	if (closestX < 0 || closestY < 0) return null; // shouldn't happen?
	return new Point(closestX, closestY);
    }

    
    /** find a node that "touches" a point on the Steiner grid */
    public Point findSteiner(Point p, int window) {
	boolean foundHoriz = false;
	boolean foundVert = false;
	int i;
	int gridX = -1;
	int gridY = -1;
	STNode n;
	for (i = 0; i < numNodes(); i++) {
	    n = getNode(i);
	    if (n.nodeMatchX(p, window)) gridX = n.getLocation().x;
	    if (n.nodeMatchY(p, window)) gridY = n.getLocation().y;
	    if (gridX > 0 && gridY > 0) return new Point(gridX, gridY);
	}
	return null;
    }


    public int numNodes() { return nodes.size(); }

    public void addEdge(STEdge e) {
	edges.add(e);
    }

    public void addEdge(STNode n1, STNode n2) {
	STEdge e = new STEdge(n1,n2);
	addEdge(e);
	n1.addEdge(e);
	n2.addEdge(e);
    }

    /** clear "visited" flag on all nodes */
    public void clearVisited() {
	int i;
	STNode n;
	for (i = 0; i < numNodes(); i++) {
	    n = getNode(i);
	    n.setVisited(false);
	}
    }

    public void clearEdges() {
	int i;
	STEdge e;
	for (i = 0; i < numEdges(); i++) {
	    e = getEdge(i);
	    e.disconnect();
	}
	edges.clear();
    }

    public void clearEdgeMarks() {
	for (int i = 0; i < numEdges(); i++)
	    getEdge(i).setMark(false);
    }

    /** remove edges marked as deleted (assumes they are already disconnected) */
    public void purgeDeletedEdges() {
	for (int i = numEdges()-1; i >= 0; i--) {
	    STEdge e = getEdge(i);
	    if (e.getDeleteMark()) edges.remove(e);
	}
    }

    public STEdge getEdge(int i) {
	return (STEdge)edges.elementAt(i);
    }

    public int numEdges() { return edges.size(); }

    // probably want to store this and adjust incrementally when nodes are added/removed
    public int edgeLength() {
	int length = 0;
	for (int i = 0; i < numEdges(); i++)
	    length += getEdge(i).length();
	return length;
    }

    // probably want to store this and recalculate only when nodes are added/removed
    public int halfPerim() {
	int minX = Integer.MAX_VALUE;
	int minY = Integer.MAX_VALUE;
	int maxX = 0;
	int maxY = 0;
	for (int i = 0; i < numNodes(); i++) {
	    STNode n = getNode(i);
	    int nx = n.getLocation().x;
	    int ny = n.getLocation().y;
	    minX = Math.min(minX, nx);
	    minY = Math.min(minY, ny);
	    maxX = Math.max(maxX, nx);
	    maxY = Math.max(maxY, ny);
	}
	return (maxX - minX) + (maxY - minY);
    }

    /** remove all edges and nodes */
    public void clearGraph() {
	edges.clear();
	nodes.clear();
    }

    public String toString() {
	String desc = "Graph: ";
	int i;
	for (i = 0; i < numNodes(); i++)
	    desc = desc + "\n    " + getNode(i);
	for (i = 0; i < numEdges(); i++)
	    desc = desc + "\n    " + getEdge(i);
	return desc;
    }

    public void readGraph(BufferedReader in) throws IOException {
	String line;
	StreamTokenizer t = new StreamTokenizer(in);
	t.parseNumbers();
	int expectedCount, actualCount, x, y;

	actualCount = 0;
	if (t.nextToken() != StreamTokenizer.TT_NUMBER) {
	    System.out.println("STGraph.readGraph(): unexpected token " + t);
	    return;
	}
	expectedCount = (int)t.nval;
	while (t.nextToken() == StreamTokenizer.TT_NUMBER) {
	    x = (int)t.nval;
	    if (t.nextToken() != StreamTokenizer.TT_NUMBER) {
		System.out.println("STGraph.readGraph(): unexpected token " + t);
		return;
	    }
	    y = (int)t.nval;
	    addNode(new STNode(x,y));
	    actualCount++;
	}
	if (actualCount != expectedCount)
	    System.out.println("STRraph.readGraph: expected " + expectedCount +
			       " nodes actually read " + actualCount);
    }

    public static void main(String [] args) {
	STGraph g = new STGraph();
	STNode n1, n2, n3;
	n1 = new STNode(5,5);
	n2 = new STNode(100,100);
	n3 = new STNode(150,200);
	STEdge e1, e2;
	e1 = new STEdge(n1, n2);
	e2 = new STEdge(n1, n3);
	g.addNode(n1);
	g.addNode(n2);
	g.addNode(n3);
	g.addEdge(e1);
	g.addEdge(e2);
	System.out.println(g);
    }
}



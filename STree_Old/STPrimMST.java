public class STPrimMST  {

    private PrimMSTInterface ui;
    private STGraph gr;

    public STPrimMST(PrimMSTInterface pri, STGraph g)  {
	ui = pri;
	gr = g;
    }

    
    /** find the Minimum Spanning tree using Prim's algorithm.  Adapted from Joe Ganley's Spanning Tree Applet */
    public void primMST(boolean animate) throws InterruptedException {
	int n = gr.numNodes();
        int dist[], neigh[], closest, minDist, d;

        dist = new int[n];
        neigh = new int[n];  // "parent" array - 

	STNode rootNode = gr.getNode(0);

	gr.clearEdges();
	gr.clearVisited();
	
	if (animate) ui.displayPartialTree();

	rootNode.setVisited(true);

	if (animate) ui.displayPartialTree();

        // initialize data structures
        for (int i = 0; i < n; i++) {
	    dist[i] = rootNode.distanceL1(gr.getNode(i));
            neigh[i] = 0;
        }
	if (animate) ui.displayDistances();

        // find terminal closest to current partial tree
        for (int i = 1; i < n; i++) {
            closest = -1;
            minDist = Integer.MAX_VALUE;
            for (int j = 1; j < n; j++) {
                if ((dist[j] != 0) && (dist[j] < minDist)) {
	            closest = j;
	            minDist = dist[j];
                }
            }

    
	    STNode closestNode = gr.getNode(closest);
	    if (animate) {
		ui.displayClosestNode(closestNode);
	    }

            // set an edge from it to its nearest neighbor
	    gr.addEdge(closestNode, gr.getNode(neigh[closest]));
	    closestNode.setVisited(true);
	    if (animate) ui.displayPartialTree();

            // update nearest distances to current partial tree
            for (int j = 1; j < n; j++) {
		d = closestNode.distanceL1(gr.getNode(j));
                if (d < dist[j]) {
	            dist[j] = d;
	            neigh[j] = closest;
                }
            }
	    if (animate) ui.displayDistances();
        }
    } // mst()


}

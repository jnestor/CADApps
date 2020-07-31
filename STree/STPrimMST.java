
/**
 * @Author: John Nestor <nestorj>
 * @Date: 2020-06-24T20:48:39-04:00
 * @Email: nestorj@lafayette.edu
 * @Last modified by: nestorj
 * @Last modified time: 2020-06-24T20:49:32-04:00
 */
public class STPrimMST {

    private PrimMSTInterface ui;
    private STGraph gr;
    private UIPrimDisTable table;

    public STPrimMST(PrimMSTInterface pri, STGraph g) {
        ui = pri;
        gr = g;
    }

    public STPrimMST(PrimMSTInterface pri, STGraph g, UIPrimDisTable t) {
        ui = pri;
        gr = g;
        table = t;
    }

    /**
     * find the Minimum Spanning tree using Prim's algorithm. Adapted from Joe
     * Ganley's Spanning Tree Applet
     */
    public void primMST(boolean animate) throws InterruptedException {
        int n = gr.numNodes();
        int dist[], neigh[], closest, minDist, d;

        dist = new int[n];
        neigh = new int[n];  // "parent" array -

        STNode rootNode = gr.getNode(0);
        if(rootNode==null){
            gr.clearGraph();
            return;
        }

        gr.clearEdges();
        gr.clearVisited();

        if (animate) {
            ui.displayPartialTree();
        }

        rootNode.setVisited(true);
        if (animate) {
            refreshTable();
            ui.displayPartialTree();
            table.highlight();
        }
        // initialize data structures
        for (int i = 0; i < n; i++) {
            dist[i] = rootNode.distanceL1(gr.getNode(i));
            neigh[i] = 0;
        }
//        if (animate) {
//            ui.displayDistances();
//        }

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
                //System.out.println("displayClosetNode: " + closestNode);
                ui.displayClosestNode(closestNode);
            }

            // set an edge from it to its nearest neighbor
            if (animate) {
                ui.setText("Connect from node " + gr.getNode(neigh[closest]).getID() + " to node " + closestNode.getID());
            }
            gr.addEdge(closestNode, gr.getNode(neigh[closest]));
            closestNode.setVisited(true);

            if (animate) {
                refreshTable();
                ui.displayPartialTree();
                highlight();
            }
            // update nearest distances to current partial tree
            for (int j = 1; j < n; j++) {
                d = closestNode.distanceL1(gr.getNode(j));
                if (d < dist[j]) {
                    dist[j] = d;
                    neigh[j] = closest;
                }
            }
//            if (animate) {
//                ui.displayDistances();
//            }
        }
        if (animate) {
            ui.setText("Click to create nodes");
        }
        empty();
    } // mst()

    private void refreshTable() throws InterruptedException {
        if (table != null) {
            table.refresh();
        }
    }

    private void highlight() {
        if (table != null) {
            table.highlight();
        }
    }

    private void empty() {
        if (table != null) {
            table.empty();
        }
    }
}


/**
 * @Author: John Nestor <nestorj>
 * @Date: 2020-06-24T20:51:35-04:00
 * @Email: nestorj@lafayette.edu
 * @Last modified by: nestorj
 * @Last modified time: 2020-06-28T12:12:53-04:00
 */
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.LinkedList;
import java.util.Collections;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Timer;

public class STBOI {
    private BOIInterface ui;
    private STGraph gr;
    private PriorityBlockingQueue<STNEPair> mods = new PriorityBlockingQueue<STNEPair>();
    private boolean modified = false;
    private boolean completed = false;
    UISteinerBOITable table = new UISteinerBOITable(this);
    private Timer timer;

    public STBOI(BOIInterface u, STGraph g) {
        ui = u;
        gr = g;
    }

    // inner class  used in internal queue to keep track of node
    // to search and longest edge seen so far
    class SearchRecord {

        STNode n;  // the node currently being searched
        STEdge e;  // the longest edge seen on the current path

        public SearchRecord(STNode sn, STEdge se) {
            n = sn;
            e = se;
        }

    }

    /**
     * find the longest edge on the cycle from start back to either t2 or t2
     */
    public STEdge findLongestEdge(STNode start, STNode t1, STNode t2) {
        LinkedList<SearchRecord> searchQueue = new LinkedList<SearchRecord>();
        gr.clearEdgeMarks();
        STEdge e = null;
        STEdge ne = null;;
        STNode nn = null;
        //System.out.println("findLongestEdge: start= " + start + " t1= " + t1 + " t2= " + t2);
        searchQueue.add(new SearchRecord(start, null));
        do {
            SearchRecord active = (SearchRecord) searchQueue.removeFirst();
            //System.out.println("active: n=" + active.n + " edge=" + active.e);
            if ((active.n == t1) || (active.n == t2)) {
                //System.out.println("found! edge=" + active.e);
                gr.clearEdgeMarks();
                return active.e; // Found the cycle; return the longest edge
            }

            /* Check all of the edges going out of active.n.  As we traverse, we keep
      * track of the highest cost edge we've seen on this traversal so far using active.e
             */
            for (int i = 0; i < active.n.numEdges(); i++) {
                e = active.n.getEdge(i);
                if (e.getMark()) {
                    continue; // been there, done that, what's next?
                }
                e.setMark(true);
                if (active.n == e.getP1()) {
                    nn = e.getP2();
                } else {
                    nn = e.getP1();
                }
                ne = active.e; // longest edge so far
                if ((ne == null) || (e.length() > ne.length())) {
                    ne = e;
                }
                searchQueue.add(new SearchRecord(nn, ne));
            }
        } while (searchQueue.size() > 0);
        //System.out.println("findLongestEdge: Couldn't find cycle!!!!");
        //System.out.println("start: " + start
//                + " searching for " + t1
//                + " or " + t2);
        gr.clearEdgeMarks();
        return null;
    }

    public boolean improve(boolean animate) throws InterruptedException {
        modified = false;
        //	System.out.println("starting improve");
        //	System.out.println(gr);
        STNode n;
        STEdge e = null;
        STNEPair ne = null;
        STNEPair best_ne = null;
        mods.clear(); // in case it's been run before
        if (animate) {
            ui.showBOIInit();
        }
        for (int i = 0; i < gr.numNodes(); i++) {
            n = gr.getNode(i);
//            System.out.println("looking at node: " + n);

            // find the closest non-adjacent edge to n
            best_ne = null; // want to consider edges closest to current node only
            for (int j = 0; j < gr.numEdges(); j++) {
                e = gr.getEdge(j);
//                System.out.println("looking at edge: " + e);
                if (n.equals(e.getP1()) || n.equals(e.getP2())) {
                    continue; // skip adjacent edges
                }
                ne = new STNEPair(n, e);
                if (ne.getSteinerGood()) {
                    if (best_ne == null) {
                        best_ne = ne;
                    } else if (ne.getCost() < best_ne.getCost()) {
                        best_ne = ne;
                    }
                } // ignore "non-useful" edges
            }
            if (best_ne == null) {
                continue;
            }
            // if (animate) ui.showNEPair(best_ne);

            // search for cycle & calculate gain
            n = best_ne.getNode();
            e = best_ne.getEdge();
//            System.out.println("best_ne: node " + n + " edge " + e);
            if (e != null) {
                STEdge maxEdge = findLongestEdge(n, e.getP1(), e.getP2());
//                System.out.println("maxEdge: " + maxEdge);
                best_ne.setElimEdge(maxEdge);
                if (animate) {
                    ui.showNEGain(best_ne);
                }
                if (best_ne.getGain() > 0) {
//                    System.out.println("Node " + i + " " + n + " new mod " + best_ne);
                    if (animate) {
                        ui.setMessage("Candidate n" + best_ne.getNode().getID() + " / e" + best_ne.getElimEdge().getID() + " Accepted");
                    }
                    mods.add(best_ne);
                    if (animate) {
                        table.refresh();
                        Thread.sleep(300);
                        table.highlight(Color.yellow, " " + best_ne.toTableString() + " ", " " + Integer.toString(best_ne.getGain()) + " ");
                        ui.showAccept();
                    }
                } else {
                    ui.setMessage("Candidate n" + best_ne.getNode().getID() + " / e" + best_ne.getElimEdge().getID() + " Rejected");
                    if (animate) {
                        ui.showDeny();
                    }
                }
            }
        }

        // clear the marked edges (is this really necessary?)
        // sort the mods
        //Collections.sort(mods);
        // loop through and apply mods
        modified = false;
        int i = 1;
        while (!mods.isEmpty()) {
            if (animate) {
                table.refresh();
            }
            Thread.sleep(20);
            STNEPair curmod = mods.poll();
            System.out.println("applying mod " + i + " " + curmod);
            i++;
            // re-calculate gain?
            if (!curmod.modValid()) {
                if (animate) {
                    ui.showNEMod(curmod);
                    table.highlight(Color.red);
                    ui.setMessage("Modification n" + curmod.getNode().getID() + " / e" + curmod.getElimEdge().getID() + " is no longer valid");
                    ui.showDeny();
                }
                continue;
            }
            modified = true;
            if (animate) {
                ui.showNEMod(curmod);
                table.highlight(Color.green);
            }
            ui.setMessage("Modification n" + curmod.getNode().getID() + " / e" + curmod.getElimEdge().getID() + " is valid");
            if (animate) {
                ui.showApply();
            }
            curmod.applyMod(gr);
            gr.purgeDeletedEdges();
            table.getParent().repaint();
            if (animate) {
                table.refresh();
                ui.showNEModComplete();
            }
        }
        completed=true;
        gr.purgeDeletedEdges();
        if (animate) {
            table.empty();
            ui.showBOIComplete(modified);
        }
        ui.setMessage("Click to create nodes");
        return modified;
    }

    public PriorityBlockingQueue<STNEPair> getMods() {
        return mods;
    }

    public void clear() {
        mods.clear();
        gr.clearGraph();
    }
    
    public void clearMods(){
        mods.clear();
    }

    public void empty() {
        table.stop();
        table.empty();
    }

    public boolean isModified() {
        return modified;
    }

    public boolean isRunning() {
        return timer.isRunning();
    }
    
    public boolean isComplete(){
        if(completed){
            completed=false;
            return true;
        }
        return false;
    }

    public void runImprove(boolean animate) {

    }
}

/** Interesting Events for Visualization of Borah-Owens-Iwrwin Steiner Tree Heuristic */

interface BOIInterface {

    /** Interesting event: initialization */
    public void showBOIInit() throws InterruptedException;

    /** Interesting event: show a node/edge candidate */
    public void showNEPair(STNEPair p) throws InterruptedException;

    /** Interesting event: show the edges that contribute to the gain of a node/edge candidate */
    public void showNEGain(STNEPair p) throws InterruptedException;

    /** Interesting event: show replacement of node/edge pair with SP & new edges */
    public void showNEMod(STNEPair p) throws InterruptedException;

    /** Interesting event: show completion of node/edge pair w/ deletion of loop edge */
    public void showNEModComplete() throws InterruptedException;

    /** Interesting event: BOI Completed */
    public void showBOIComplete(boolean modified) throws InterruptedException;

}

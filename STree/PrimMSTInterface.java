/**
 * @Author: John Nestor <nestorj>
 * @Date:   2020-06-24T20:50:49-04:00
 * @Email:  nestorj@lafayette.edu
 * @Last modified by:   nestorj
 * @Last modified time: 2020-06-24T20:50:49-04:00
 */



/** Interesting Events for Visualization of Prim's Minimum Spanning Tree Algorithm */

interface PrimMSTInterface {

  /** Interesting event: partial tree display/redisplay */
  public void displayPartialTree() throws InterruptedException;

  /** Interesting event: display distance calculations */
  public void displayDistances() throws InterruptedException;

  /** Interesting event: display minimum distance node */
  public void displayClosestNode(STNode cn) throws InterruptedException;
  
  public void setText(String s);

}

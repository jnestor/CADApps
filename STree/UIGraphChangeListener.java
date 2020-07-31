/**
 * @Author: John Nestor <nestorj>
 * @Date:   2020-06-24T21:04:26-04:00
 * @Email:  nestorj@lafayette.edu
 * @Last modified by:   nestorj
 * @Last modified time: 2020-06-24T21:04:26-04:00
 */



/** use to notify an object using the graph that the graph has been
*  changed through a manual edit
*/

interface UIGraphChangeListener {
  public void graphChanged(boolean modified);
}

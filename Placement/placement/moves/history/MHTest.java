import java.awt.*;
import javax.swing.*;
/* try to prototype some pictures of history */

class MHTest extends JPanel {
    private PMoveHistory h;

    private Insets myInsets = getInsets();
    private Dimension myDimension = getSize();

    public MHTest(PMoveHistory ch) { h = ch; }

    public void paintComponent(Graphics g) {
	int i;
	myDimension = getSize(myDimension);
	
	myInsets = getInsets(myInsets);
	System.out.println("start " + myDimension + " " +  myInsets + " " + myDimension.height);
	System.out.println("maxcost= " + h.getMaxCost());
	double scale =  ((double) (myDimension.height - myInsets.top - myInsets.bottom - 10)) / (double) h.getMaxCost() ;
	System.out.println("scale=" + scale);
	int width = myDimension.width  - myInsets.left - myInsets.right;
	System.out.println("width=" + width + " height=" + myDimension.height);
	if (h.size() > width) {
	    myDimension.width = h.size() + myInsets.left + myInsets.right;
	    setPreferredSize(myDimension);
	    } 
	int curCost = h.getInitCost();
	System.out.println("starting for loop");
	
	for (i=0; i < h.size(); i++) {
	    PMoveHistoryPoint hp = h.get(i);
	    if (hp.accepted()) {
		curCost += hp.getDeltaCost();
		if (hp.getDeltaCost() > 0) g.setColor(Color.orange);
		else g.setColor(Color.green);
	    } else {
		g.setColor(Color.gray);
		// draw the "rejected" bar here, too!
	    }
	    int x = myInsets.left + i;
	    int y2 = myDimension.height - myInsets.bottom - 5;
	    int y1 = y2 - (int)(scale*curCost);
	    System.out.println("cost=" + curCost + " y1=" + y1 + " y2=" + y2);
	    g.drawLine(x, y1, x, y2);
	}
    }

  public static void main(String [] args) {
    JFrame jf = new JFrame("TEST");
    PMoveHistory h = new PMoveHistory();
    h.setInitCost(100);
    h.add(new PMoveHistoryPoint(10, .9, .8));
    h.add(new PMoveHistoryPoint(10, .9, .8));
    h.add(new PMoveHistoryPoint(10, .9, .8));
    h.add(new PMoveHistoryPoint(10, .8, .9));
    h.add(new PMoveHistoryPoint(-10, .8, .9));
    h.add(new PMoveHistoryPoint(-10, .8, .9));
    h.add(new PMoveHistoryPoint(-10, .8, .9));
    h.add(new PMoveHistoryPoint(-40, .8, .9));
    h.add(new PMoveHistoryPoint(-10, .8, .9));
    h.add(new PMoveHistoryPoint(-10, .8, .9));
    h.add(new PMoveHistoryPoint(100, .8, .9));
    h.add(new PMoveHistoryPoint(100, .9, .8));
    h.add(new PMoveHistoryPoint(100, .9, .8));
    h.add(new PMoveHistoryPoint(10, .8, .9));
    h.add(new PMoveHistoryPoint(-40, .8, .9));
    h.add(new PMoveHistoryPoint(10, .9, .8));
    h.add(new PMoveHistoryPoint(10, .8, .9));
    h.add(new PMoveHistoryPoint(-10, .8, .9));
    h.add(new PMoveHistoryPoint(-10, .8, .9));

    jf.getContentPane().setLayout(new BorderLayout());
    jf.getContentPane().add(new MHTest(h));
    jf.setSize(300,200);
//      jf.getContentPane().add(ua, BorderLayout.CENTER);
      jf.setVisible(true);
/*      jf.addWindowList'ener( new WindowAdapter()
	  {
	      public void windowClosing(WindowEvent e) { System.exit(0); }
	      } ); */
  
  }


}
    

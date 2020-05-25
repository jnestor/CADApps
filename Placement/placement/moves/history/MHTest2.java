import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
/* try to prototype some pictures of history */

class MHTest2 extends JPanel {
    private PMoveHistory h;

    private Insets myInsets = getInsets();
    private Dimension myDimension = getSize();

    private static final int TPZD_WIDTH=7;

    public MHTest2(PMoveHistory ch) { h = ch; }

    public void paintComponent(Graphics g) {
	int i;
	int tpzdX[] = new int[5];  // coordinate arrays used by drawPolyGon
	int tpzdY[] = new int[5];
	myDimension = getSize(myDimension);
	
	myInsets = getInsets(myInsets);
	System.out.println("start " + myDimension + " " +  myInsets + " " + myDimension.height);
	System.out.println("maxcost= " + h.getMaxCost());
	double scale =  ((double) (myDimension.height - myInsets.top - myInsets.bottom - 10)) / (double) h.getMaxCost() ;
	System.out.println("scale=" + scale);
	int width = myDimension.width  - myInsets.left - myInsets.right;
	int baseX = myInsets.left + 5;
	int baseY = myDimension.height - myInsets.bottom - 5;

	System.out.println("width=" + width + " height=" + myDimension.height);
	if (h.size() > width) {
	    myDimension.width = h.size()*TPZD_WIDTH + myInsets.left + myInsets.right;
	    setPreferredSize(myDimension);
	    } 
	int curCost = h.getInitCost();

	for (i=0; i < h.size(); i++) {
	    tpzdX[0] = tpzdX[1] = tpzdX[4] = baseX + (i * TPZD_WIDTH);
	    tpzdX[2] = tpzdX[3] = baseX + ((i+1) * TPZD_WIDTH);
	    tpzdY[0] = tpzdY[3] = tpzdY[4] = baseY;
	    tpzdY[1] = baseY - (int)(curCost * scale);
	    PMoveHistoryPoint hp = h.get(i);
	    if (hp.accepted()) {
		curCost += hp.getDeltaCost();
		tpzdY[2] = baseY - (int)(curCost * scale);
		g.setColor(Color.gray);
		g.drawPolygon(tpzdX, tpzdY, 4);
		if (hp.getDeltaCost() > 0) g.setColor(Color.orange);
		else g.setColor(Color.green);
		g.fillPolygon(tpzdX, tpzdY, 4);
	    } else {
		tpzdY[2] = baseY - (int)(curCost * scale);
		g.setColor(Color.gray);
		g.drawPolygon(tpzdX, tpzdY, 4);
		g.setColor(Color.white);
		g.fillPolygon(tpzdX, tpzdY, 4);
		// draw the "rejected" bar here
		int barX1, barX2, barY;
		barX1 = baseX + (i * TPZD_WIDTH);
		barX2 = baseX + ((i+1) * TPZD_WIDTH);
		barY = baseY - (int)((curCost + hp.getDeltaCost()) * scale);
		if (barY < (myInsets.top + 5)) barY = myInsets.top + 5;;
		g.setColor(Color.red);
		g.drawLine(barX1, barY, barX2, barY);
	    }
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
    h.add(new PMoveHistoryPoint(20, .8, .9));
    h.add(new PMoveHistoryPoint(100, .9, .8));
    h.add(new PMoveHistoryPoint(100, .9, .8));
    h.add(new PMoveHistoryPoint(10, .8, .9));
    h.add(new PMoveHistoryPoint(-40, .8, .9));
    h.add(new PMoveHistoryPoint(10, .9, .8));
    h.add(new PMoveHistoryPoint(10, .8, .9));
    h.add(new PMoveHistoryPoint(-10, .8, .9));
    h.add(new PMoveHistoryPoint(-10, .8, .9));    h.add(new PMoveHistoryPoint(-40, .8, .9));
    h.add(new PMoveHistoryPoint(100, .9, .8));
    h.add(new PMoveHistoryPoint(100, .8, .9));
    h.add(new PMoveHistoryPoint(-10, .8, .9));
    h.add(new PMoveHistoryPoint(-10, .8, .9));    h.add(new PMoveHistoryPoint(-40, .8, .9));
    h.add(new PMoveHistoryPoint(100, .9, .8));
    h.add(new PMoveHistoryPoint(100, .8, .9));
    h.add(new PMoveHistoryPoint(-10, .8, .9));
    h.add(new PMoveHistoryPoint(-10, .8, .9));    h.add(new PMoveHistoryPoint(-40, .8, .9));
    h.add(new PMoveHistoryPoint(10, .9, .8));
    h.add(new PMoveHistoryPoint(10, .8, .9));
    h.add(new PMoveHistoryPoint(-10, .8, .9));
    h.add(new PMoveHistoryPoint(-10, .8, .9));

    jf.getContentPane().setLayout(new BorderLayout());
//    jf.getContentPane().add(new MHTest2(h));
    JPanel jp = new MHTest2(h);
    JScrollPane  sp = new JScrollPane(jp);
    jp.setPreferredSize(new Dimension(300,200));
    jf.getContentPane().add(sp);
    jf.setSize(200,250);
//      jf.getContentPane().add(ua, BorderLayout.CENTER);
      jf.setVisible(true);
      jf.addWindowListener( new WindowAdapter()
	  {
	      public void windowClosing(WindowEvent e) { System.exit(0); }
	      } );
  
  }


}
    

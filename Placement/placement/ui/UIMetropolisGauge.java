package placement.ui;

/**
   Displays a Metropolis algorithm "decision" by showing the
   acceptance probability p, a random number r, and shading to show
   whether the move is accepted or rejected.

   A value of p >= 1 implies that the move is always accepted (i.e. downhill move)

   A negative value for r implies that the gauge is inactive.
   

 */
import java.awt.*;
import javax.swing.JPanel;
import javax.swing.JFrame;
import java.text.NumberFormat;

public class UIMetropolisGauge extends JPanel {

    private double r;
    private double p;

    public void setValues(double r, double p) {
	if (this.r != r && this.p != p) {
	    if (r < 0.0 || p < 0.0) setToolTipText("");
	    else setToolTipText("P=" + nf.format(p) + " r=" + nf.format(r));
	    this.r = r;
	    this.p = p;
	    repaint();
	}
    }


    public UIMetropolisGauge(double r, double p) {
	this.r = r;
        this.p = p;
    }

    public UIMetropolisGauge() { this(0.0, 0.0); }

    public Dimension getMinimumSize() {
	return new Dimension(GAUGE_WIDTH+10,GAUGE_HEIGHT+10);
    }

    public Dimension getPreferredSize() {
	return new Dimension(GAUGE_WIDTH+10,GAUGE_HEIGHT+10);
    }

    private static final int GAUGE_WIDTH = 60;
    private static final int GAUGE_HEIGHT = 14;
    private static final int BASELINE_HEIGHT = 3;  // should be an odd number
    private static final int R_SIZE = 8;

    private static Color pBarAcceptColor = Color.green;
    private static Color pBarRejectColor = Color.red;
    private static Color trAcceptColor = Color.white;
    private static Color trRejectColor = Color.black;

    private int [] diamondX = new int[4];
    private int [] diamondY = new int[4];

    private int [] triangleX = new int[3];
    private int [] triangleY = new int[3];

    private void drawTriangle(Graphics g, boolean accept, int x, int y) {
        triangleX[0] = x - (R_SIZE/2 + 1);
	triangleX[1] = x + R_SIZE/2 + 1;
	triangleX[2] = x;
	triangleY[0] = y - R_SIZE;
	triangleY[1] = y - R_SIZE;
	triangleY[2] = y;
	if (accept) g.setColor(trAcceptColor);
	else g.setColor(trRejectColor);
	g.fillPolygon(triangleX, triangleY, 3);
	g.setColor(Color.black);
	g.drawPolygon(triangleX, triangleY, 3);
    }

    private void drawDiamond(Graphics g, int x, int y) {
	diamondX[0] = x - R_SIZE/2;
	diamondX[1] = x;
	diamondX[2] = x + R_SIZE/2;
	diamondX[3] = x;
	diamondY[0] = y;
	diamondY[1] = y + R_SIZE/2;
	diamondY[2] = y;
	diamondY[3] = y - R_SIZE/2;
	g.setColor(Color.blue);
	g.fillPolygon(diamondX, diamondY, 4);
    }

    // new version 6/19/02
    public void paintComponent(Graphics g) {

        Insets myInset =getInsets();
	int w = getWidth();
	int xleft = getWidth()/2 - GAUGE_WIDTH/2;
	int ytop = getHeight()/2 - GAUGE_HEIGHT/2;
        int ybot = ytop + GAUGE_HEIGHT;
	boolean accept = r < p;
	      
	// draw baseline
	g.setColor(Color.black);
	g.fillRect(xleft, ytop + GAUGE_HEIGHT/2 - BASELINE_HEIGHT/2 ,GAUGE_WIDTH,BASELINE_HEIGHT);

	if (r < 0 || p < 0) return;  // do nothing else when inactive

	// now draw the "probability" bar below the baseline
	if (p > 1.0) p = 1.0; // just in case
	int pBarTop = ytop + GAUGE_HEIGHT/2 + BASELINE_HEIGHT/2 + 1;
	if (accept) g.setColor(pBarAcceptColor);
	else g.setColor(pBarRejectColor);
	g.fillRect(xleft,pBarTop, (int)(GAUGE_WIDTH*p), GAUGE_HEIGHT/2 - BASELINE_HEIGHT/2);

	// now draw the "r" marker
	if (p < 1.0 && r < 1.0)
	    drawTriangle(g, accept, xleft + (int)(GAUGE_WIDTH*r), ytop+GAUGE_HEIGHT/2-BASELINE_HEIGHT/2);
    }

    private static NumberFormat nf = NumberFormat.getInstance();

/*    public void paintComponent(Graphics g) {
	if (r < 0) return;  // do nothing when inactive (assumes we cleared elsewhere?);
        Insets myInset =getInsets();
	int w = getWidth();
	int xleft = getWidth()/2 - GAUGE_WIDTH/2;
	int ytop = getHeight()/2 - GAUGE_HEIGHT/2;
        int ybot = ytop + GAUGE_HEIGHT;
	boolean accept = r < p;
	      
	// draw baseline midway between top and bottom
	g.setColor(Color.black);
	g.fillRect(xleft, ybot+GAUGE_HEIGHT/2 ,GAUGE_WIDTH,BASELINE_HEIGHT);

	// now draw the "probability" bar
	if (accept) g.setColor(pBarAcceptColor);
	else g.setColor(pBarRejectColor);
	g.fillRect(xleft,ytop, (int)(GAUGE_WIDTH*p), GAUGE_HEIGHT);
	g.setColor(Color.black);
	g.drawRect(xleft,ytop, (int)(GAUGE_WIDTH*p), GAUGE_HEIGHT);
//	g.fillRect(xleft + (int)(GAUGE_WIDTH*p),ytop + GAUGE_HEIGHT/2, 1, GAUGE_HEIGHT/2+5 );

	// now draw the "r" marker
	if (p < 1.0 && r < 1.0)
	    drawTriangle(g, accept, xleft + (int)(GAUGE_WIDTH*r), ybot);
    }

*/

/*
    public void paintComponent(Graphics g) {
        Insets myInset =getInsets();
	int w = getWidth();
	int xleft = getWidth()/2 - GAUGE_WIDTH/2;
	int ytop = getHeight()/2 - GAUGE_HEIGHT/2;
        int ybot = ytop + GAUGE_HEIGHT
	// fill box
	g.setColor(Color.black);
	g.drawRect(xleft, ytop ,GAUGE_WIDTH,GAUGE_HEIGHT);
        if (r >= 0.0 && r <= 1.0) {
	    if ( p < 1.0) {
		int pw = (int)(GAUGE_WIDTH*p);
		g.setColor(Color.green);
		g.fillRect(xleft+1, ytop+1, pw, GAUGE_HEIGHT-1);
		if (r > p) {
		    g.setColor(Color.red);
		    g.fillRect(xleft+pw+1, ytop+1, GAUGE_WIDTH-pw-1, GAUGE_HEIGHT-1);
		}
		int rx  = xleft + (int)(GAUGE_WIDTH*r);
		int ry = ytop + GAUGE_HEIGHT/2;
		drawDiamond(g,rx, ry);
	    } else {  // don't draw diamond if always downhill
		g.setColor(Color.green);
		g.fillRect(xleft+1, ytop+1, GAUGE_WIDTH-1, GAUGE_HEIGHT-1);
	    }
	}
	// draw "r" diamond
    }
*/
    public static void main(String [] args) {
	UIMetropolisGauge tg = new UIMetropolisGauge(0.9, 0.6);
	UIMetropolisGauge tg2 = new UIMetropolisGauge(0.4, 0.75);
	UIMetropolisGauge tg3 = new UIMetropolisGauge(0.1, 0.05);
	UIMetropolisGauge tg4 = new UIMetropolisGauge(0.1, 0.2);
	UIMetropolisGauge tg5 = new UIMetropolisGauge(0.9, 0.95);
        UIMetropolisGauge tg6 = new UIMetropolisGauge(0.95, 0.9);
        UIMetropolisGauge tg7 = new UIMetropolisGauge(0.001, 0.0005);
        UIMetropolisGauge tg8 = new UIMetropolisGauge(0.001, 0.0015);
        UIMetropolisGauge tg9 = new UIMetropolisGauge(0.995, 0.996);
        UIMetropolisGauge tg10 = new UIMetropolisGauge(0.995, 0.994);
	JFrame myFrame = new JFrame();
	myFrame.getContentPane().setLayout(new FlowLayout());
	myFrame.getContentPane().add(tg2);
	myFrame.getContentPane().add(tg);
	myFrame.getContentPane().add(tg2);
	myFrame.getContentPane().add(tg3);
	myFrame.getContentPane().add(tg4);
	myFrame.getContentPane().add(tg5);
	myFrame.getContentPane().add(tg6);
	myFrame.getContentPane().add(tg7);
	myFrame.getContentPane().add(tg8);
	myFrame.getContentPane().add(tg9);
	myFrame.getContentPane().add(tg10);
	myFrame.setVisible(true);
    }

} 








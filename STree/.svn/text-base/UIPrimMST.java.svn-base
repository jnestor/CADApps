import java.lang.Math;
import java.awt.FlowLayout;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.ButtonGroup;
import javax.swing.JFrame; // for unit test
import javax.swing.BorderFactory;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Graphics;
import java.io.*;

public class UIPrimMST extends JPanel implements PrimMSTInterface, UIAnimated, ActionListener, UIGraphChangeListener {

    private STGraph gr;
    private UIGraph ugr;
    private STPrimMST prim;

    private JPanel statusPanel;
    private JPanel controlPanel;

    private UIValDisplay halfPerimDisplay;
    private UIValDisplay lengthDisplay;
    
    private boolean animate = true;  // don't need this here?

    private UIAnimationController ucontrol;

    public static final long serialVersionUID = 1L;  // to shut up serialization warning

    private JToggleButton autoButton;

    private boolean autoMode = false;

    public UIPrimMST() {
	super();
	setLayout(new BorderLayout());
	gr = new STGraph();
	ugr = new UIGraph(gr, this);
	prim = new STPrimMST(this, gr);
	ucontrol = new UIAnimationController(this);
	autoButton = new JToggleButton("AUTO");
	autoButton.setActionCommand("AUTO");
	autoButton.setToolTipText("Auto-Update on Edit");
	autoButton.addActionListener(this);
	      

        statusPanel = new JPanel();
        statusPanel.setLayout(new GridLayout(1,2));
        halfPerimDisplay = new UIValDisplay("Half Perimeter",0);
        statusPanel.add(halfPerimDisplay);
        lengthDisplay = new UIValDisplay("Edge Length",0);
        statusPanel.add(lengthDisplay);
	add(statusPanel, BorderLayout.NORTH);
	add(ugr, BorderLayout.CENTER);

	controlPanel = new JPanel();
	controlPanel.setLayout(new FlowLayout());
	controlPanel.add(ucontrol);
	controlPanel.add(autoButton);
	add(controlPanel, BorderLayout.SOUTH);


        setBorder( BorderFactory.createEmptyBorder(2,2,2,2) );
	
    }

    public void setCostDisplay() {
	halfPerimDisplay.setValue(gr.halfPerim());
	lengthDisplay.setValue(gr.edgeLength());
    }

    public void initRandom() {
	int width = ugr.getWidth();
	int height = ugr.getHeight();
	gr.clearGraph();
	gr.addRandomNodes(10, width, height); // change to use range of graphics window
	try {
	    prim.primMST(false);
	} catch (InterruptedException e) {}
    }

    public void readGraph(BufferedReader in) throws IOException {
	gr.readGraph(in);
    }
	


    public void paintComponent(Graphics g) {
	super.paintComponent(g);
	setCostDisplay();
    }

    /*----------------------------------------------------------------------*/	
    /*        methods from UIAnimated interface                             */
    /*----------------------------------------------------------------------*/
	
    /* from the new thread - use to call the algorithm code */
    public void runAnimation() throws InterruptedException {
	prim.primMST(true);
    }

    /* use to clean up when animation is terminated */
    public void stopAnimation() {
	ucontrol.interruptAnimation();	
    }
    
    /*----------------------------------------------------------------------*/	
    /*        methods from PrimMSTInterface ace                             */
    /*----------------------------------------------------------------------*/

    /** Interesting event: partial tree display/redisplay */
    public void displayPartialTree() throws InterruptedException {
	repaint();
	ucontrol.animateDelay();
    }

    /** Interesting event: display distance calculations */
    public void displayDistances() throws InterruptedException { } // do nothing for now
    // when we have a distance display, use this to update

    /** Interesting event: display minimum distance node */
    public void displayClosestNode(STNode cn) throws InterruptedException {
	ugr.selectNode(cn); // select the closest node
	repaint();
	ucontrol.animateDelay();
	ugr.selectNode(null);
    }


    /*----------------------------------------------------------------------*/	
    /*        ActionListener method                                         */
    /*----------------------------------------------------------------------*/

    public synchronized void actionPerformed(ActionEvent e) {
	String cmd = e.getActionCommand();
	if (cmd == "AUTO") {
	    if (autoButton.isSelected()) {
		autoMode = true;
		ucontrol.disableAnimation();
		ugr.selectNode(null);
		try {
		    prim.primMST(false);
		} catch (InterruptedException except) { }
		repaint();
	    }
	    else {
		autoMode = false;
		ucontrol.enableAnimation();
	    }
	}
    }

    /*----------------------------------------------------------------------*/	
    /*        UIGraphChangeListener method                                  */
    /*----------------------------------------------------------------------*/

    public void graphChanged() {
	if (autoMode) {
	    try {
		prim.primMST(false);
		repaint();
	    } catch (InterruptedException e) {}
	} else {  // can't continue animaton with a changed graph!
	    ucontrol.interruptAnimation();
	    gr.clearEdges();
	    gr.clearVisited();
	    repaint();
	}
    }


    /*----------------------------------------------------------------------*/	
    /*        main() / unit test                                            */
    /*----------------------------------------------------------------------*/

    public static void main(String [] args) {
	JFrame f = new JFrame();
	UIPrimMST p = new UIPrimMST();
	f.setSize(300,300);
	f.getContentPane().add(p);
	f.setVisible(true);
    }

}

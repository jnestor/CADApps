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
import java.net.*;


public class UISteinerDemo extends JPanel implements ActionListener, UIGraphChangeListener {

    private STGraph gr;
    private UIHananGraph ugr;
    private STPrimMST prim; // used in non-interactive mode to calculate RMST

    private JPanel statusPanel;
    private JPanel controlPanel;

    private UIValDisplay halfPerimDisplay;
    private UIValDisplay lengthDisplay;
    private UIValDisplay improveDisplay;

    private JToggleButton steinerModeButton;

    private boolean steinerMode = false;
    private int rmstLength = 0;

    public UISteinerDemo() {
	super();
	setLayout(new BorderLayout());
	gr = new STGraph();
	ugr = new UIHananGraph(gr, this);
	prim = new STPrimMST(null, gr);
	steinerModeButton = new JToggleButton("STMODE");
	steinerModeButton.setActionCommand("STMODE");
	steinerModeButton.setToolTipText("Editing Terminals - Click to Edit Steiner Points");
	steinerModeButton.addActionListener(this);
	      

        statusPanel = new JPanel();
        statusPanel.setLayout(new GridLayout(1,2));
        halfPerimDisplay = new UIValDisplay("Half Perimeter",0);
	halfPerimDisplay.setToolTipText("Half Perimeter Length Estimate");
        statusPanel.add(halfPerimDisplay);
        lengthDisplay = new UIValDisplay("Edge Length",0);
	lengthDisplay.setToolTipText("Edge Length of Current Tree");
        statusPanel.add(lengthDisplay);
	improveDisplay = new UIValDisplay("Improvement", 1, 1, 2);
	improveDisplay.setToolTipText("Improvement Factor over RMST");
	statusPanel.add(improveDisplay);
	add(statusPanel, BorderLayout.NORTH);
	add(ugr, BorderLayout.CENTER);

	controlPanel = new JPanel();
	controlPanel.setLayout(new FlowLayout());
	controlPanel.add(steinerModeButton);
	add(controlPanel, BorderLayout.SOUTH);

        setBorder( BorderFactory.createEmptyBorder(2,2,2,2) );
	
    }

    public void setCostDisplay() {
	halfPerimDisplay.setValue(gr.halfPerim());
	lengthDisplay.setValue(gr.edgeLength());
	if (steinerMode) improveDisplay.setValue( (double)rmstLength / (double)gr.edgeLength() );
	else improveDisplay.setValue(1.0);
    }

    public void paintComponent(Graphics g) {
	super.paintComponent(g);
	setCostDisplay();
    }


    public void initRandom() {
	int width = ugr.getWidth();
	int height = ugr.getHeight();
	gr.clearGraph();
	gr.addRandomNodes(10, width, height); // change to use range of graphics window
	try {
	    prim.primMST(false);
	} catch (InterruptedException e) {}
	repaint();

    }

    public void readGraph(BufferedReader in) throws IOException {
	gr.readGraph(in);
    }
	

    /*----------------------------------------------------------------------*/	
    /*        ActionListener method                                         */
    /*----------------------------------------------------------------------*/

    public synchronized void actionPerformed(ActionEvent e) {
	String cmd = e.getActionCommand();
	if (cmd == "STMODE") {
	    if (steinerModeButton.isSelected()) {
		steinerMode = true;
		steinerModeButton.setToolTipText("Editing Steiner Points - Click to Edit Terminals");
		ugr.setHananMode(true);
		ugr.selectNode(null);
		try {
		    prim.primMST(false);
		} catch (InterruptedException except) { }
		rmstLength = gr.edgeLength(); // use to figure improvement
		repaint();
	    }
	    else {
		steinerMode = false;
		steinerModeButton.setToolTipText("Editing Terminals - Click to Edit Steiner Points");
		ugr.setHananMode(false);
		gr.removeNonTerminalNodes();
		try {
		    prim.primMST(false);
		} catch (InterruptedException except) {}
		repaint();
	    }
	}
    }

    /*----------------------------------------------------------------------*/	
    /*        UIGraphChangeListener method                                  */
    /*----------------------------------------------------------------------*/

    public void graphChanged() {
	try {
	    prim.primMST(false);
	} catch (InterruptedException e) {}
    }


    /*----------------------------------------------------------------------*/	
    /*        main() / unit test                                            */
    /*----------------------------------------------------------------------*/

    public static void main(String [] args) {
	JFrame f = new JFrame();
	UISteinerDemo d = new UISteinerDemo();
	f.setSize(400,400);
	f.getContentPane().add(d);
	f.setVisible(true);
    }

}

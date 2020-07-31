
/**
 * @Author: John Nestor <nestorj>
 * @Date: 2020-06-24T20:49:46-04:00
 * @Email: nestorj@lafayette.edu
 * @Last modified by: nestorj
 * @Last modified time: 2020-06-24T20:56:56-04:00
 */
import java.lang.Math;
import java.awt.FlowLayout;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
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
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

public class UIPrimMST extends JPanel implements PrimMSTInterface, UIAnimated, ActionListener, UIGraphChangeListener {

    private STGraph gr;
    private UIGraph ugr;
    private STPrimMST prim;

    private JPanel statusPanel;
    private JPanel controlPanel;
    private Border border = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);

    private UIValDisplay halfPerimDisplay;
    private UIValDisplay lengthDisplay;

    private boolean animate = true;  // don't need this here?

    private UIAnimationController ucontrol;

    public static final long serialVersionUID = 1L;  // to shut up serialization warning

    private JToggleButton autoButton;

    private JLabel msgBoard;
    private String msg;
    private UIPrimDisTable table;
    private boolean autoMode = false;

    public UIPrimMST() {
        super();
        setLayout(new BorderLayout());
        gr = new STGraph();
        ugr = new UIGraph(gr, this);
        table = new UIPrimDisTable(gr);
        prim = new STPrimMST(this, gr, table);
        ucontrol = new UIAnimationController(this);
        autoButton = new JToggleButton("AUTO");
        autoButton.setActionCommand("AUTO");
        autoButton.setToolTipText("Auto-Update on Edit");
        autoButton.addActionListener(this);
        msgBoard = new JLabel("Click to create nodes");
        msgBoard.setPreferredSize(new Dimension(200, 25));
        msg = "Click to create nodes";

        ugr.setBorder(border);
        statusPanel = new JPanel();
        statusPanel.setLayout(new GridLayout(1, 2));
        halfPerimDisplay = new UIValDisplay("Half Perimeter", 0);
        statusPanel.add(halfPerimDisplay);
        lengthDisplay = new UIValDisplay("Edge Length", 0);
        statusPanel.add(lengthDisplay);
        add(statusPanel, BorderLayout.NORTH);
        add(ugr, BorderLayout.CENTER);

        controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());
        controlPanel.add(msgBoard);
        controlPanel.add(ucontrol);
        controlPanel.add(autoButton);
        add(controlPanel, BorderLayout.SOUTH);
        setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
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
        } catch (InterruptedException e) {
        }
    }

    public void readGraph(BufferedReader in) throws IOException {
        gr.readGraph(in);
    }

    public void clear() {
        gr.clearGraph();
        table.empty();
        setText("Click to create nodes");
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        setCostDisplay();
        msgBoard.setText(msg);
    }

    /*----------------------------------------------------------------------*/
 /*        methods from UIAnimated interface                             */
 /*----------------------------------------------------------------------*/

 /* from the new thread - use to call the algorithm code */
    @Override
    public void runAnimation() throws InterruptedException {
            prim.primMST(true);
    }

    /* use to clean up when animation is terminated */
    @Override
    public void stopAnimation() {
        setText("Click to create nodes");
        table.stop();
        table.empty();
    }

    /*----------------------------------------------------------------------*/
 /*        methods from PrimMSTInterface ace                             */
 /*----------------------------------------------------------------------*/
    /**
     * Interesting event: partial tree display/redisplay
     *
     * @throws java.lang.InterruptedException
     */
    @Override
    public void displayPartialTree() throws InterruptedException {
        repaint();
        ucontrol.animateDelay();
    }

    /**
     * Interesting event: display distance calculations
     *
     * @throws java.lang.InterruptedException
     */
    @Override
    /**
     * Interesting event: display distance calculations
     */
    public void displayDistances() throws InterruptedException {
        System.out.println("displayDistances not implemented!");

    } // do nothing for now
    // when we have a distance display, use this to update

    /**
     * Interesting event: display minimum distance node
     *
     * @param cn
     * @throws java.lang.InterruptedException
     */
    @Override
    public void displayClosestNode(STNode cn) throws InterruptedException {
        ugr.selectNode(cn); // select the closest node
        repaint();
        ucontrol.animateDelay();
        ugr.selectNode(null);
    }


    /*----------------------------------------------------------------------*/
 /*        ActionListener method                                         */
 /*----------------------------------------------------------------------*/
    @Override
    public synchronized void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if (cmd == "AUTO") {
            if (autoButton.isSelected()) {
                setText("Auto Mode");
                autoMode = true;
                ucontrol.disableAnimation();
                ugr.selectNode(null);
                try {
                    table.setToolTipText("This is not available for auto mode");
                        prim.primMST(false);
                } catch (InterruptedException except) {
                }
                repaint();
            } else {
                setText("Click to create nodes");
                table.setToolTipText("");
                autoMode = false;
                ucontrol.enableAnimation();
            }
        }
    }

    /*----------------------------------------------------------------------*/
 /*        UIGraphChangeListener method                                  */
 /*----------------------------------------------------------------------*/
    @Override
    public void graphChanged(boolean b) {
        if (autoMode) {
            try {
                if (!gr.isEmpty()) {
                    prim.primMST(false);
                } else {
                    gr.clearGraph();
                }
                repaint();
            } catch (InterruptedException e) {
            }
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
    public void creatGUI(){
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setLayout(new BorderLayout());
        f.setSize(1000, 600);
        f.getContentPane().add(this, BorderLayout.CENTER);
        f.getContentPane().add(this.table, BorderLayout.EAST);
        f.setVisible(true);
    }
    
    public static void main(String[] args) {
        UIPrimMST p = new UIPrimMST();
        p.creatGUI();
    }

    public void setText(String s) {
        msg = s;
        repaint();
    }

}


/**
 * @Author: John Nestor <nestorj>
 * @Date: 2020-06-24T21:01:04-04:00
 * @Email: nestorj@lafayette.edu
 * @Last modified by: nestorj
 * @Last modified time: 2020-06-24T21:01:04-04:00
 */
import java.lang.Math;
import java.awt.FlowLayout;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JFrame; // for unit test
import javax.swing.BorderFactory;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Graphics;
import java.awt.event.ItemEvent;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JToggleButton;

public class UISteinerBOI extends JPanel implements BOIInterface, UIAnimated, UIGraphChangeListener {

    private STGraph gr;
    private UIHananGraph ugr;
    private STPrimMST prim;  // needed for tree calculations
    private STBOI boi;

    private JPanel statusPanel;
    private JPanel controlPanel;
    private JLabel messageLabel;
    private JToggleButton steinerModeButton;
    private JCheckBox gridModBox = new JCheckBox("Modifiable",true);
    private JToggleButton clearSTBtn;

    private UIValDisplay halfPerimDisplay;
    private UIValDisplay lengthDisplay;
    private UIValDisplay improveDisplay;
    private UIValDisplay trailDisplay;

    private UIAnimationController ucontrol;

    private int numTrail = 1;

    public static final long serialVersionUID = 1L;  // to shut up serialization warning

    public UISteinerBOI() {
        super();
        setLayout(new BorderLayout());
        gr = new STGraph();
        ugr = new UIHananGraph(gr, this);
        prim = new STPrimMST(null, gr);  // no animation callbacks for MST
        boi = new STBOI(this, gr);
        ucontrol = new UIAnimationController(this);
        steinerModeButton = new JToggleButton("HGrid");
        steinerModeButton.setToolTipText("show Hanan Grids");
        steinerModeButton.setActionCommand("STMODE");
        steinerModeButton.addActionListener(this::showHananGrids);
        clearSTBtn=new JToggleButton(new ImageIcon(getClass().getResource("images/clearST.gif")));
        gridModBox.addItemListener(this::setModifiable);
        clearSTBtn.addActionListener(this::clearST);
        clearSTBtn.setToolTipText("Remove all Steiner Nodes");
        
        statusPanel = new JPanel();
        statusPanel.setLayout(new GridLayout(1, 2));
        halfPerimDisplay = new UIValDisplay("Half Perimeter", 0);
        statusPanel.add(halfPerimDisplay);
        lengthDisplay = new UIValDisplay("Edge Length", 0);
        statusPanel.add(lengthDisplay);
        improveDisplay = new UIValDisplay("Improvement", 1, 1, 2);
        statusPanel.add(improveDisplay);
        trailDisplay = new UIValDisplay("Current Pass", 1);
        statusPanel.add(trailDisplay);
        add(statusPanel, BorderLayout.NORTH);
        add(ugr, BorderLayout.CENTER);

        controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        messageLabel = new JLabel("");
        messageLabel.setPreferredSize(new Dimension(250,25));
        controlPanel.add(messageLabel);
        controlPanel.add(ucontrol);
        controlPanel.add(clearSTBtn);
        controlPanel.add(steinerModeButton);
        controlPanel.add(gridModBox);
        add(controlPanel, BorderLayout.SOUTH);

        setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

    }

    public void setCostDisplay() {
        halfPerimDisplay.setValue(gr.halfPerim());
        lengthDisplay.setValue(gr.edgeLength());
        improveDisplay.setValue((float) ugr.getPastLength() / gr.edgeLength());
        if(boi.isComplete()) numTrail++;
        trailDisplay.setValue(numTrail);
        for(STNode n : gr.getNodes()){
            if(!n.isTerminal())
                System.out.println(n.toString());
        }
    }

    public void setMessage(String m) {
        messageLabel.setText(m);
    }

    public void initRandom() {
        int width = ugr.getWidth();
        int height = ugr.getHeight();
        gr.clearGraph();
        gr.addRandomNodes(10, width, height); // change to use range of graphics window
        try {
            prim.primMST(false);
            ugr.setPastLength(gr.edgeLength());
        } catch (InterruptedException e) {
        }
    }

    public void readGraph(BufferedReader in) throws IOException {
        gr.readGraph(in);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        setCostDisplay();
    }
    
     /*----------------------------------------------------------------------*/
 /*        ActionListener method                                         */
 /*----------------------------------------------------------------------*/
    public synchronized void showHananGrids(ActionEvent e) {
        String cmd = e.getActionCommand();
        if (cmd == "STMODE") {
            if (steinerModeButton.isSelected()) {
                steinerModeButton.setToolTipText("Editing Steiner Points - Click to Edit Terminals");
                ugr.setHananMode(true);
                ugr.selectNode(null);            
            } else {
                steinerModeButton.setToolTipText("Editing Terminals - Click to Edit Steiner Points");
                ugr.setHananMode(false);
            }
             repaint();
        }
    }
    
    public synchronized void setModifiable(ItemEvent e){
        ugr.enableModification(e.getStateChange() == 1);
    }
    
    public void clearST(ActionEvent e){
        stopAnimation();
        gr.removeNonTerminalNodes();
        graphChanged(true);
        ugr.setPastLength(gr.edgeLength());
        repaint();
        clearSTBtn.setSelected(false);
    }

    /*----------------------------------------------------------------------*/
 /*        methods from UIAnimated interface                             */
 /*----------------------------------------------------------------------*/
 /* from the new thread - use to call the algorithm code */
    public void runAnimation() throws InterruptedException {
        boi.empty();
        boi.improve(true);
        setCostDisplay();
    }

    /* use to clean up when animation is terminated */
    public void stopAnimation() {
        ugr.selectNEPair(null);
        setMessage("Click to create nodes");
        setCostDisplay();
        boi.empty();
        repaint();
    }

    public void clear() {
        boi.clear();
        ugr.selectNEPair(null);
        numTrail = 1;
        improveDisplay.setString("NaN");
    }

    public void step() throws InterruptedException {
        ucontrol.animateDelay();
    }

    /*----------------------------------------------------------------------*/
 /*        methods from BOIInterface                                     */
 /*----------------------------------------------------------------------*/
    /**
     * Interesting event: initialization
     */
    public void showBOIInit() throws InterruptedException {
        ugr.selectNode(null);
        ugr.selectEdge(null);
        setMessage("Starting BOI improve phase");
        repaint();
        ucontrol.animateDelay();
        setMessage(null);
    }

    /**
     * Interesting event: show a node/edge candidate
     */
    public void showNEPair(STNEPair p) throws InterruptedException {
        System.out.println("showNEPair - node " + p.getNode() + " edge " + p.getEdge());
        ugr.selectNEPair(null);  // don't display until calculating gain!
        ugr.selectNode(p.getNode());
        ugr.selectEdge(p.getEdge());
        setMessage("Pair Candidate");
        repaint();
        //	setMessage("Pair Candidate " + p.getNode() + " " + p.getEdge());
        ucontrol.animateDelay();
        //	ugr.selectNode(null);
        //	ugr.selectEdge(null);
        //	setMessage(null);
    }

    /**
     * Interesting event: show the edges that contribute to the gain of a
     * node/edge candidate
     */
    public void showNEGain(STNEPair p) throws InterruptedException {
        System.out.println("showNEGain - node " + p.getNode() + " elimEdge " + p.getElimEdge() + " gain " + p.getGain());
        ugr.selectNode(p.getNode());
        ugr.selectEdge(p.getElimEdge());
        ugr.selectNEPair(p);
        setMessage("Node-edge candidate:  n" + p.getNode().getID() + " / e" + p.getElimEdge().getID() + " gain: " + p.getGain());
        repaint();
        ucontrol.animateDelay();
    }

    /**
     * Interesting event: show replacement of node/edge pair with SP & new edges
     */
    public void showNEMod(STNEPair p) throws InterruptedException {
        ugr.selectNode(null);
        ugr.selectEdge(null);
        ugr.selectNEPair(p);
        setMessage("Applying modification n" + p.getNode().getID() + " / e" + p.getElimEdge().getID() + " gain=" + p.getGain());
        repaint();
        ucontrol.animateDelay();
    }

    /**
     * Interesting event: show completion of node/edge pair w/ deletion of loop
     * edge
     */
    public void showNEModComplete() throws InterruptedException {
        ugr.selectNEPair(null);
        setMessage("Modification complete");
        repaint();
        ucontrol.animateDelay();
    }

    /**
     * Interesting event: BOI Completed
     */
    public void showBOIComplete(boolean modified) throws InterruptedException {
        ugr.selectNEPair(null);
        if (modified) {
            setMessage("Pass completed");
        } else {
            setMessage("Pass completed (no improvemnt)");
        }
        repaint();
        ucontrol.animateDelay();
    }

    /**
     * Interesting event: display distance calculations
     */
    public void displayDistances() throws InterruptedException {
    } // do nothing for now
    // when we have a distance display, use this to update

    /**
     * Interesting event: display minimum distance node
     */
    public void displayClosestNode(STNode cn) throws InterruptedException {
        ugr.selectNode(cn); // select the closest node
        repaint();
        ucontrol.animateDelay();
        ugr.selectNode(null);
    }
    
    public void showAccept() throws InterruptedException{
        ugr.setColor(Color.yellow);
        repaint();
        ucontrol.animateDelay();
        ugr.setColor(Color.gray);
    }
    
    public void showDeny() throws InterruptedException{
        ugr.setColor(Color.red);
        repaint();
        ucontrol.animateDelay();
        ugr.setColor(Color.gray);
    }
    
    public void showApply() throws InterruptedException{
        ugr.setColor(Color.green);
        repaint();
        ucontrol.animateDelay();
        ugr.setColor(Color.gray);
    }

    /*----------------------------------------------------------------------*/
 /*        UIGraphChangeListener method                                  */
 /*----------------------------------------------------------------------*/
    @Override
    public void graphChanged(boolean changed) {
        if(changed)
        numTrail = 1;
        ucontrol.interruptAnimation();
        boi.empty();
        boi.clearMods();
        try {
            prim.primMST(false);
        } catch (InterruptedException e) {
        }

        /*	if (autoMode) { // re-calculate immediately
      try {
      prim.primMST(false);
    } catch (InterruptedException e) { }
  } else {  // can't continue animaton with a changed graph!
  ucontrol.interruptAnimation();
  gr.clearEdges();
  gr.clearVisited(); */
    }

    /*----------------------------------------------------------------------*/
 /*        main() / unit test                                            */
 /*----------------------------------------------------------------------*/
    public static void main(String[] args) {
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setLayout(new BorderLayout());
        UISteinerBOI p = new UISteinerBOI();
        f.setSize(1000, 600);
        f.getContentPane().add(p, BorderLayout.CENTER);
        f.getContentPane().add(p.boi.table, BorderLayout.EAST);
        f.setVisible(true);
        p.initRandom();
    }

}


/**
 * @Author: John Nestor <nestorj>
 * @Date: 2020-06-24T21:00:17-04:00
 * @Email: nestorj@lafayette.edu
 * @Last modified by: nestorj
 * @Last modified time: 2020-06-24T21:00:17-04:00
 */
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
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.*;
import java.net.*;
import javax.swing.ImageIcon;

public class UISteinerDemo extends JPanel implements ActionListener, UIGraphChangeListener{

    private STGraph gr;
    private UIHananGraph ugr;
    private STPrimMST prim; // used in non-interactive mode to calculate RMST

    private JPanel statusPanel;
    private JPanel controlPanel;

    private UIValDisplay halfPerimDisplay;
    private UIValDisplay lengthDisplay;
    private UIValDisplay improveDisplay;

    private JToggleButton steinerModeButton;
    private JToggleButton clearButton;
    private JToggleButton clearSTBtn;

    private boolean steinerMode = false;

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
        clearButton = new JToggleButton(new ImageIcon(getClass().getResource("images/clear.gif")));
        clearButton.setToolTipText("Clear all nodes");
        clearButton.setActionCommand("CLEAR");
        clearButton.addActionListener(this);
        clearSTBtn=new JToggleButton(new ImageIcon(getClass().getResource("images/clearST.gif")));
        clearSTBtn.addActionListener(this::clearST);
        clearSTBtn.setToolTipText("Remove all Steiner Nodes");

        statusPanel = new JPanel();
        statusPanel.setLayout(new GridLayout(1, 2));
        halfPerimDisplay = new UIValDisplay("Half Perimeter", 0);
        halfPerimDisplay.setToolTipText("Half Perimeter Length Estimate");
        statusPanel.add(halfPerimDisplay);
        lengthDisplay = new UIValDisplay("Edge Length", 0);
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
        controlPanel.add(clearButton);
        controlPanel.add(clearSTBtn);
        add(controlPanel, BorderLayout.SOUTH);

        setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

    }

    public void setCostDisplay() {
        halfPerimDisplay.setValue(gr.halfPerim());
        lengthDisplay.setValue(gr.edgeLength());
        if (steinerMode) {
            improveDisplay.setValue((double) ugr.getPastLength() / (double) gr.edgeLength());
        } else {
            improveDisplay.setValue(1.0);
        }
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
        } catch (InterruptedException e) {
        }
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
                if (gr.isEmpty()) {
                    steinerMode = false;
                    steinerModeButton.setToolTipText("Editing Terminals - Click to Edit Steiner Points");
                    ugr.setHananMode(false);
                    steinerModeButton.setSelected(false);
                } else{
                try {
                    prim.primMST(false);
                } catch (InterruptedException except) {
                }
                }
                repaint();
            } else {
                steinerMode = false;
                steinerModeButton.setToolTipText("Editing Terminals - Click to Edit Steiner Points");
                ugr.setHananMode(false);
                gr.removeNonTerminalNodes();
                try {
                    prim.primMST(false);
                } catch (InterruptedException except) {
                }
                repaint();
            }
        } else if (cmd == "CLEAR") {
            ugr.clear();
            ugr.repaint();
            revalidate();
            repaint();
            clearButton.setSelected(false);
            steinerMode = false;
            ugr.setHananMode(false);
            steinerModeButton.setSelected(false);
        }
    }

    /*----------------------------------------------------------------------*/
 /*        UIGraphChangeListener method                                  */
 /*----------------------------------------------------------------------*/
    public void graphChanged(boolean b) {
        try {
            if (!gr.isEmpty()) {
                prim.primMST(false);
            } else {
                ugr.clear();
            }
            repaint();
        } catch (InterruptedException e) {
        }
    }

    /*----------------------------------------------------------------------*/
 /*        main() / unit test                                            */
 /*----------------------------------------------------------------------*/
    public static void main(String[] args) {
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        UISteinerDemo d = new UISteinerDemo();
        f.setSize(600, 600);
        f.getContentPane().add(d);
        f.setVisible(true);
    }

    public void clearST(ActionEvent e){
        gr.removeNonTerminalNodes();
        graphChanged(true);
        ugr.setPastLength(gr.edgeLength());
        repaint();
        clearSTBtn.setSelected(false);
    }
    
    @Override
    public void repaint(){
        super.repaint();
    }
}

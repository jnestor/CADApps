/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pathfinder_demo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.LinkedList;
import javax.swing.JFrame;
import static pathfinder_demo.UIGraph.IP;
import static pathfinder_demo.UIGraph.LB;
import static pathfinder_demo.UIGraph.OP;
import static pathfinder_demo.UIGraph.SB;
import static pathfinder_demo.UIGraph.SW;
import static pathfinder_demo.UIGraph.TM;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import static pathfinder_demo.UIGraph.IP;
import static pathfinder_demo.UIGraph.SW;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;

/**
 *
 * @author 15002
 */
public class PathFinderFrame_demo extends JFrame /*implements Runnable*/ {

    private static Timer timer;
    private int delay = 400;

    CopyOnWriteArrayList<PFNet> nets = new CopyOnWriteArrayList<PFNet>();
    private UIPathFinder p;
    RoutibilityPathFinder router;

    private UIGraph graph;

    private static final int DONE = 0;
    private static final int EXPANDING = 1;

    private final JLabel msgBoard = new JLabel();
    private final JLabel title = new JLabel("PathFinder Algorithm", SwingConstants.CENTER);
    private final ImageIcon pause = new ImageIcon(getClass().getResource("images/pause.gif"));
    private final ImageIcon resume = new ImageIcon(getClass().getResource("images/start.gif"));
//    private final JToggleButton clearBtn = new JToggleButton(new ImageIcon(getClass().getResource("images/clear.png")));
    private final JToggleButton pauseBtn = new JToggleButton(pause);
//    private final JToggleButton stopBtn = new JToggleButton(new ImageIcon(getClass().getResource("images/stop.gif")));
    private final JToggleButton stepBtn = new JToggleButton(new ImageIcon(getClass().getResource("images/step.gif")));
//    private final JToggleButton creanetBtn = new JToggleButton("new net");
    private final JButton resizeWindowBtn = new JButton("RESIZE");
    private JDialog resizeWindow = new JDialog(this, "Resize Option", true);
    private WarningDialog resizeWarning = new WarningDialog("Resize the Router will clear all the grids, "
            + "are you sure you want to resize");
    private WarningDialog clearWarning = new WarningDialog("This will clear all the grids, are you "
            + "sure you want to do this");
    private JSlider speedSlider = new JSlider(10, 800, 400);

    private final JCheckBox hValBox = new JCheckBox("h(n) Heatmap");
    private final JCheckBox penaltyBox = new JCheckBox("Penalty Heatmap");
    private boolean hBoxSw = false;
    private boolean pBoxSw = true;
    


    public PathFinderFrame_demo(int w, int h) {
        title.setFont(new Font("Bold", Font.PLAIN, 25));
        title.setBorder(new EmptyBorder(10, 10, 10, 20));
        p = new UIPathFinder(w, h);
        router = new RoutibilityPathFinder(nets, p.getNodes(), p.getGraph());
        timer = router.getRoutingTimer();
        graph = p.getGraph();
        setLayout(new BorderLayout());
        getContentPane().add(p.getGraph(), "Center");
        getContentPane().add(title, "North");
        JPanel btnPanel = new JPanel();
//        clearBtn.addActionListener(this::clearAction);
        pauseBtn.addActionListener(this::pauseAction);
//        stopBtn.addActionListener(this::stopAction);
        stepBtn.addActionListener(this::stepAction);
        resizeWindowBtn.addActionListener(this::resizeAction);
        speedSlider.addChangeListener(this::speedChanged);

        btnPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        msgBoard.setPreferredSize(new Dimension(290, 25));
        pauseBtn.setPreferredSize(new Dimension(25, 25));
//        clearBtn.setPreferredSize(new Dimension(25, 25));
        stepBtn.setPreferredSize(new Dimension(25, 25));
//        stopBtn.setPreferredSize(new Dimension(25, 25));
        resizeWindowBtn.setPreferredSize(new Dimension(90, 25));
        speedSlider.setPreferredSize(new Dimension(160, 25));
        speedSlider.setToolTipText("Change the speed of the expansion");
        pauseBtn.setToolTipText("Pause");
        stepBtn.setToolTipText("Step");
//        stopBtn.setToolTipText("Stop");
//        clearBtn.setToolTipText("Delete everything on the screen");
        resizeWindowBtn.setToolTipText("Drag the window to your preferred size and "
                + "click on this button to refill the entire window");
        hValBox.addItemListener(this::hBoxAction);
        penaltyBox.addItemListener(this::pBoxAction);

        btnPanel.add(msgBoard);
//        btnPanel.add(creanetBtn);
        btnPanel.add(pauseBtn);
        btnPanel.add(stepBtn);
//        btnPanel.add(stopBtn);
//        btnPanel.add(clearBtn);
//        btnPanel.add(resizeWindowBtn);
        btnPanel.add(penaltyBox);
        btnPanel.add(hValBox);
        btnPanel.add(speedSlider);

        getContentPane().add(btnPanel, "South");
//        refreshTimer.start();
        repaint();
        stepBtn.setEnabled(true);
        router.setPause(true);
        pauseBtn.setSelectedIcon(resume);
        pauseBtn.setSelected(true);
        pauseBtn.setToolTipText(router.isPause() ? "Start" : "Pause");
        refreshTimer.start();
    }

    public UIGraph getUIG() {
        return p.getGraph();
    }

    public static void main(String[] args) throws InterruptedException {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }

    /**
     * @param args the command line arguments
     */
    private static void createAndShowGUI() {

        PathFinderFrame_demo demo = new PathFinderFrame_demo(4, 3);
        CopyOnWriteArrayList<PFNet> nets = new CopyOnWriteArrayList<PFNet>();
        LinkedList<PFNode> sinks1 = new LinkedList<PFNode>();
        sinks1.add(demo.getP().getSinks()[1][0]);
        sinks1.add(demo.getP().getSinks()[1][1]);
        sinks1.add(demo.getP().getSinks()[0][0]);
        sinks1.add(demo.getP().getSinks()[2][1]);
        sinks1.add(demo.getP().getSinks()[2][0]);
        PFNode source1 = demo.getP().getSources()[2][0];
        PFNet net1 = new PFNet(sinks1, source1);
        net1.setColor(Color.yellow);

        LinkedList<PFNode> sinks2 = new LinkedList<PFNode>();
        sinks2.add(demo.getP().getSinks()[0][0]);
        sinks2.add(demo.getP().getSinks()[0][1]);

        sinks2.add(demo.getP().getSinks()[2][0]);
        sinks2.add(demo.getP().getSinks()[2][1]);
        PFNode source2 = demo.getP().getSources()[0][1];
        PFNet net2 = new PFNet(sinks2, source2);
        net2.setColor(Color.blue);

        LinkedList<PFNode> sinks3 = new LinkedList<PFNode>();
        sinks3.add(demo.getP().getSinks()[1][0]);
        sinks3.add(demo.getP().getSinks()[0][0]);
        sinks3.add(demo.getP().getSinks()[2][0]);
        sinks3.add(demo.getP().getSinks()[0][1]);
        PFNode source3 = demo.getP().getSources()[0][0];
        PFNet net3 = new PFNet(sinks3, source3);
        net3.setColor(Color.green);

        LinkedList<PFNode> sinks4 = new LinkedList<PFNode>();
        sinks4.add(demo.getP().getSinks()[1][0]);
//        sinks4.add(demo.getP().getSinks()[2][1]);
        PFNode source4 = demo.getP().getSources()[2][1];
        PFNet net4 = new PFNet(sinks4, source4);
        net4.setColor(Color.cyan);

        LinkedList<PFNode> sinks5 = new LinkedList<PFNode>();
        sinks5.add(demo.getP().getSinks()[1][0]);
//        sinks5.add(demo.getP().getSinks()[2][0]);

        sinks5.add(demo.getP().getSinks()[2][0]);
        sinks5.add(demo.getP().getSinks()[2][1]);
//        sinks5.add(demo.getP().getSinks()[2][1]);
        sinks5.add(demo.getP().getSinks()[1][1]);
        PFNode source5 = demo.getP().getSources()[1][0];
        PFNet net5 = new PFNet(sinks5, source5);
        net5.setColor(new Color(138, 43, 226));

        LinkedList<PFNode> sinks6 = new LinkedList<PFNode>();
        sinks6.add(demo.getP().getSinks()[1][1]);
        sinks6.add(demo.getP().getSinks()[1][0]);
        sinks6.add(demo.getP().getSinks()[0][0]);
        sinks6.add(demo.getP().getSinks()[0][1]);

        PFNode source6 = demo.getP().getSources()[1][1];
        PFNet net6 = new PFNet(sinks6, source6);
        net6.setColor(Color.magenta);

        nets.add(net2);
        nets.add(net3);
        nets.add(net4);
        nets.add(net6);
        nets.add(net1);

        nets.add(net5);

        demo.nets = nets;
        demo.router.setNets(nets);

        JFrame f = demo;

        //f.add(demo);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(1030, 1000);
        f.setMinimumSize(new Dimension(1030, 1000));
        f.pack();
        f.setVisible(true);

//        demo.router.route();
    }

    public UIPathFinder getP() {
        return p;
    }

    private void pauseAction(ActionEvent evt) {
//        timer.setRepeats(false);
//        timer.start();
        if (graph.getState() == DONE) {
            System.out.println("first start");
            graph.setState(EXPANDING);
            router.restartReset();
            router.setPause(false);
            timer.setDelay(delay);
            timer.setRepeats(true);
            timer.start();
        } else {
            if (router.isPause()) {
                timer.setDelay(delay);
                timer.setRepeats(true);
                timer.start();
            }
            router.setPause(!router.isPause());
            pauseBtn.setSelectedIcon(router.isPause() ? resume : pause);
            pauseBtn.setToolTipText(router.isPause() ? "Start" : "Pause");
            System.out.println(router.isPause() ? "Start" : "Pause");
        }
    }

    private void stepAction(ActionEvent evt) {
        if (graph.getState() == DONE) {
            System.out.println("first step");
            graph.setState(EXPANDING);
            router.restartReset();
        }
        timer.setRepeats(false);
        timer.start();
//        stopBtn.setSelected(false);
    }

    private void stopAction(ActionEvent evt) {
//        if (myGrid.isPaused()) {
//            myGrid.pauseResume();
//        }
//        myGrid.stopRouter();
//        stopBtn.setSelected(false);
    }

//    private void clearAction(ActionEvent evt) {
//        Toolkit.getDefaultToolkit().beep();
//        int n = clearWarning.showConfirmDialog(this);
//        if (n == JOptionPane.YES_OPTION) {
//            router.resetAll();
//            p.getGraph().setMaxPenalty(0);
//            p.getGraph().repaint();
//        }
//        clearBtn.setSelected(false);
//    }
    private void resizeAction(ActionEvent evt) {
        resizeWindow.setVisible(true);
        resizeWindow.requestFocusInWindow();
    }

    private void hBoxAction(ItemEvent evt) {
        hBoxSw = evt.getStateChange() == 1;
        p.getGraph().setHSw(hBoxSw);
        p.getGraph().repaint();
    }

    private void pBoxAction(ItemEvent evt) {
        pBoxSw = evt.getStateChange() == 1;
        p.getGraph().setPSw(pBoxSw);
        p.getGraph().repaint();
    }

    public void speedChanged(ChangeEvent e) {
        JSlider source = (JSlider) e.getSource();
        if (!source.getValueIsAdjusting()) {
            int speed = (int) source.getValue();
            delay = speed;
            router.getRoutingTimer().setDelay(speed);
        }
    }

    Timer refreshTimer = new Timer(5, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            pauseBtn.setSelectedIcon(router.isPause() ? resume : pause);
            pauseBtn.setToolTipText(router.isPause() ? "Start" : "Pause");
            pauseBtn.setSelected(router.isPause());
//            msgBoard.setText(myGrid.getMSG());
            if (graph.getState() == DONE) {
                pauseBtn.setEnabled(true);
//                stopBtn.setEnabled(false);
                stepBtn.setEnabled(true);
//                clearBtn.setEnabled(true);
//                routerComboBox.setEnabled(true);
//                if (routerMode == 0) {
//                    parallelExpandBox.setVisible(true);
//                    parallelExpandBox.setEnabled(true);
//                } else {
//                    parallelExpandBox.setVisible(false);
//                    parallelExpandBox.setEnabled(false);
//                }
            } else if (graph.getState() == EXPANDING) {
                pauseBtn.setEnabled(true);
//                stopBtn.setEnabled(true);
                stepBtn.setEnabled(router.isPause());
//                clearBtn.setEnabled(false);
//                routerComboBox.setEnabled(false);
//                parallelExpandBox.setEnabled(false);
            }
//            else if (myGrid.getState() == EXPANDING) {
//                pauseBtn.setEnabled(true);
//                stopBtn.setEnabled(true);
//                stepBtn.setEnabled(myGrid.isPaused());
//                clearBtn.setEnabled(false);
//                routerComboBox.setEnabled(false);
//                parallelExpandBox.setEnabled(false);
//            } else if (myGrid.getState() == TRACKBACK) {
//                pauseBtn.setEnabled(true);
//                stopBtn.setEnabled(false);
//                stepBtn.setEnabled(myGrid.isPaused());
//                clearBtn.setEnabled(false);
//                routerComboBox.setEnabled(false);
//                parallelExpandBox.setEnabled(false);
//            }
        }
    }
    );
//    private void initiResizeWindow() {
//        this.setLocationByPlatform(true);
//        JTextField layerField = new JTextField("1");
//        JTextField gridSizeField = new JTextField("21");
//        layerField.setPreferredSize(new Dimension(45, 25));
//        gridSizeField.setPreferredSize(new Dimension(45, 25));
//        JLabel NoL = new JLabel("No of Layers");
//        JLabel GS = new JLabel("Grid Size");
//        JPanel panel = new JPanel();
//        JButton resizeBtn = new JButton("Resize");
//        panel.add(NoL);
//        panel.add(layerField);
//        panel.add(GS);
//        panel.add(gridSizeField);
//        panel.add(resizeBtn);
//        resizeBtn.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent evt) {
//                Toolkit.getDefaultToolkit().beep();
//                int n = resizeWarning.showConfirmDialog(panel);
//                if (n == JOptionPane.YES_OPTION) {
//                    try {
//                        size = Integer.parseInt(gridSizeField.getText());
//                        nOL = Integer.parseInt(layerField.getText());
//                        if (nOL < 1) {
//                            Toolkit.getDefaultToolkit().beep();
//                            JOptionPane.showMessageDialog(
//                                    panel, "You must have at least one layer !", "Invalid Layer", JOptionPane.ERROR_MESSAGE);
//                        } else if (size <= 19) {
//                            Toolkit.getDefaultToolkit().beep();
//                            JOptionPane.showMessageDialog(
//                                    panel, "The grid size is too small, it should be at least 20", "Invalid Grid Size", JOptionPane.ERROR_MESSAGE);
//                        } else {
//                            resizeWindow.setVisible(false);
//                            resetGrids(size, nOL);
//                        }
//                    } catch (NumberFormatException e) {
//                        Toolkit.getDefaultToolkit().beep();
//                        JOptionPane.showMessageDialog(
//                                panel, "wrong input !", "Invalid Input", JOptionPane.ERROR_MESSAGE);
//                    }
//                }
//            }
//        });
//        resizeWindow.add(panel);
//        resizeWindow.pack();
//        resizeWindow.setLocationRelativeTo(this);
//    }

//    @Override
//    public void run() {
//        while (true) {
//            if (graph.getState() == DONE) {
//                router.route();
//            }
//        }
//    }
//    public void start() {
//        if (myThread == null) {
//            myThread = new Thread(this);
//            myThread.start();
//        }
//    }
}

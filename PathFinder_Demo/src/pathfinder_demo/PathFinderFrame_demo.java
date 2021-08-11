/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pathfinder_demo;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Scanner;
import javax.swing.JFrame;
import static pathfinder_demo.UIGraph.IP;
import static pathfinder_demo.UIGraph.LB;
import static pathfinder_demo.UIGraph.OP;
import static pathfinder_demo.UIGraph.SB;
import static pathfinder_demo.UIGraph.SW;
import static pathfinder_demo.UIGraph.TM;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import static pathfinder_demo.UIGraph.IP;
import static pathfinder_demo.UIGraph.SW;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.ToolTipManager;
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
    private UIPathFinder upf;
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
    private final JToggleButton stopBtn = new JToggleButton(new ImageIcon(getClass().getResource("images/stop.gif")));
    private final JToggleButton stepBtn = new JToggleButton(new ImageIcon(getClass().getResource("images/step.gif")));
    private final JToggleButton backBtn = new JToggleButton(new ImageIcon(getClass().getResource("images/back.gif")));
    private final JCheckBox tooltipBox = new JCheckBox("Display Cost of Nodes");
//    private final JToggleButton creanetBtn = new JToggleButton("new net");
    private final JButton resizeWindowBtn = new JButton("RESIZE");
    private final JButton openBtn = new JButton("Open");
    private JDialog resizeWindow = new JDialog(this, "Resize Option", true);
    private WarningDialog resizeWarning = new WarningDialog("Resize the Router will clear all the grids, "
            + "are you sure you want to resize");
    private WarningDialog clearWarning = new WarningDialog("This will clear all the grids, are you "
            + "sure you want to do this");
    private JSlider speedSlider = new JSlider(10, 800, 400);
    private File configuration;

    private JFileChooser fc;

    private final JCheckBox hValBox = new JCheckBox("h(n) Heatmap");
    private final JCheckBox penaltyBox = new JCheckBox("Penalty Heatmap");
    private boolean hBoxSw = false;
    private boolean pBoxSw = true;

    public PathFinderFrame_demo(int w, int h) {
        title.setFont(new Font("Bold", Font.PLAIN, 25));
        title.setBorder(new EmptyBorder(10, 10, 10, 20));

        setLayout(new BorderLayout());

        getContentPane().add(title, "North");
        JPanel btnPanel = new JPanel();
//        clearBtn.addActionListener(this::clearAction);
        fc = new JFileChooser();
        openBtn.addActionListener(this::openAction);
        pauseBtn.addActionListener(this::pauseAction);
        stopBtn.addActionListener(this::stopAction);
        backBtn.addActionListener(this::backAction);
        stepBtn.addActionListener(this::stepAction);
        resizeWindowBtn.addActionListener(this::resizeAction);
        speedSlider.addChangeListener(this::speedChanged);
        
        btnPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        msgBoard.setPreferredSize(new Dimension(290, 25));
        pauseBtn.setPreferredSize(new Dimension(25, 25));
//        clearBtn.setPreferredSize(new Dimension(25, 25));
        stepBtn.setPreferredSize(new Dimension(25, 25));
        backBtn.setPreferredSize(new Dimension(25, 25));
        stopBtn.setPreferredSize(new Dimension(25, 25));
        tooltipBox.addItemListener(this::traceAction);
        resizeWindowBtn.setPreferredSize(new Dimension(90, 25));
        speedSlider.setPreferredSize(new Dimension(160, 25));
        speedSlider.setToolTipText("Change the speed of the expansion");
        pauseBtn.setToolTipText("Pause");
        stepBtn.setToolTipText("Step");
        backBtn.setToolTipText("back");
        stopBtn.setToolTipText("Stop");
//        clearBtn.setToolTipText("Delete everything on the screen");
        resizeWindowBtn.setToolTipText("Drag the window to your preferred size and "
                + "click on this button to refill the entire window");
        hValBox.addItemListener(this::hBoxAction);
        penaltyBox.addItemListener(this::pBoxAction);

        btnPanel.add(msgBoard);
//        btnPanel.add(creanetBtn);
        btnPanel.add(openBtn);
        btnPanel.add(pauseBtn);
        btnPanel.add(backBtn);
        btnPanel.add(stepBtn);
        btnPanel.add(stopBtn);
//        btnPanel.add(clearBtn);
//        btnPanel.add(resizeWindowBtn);
        btnPanel.add(penaltyBox);
        btnPanel.add(hValBox);
        btnPanel.add(tooltipBox);
        btnPanel.add(speedSlider);

        getContentPane().add(btnPanel, "South");
//        refreshTimer.start();
        repaint();
        stepBtn.setEnabled(false);
        pauseBtn.setToolTipText("Start");
        refreshTimer.start();
        speedSlider.setEnabled(false);
        hValBox.setEnabled(false);
        pauseBtn.setEnabled(false);
        penaltyBox.setEnabled(false);
        stopBtn.setEnabled(false);
        tooltipBox.setEnabled(false);
        backBtn.setEnabled(false);
    }

    public UIGraph getUIG() {
        return upf.getGraph();
    }

    public static void main(String[] args) {
        createAndShowGUI();
    }

    /**
     * @param args the command line arguments
     */
    private static void createAndShowGUI() {

        PathFinderFrame_demo demo = new PathFinderFrame_demo(4, 3);
//        CopyOnWriteArrayList<PFNet> nets = new CopyOnWriteArrayList<PFNet>();
//        LinkedList<PFNode> sinks1 = new LinkedList<PFNode>();
//        sinks1.add(demo.getP().getSinks()[1][0]);
//        sinks1.add(demo.getP().getSinks()[1][1]);
//        sinks1.add(demo.getP().getSinks()[0][0]);
//        sinks1.add(demo.getP().getSinks()[2][1]);
//        sinks1.add(demo.getP().getSinks()[2][0]);
//        PFNode source1 = demo.getP().getSources()[2][0];
//        PFNet net1 = new PFNet(sinks1, source1);
//        net1.setColor(Color.yellow);
//
//        LinkedList<PFNode> sinks2 = new LinkedList<PFNode>();
//        sinks2.add(demo.getP().getSinks()[0][0]);
//        sinks2.add(demo.getP().getSinks()[0][1]);
//
//        sinks2.add(demo.getP().getSinks()[2][0]);
//        sinks2.add(demo.getP().getSinks()[2][1]);
//        PFNode source2 = demo.getP().getSources()[0][1];
//        PFNet net2 = new PFNet(sinks2, source2);
//        net2.setColor(Color.blue);
//
//        LinkedList<PFNode> sinks3 = new LinkedList<PFNode>();
//        sinks3.add(demo.getP().getSinks()[1][0]);
//        sinks3.add(demo.getP().getSinks()[0][0]);
//        sinks3.add(demo.getP().getSinks()[2][0]);
//        sinks3.add(demo.getP().getSinks()[0][1]);
//        PFNode source3 = demo.getP().getSources()[0][0];
//        PFNet net3 = new PFNet(sinks3, source3);
//        net3.setColor(Color.green);
//
//        LinkedList<PFNode> sinks4 = new LinkedList<PFNode>();
//        sinks4.add(demo.getP().getSinks()[1][0]);
////        sinks4.add(demo.getP().getSinks()[2][1]);
//        PFNode source4 = demo.getP().getSources()[2][1];
//        PFNet net4 = new PFNet(sinks4, source4);
//        net4.setColor(Color.cyan);
//
//        LinkedList<PFNode> sinks5 = new LinkedList<PFNode>();
//        sinks5.add(demo.getP().getSinks()[1][0]);
////        sinks5.add(demo.getP().getSinks()[2][0]);
//
//        sinks5.add(demo.getP().getSinks()[2][0]);
//        sinks5.add(demo.getP().getSinks()[2][1]);
////        sinks5.add(demo.getP().getSinks()[2][1]);
//        sinks5.add(demo.getP().getSinks()[1][1]);
//        PFNode source5 = demo.getP().getSources()[1][0];
//        PFNet net5 = new PFNet(sinks5, source5);
//        net5.setColor(new Color(138, 43, 226));
//
//        LinkedList<PFNode> sinks6 = new LinkedList<PFNode>();
//        sinks6.add(demo.getP().getSinks()[1][1]);
//        sinks6.add(demo.getP().getSinks()[1][0]);
//        sinks6.add(demo.getP().getSinks()[0][0]);
//        sinks6.add(demo.getP().getSinks()[0][1]);
//
//        PFNode source6 = demo.getP().getSources()[1][1];
//        PFNet net6 = new PFNet(sinks6, source6);
//        net6.setColor(Color.magenta);
//
//        nets.add(net2);
//        nets.add(net3);
//        nets.add(net4);
//        nets.add(net6);
//        nets.add(net1);
//
//        nets.add(net5);
//
//        demo.nets = nets;
//        demo.router.setNets(nets);

        JFrame f = demo;

        //f.add(demo);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(1030, 1000);
        f.setMinimumSize(new Dimension(1030, 1000));
//        f.pack();
        f.setVisible(true);

//        demo.router.route();
    }

    public UIPathFinder getP() {
        return upf;
    }

    private void openAction(ActionEvent evt) {
        if (configuration != null) {
            timer.stop();
            router.restartReset();
//            nets.clear();
            System.out.println("prereset");
        }
        int returnVal = fc.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            configuration = fc.getSelectedFile();
            if (configuration != null) {
                String name = configuration.getName();
                int index = name.lastIndexOf(".");
                String extension = name.substring(index + 1);

                if (extension.equals("csv")) {
                    readConfig();
                } else {
                    JOptionPane.showMessageDialog(this, "Wrong file type", "Invalid File", JOptionPane.ERROR_MESSAGE);
                }
            }
        }

//        else JOptionPane.showMessageDialog(this, "Null File", "Invalid File", JOptionPane.ERROR_MESSAGE);
    }

    private void readConfig() {
        ColorSequencer.reset();
        UIPathFinder tempPF = null;
        UIGraph tempGraph = null;
        CopyOnWriteArrayList<PFNet> netsTemp = new CopyOnWriteArrayList<PFNet>();
        try {
            fc = new JFileChooser(configuration.getAbsolutePath());
            Scanner fR = new Scanner(configuration);
            Scanner lR = new Scanner(fR.nextLine());
            if (!fR.hasNext()) {
                if (timer == null) {
                    configuration = null;
                }
                JOptionPane.showMessageDialog(this, "Wrong configuration file type", "Invalid File Type", JOptionPane.ERROR_MESSAGE);
                return;
            }
            lR = new Scanner(fR.nextLine());
            String[] a = lR.next().split(",");
//            System.out.println(a[0]);
            try {
                int w = Integer.parseInt(a[0]);
                int h = Integer.parseInt(a[1]);
                tempPF = new UIPathFinder(w, h);
                tempGraph = tempPF.getGraph();

            } catch (NumberFormatException e) {
                Toolkit.getDefaultToolkit().beep();
                JOptionPane.showMessageDialog(
                        this, "wrong width and height", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                if (timer == null) {
                    configuration = null;
                }
                return;
            }
            lR = new Scanner(fR.nextLine());
            if (fR.hasNext()) {

                while (fR.hasNext()) {
                    try {
                        lR = new Scanner(fR.nextLine());
                        a = lR.next().split(",");
                        ArrayList<String> netLocs = new ArrayList<String>(Arrays.asList(a));
                        Iterator arrIterator = netLocs.iterator();
                        arrIterator.next();
                        int x = Integer.parseInt((String) arrIterator.next());
                        int y = Integer.parseInt((String) arrIterator.next());
                        try {
                            PFNode source = tempPF.getSources()[x][y];
                            source.occupy();
                            LinkedList<PFNode> sinks = new LinkedList<PFNode>();
                            while (arrIterator.hasNext()) {
                                x = Integer.parseInt((String) arrIterator.next());
                                y = Integer.parseInt((String) arrIterator.next());
                                PFNode sink = tempPF.getSinks()[x][y];
                                sink.occupy();
                                sinks.add(sink);
                            }
                            if (sinks.isEmpty()) {
                                Toolkit.getDefaultToolkit().beep();
                                JOptionPane.showMessageDialog(
                                        this, "no sinks", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                                if (timer == null) {
                                    configuration = null;
                                }
                                return;
                            }
                            PFNet net = new PFNet(sinks, source);
                            net.setColor(ColorSequencer.next());
                            netsTemp.add(net);
                        } catch (IndexOutOfBoundsException e) {
                            Toolkit.getDefaultToolkit().beep();
                            JOptionPane.showMessageDialog(
                                    this, "wrong source or sink locations", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                            if (timer == null) {
                                configuration = null;
                            }
                            return;
                        }
                    } catch (NumberFormatException e) {
                        Toolkit.getDefaultToolkit().beep();
                        JOptionPane.showMessageDialog(
                                this, "wrong source or sink locations", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                        if (timer == null) {
                            configuration = null;
                        }
                        return;
                    }

                }

            } else {
                JOptionPane.showMessageDialog(
                        this, "No nets", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                configuration = null;
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PathFinderFrame_demo.class.getName()).log(Level.SEVERE, null, ex);
        }
        upf = tempPF;
        if (graph != null) {
            getContentPane().remove(graph);
        }
        graph = tempGraph;
        nets = netsTemp;
        router = new RoutibilityPathFinder(nets, upf.getNodes(), upf.getGraph(), msgBoard);
        timer = router.getRoutingTimer();
        getContentPane().add(upf.getGraph(), "Center");
        setVisible(true);
        router.setPause(true);
        msgBoard.setText("Press Start or Step to start routing");
        speedSlider.setEnabled(true);
        hValBox.setEnabled(true);
        hValBox.setSelected(false);
        pauseBtn.setEnabled(true);
        penaltyBox.setEnabled(true);
        penaltyBox.setSelected(false);
        stopBtn.setEnabled(true);
        tooltipBox.setSelected(false);
        tooltipBox.setEnabled(true);
//        System.out.println(p.getChanVer()[0][0].getID());
    }

    private void pauseAction(ActionEvent evt) {
//        timer.setRepeats(false);
//        timer.start();
        if (configuration != null) {
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
                    msgBoard.setText("Routing");
                } else {
                    msgBoard.setText("Paused");
                }
                router.setPause(!router.isPause());
                pauseBtn.setSelectedIcon(router.isPause() ? resume : pause);
                pauseBtn.setToolTipText(router.isPause() ? "Start" : "Pause");
                System.out.println(router.isPause() ? "Start" : "Pause");
            }
        }
    }

    private void stepAction(ActionEvent evt) {
        if (configuration != null) {
            if (graph.getState() == DONE) {
                System.out.println("first step");
                graph.setState(EXPANDING);
                router.restartReset();
            }
            msgBoard.setText("Routing");
            timer.setRepeats(false);
            timer.start();

//        stopBtn.setSelected(false);
        }
    }

    private void backAction(ActionEvent evt) {
        if (configuration != null) {
            router.backwardIterate();
            graph.repaint();
//        stopBtn.setSelected(false);
        }
    }
    
    private void stopAction(ActionEvent evt) {
        graph.setState(DONE);
        router.setPause(true);
        timer.stop();
        router.restartReset();
    }

    private void traceAction(ItemEvent evt) {
        if (evt.getStateChange() == 1) {
            ToolTipManager.sharedInstance().setEnabled(true);
            mouseTracer.setDelay(1000);
            mouseTracer.start();
        } else {
            mouseTracer.stop();
            ToolTipManager.sharedInstance().setEnabled(false);
        }
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
        if (configuration != null) {
            hBoxSw = evt.getStateChange() == 1;
            upf.getGraph().setHSw(hBoxSw);
            upf.getGraph().repaint();
        }
    }

    private void pBoxAction(ItemEvent evt) {
        if (configuration != null) {
            pBoxSw = evt.getStateChange() == 1;
            upf.getGraph().setPSw(pBoxSw);
            upf.getGraph().repaint();
        }
    }

    public void speedChanged(ChangeEvent e) {
        if (configuration != null) {
            JSlider source = (JSlider) e.getSource();
            if (!source.getValueIsAdjusting()) {
                int speed = (int) source.getValue();
                delay = 800 - speed;
                router.getRoutingTimer().setDelay(delay);
            }
        }
    }

    Timer refreshTimer = new Timer(5, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (router != null) {
                pauseBtn.setSelectedIcon(router.isPause() ? resume : pause);
                pauseBtn.setToolTipText(router.isPause() ? "Start" : "Pause");
                pauseBtn.setSelected(router.isPause());
//            msgBoard.setText(myGrid.getMSG());
                if (graph.getState() == DONE) {
                    pauseBtn.setEnabled(true);
//                stopBtn.setEnabled(false);
                    stepBtn.setEnabled(true);
                    backBtn.setEnabled(false);
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
                    backBtn.setEnabled(router.isPause());
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
            if (configuration == null) {
                msgBoard.setText("Open a configuration file to start routing");
            }
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

    Timer mouseTracer = new Timer(0, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (router != null) {
                Point point = MouseInfo.getPointerInfo().getLocation();
                SwingUtilities.convertPointFromScreen(point, graph);
                for (PFNode node : upf.getNodes()) {
                    if (node.getBlock().getDot().dotFound(point) && !node.getEdges().isEmpty()) {
                        graph.setToolTipText(node.getID()+"node type: " + node.getType() + " node cost: " + router.heatMapVal(node));
                        break;
                    } else {
                        graph.setToolTipText("");
                    }
                }
            }
        }
    });

}

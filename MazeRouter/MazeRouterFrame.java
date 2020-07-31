
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;

/**
 * Title: MazeApplet Description: Animation of Lee Algorithm for maze routing.
 * Copyright: Copyright (c) 2001 Company: Lafayette College
 *
 * @author John A. Nestor
 * @version 1.0
 */
public class MazeRouterFrame extends JFrame implements Runnable {

    boolean mute = false;
    private int max = 0;
    private static final int WAITFORSRC = 0;
    private static final int WAITFORTGT = 1;
    private static final int EXPANDING = 2;
    private static final int TRACKBACK = 3;
    private Grid myGrid = null;
    private Thread myThread = null;
    private int routerMode = 0;
    private final JLabel msgBoard = new JLabel();
    private final JLabel title = new JLabel("Lee Algorithm", SwingConstants.CENTER);
    private boolean parallel = false;
    private JSlider speedSlider = new JSlider(10, 100, 50);
    int size = 21;
    int nOL = 1;
    private final ImageIcon pause = new ImageIcon(getClass().getResource("/images/pause.gif"));
    private final ImageIcon resume = new ImageIcon(getClass().getResource("/images/start.gif"));
    private final JToggleButton clearBtn = new JToggleButton(new ImageIcon(getClass().getResource("/images/clear.png")));
    private final JToggleButton pauseBtn = new JToggleButton(new ImageIcon(getClass().getResource("/images/pause.gif")));
    private final JToggleButton stopBtn = new JToggleButton(new ImageIcon(getClass().getResource("images/stop.gif")));
    private final JToggleButton stepBtn = new JToggleButton(new ImageIcon(getClass().getResource("images/step.gif")));
    private final JButton resizeWindowBtn = new JButton("RESIZE");
    private final JCheckBox parallelExpandBox = new JCheckBox("Parallel Mode");
    private final JCheckBox tooltipBox = new JCheckBox("Tooltips for Grids");
    private final JCheckBox muteBox = new JCheckBox("Mute");
    private final String[] routerNames = {"Lee Algorithm", "Hadlock Algorithm", "A* Algorithm"};
    private JComboBox<String> routerComboBox = new JComboBox<String>(routerNames);
    private MazeRouter[] routerList = new MazeRouter[3];
    private JDialog resizeWindow = new JDialog(this, "Resize Option", true);
    private WarningDialog resizeWarning = new WarningDialog("Resize the Router will clear all the grids, are you sure you want to resize");
    private WarningDialog clearWarning = new WarningDialog("This will clear all the grids, are you sure you want to do this");
    Sound s = new Sound();

    public synchronized void initRouterFrame(int size, int nlayers) {
        beeper.start();
        routerMode = WAITFORSRC;
        setLayout(new BorderLayout());
        title.setFont(new Font("Bold", Font.PLAIN, 25));
        title.setBorder(new EmptyBorder(20, 0, 0, 0));
        msgBoard.setBorder(new EmptyBorder(0, 10, 0, 0));
        initAllGrids(size, nlayers);
        getContentPane().add(title, "North");
        clearBtn.addActionListener(this::clearAction);
        JPanel btnPanel = new JPanel();
        pauseBtn.addActionListener(this::pauseAction);
        stopBtn.addActionListener(this::stopAction);
        stepBtn.addActionListener(this::stepAction);
        routerComboBox.addActionListener(this::switchAction);
        parallelExpandBox.addItemListener(this::parallelBoxAction);
        tooltipBox.addItemListener(this::traceAction);
        muteBox.addItemListener(this::muteBoxAction);
        resizeWindowBtn.addActionListener(this::resizeAction);
        speedSlider.addChangeListener(this::speedChanged);
        btnPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        msgBoard.setPreferredSize(new Dimension(290, 25));
        pauseBtn.setPreferredSize(new Dimension(25, 25));
        clearBtn.setPreferredSize(new Dimension(25, 25));
        stepBtn.setPreferredSize(new Dimension(25, 25));
        stopBtn.setPreferredSize(new Dimension(25, 25));
        resizeWindowBtn.setPreferredSize(new Dimension(90, 25));
        speedSlider.setPreferredSize(new Dimension(160,25));
        pauseBtn.setToolTipText("Pause");
        stepBtn.setToolTipText("Step");
        stopBtn.setToolTipText("Stop");
        routerComboBox.setToolTipText("Choose the algorithm for routing");
        clearBtn.setToolTipText("Delete everything on the screen");
        speedSlider.setToolTipText("Change the speed of the expansion");
        resizeWindowBtn.setToolTipText("Drag the window to your preferred size and click on this button to refill the entire window");
        btnPanel.add(msgBoard);
        btnPanel.add(routerComboBox);
        btnPanel.add(pauseBtn);
        btnPanel.add(stepBtn);
        btnPanel.add(stopBtn);
        btnPanel.add(parallelExpandBox);
        //Uncomment the below line for bebugging
        //btnPanel.add(tooltipBox);
        btnPanel.add(muteBox);
        btnPanel.add(clearBtn);
        btnPanel.add(resizeWindowBtn);
        btnPanel.add(speedSlider);
        getContentPane().add(btnPanel, "South");
        initiResizeWindow();
        refreshTimer.start();
        repaint();
        start();
    }

    public void start() {
        if (myThread == null) {
            myThread = new Thread(this);
            myThread.start();
        }
    }

    public void stop() {
        myThread.interrupt();
        myThread = null;
    }
    //private static boolean paused = false;

    private void pauseAction(ActionEvent evt) {
        myGrid.pauseResume();
        pauseBtn.setSelectedIcon(myGrid.isPaused() ? resume : pause);
        pauseBtn.setToolTipText(myGrid.isPaused() ? "Start":"Pause");
    }
    
    private void stepAction(ActionEvent evt){
        myGrid.step();
        stopBtn.setSelected(false);
    }

    private void stopAction(ActionEvent evt) {
        if (myGrid.isPaused()) {
            myGrid.pauseResume();
        }
        myGrid.stopRouter();
        stopBtn.setSelected(false);
    }

    private void clearAction(ActionEvent evt) {
        Toolkit.getDefaultToolkit().beep();
        int n = clearWarning.showConfirmDialog(this);
        if(n==JOptionPane.YES_OPTION)
            myGrid.requestClear();
        clearBtn.setSelected(false);
    }

    private void switchAction(ActionEvent evt) {
        routerMode = routerComboBox.getSelectedIndex();
        myGrid.setRouter(routerList[routerMode]);
        title.setText(routerNames[routerMode]);
    }

    private void parallelBoxAction(ItemEvent evt) {
        parallel =evt.getStateChange() == 1;
        myGrid.setParallelExpand(parallel);
    }

    private void muteBoxAction(ItemEvent evt) {
        mute=(evt.getStateChange() == 1);
    }

    private void traceAction(ItemEvent evt) {
        myGrid.setParallelExpand(evt.getStateChange() == 1);
        if (evt.getStateChange() == 1) {
            ToolTipManager.sharedInstance().setEnabled(true);
            mouseTracer.start();
        } else {
            mouseTracer.stop();
            ToolTipManager.sharedInstance().setEnabled(false);
        }
    }

    private void resizeAction(ActionEvent evt) {
        resizeWindow.setVisible(true);
        resizeWindow.requestFocusInWindow();
    }
    
    public void speedChanged(ChangeEvent e) {
      JSlider source = (JSlider)e.getSource();
      if (!source.getValueIsAdjusting()) {
        int speed = (int)source.getValue();
	myGrid.setDelay(101-speed);
      }
    }

    Timer refreshTimer = new Timer(5, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            pauseBtn.setSelectedIcon(myGrid.isPaused() ? resume : pause);
            pauseBtn.setToolTipText(myGrid.isPaused() ? "Start":"Pause");
            pauseBtn.setSelected(myGrid.isPaused());
            msgBoard.setText(myGrid.getMSG());
            if (myGrid.getState() == WAITFORSRC) {
                pauseBtn.setEnabled(false);
                stopBtn.setEnabled(false);
                stepBtn.setEnabled(false);
                clearBtn.setEnabled(true);
                routerComboBox.setEnabled(true);
                if (routerMode == 0) {
                    parallelExpandBox.setVisible(true);
                    parallelExpandBox.setEnabled(true);
                } else {
                    parallelExpandBox.setVisible(false);
                    parallelExpandBox.setEnabled(false);
                }
            } else if (myGrid.getState() == WAITFORTGT) {
                pauseBtn.setEnabled(false);
                stopBtn.setEnabled(false);
                stepBtn.setEnabled(false);
                clearBtn.setEnabled(false);
                routerComboBox.setEnabled(false);
                parallelExpandBox.setEnabled(false);
            } else if (myGrid.getState() == EXPANDING) {
                pauseBtn.setEnabled(true);
                stopBtn.setEnabled(true);
                stepBtn.setEnabled(myGrid.isPaused());
                clearBtn.setEnabled(false);
                routerComboBox.setEnabled(false);
                parallelExpandBox.setEnabled(false);
            } else if (myGrid.getState() == TRACKBACK) {
                pauseBtn.setEnabled(true);
                stopBtn.setEnabled(false);
                stepBtn.setEnabled(myGrid.isPaused());
                clearBtn.setEnabled(false);
                routerComboBox.setEnabled(false);
                parallelExpandBox.setEnabled(false);
            }
        }
    });

    public void run() {
        try {
            myGrid.run();
        } catch (InterruptedException e) {
        }
    }

    Timer mouseTracer = new Timer(0, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            Point p = MouseInfo.getPointerInfo().getLocation();
            SwingUtilities.convertPointFromScreen(p, myGrid);
            GridPoint gp = myGrid.mouseToGridPoint((int) p.getX(), (int) p.getY());
            if (gp != null) {
                myGrid.setToolTipText(gp.toString());
            }
        }
    });

    Timer beeper = new Timer(10, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (!mute&&myGrid.getRouter().willBeep()) {
                myGrid.getRouter().stopBeep();
                s.play(myGrid.getRouter().getMax());
            }
        }
    });

    public boolean initAllGrids(int size, int nlayers) {
        Grid.resetGridSize(size);
        int ncols = Grid.calculateCols(getSize().width);
        int nrows = Grid.calculateRows(getSize().height, nlayers);
        myGrid = new Grid(ncols, nrows, nlayers);
        routerList[0] = new LeeRouter(myGrid);
        routerList[1] = new HadlockRouter(myGrid);
        routerList[2] = new AStarRouter(myGrid);
        myGrid.setRouter(routerList[routerMode]);
        getContentPane().add(myGrid, "Center");
        start();
        return true;
    }

    public void resetGrids(int size, int nlayers) {
        int originalSize = myGrid.getGridSize();
        Grid.resetGridSize(size);
        int ncols = Grid.calculateCols(getSize().width);
        int nrows = Grid.calculateRows(getSize().height, nlayers);
        if (nrows < 1 || ncols < 1) {
            JOptionPane.showMessageDialog(this, "There are too many layers and too large grids", "Too Many Layers", JOptionPane.ERROR_MESSAGE);
            Grid.resetGridSize(originalSize);
            return;
        }
        stop();
        getContentPane().remove(myGrid);
        initAllGrids(size, nlayers);
        myGrid.setParallelExpand(parallel);
        SwingUtilities.updateComponentTreeUI(this);
    }

    private void initiResizeWindow() {
        this.setLocationByPlatform(true);
        JTextField layerField = new JTextField("1");
        JTextField gridSizeField = new JTextField("21");
        layerField.setPreferredSize(new Dimension(45, 25));
        gridSizeField.setPreferredSize(new Dimension(45, 25));
        JLabel NoL = new JLabel("No of Layers");
        JLabel GS = new JLabel("Grid Size");
        JPanel panel = new JPanel();
        JButton resizeBtn = new JButton("Resize");
        panel.add(NoL);
        panel.add(layerField);
        panel.add(GS);
        panel.add(gridSizeField);
        panel.add(resizeBtn);
        resizeBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                Toolkit.getDefaultToolkit().beep();
                int n = resizeWarning.showConfirmDialog(panel);
                if (n == JOptionPane.YES_OPTION) {
                    try {
                        size = Integer.parseInt(gridSizeField.getText());
                        nOL = Integer.parseInt(layerField.getText());
                        if (nOL < 1) {
                            Toolkit.getDefaultToolkit().beep();
                            JOptionPane.showMessageDialog(
                                    panel, "You must have at least one layer !", "Invalid Layer", JOptionPane.ERROR_MESSAGE);
                        } else if (size <= 19) {
                            Toolkit.getDefaultToolkit().beep();
                            JOptionPane.showMessageDialog(
                                    panel, "The grid size is too small, it should be at least 20", "Invalid Grid Size", JOptionPane.ERROR_MESSAGE);
                        } else {
                            resizeWindow.setVisible(false);
                            resetGrids(size, nOL);
                        }
                    } catch (NumberFormatException e) {
                        Toolkit.getDefaultToolkit().beep();
                        JOptionPane.showMessageDialog(
                                panel, "wrong input !", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        resizeWindow.add(panel);
        resizeWindow.pack();
        resizeWindow.setLocationRelativeTo(this);
    }

}

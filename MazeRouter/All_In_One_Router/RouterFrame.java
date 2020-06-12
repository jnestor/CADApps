
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * Title: MazeApplet Description: Animation of Lee Algorithm for maze routing.
 * Copyright: Copyright (c) 2001 Company: Lafayette College
 *
 * @author John A. Nestor
 * @version 1.0
 */
public class RouterFrame extends JFrame implements Runnable {

    private static final int WAITFORSRC = 0;
    private static final int WAITFORTGT = 1;
    private static final int EXPANDING = 2;
    private static final int TRACKBACK = 3;
    private Grid myGrid = null;
    private Thread myThread = null;
    private int routerMode = 0;
    private final JLabel msgBoard = new JLabel();
    private final JLabel title = new JLabel("Maze Router", SwingConstants.CENTER);
    private final JLabel NoL = new JLabel("No of Layers");
    private final JLabel GS = new JLabel("Grid Size");
    //private final JLabel 
    private final JButton clearBtn = new JButton("CLEAR");
    private final JButton pauseBtn = new JButton("");
    private final JButton stopBtn = new JButton("STOP");
    private final JCheckBox parallelExpandBox = new JCheckBox("Parallel Mode");
    private final JCheckBox tooltipBox = new JCheckBox("Tooltips for Grids");
    private final String[] routerNames = {"Maze Router", "Hadlock Router", "A* Router"};
    private JComboBox<String> routerComboBox = new JComboBox<String>(routerNames);
    private Router[] routerList = new Router[3];
    private JTextField layerField = new JTextField("1");
    private JTextField gridSizeField = new JTextField("21");
    private JButton resizeBtn = new JButton("Resize");

    public synchronized void initRouterFrame(int size, int nlayers) {
        routerMode = 0;
        setLayout(new BorderLayout());
        title.setFont(new Font("Serif", Font.PLAIN, 25));
        JLabel ghostLabel = new JLabel();
        ghostLabel.setPreferredSize(new Dimension(10, 25));
        initAllGrids(size, nlayers);
        getContentPane().add(title, "North");
        clearBtn.addActionListener(this::clearAction);
        JPanel btnPanel = new JPanel();
        pauseBtn.addActionListener(this::pauseAction);
        stopBtn.addActionListener(this::stopAction);
        routerComboBox.addActionListener(this::switchAction);
        parallelExpandBox.addItemListener(this::checkBoxAction);
        tooltipBox.addItemListener(this::traceAction);
        resizeBtn.addActionListener(this::resizeAction);
        btnPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        msgBoard.setPreferredSize(new Dimension(270, 25));
        pauseBtn.setPreferredSize(new Dimension(90, 25));
        clearBtn.setPreferredSize(new Dimension(90, 25));
        stopBtn.setPreferredSize(new Dimension(90, 25));
        layerField.setPreferredSize(new Dimension(45, 25));
        gridSizeField.setPreferredSize(new Dimension(45, 25));
        btnPanel.add(ghostLabel);
        btnPanel.add(msgBoard);
        btnPanel.add(clearBtn);
        btnPanel.add(pauseBtn);
        btnPanel.add(stopBtn);
        btnPanel.add(routerComboBox);
        btnPanel.add(parallelExpandBox);
        btnPanel.add(tooltipBox);
        btnPanel.add(NoL);
        btnPanel.add(layerField);
        btnPanel.add(GS);
        btnPanel.add(gridSizeField);
        btnPanel.add(resizeBtn);
        getContentPane().add(btnPanel, "South");
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
        System.out.println(myGrid.isPaused());
        myGrid.pauseResume();
        pauseBtn.setText(myGrid.isPaused() ? "RESUME" : "PAUSE");
    }

    private void stopAction(ActionEvent evt) {
        System.out.println("stop");
        if (myGrid.isPaused()) {
            myGrid.pauseResume();
        }
        myGrid.stopRouter();
    }

    private void clearAction(ActionEvent evt) {
        myGrid.requestClear();
    }

    private void switchAction(ActionEvent evt) {
        routerMode = routerComboBox.getSelectedIndex();
        myGrid.setRouter(routerList[routerMode]);
        title.setText(routerNames[routerMode]);
    }

    private void checkBoxAction(ItemEvent evt) {
        myGrid.setParallelExpand(evt.getStateChange() == 1);
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
        int n = JOptionPane.showConfirmDialog(
                this, "Resize the Router will clear all the grids, are you sure you want to resize",
                "WARNING",
                JOptionPane.YES_NO_OPTION);
        if (n == JOptionPane.YES_OPTION) {
            try {
                int size = Integer.parseInt(gridSizeField.getText());
                int nOL = Integer.parseInt(layerField.getText());
                if (nOL < 1) {
                    JOptionPane.showMessageDialog(
                            this, "You must have at least one layer !");
                } else if (size <= 19) {
                    JOptionPane.showMessageDialog(
                            this, "The grid size is too small, it should be at least 20");
                } else {
                    resetGrids(size,nOL);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(
                        this, "wrong input !");
            }
        }
    }

    Timer refreshTimer = new Timer(5, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            pauseBtn.setText(myGrid.isPaused() ? "RESUME" : "PAUSE");
            msgBoard.setText(myGrid.getMSG());
            if (myGrid.getState() == WAITFORSRC) {
                pauseBtn.setEnabled(false);
                stopBtn.setEnabled(false);
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
                clearBtn.setEnabled(false);
                routerComboBox.setEnabled(false);
                parallelExpandBox.setEnabled(false);
            } else if (myGrid.getState() == EXPANDING) {
                pauseBtn.setEnabled(true);
                stopBtn.setEnabled(true);
                clearBtn.setEnabled(false);
                routerComboBox.setEnabled(false);
                parallelExpandBox.setEnabled(false);
            } else if (myGrid.getState() == TRACKBACK) {
                pauseBtn.setEnabled(true);
                stopBtn.setEnabled(false);
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

    public boolean initAllGrids(int size, int nlayers) {
        Grid.resetGridSize(size);
        int ncols = Grid.calculateCols(getSize().width);
        int nrows = Grid.calculateRows(getSize().height, nlayers);
        System.out.println("width=" + getSize().width + " height=" + getSize().height);
        myGrid = new Grid(ncols, nrows, nlayers);
        routerList[0] = new MazeRouter(myGrid);
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
        if(nrows<1||ncols<1){
            JOptionPane.showMessageDialog(this, "There are too many layers and too large grids");
            Grid.resetGridSize(originalSize);
            return;
        }
        stop();
        getContentPane().remove(myGrid);
        initAllGrids(size,nlayers);
    }
}


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
    private final JLabel title = new JLabel("Maze Router",SwingConstants.CENTER);
    //private final JLabel 
    private final JButton clearBtn = new JButton("CLEAR");
    private final JButton pauseBtn = new JButton("");
    private final JButton stopBtn = new JButton("STOP");
    private final JCheckBox parallelExpandBox = new JCheckBox("Parallel Mode");
    private final String[] routerNames = {"Maze Router", "Hadlock Router", "A* Router"};
    private JComboBox<String> routerComboBox = new JComboBox<String>(routerNames);
    private Router[] routerList = new Router[3];

    public synchronized void initMazeRouterFrame(int nlayers) {
        title.setFont(new Font("Serif", Font.PLAIN, 25));
        JLabel ghostLabel = new JLabel();
        ghostLabel.setPreferredSize(new Dimension(10, 25));
        int ncols = Grid.calculateCols(getSize().width);
        int nrows = Grid.calculateRows(getSize().height, nlayers);
        System.out.println("width=" + getSize().width + " height=" + getSize().height);
        myGrid = new Grid(ncols, nrows, nlayers);
        routerList[0] = new MazeRouter(myGrid);
        routerList[1] = new HadlockRouter(myGrid);
        routerList[2] = new AStarRouter(myGrid);
        myGrid.setRouter(routerList[routerMode]);
        setLayout(new BorderLayout());
        getContentPane().add(title,"North");
        getContentPane().add(myGrid, "Center");
        clearBtn.addActionListener(this::clearAction);
        JPanel btnPanel = new JPanel();
        pauseBtn.addActionListener(this::pauseAction);
        stopBtn.addActionListener(this::stopAction);
        routerComboBox.addActionListener(this::switchAction);
        parallelExpandBox.addItemListener(this::checkBoxAction);
        btnPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        msgBoard.setPreferredSize(new Dimension(270, 25));
        pauseBtn.setPreferredSize(new Dimension(90, 25));
        clearBtn.setPreferredSize(new Dimension(90, 25));
        stopBtn.setPreferredSize(new Dimension(90, 25));
        btnPanel.add(ghostLabel);
        btnPanel.add(msgBoard);
        btnPanel.add(clearBtn);
        btnPanel.add(pauseBtn);
        btnPanel.add(stopBtn);
        btnPanel.add(routerComboBox);
        btnPanel.add(parallelExpandBox);
        getContentPane().add(btnPanel, "South");
        refreshTimer.start();
        //clearBtn.addActionListener(this);
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
        pauseBtn.setText(myGrid.isPaused() ? "RESUME" : "PAUSE");
    }

    private void stopAction(ActionEvent evt) {
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

}


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
public class MazeRouterFrame extends JFrame implements Runnable {

    private Grid myGrid = null;
    private Thread myThread = null;
    private static final JLabel notePad = new JLabel();
    private static final JButton clearBtn = new JButton("CLEAR");
    private static final JButton pauseBtn = new JButton("PAUSE");
    private static final JButton stopBtn = new JButton("STOP");

    public synchronized void initMazeRouterFrame(int nlayers, boolean parallelMode) {
        int ncols = Grid.calculateCols(getSize().width);
        int nrows = Grid.calculateRows(getSize().height, nlayers);
        System.out.println("width=" + getSize().width + " height=" + getSize().height);
        myGrid = new Grid(ncols, nrows, nlayers);
        if (parallelMode) {
            myGrid.setParallelExpand();
        } else {
            myGrid.setSerialExpand();
        }
        setLayout(new BorderLayout());
        getContentPane().add(myGrid, "Center");
        clearBtn.addActionListener(this::clearAction);
        JPanel btnPanel = new JPanel();
        pauseBtn.addActionListener(this::pauseAction);
        stopBtn.addActionListener(this::stopAction);
        btnPanel.setLayout(new FlowLayout());
        notePad.setPreferredSize(new Dimension(290, 25));
        pauseBtn.setPreferredSize(new Dimension(90, 25));
        clearBtn.setPreferredSize(new Dimension(90, 25));
        stopBtn.setPreferredSize(new Dimension(90, 25));
        btnPanel.add(notePad);
        btnPanel.add(clearBtn);
        btnPanel.add(pauseBtn);
        btnPanel.add(stopBtn);
        pauseBtn.setEnabled(false);
        stopBtn.setEnabled(false);
        clearBtn.setEnabled(true);
        getContentPane().add(btnPanel, "South");
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

    public static void setText(String msg) {
        notePad.setText(msg);
    }

    private static final int WAITFORSRC = 0;
    private static final int WAITFORTGT = 1;

    private GridPoint clicked = null;

    public static void changePauseBtn(boolean state) {
        pauseBtn.setText("Pause");
        pauseBtn.setEnabled(state);
    }

    public static void changeClearBtn(boolean state) {
        clearBtn.setEnabled(state);
    }
    
    public static void changeStopBtn(boolean state){
        stopBtn.setEnabled(state);
    }

    public void run() {
        try {
            myGrid.run();
        } catch (InterruptedException e) {
        }
    }
    private static boolean paused = false;

    private void pauseAction(ActionEvent evt) {
        myGrid.pauseResume();
        setText(myGrid.getMSG());
        pauseBtn.setText(myGrid.isPaused() ? "Resume" : "Pause");
    }
    
    private void stopAction(ActionEvent evt){
        if(myGrid.isPaused()){
            myGrid.pauseResume();
        }
        myGrid.stopRouter();
    }
    
    private void clearAction(ActionEvent evt){
        myGrid.requestClear();
    }
    
}

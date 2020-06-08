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
public class HadlockFrame extends JFrame implements Runnable{

    private static final int WAITFORSRC = 0;
    private static final int WAITFORTGT = 1;
    private static final int EXPANDING = 2;
    private static final int TRACKBACK = 3;
    private Grid myGrid = null;
    private Thread myThread = null;
    private final JLabel msgBoard = new JLabel();
    private final JButton clearBtn = new JButton("CLEAR");
    private final JButton pauseBtn = new JButton("");
    private final JButton stopBtn = new JButton("STOP");

    public void initHadlockFrame(int nlayers) {

//      String lstr = getParameter("layers");
//      int nlayers;
//      if (lstr != null) nlayers = Integer.parseInt(lstr);
//      else nlayers = 1;
        int ncols = Grid.calculateCols(getSize().width);
        int nrows = Grid.calculateRows(getSize().height, nlayers);
        myGrid = new Grid(ncols, nrows, nlayers);
        myGrid.setRouter(new HadlockRouter(myGrid));
        //String modestr = getParameter("mode");
        setLayout(new BorderLayout());
        getContentPane().add(myGrid, "Center");
        clearBtn.addActionListener(this::clearAction);
        JPanel btnPanel = new JPanel();
        pauseBtn.addActionListener(this::pauseAction);
        stopBtn.addActionListener(this::stopAction);
        btnPanel.setLayout(new FlowLayout());
        msgBoard.setPreferredSize(new Dimension(290, 25));
        pauseBtn.setPreferredSize(new Dimension(90, 25));
        clearBtn.setPreferredSize(new Dimension(90, 25));
        stopBtn.setPreferredSize(new Dimension(90, 25));
        btnPanel.add(msgBoard);
        btnPanel.add(clearBtn);
        btnPanel.add(pauseBtn);
        btnPanel.add(stopBtn);
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

    Timer refreshTimer = new Timer(5, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            pauseBtn.setText(myGrid.isPaused() ? "RESUME" : "PAUSE");
            msgBoard.setText(myGrid.getMSG());
            if (myGrid.getState() == WAITFORSRC) {
                pauseBtn.setEnabled(false);
                stopBtn.setEnabled(false);
                clearBtn.setEnabled(true);
            }
            else if(myGrid.getState() == WAITFORTGT){
                pauseBtn.setEnabled(false);
                stopBtn.setEnabled(false);
                clearBtn.setEnabled(false);
            }
            else if(myGrid.getState()==EXPANDING){
                pauseBtn.setEnabled(true);
                stopBtn.setEnabled(true);
                clearBtn.setEnabled(false);
            }
            else if(myGrid.getState()==TRACKBACK){
                pauseBtn.setEnabled(true);
                stopBtn.setEnabled(false);
                clearBtn.setEnabled(false);
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

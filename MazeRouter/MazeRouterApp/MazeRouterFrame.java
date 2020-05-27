import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * Title:        MazeApplet
 * Description:  Animation of Lee Algorithm for maze routing.
 * Copyright:    Copyright (c) 2001
 * Company:      Lafayette College
 * @author John A. Nestor
 * @version 1.0
 */

public class MazeRouterFrame extends JFrame implements Runnable, ActionListener
{
  private Grid myGrid = null;

  private Thread myThread = null;

  public void initMazeRouterFrame(int nlayers, boolean parallelMode) {
      int ncols = Grid.calculateCols(getSize().width);
      int nrows = Grid.calculateRows(getSize().height, nlayers);
      System.out.println("width=" + getSize().width + " height=" + getSize().height);
      myGrid = new Grid(ncols,nrows,nlayers);
      if (parallelMode) myGrid.setParallelExpand();
      else myGrid.setSerialExpand();
      setLayout(new BorderLayout());
      getContentPane().add(myGrid, "Center");
      JButton clearBtn = new JButton("CLEAR");
      JPanel btnPanel = new JPanel();
      btnPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
      btnPanel.add(clearBtn);
      getContentPane().add(btnPanel, "South");
      clearBtn.addActionListener(this);
      repaint();
      if (myThread == null) {
        myThread = new Thread(this);
        myThread.start();
      }
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

   public void actionPerformed(ActionEvent e) {
     myGrid.requestClear();
   }

   private static final int WAITFORSRC = 0;
   private static final int WAITFORTGT = 1;

   private GridPoint clicked = null;

   public void run() {
     try {
       myGrid.run();
     } catch (InterruptedException e) {}
   }
}

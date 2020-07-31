import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import javax.swing.*;

/**
 * Title:        MazeApplet
 * Description:  Animation of Lee Algorithm for maze routing.
 * Copyright:    Copyright (c) 2001
 * Company:      Lafayette College
 * @author John A. Nestor
 * @version 1.0
 */

public class MazeApplet extends Applet implements Runnable, ActionListener
{
  private Grid myGrid = null;

  private Thread myThread = null;

  public void init() {

      String lstr = getParameter("layers");
      int nlayers;
      if (lstr != null) nlayers = Integer.parseInt(lstr);
      else nlayers = 1;
      int ncols = Grid.calculateCols(getSize().width);
      int nrows = Grid.calculateRows(getSize().height, nlayers);
      myGrid = new Grid(ncols,nrows,nlayers);
      String modestr = getParameter("mode");
      setLayout(new BorderLayout());
      add(myGrid, "Center");
      Button clearBtn = new Button("CLEAR");
      Panel btnPanel = new Panel();
      btnPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
      btnPanel.add(clearBtn);
      add(btnPanel, "South");
      clearBtn.addActionListener(this);
      show();
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



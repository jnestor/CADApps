package placement.ui;

import placement.*;
import placement.moves.PMove;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JRootPane;

/** Display the modules of a placement, and draw nets over
    the top using the glassPane */

public class UIPlacementPanel extends JRootPane {
  private PLayout myLayout;

  public UIPlacementPanel(PLayout ml) {
    getContentPane().setLayout(null);
    UINetPane gp = new UINetPane(ml);
    setGlassPane(gp);
    gp.setOpaque(false);
    gp.setVisible(true);
  }

}

class UINetPane extends JPanel {
  private PLayout myLayout;

  public UINetPane(PLayout ml) {
    myLayout = ml;
    setLayout(null);
  }

  public void paintComponent(Graphics g) {
      setBackground(Color.lightGray);
      super.paintComponent(g);
    UILayout ul = (UILayout)getParent().getParent();
    for (int i=0; i<myLayout.numNets(); i++) {
      PNet pn = myLayout.getNet(i);
      if (pn.numTerminals() <= 1) continue;
      PTerminal ot = pn.getTerminal(0);
      g.setColor(Color.red);
      for (int j=1; j < pn.numTerminals(); j++) {
        PTerminal dt = pn.getTerminal(j);
        g.drawLine( ul.translateX(ot.getLayoutX()), ul.translateY(ot.getLayoutY()), ul.translateX(dt.getLayoutX()), ul.translateY(dt.getLayoutY()) );
      }
      // now draw the move
      PMove curMove = myLayout.getLastMove();

      if (curMove != null) {
        UIMove um = myLayout.getLastMove().getUIMove();
        um.drawMove(ul,g);
      }
    }
  }
}


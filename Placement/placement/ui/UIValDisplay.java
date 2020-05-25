package placement.ui;

import java.text.NumberFormat;
import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.BorderLayout;

public class UIValDisplay extends JPanel {
  private JLabel nlabel;
  private JLabel vlabel;
  private NumberFormat myFormat;

  public UIValDisplay(String n, int v, int idigits, int fdigits ) {
    myFormat = NumberFormat.getInstance();
    myFormat.setMaximumIntegerDigits(idigits);
    myFormat.setMaximumFractionDigits(fdigits);
    nlabel = new JLabel(n,JLabel.CENTER);
    vlabel = new JLabel(myFormat.format(v),JLabel.CENTER);
    setLayout(new BorderLayout());
    add(nlabel,BorderLayout.NORTH);
    add(vlabel,BorderLayout.SOUTH);
  }

  public UIValDisplay(String n, int v) {
    this(n, v, 8, 2);
  }

  public void setValue(int v) { vlabel.setText(myFormat.format(v)); }
  public void setValue(double d) { vlabel.setText(myFormat.format(d)); }

}


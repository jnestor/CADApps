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

    public void setValue(double d) {
	if (Math.abs(d) > 1.0E7 || (Math.abs(d) < 1.0E-3 && d != 0.0)) {
	    String vs = Double.toString(d); // format in scientific notation
	    int vl = vs.length();
	    if (vl > 10) {   // ugly code - whack the string down to 10 chars
		int ei, i;
		i = vl-1;
		while (vs.charAt(i) != 'E' && i>0) {
		    i--;
		}
		ei = i;
		i = ei - (vl-10);
		if (i>0) vs = vs.substring(0,i) + vs.substring(ei,vl);
		vlabel.setText(vs);
	    }
	}
	else vlabel.setText(myFormat.format(d));
    }

}


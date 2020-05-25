package placement.ui;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.border.*;
import java.text.NumberFormat;
import java.text.DecimalFormat;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author
 * @version 1.0
 */

public class UIOptionsDialog extends JFrame {
    UIAnnealer myUI;
    JPanel panel1 = new JPanel();
    ButtonGroup buttonGroup1 = new ButtonGroup();
    JPanel animationPanel = new JPanel();
    TitledBorder titledBorder1;
    Border border1;
    JPanel updatePanel = new JPanel();
    JLabel updateLabel = new JLabel();
    JComboBox updateComboBox = new JComboBox();
    JPanel speedPanel = new JPanel();
    Component component4;
    JSlider speedSlider = new JSlider(1, 100, 50);
    JLabel jLabel2 = new JLabel();
    JPanel annealingPanel = new JPanel();
    TitledBorder titledBorder3;
    // Initial temperature controls
    JPanel initialTempPanel = new JPanel();
    JLabel initTempLabel = new JLabel();
    JComboBox initialTempComboBox = new JComboBox();
    JLabel t0Label = new JLabel();
    DecimalFormat nf = (DecimalFormat)NumberFormat.getNumberInstance();
    DecimalField t0Field = new DecimalField(0,8,nf);
    Border border2;
    JPanel movesPerTempPanel = new JPanel();
    JLabel movesPerTempLabel = new JLabel();
    WholeNumberField movesPerTempField = new WholeNumberField(0,8);
    JComboBox movesPerTempComboBox = new JComboBox();
    // Cooling rate stuff
    JPanel coolRatePanel = new JPanel();
    JLabel coolRateLabel = new JLabel();
    DecimalField coolRateField = new DecimalField(0,5,nf);
    
    public UIOptionsDialog(UIAnnealer a) {
	// super("Annealing Options");
	myUI = a;
	nf.setGroupingUsed(false);
	try {
	    jbInit();
	    pack();
	}
	catch(Exception ex) {
	    ex.printStackTrace();
	}
    }
    
    void jbInit() throws Exception {
	titledBorder1 = new TitledBorder(BorderFactory.createEtchedBorder(Color.white,new Color(134, 134, 134)),"Animation");
    border1 = BorderFactory.createCompoundBorder(
	new TitledBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED,Color.white,Color.white,
							 new Color(93, 93, 93),new Color(134, 134, 134)),"Animation"),
	BorderFactory.createEmptyBorder(5,5,5,5));
    component4 = Box.createGlue();
    titledBorder3 = new TitledBorder(BorderFactory.createEtchedBorder(Color.white,new Color(134, 134, 134)),"Annealing");
    border2 = BorderFactory.createCompoundBorder(new TitledBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED,Color.white,Color.white,new Color(93, 93, 93),new Color(134, 134, 134)),"Annealing"),BorderFactory.createEmptyBorder(5,5,5,5));
    panel1.setLayout(new BoxLayout(panel1,BoxLayout.Y_AXIS));
    getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));
    // set up speedSlider
    speedSlider.addChangeListener(new SpeedListener());
    myUI.setOptSpeed(50);
    
    // set up animationPanel
    animationPanel.setLayout(new BoxLayout(animationPanel,BoxLayout.Y_AXIS));
    animationPanel.setBorder(border1);
    animationPanel.setMinimumSize(new Dimension(264, 103));
    updateLabel.setText("Placement Update: ");
    updateComboBox.setLightWeightPopupEnabled(false);
    updateComboBox.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        updateComboBox_actionPerformed(e);
      }
    });
    updateComboBox.addItem("after each move");
    updateComboBox.addItem("at end of each temp.");
    updatePanel.setLayout(new BoxLayout(updatePanel,BoxLayout.X_AXIS));
    speedPanel.setLayout(new BoxLayout(speedPanel,BoxLayout.X_AXIS));
    jLabel2.setText("Animation Speed: ");
    // set up annealingPanel
    annealingPanel.setBorder(border2);
    annealingPanel.setLayout(new BoxLayout(annealingPanel,BoxLayout.Y_AXIS));
    initTempLabel.setText("Initial Temp. ");
    t0Label.setText("T0=");
    t0Field.setValue(10000.0);
    myUI.setOptInitialTemperature(t0Field.getValue());
    t0Field.getDocument().addDocumentListener(new MyDocumentListener() {
        public void updateValue() {
          myUI.setOptInitialTemperature(t0Field.getValue());
        }
      } );
    // set up initialTempPanel
    initialTempPanel.setLayout(new BoxLayout(initialTempPanel,BoxLayout.X_AXIS));
    initialTempComboBox.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        initialTempComboBox_actionPerformed(e);
      }
    });
    initialTempComboBox.addItem("fixed");
    initialTempComboBox.addItem("adaptive");
    this.setTitle("Placement Options");
    // set up movesPerTempPanel
    movesPerTempLabel.setText("Moves per Temp. = ");
    movesPerTempField.setValue(50);
    myUI.setOptMovesPerTemp(movesPerTempField.getValue());
    movesPerTempField.getDocument().addDocumentListener(new MyDocumentListener() {
        public void updateValue() {
          myUI.setOptMovesPerTemp(movesPerTempField.getValue());
        }
      } );
    movesPerTempPanel.setLayout(new BoxLayout(movesPerTempPanel,BoxLayout.X_AXIS));
    movesPerTempComboBox.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        movesPerTempComboBox_actionPerformed(e);
      }
    });
    // set up coolRatePanel
    coolRatePanel.setLayout(new BoxLayout(coolRatePanel,BoxLayout.X_AXIS));
    coolRateLabel.setText("Cooling Rate: ");
    coolRateField.setValue(0.95);
    myUI.setOptCoolRate(coolRateField.getValue());
    coolRateField.getDocument().addDocumentListener(new MyDocumentListener() {
        public void updateValue() {
	  myUI.setOptCoolRate(coolRateField.getValue());
	}
      } );
    this.getContentPane().add(panel1);
    panel1.add(animationPanel, null);
    animationPanel.add(speedPanel, null);
    speedPanel.add(jLabel2, null);
    speedPanel.add(speedSlider, null);
    animationPanel.add(updatePanel, null);
    updatePanel.add(updateLabel, null);
    updatePanel.add(updateComboBox, null);
    animationPanel.add(component4, null);
    panel1.add(annealingPanel, null);
    annealingPanel.add(movesPerTempPanel, null);
    movesPerTempPanel.add(movesPerTempLabel, null);
    movesPerTempPanel.add(movesPerTempField, null);
    movesPerTempPanel.add(movesPerTempComboBox, null);
    movesPerTempComboBox.addItem("per module");
    movesPerTempComboBox.addItem("fixed");
    annealingPanel.add(initialTempPanel, null);
    initialTempPanel.add(initTempLabel, null);
    initialTempPanel.add(initialTempComboBox, null);
    initialTempPanel.add(t0Label, null);
    initialTempPanel.add(t0Field, null);
    // set up coolRatePanel
    coolRateLabel.setText("Cooling Rate: ");
    coolRatePanel.add(coolRateLabel);
    coolRatePanel.add(coolRateField);
    coolRatePanel.add(Box.createGlue());
    annealingPanel.add(coolRatePanel,null);
  }

  void initialTempComboBox_actionPerformed(ActionEvent e) {
    String selString = (String)initialTempComboBox.getSelectedItem();
    if (selString.equals("adaptive")) {
        t0Label.setEnabled(false);
        t0Field.setEnabled(false);
        myUI.setOptT0Adaptive(true);
    } else if (selString.equals("fixed")) {
      t0Label.setEnabled(true);
      t0Field.setEnabled(true);
      myUI.setOptT0Adaptive(false);
    }
  }

  void updateComboBox_actionPerformed(ActionEvent e) {
    String selString = (String)updateComboBox.getSelectedItem();
    if (selString.equals("after each move")) {
      myUI.setOptAnimateMoves(true);
    } else if (selString.equals("at end of each temp.")) {
      myUI.setOptAnimateMoves(false);
    }
  }

  public static void main(String [] args) {
    JFrame jf = new UIOptionsDialog(null);
    jf.setVisible(true);
    JFrame jf2 = new UIOptionsDialog(null);
    jf2.setVisible(true);
  }

  void movesPerTempComboBox_actionPerformed(ActionEvent e) {
    String selString = (String)movesPerTempComboBox.getSelectedItem();
    if (selString.equals("fixed")) {
        myUI.setOptMovesPerTempPerModule(false);
    } else if (selString.equals("per module")) {
        myUI.setOptMovesPerTempPerModule(true);
    }
  }

  void t0Field_actionPerformed(ActionEvent e) {
    myUI.setOptInitialTemperature(t0Field.getValue());
  }

  void movesPerTempField_actionPerformed(ActionEvent e) {
    myUI.setOptMovesPerTemp(movesPerTempField.getValue());
  }

  abstract class MyDocumentListener implements DocumentListener {
    public void insertUpdate(DocumentEvent e) { updateValue(); }
    public void removeUpdate(DocumentEvent e) { updateValue(); }
    public void changedUpdate(DocumentEvent e) { }
    public abstract void updateValue();
  }

  private class SpeedListener implements ChangeListener {
    public void stateChanged(ChangeEvent e) {
      JSlider source = (JSlider)e.getSource();
      if (!source.getValueIsAdjusting()) {
        int speed = (int)source.getValue();
	myUI.setOptSpeed(speed);
      }
    }
  }


}

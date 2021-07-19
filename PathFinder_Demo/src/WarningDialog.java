package pathfinder_demo;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author 15002
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class WarningDialog extends JPanel {

    private boolean willDisplay = true;
    JLabel message = new JLabel();

    public WarningDialog(String text) {
        JButton confirmBtn = new JButton("Confirm");
        confirmBtn.addActionListener(this::confirmAction);
        JCheckBox dontAskMeAgain= new JCheckBox("Don't ask me again");
        dontAskMeAgain.addItemListener(this::doNotDisplay);
        setLayout(new BorderLayout());
        add(message);
        message.setText(text);
        JPanel subPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        subPanel.add(dontAskMeAgain);
        add(subPanel,BorderLayout.SOUTH);
    }
    
    public void doNotDisplay(ItemEvent evt){
        willDisplay = !(evt.getStateChange() == 1);
    }
    
    private void confirmAction(ActionEvent evt){
        setVisible(false);
    }
    
   public int showConfirmDialog(Component parent) {
        int result = JOptionPane.YES_OPTION;
        if(willDisplay)
            result = JOptionPane.showConfirmDialog(parent, this,
                        "WARNING",
                        JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
        return result;
    }
   
   public void setMessage(String text){
       message.setText(text);
   }
}

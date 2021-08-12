/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package virtualmemorysim;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;

/**
 *
 * @author 15002
 */
public class VMSim_demo extends JFrame {

    JScrollPane vmPane;
    JPanel bottonPane;
    JScrollPane instruPane;
    private final JButton openBtn = new JButton("Open");
    private VMJTable instruTable;
    private final JLabel msgBoard = new JLabel();
    private final JLabel title = new JLabel("Virtual Memory Simulation", SwingConstants.CENTER);
    private final ImageIcon pause = new ImageIcon(getClass().getResource("images/pause.gif"));
    private final ImageIcon resume = new ImageIcon(getClass().getResource("images/start.gif"));
//    private final JToggleButton clearBtn = new JToggleButton(new ImageIcon(getClass().getResource("images/clear.png")));
    private final JToggleButton pauseBtn = new JToggleButton(resume);
//    private final JToggleButton stopBtn = new JToggleButton(new ImageIcon(getClass().getResource("images/stop.gif")));
    private final JToggleButton stepBtn = new JToggleButton(new ImageIcon(getClass().getResource("images/step.gif")));
    private final JCheckBox tlbBox = new JCheckBox("TLB");
    VMPanel vmSim;
    private int delay = 400;
    private JSlider speedSlider = new JSlider(10, 800, 400);
    private File configuration;
    private JFileChooser fc;
    private boolean running = false;

    private Timer timer = new Timer(delay, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            vmSim.fsm();
            vmSim.repaint();
            instruPane.repaint();
        }
    });

    public VMSim_demo() {
        title.setFont(new Font("Bold", Font.PLAIN, 25));
        title.setBorder(new EmptyBorder(10, 10, 10, 20));
        setLayout(new BorderLayout());
        getContentPane().add(title, "North");
        JPanel btnPanel = new JPanel();
        fc = new JFileChooser();
        openBtn.addActionListener(this::openAction);
        pauseBtn.addActionListener(this::pauseAction);
        stepBtn.addActionListener(this::stepAction);
        speedSlider.addChangeListener(this::speedChanged);
        btnPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        msgBoard.setPreferredSize(new Dimension(290, 25));
        pauseBtn.setPreferredSize(new Dimension(25, 25));
        stepBtn.setPreferredSize(new Dimension(25, 25));
        speedSlider.setPreferredSize(new Dimension(160, 25));
        speedSlider.setToolTipText("Change the speed of the expansion");
        pauseBtn.setToolTipText("Pause");
        stepBtn.setToolTipText("Step");
        btnPanel.add(msgBoard);
        btnPanel.add(openBtn);
        btnPanel.add(pauseBtn);
        btnPanel.add(stepBtn);
        btnPanel.add(speedSlider);
        stepBtn.setEnabled(false);
        pauseBtn.setEnabled(false);
        pauseBtn.setToolTipText("Start");
        pauseBtn.setSelectedIcon(resume);
        speedSlider.setEnabled(false);
        getContentPane().add(btnPanel, "South");
    }

    private void stepAction(ActionEvent evt) {
        if (configuration != null) {
            vmSim.fsm();
            vmSim.repaint();
            instruPane.repaint();
        }
    }

    private void pauseAction(ActionEvent evt) {
        if (configuration != null) {
            if (!running) {
                msgBoard.setText("running");
                timer.setRepeats(true);
                timer.setDelay(delay);
                timer.start();
                stepBtn.setEnabled(false);
                openBtn.setEnabled(false);
                running = true;
            } else {
                msgBoard.setText("paused");
                timer.stop();
                timer.setRepeats(false);
                stepBtn.setEnabled(true);
                openBtn.setEnabled(true);
                running = false;
            }
            pauseBtn.setSelectedIcon(running ? resume : pause);
            pauseBtn.setToolTipText(running ? "Start" : "Pause");
        }
    }

    public void speedChanged(ChangeEvent e) {
        if (configuration != null) {
            JSlider source = (JSlider) e.getSource();
            if (!source.getValueIsAdjusting()) {
                int speed = (int) source.getValue();
                delay = 800 - speed;
                timer.setDelay(delay);
            }
        }
    }

    private void openAction(ActionEvent evt) {
        if (configuration != null) {
//            timer.stop();
//            router.restartReset();
//            nets.clear();
            System.out.println("prereset");
        }
        int returnVal = fc.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            configuration = fc.getSelectedFile();
            if (configuration != null) {
                String name = configuration.getName();
                int index = name.lastIndexOf(".");
                String extension = name.substring(index + 1);

                if (extension.equals("csv")) {
                    readConfig();
                } else {
                    JOptionPane.showMessageDialog(this, "Wrong file type", "Invalid File", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
//        else JOptionPane.showMessageDialog(this, "Null File", "Invalid File", JOptionPane.ERROR_MESSAGE);
    }

    private void readConfig() {
        LinkedList<Pair<Integer, Integer>> instructions = new LinkedList<Pair<Integer, Integer>>();
        VMPanel vmTemp = null;
        ArrayList<String> vpnList = new ArrayList<String>();
        try {
            fc = new JFileChooser(configuration.getAbsolutePath());
            Scanner fR = new Scanner(configuration);
            Scanner lR = new Scanner(fR.nextLine());
            if (!fR.hasNext()) {
                if (vmSim == null) {
                    configuration = null;
                }
                JOptionPane.showMessageDialog(this, "Wrong configuration file type", "Invalid File Type", JOptionPane.ERROR_MESSAGE);
                return;
            }
            lR = new Scanner(fR.nextLine());
            String[] a = lR.next().split(",");
            if (a.length < 5) {
                if (vmSim == null) {
                    configuration = null;
                }
                JOptionPane.showMessageDialog(this, "Incomplete configuration file", "Invalid File", JOptionPane.ERROR_MESSAGE);
                return;
            }
//            System.out.println(a[0]);
            try {
                int pmSize = Integer.parseInt(a[0]);
                int vmSize = Integer.parseInt(a[1]);
                int pmCap = Integer.parseInt(a[2]);
                int vmCap = Integer.parseInt(a[3]);
                int offset = Integer.parseInt(a[4]);
                if (pmSize < pmCap || vmSize < vmCap) {
                    if (vmSim == null) {
                        configuration = null;
                    }
                    JOptionPane.showMessageDialog(this, "Segment size is larger than memory size", "Invalid Configuration", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (pmSize > vmSize) {
                    JOptionPane.showMessageDialog(this, "Why would you need virtual memory?", "Small Virtual Memory Size", JOptionPane.QUESTION_MESSAGE);
                }
                vmTemp = new VMPanel(10, pmSize, vmSize, offset, pmCap, vmCap);
            } catch (NumberFormatException e) {
                Toolkit.getDefaultToolkit().beep();
                JOptionPane.showMessageDialog(
                        this, "Wrong memory size, please use decimal number", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                if (vmSim == null) {
                    configuration = null;
                }
                return;
            }
            lR = new Scanner(fR.nextLine());
            if (fR.hasNext()) {
                while (fR.hasNext()) {
                    try {
                        lR = new Scanner(fR.nextLine());
                        a = lR.next().split(",");
                        ArrayList<String> netLocs = new ArrayList<String>(Arrays.asList(a));
                        int instru = Integer.parseInt(a[0]);
                        int addr = Integer.parseInt(a[1], 16);
                        Pair<Integer,Integer> pair = new Pair<Integer,Integer>(instru, addr);
                        vpnList.add(a[1]);
                        instructions.add(pair);
                    } catch (NumberFormatException|NullPointerException e) {
                        Toolkit.getDefaultToolkit().beep();
                        JOptionPane.showMessageDialog(
                                this, "wrong instruction or address", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                        if (timer == null) {
                            configuration = null;
                        }
                        return;
                    }

                }
            } else {
                JOptionPane.showMessageDialog(
                        this, "No instructions", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                configuration = null;
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(VMSim_demo.class.getName()).log(Level.SEVERE, null, ex);
        }
        vmSim = vmTemp;
        vmPane=new JScrollPane(vmSim);
        if (vmSim != null) {
            getContentPane().remove(vmPane);
        }
        vmSim.setInstructions(instructions);
        getContentPane().add(vmPane, "Center");
        setVisible(true);
        msgBoard.setText("Press Start or Step to start");
        speedSlider.setEnabled(true);
        pauseBtn.setEnabled(true);
        stepBtn.setEnabled(true);
        vmSim.getInstruTable().getColumnModel().getColumn(0).setPreferredWidth(31);
        vmSim.getInstruTable().getColumnModel().getColumn(1).setPreferredWidth(95);
        vmSim.getInstruTable().getColumnModel().getColumn(0).setHeaderValue("r/w");
        vmSim.getInstruTable().getColumnModel().getColumn(1).setHeaderValue("Virtual Address");
        if(instruTable!=null){
            getContentPane().remove(instruPane);
        }
        instruTable = vmSim.getInstruTable();
        instruPane=new JScrollPane(instruTable);
        instruPane.setPreferredSize(new Dimension(31+95, vmPane.getHeight()));
        for(int i =0; i<instructions.size();i++){
            instruTable.setValueAt(instructions.get(i).getK(), i, 0);
            instruTable.setValueAt(vpnList.get(i), i, 1);
        }
        getContentPane().add(instruPane, "East");
//        System.out.println(p.getChanVer()[0][0].getID());
    }

    private static void createAndShowGUI() {
        VMSim_demo demo = new VMSim_demo();
        JFrame f = demo;
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(1030, 1000);
        f.setMinimumSize(new Dimension(1150, 500));
        f.setVisible(true);
    }

    public static void main(String[] args) {
        createAndShowGUI();
    }
}

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
import javax.swing.border.EmptyBorder;

/**
 *
 * @author 15002
 */
public class VMSim_demo extends JFrame {

    JScrollPane vmPane;
    JPanel bottonPane;
    JScrollPane instruPane;
    private final JButton openBtn = new JButton("Open");
    private JTable instruTable;
    private final JLabel msgBoard = new JLabel();
    private final JLabel title = new JLabel("Virtual Memory Simulation", SwingConstants.CENTER);
    private final ImageIcon pause = new ImageIcon(getClass().getResource("images/pause.gif"));
    private final ImageIcon resume = new ImageIcon(getClass().getResource("images/start.gif"));
//    private final JToggleButton clearBtn = new JToggleButton(new ImageIcon(getClass().getResource("images/clear.png")));
    private final JToggleButton pauseBtn = new JToggleButton(pause);
//    private final JToggleButton stopBtn = new JToggleButton(new ImageIcon(getClass().getResource("images/stop.gif")));
    private final JToggleButton stepBtn = new JToggleButton(new ImageIcon(getClass().getResource("images/step.gif")));
    private final JCheckBox tlbBox = new JCheckBox("TLB");
    VMPanel vmSim;
    private JSlider speedSlider = new JSlider(10, 800, 400);
    private File configuration;
    private JFileChooser fc;
    private boolean running = false;

    public VMSim_demo() {
        title.setFont(new Font("Bold", Font.PLAIN, 25));
        title.setBorder(new EmptyBorder(10, 10, 10, 20));
        setLayout(new BorderLayout());
        getContentPane().add(title, "North");
        JPanel btnPanel = new JPanel();
        fc = new JFileChooser();
        openBtn.addActionListener(this::openAction);
//        pauseBtn.addActionListener(this::pauseAction);
//        stepBtn.addActionListener(this::stepAction);
//        speedSlider.addChangeListener(this::speedChanged);
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
        pauseBtn.setToolTipText("Start");
        speedSlider.setEnabled(false);
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
//                    readConfig();
                } else {
                    JOptionPane.showMessageDialog(this, "Wrong file type", "Invalid File", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
//        else JOptionPane.showMessageDialog(this, "Null File", "Invalid File", JOptionPane.ERROR_MESSAGE);
    }
    
//    private void readConfig() {
//        ColorSequencer.reset();
//        UIPathFinder tempPF = null;
//        UIGraph tempGraph = null;
//        CopyOnWriteArrayList<PFNet> netsTemp = new CopyOnWriteArrayList<PFNet>();
//        try {
//            fc = new JFileChooser(configuration.getAbsolutePath());
//            Scanner fR = new Scanner(configuration);
//            Scanner lR = new Scanner(fR.nextLine());
//            if (!fR.hasNext()) {
//                if (timer == null) {
//                    configuration = null;
//                }
//                JOptionPane.showMessageDialog(this, "Wrong configuration file type", "Invalid File Type", JOptionPane.ERROR_MESSAGE);
//                return;
//            }
//            lR = new Scanner(fR.nextLine());
//            String[] a = lR.next().split(",");
////            System.out.println(a[0]);
//            try {
//                int w = Integer.parseInt(a[0]);
//                int h = Integer.parseInt(a[1]);
//                tempPF = new UIPathFinder(w, h);
//                tempGraph = tempPF.getGraph();
//
//            } catch (NumberFormatException e) {
//                Toolkit.getDefaultToolkit().beep();
//                JOptionPane.showMessageDialog(
//                        this, "wrong width and height", "Invalid Input", JOptionPane.ERROR_MESSAGE);
//                if (timer == null) {
//                    configuration = null;
//                }
//                return;
//            }
//            lR = new Scanner(fR.nextLine());
//            if (fR.hasNext()) {
//
//                while (fR.hasNext()) {
//                    try {
//                        lR = new Scanner(fR.nextLine());
//                        a = lR.next().split(",");
//                        ArrayList<String> netLocs = new ArrayList<String>(Arrays.asList(a));
//                        Iterator arrIterator = netLocs.iterator();
//                        arrIterator.next();
//                        int x = Integer.parseInt((String) arrIterator.next());
//                        int y = Integer.parseInt((String) arrIterator.next());
//                        try {
//                            PFNode source = tempPF.getSources()[x][y];
//                            source.occupy();
//                            LinkedList<PFNode> sinks = new LinkedList<PFNode>();
//                            while (arrIterator.hasNext()) {
//                                x = Integer.parseInt((String) arrIterator.next());
//                                y = Integer.parseInt((String) arrIterator.next());
//                                PFNode sink = tempPF.getSinks()[x][y];
//                                sink.occupy();
//                                sinks.add(sink);
//                            }
//                            if (sinks.isEmpty()) {
//                                Toolkit.getDefaultToolkit().beep();
//                                JOptionPane.showMessageDialog(
//                                        this, "no sinks", "Invalid Input", JOptionPane.ERROR_MESSAGE);
//                                if (timer == null) {
//                                    configuration = null;
//                                }
//                                return;
//                            }
//                            PFNet net = new PFNet(sinks, source);
//                            net.setColor(ColorSequencer.next());
//                            netsTemp.add(net);
//                        } catch (IndexOutOfBoundsException e) {
//                            Toolkit.getDefaultToolkit().beep();
//                            JOptionPane.showMessageDialog(
//                                    this, "wrong source or sink locations", "Invalid Input", JOptionPane.ERROR_MESSAGE);
//                            if (timer == null) {
//                                configuration = null;
//                            }
//                            return;
//                        }
//                    } catch (NumberFormatException e) {
//                        Toolkit.getDefaultToolkit().beep();
//                        JOptionPane.showMessageDialog(
//                                this, "wrong source or sink locations", "Invalid Input", JOptionPane.ERROR_MESSAGE);
//                        if (timer == null) {
//                            configuration = null;
//                        }
//                        return;
//                    }
//
//                }
//
//            } else {
//                JOptionPane.showMessageDialog(
//                        this, "No nets", "Invalid Input", JOptionPane.ERROR_MESSAGE);
//                configuration = null;
//            }
//        } catch (FileNotFoundException ex) {
//            Logger.getLogger(PathFinderFrame_demo.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        upf = tempPF;
//        if (graph != null) {
//            getContentPane().remove(graph);
//        }
//        graph = tempGraph;
//        nets = netsTemp;
//        router = new RoutibilityPathFinder(nets, upf.getNodes(), upf.getGraph(), msgBoard);
//        timer = router.getRoutingTimer();
//        getContentPane().add(upf.getGraph(), "Center");
//        setVisible(true);
//        router.setPause(true);
//        msgBoard.setText("Press Start or Step to start routing");
//        speedSlider.setEnabled(true);
//        hValBox.setEnabled(true);
//        hValBox.setSelected(false);
//        pauseBtn.setEnabled(true);
//        penaltyBox.setEnabled(true);
//        penaltyBox.setSelected(false);
//        stopBtn.setEnabled(true);
//        tooltipBox.setSelected(false);
//        tooltipBox.setEnabled(true);
////        System.out.println(p.getChanVer()[0][0].getID());
//    }
    

}

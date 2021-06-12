/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pathfinder_demo;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.LinkedList;
import javax.swing.JFrame;
import static pathfinder_demo.UIGraph.IP;
import static pathfinder_demo.UIGraph.LB;
import static pathfinder_demo.UIGraph.OP;
import static pathfinder_demo.UIGraph.SB;
import static pathfinder_demo.UIGraph.SW;
import static pathfinder_demo.UIGraph.TM;
import java.util.concurrent.CopyOnWriteArrayList;
import static pathfinder_demo.UIGraph.IP;
import static pathfinder_demo.UIGraph.SW;
import javax.swing.JPanel;

/**
 *
 * @author 15002
 */
public class PathFinder_demo  {


    private UIPathFinder p ;
    

    public PathFinder_demo(int w, int h) {
        p= new UIPathFinder(w,h);
    }

    public UIGraph getUIG() {
        return p.getU();
    }

    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        PathFinder_demo demo = new PathFinder_demo(4, 4);
        LinkedList<PFNet> nets = new LinkedList<PFNet> ();
        
        LinkedList<PFNode> sinks1 = new LinkedList<PFNode>();
        sinks1.add(demo.getP().getSinks()[2][2]);
        System.out.println(demo.getP().getSinks()[2][2].getID());
        PFNode source1 = demo.getP().getSources()[0][0];
        PFNet net1 = new PFNet(sinks1,source1);
        net1.setColor(Color.yellow);
        
        
        LinkedList<PFNode> sinks2 = new LinkedList<PFNode>();
        sinks2.add(demo.getP().getSinks()[1][1]);
        System.out.println(demo.getP().getSinks()[1][1].getID());
        PFNode source2 = demo.getP().getSources()[0][1];
        PFNet net2 = new PFNet(sinks2,source2);
        net2.setColor(Color.blue);
        
        LinkedList<PFNode> sinks3 = new LinkedList<PFNode>();
        sinks3.add(demo.getP().getSinks()[0][0]);
        System.out.println(demo.getP().getSinks()[1][1].getID());
        PFNode source3 = demo.getP().getSources()[0][2];
        PFNet net3 = new PFNet(sinks3,source3);
        net3.setColor(Color.green);
        
        nets.add(net2);
        nets.add(net1);
        nets.add(net3);
        
        RoutibilityPathFinder router = new RoutibilityPathFinder(nets,demo.getP().getNodes());
        JFrame f = new JFrame();
        f.add(demo.getUIG());
        //f.add(demo);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(1000, 1000);
        f.setVisible(true);
        //System.out.println("ID of 25: "+demo.getP().getSources()[1][1].getID());
        router.route();
        System.out.println(demo.getP().getChanHori()[2][2].getID());
        System.out.println(demo.getP().getChanHori()[1][2].getID());
        System.out.println(demo.getP().getChanHori()[2][2].getWires().get(1).getTargetSink().getID());
    }

    public UIPathFinder getP() {
        return p;
    }
    
    

}

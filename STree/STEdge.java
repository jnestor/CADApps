
/**
 * @Author: John Nestor <nestorj>
 * @Date: 2020-06-24T20:51:49-04:00
 * @Email: nestorj@lafayette.edu
 * @Last modified by: nestorj
 * @Last modified time: 2020-06-24T20:51:49-04:00
 */

import java.awt.Point;

public class STEdge implements Comparable<STEdge>{

    private int id;
    private STNode p1, p2;
    private boolean mark;
    private boolean deleteMark;
    private static int nextID = 1;

    public STEdge(STNode sp1, STNode sp2) {
        id = nextID++;
        p1 = sp1;
        p2 = sp2;
        mark = false;
        deleteMark = false;
    }
    
    public STEdge(STNode sp1, STNode sp2,boolean increment) {
        if(increment)
        id = nextID++;
        p1 = sp1;
        p2 = sp2;
        mark = false;
        deleteMark = false;
    }

    public STNode getP1() {
        return p1;
    }

    public STNode getP2() {
        return p2;
    }

    public int getID() {
        return id;
    }

    public static void resetIDs() {
        nextID = 1;
    }

    public int length() {
        return p1.distanceL1(p2);
    }

    public void setMark(boolean m) {
        mark = m;
    }

    public boolean getMark() {
        return mark;
    }

    /**
     * mark edge for deletion (but don't delete until dangling refs are gone
     */
    public void setDeleteMark(boolean d) {
        deleteMark = d;
    }

    public boolean getDeleteMark() {
        return deleteMark;
    }

    public void disconnect() {
        //	System.out.println("disconnect p1=" + getP1());
        //	System.out.println("disconnect p2=" + getP2());
        getP1().removeEdge(this);
        getP2().removeEdge(this);
        setDeleteMark(true);
        //	System.out.println("STEdge.disconnect: " + this);
        //	System.out.println("disconnect p1=" + getP1());
        //	System.out.println("disconnect p2=" + getP2());
    }

    public String toString() {
        Point loc1 = p1.getLocation();
        Point loc2 = p2.getLocation();
        String s = "Edge " + id + " [(" + loc1.x + "," + loc1.y + ")-("
                + loc2.x + "," + loc2.y + ")]";
        if (getDeleteMark()) {
            s = s + "<DELETED>";
        }
        return s;
    }

    @Override
    public boolean equals(Object ob) {
        if (ob instanceof STEdge) {
            STEdge edge = (STEdge) ob;
            return p1.equals(edge.getP1())&&p2.equals(edge.getP2());
        }
        return false;
    }
    
    public int compareTo(STEdge b){
        return Integer.valueOf(length()).compareTo(b.length());
    }
    
    public String toTableString(){
        return "[ n" + p1.getID()+" -- n"+p2.getID()+" ]";
    }
}

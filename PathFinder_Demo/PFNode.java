/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pathfinder_demo;
import java.awt.Point;
import java.util.LinkedList;
/**
 *
 * @author 15002
 */
public class PFNode implements Comparable<PFNode>{
    private static int currID = 0;
    private int ID;
    private int capacity;
    private int occupied;
    private LinkedList<PFEdge> edges = new LinkedList<PFEdge>();
    private LinkedList<UIWire> wires = new LinkedList <UIWire>(); //For Channel
    private double baseCost;
    private UIBlock block;
    private boolean isVertical;
    private PFNode prev;
    private double pathCost;
    private double hVal =1;
    private double hPrev = 1;
    private double hValBackUp= hVal;
    private boolean visitException =false;
    public boolean inCapacity(){
        return occupied <= capacity;
    }
    
    public PFNode(int c,double bc,UIBlock b){
        capacity = c;
        baseCost = bc;
        isVertical = b.getDot().isIsVertical();
        block = b;
        wires=block.getWires();
        ID = currID;
        currID++;
    }
    
    public void addEdge(PFEdge edge){
        edges.add(edge);
    }
    
    public LinkedList<PFEdge> getEdges(){
        return edges;
    }
    
    public void occupy(){
        occupied++;
    }
    
    public void setOccupied(int o){
        occupied=o;
    }
    
    public void clearStats(boolean h){
        occupied = 0;
        prev=null;
        pathCost=0;
        if(h){
            hVal=1;
            hPrev=1;
        }
    }
    
    public void resetWires(){
        if(!wires.isEmpty()){
            for(UIWire wire : wires){
                wire.clearTargets();
            }
        }
    }
    
    @Override 
    public boolean equals(Object obj){
        if(obj instanceof PFNode){
            if (ID==((PFNode)obj).ID) return true;
        }
        return false;
    }
    
    public int getID(){
        return ID;
    }
//    public void clearEdges(){
//        edges.clear();
//    }
    
    public double getBaseCost(){
        return baseCost;
    }
    
    public void setBaseCost(double c){
        baseCost = c;
    }
    
    public void setBlock(UIBlock b){
        block = b;
    }
    
    public UIBlock getBlock(){
        return block;
    }
    
    public String getType(){
        return block.getDot().getType();
    }

    public int getCapacity() {
        return capacity;
    }

    public int getOccupied() {
        return occupied;
    }

    public boolean isIsVertical() {
        return isVertical;
    }

    public PFNode getPrev() {
        return prev;
    }

    public void setPrev(PFNode prev) {
        this.prev = prev;
    }

    public double getPathCost() {
        return pathCost;
    }

    public void setPathCost(double pathCost) {
        this.pathCost = pathCost;
    }

    public double getHVal() {
        return hVal;
    }

    public void setHVal(double hprev) {
        this.hVal = hprev;
    }
    
    public void backUpHVal(){
        hValBackUp=hVal;
    }
    
    public void recoverHVal(){
        hVal=hValBackUp;
    }
    
    @Override
    public int compareTo(PFNode n){
        return Double.valueOf(pathCost).compareTo(Double.valueOf(n.getPathCost()));
    }

    public LinkedList<UIWire> getWires() {
        return wires;
    }

    public boolean isVisitException() {
        return visitException;
    }

    public void setVisitException(boolean visitException) {
        this.visitException = visitException;
    }

    public double gethPrev() {
        return hPrev;
    }

    public void sethPrev(double hPrev) {
        this.hPrev = hPrev;
    }
    
    
    
    
}

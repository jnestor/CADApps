/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pathfinder_demo;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 * @author 15002
 */
public class SwitchBlock extends UIBlock {

    private CopyOnWriteArrayList<PFEdge> edges = new CopyOnWriteArrayList<PFEdge>();
    
    private CopyOnWriteArrayList<Channel> channels = new CopyOnWriteArrayList<Channel>();
    
    
    public SwitchBlock(UIDot d){
        super(d);
    }
    
    public void addChannel(Channel c){
        channels.add(c);
    }
    
    public void addEdge(PFEdge e){
        edges.add(e);
    }
    
    public CopyOnWriteArrayList<Channel> getChannels(){
        return channels;
    }
    
}

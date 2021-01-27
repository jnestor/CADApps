/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pathfinder_demo;

/**
 *
 * @author 15002
 */
public class Terminal extends UIDotContainer{
    Channel channel;
    public Terminal(UIDot n){
        super(n);
    }
    
    public void setChannel(Channel c){
        channel = c;
    }
    
    public Channel getChannel(){
        return channel;
    }
}

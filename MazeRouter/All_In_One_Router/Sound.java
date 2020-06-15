/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package all.in.onerouter;

import javax.sound.sampled.*;
import java.io.File;

/**
 *
 * @author 15002
 */
public class Sound {
    Clip clip;
    public Sound(){
        try{
        AudioInputStream beep = AudioSystem.getAudioInputStream(new File("src\\beep.wav"));//I am not sure about how file path works, blueJ is another story
        clip=AudioSystem.getClip();
        clip.open(beep);
        }
        catch(Exception e){
            System.out.println(e.toString());
        }
    } 
    public void play(){
        clip.setFramePosition(0);
        clip.start();
    }
}

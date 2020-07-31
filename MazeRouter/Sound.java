/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import javax.sound.sampled.*;
import javax.sound.sampled.AudioSystem;
import java.io.*;

/**
 *
 * @author 15002
 */
public class Sound {

    public Sound()

    {
        byte[] buf = new byte[(int) SAMPLERATE * 100 / 1000];
        for (int i = 0; i < buf.length; i++) {
            double angle = i / (SAMPLERATE / 100) * 2.0 * Math.PI;
            buf[i] = (byte) (Math.sin(angle) * 127.0 * 0);
        }

        for (int i = 0; i < SAMPLERATE / 100.0 && i < buf.length / 2; i++) {
            buf[i] = (byte) (buf[i] * i / (SAMPLERATE / 100.0));
            buf[buf.length - 1 - i]
                    = (byte) (buf[buf.length - 1 - i] * i / (SAMPLERATE / 100.0));
        }
        try {
            AudioFormat af = new AudioFormat(SAMPLERATE, 16, 1, true, false);
            SourceDataLine sdl = AudioSystem.getSourceDataLine(af);
            sdl.open(af);
            sdl.start();
            sdl.write(buf, 0, buf.length);
        } catch (LineUnavailableException e) {
        }
    }
    public static float SAMPLERATE = 8000f;
    private static int playCount = 0;

    public static void changeFreq(int hz) throws LineUnavailableException {
        byte[] buf = new byte[(int) SAMPLERATE * 70 / 1000];
        for (int i = 0; i < buf.length - 256; i++) {
            double angle = i / (SAMPLERATE / hz) * 2.0 * Math.PI;
            buf[i] = (byte) (Math.sin(angle) * 127.0 * 1);
        }

        for (int i = buf.length - 256; i < buf.length; i++) {
            double angle = i / (SAMPLERATE / hz) * 2.0 * Math.PI;
            buf[i] = (byte) (Math.sin(angle) * 127.0 * (buf.length - i) / 256);
        }

        for (int i = 0; i < SAMPLERATE / 100.0 && i < buf.length / 2; i++) {
            buf[i] = (byte) (buf[i] * i / (SAMPLERATE / 100.0));
            buf[buf.length - 1 - i]
                    = (byte) (buf[buf.length - 1 - i] * i / (SAMPLERATE / 100.0));
        }

        AudioFormat af = new AudioFormat(SAMPLERATE, 16, 1, true, false);
        SourceDataLine sdl = AudioSystem.getSourceDataLine(af);
        sdl.open(af);
        sdl.start();
        sdl.write(buf, 0, buf.length);
        sdl.drain();
        sdl.close();

    }

    public void play(int i) {
        try {
            changeFreq(100 + i * 5);
        } catch (LineUnavailableException e) {
        }
    }
}

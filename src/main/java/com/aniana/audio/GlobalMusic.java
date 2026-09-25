package com.aniana.audio;

import javax.sound.sampled.*;
import java.io.InputStream;

public class GlobalMusic {

    private static Clip clip;

    private static boolean initialized = false;

    public static void playLoop(String path) {

        if (initialized) return; 

        try {
            InputStream is = GlobalMusic.class.getResourceAsStream(path);
            if (is == null) {
                System.err.println("Music not found: " + path);
                return;
            }

            AudioInputStream audio = AudioSystem.getAudioInputStream(is);
            clip = AudioSystem.getClip();
            clip.open(audio);

            clip.loop(Clip.LOOP_CONTINUOUSLY); 
            clip.start();

            initialized = true;

        } catch (Exception e) {
            System.err.println("Music error: " + e.getMessage());
        }
    }

    public static void stop() {
        if (clip != null) {
            clip.stop();
            clip.close();
        }
        initialized = false;
    }
}
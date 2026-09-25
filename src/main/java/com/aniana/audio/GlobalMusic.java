package com.aniana.audio;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
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

            // BufferedInputStream supports mark/reset, which AudioSystem requires
            AudioInputStream audio = AudioSystem.getAudioInputStream(
                    new BufferedInputStream(is)
            );

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
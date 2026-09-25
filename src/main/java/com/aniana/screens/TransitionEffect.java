package com.aniana.screens;

import java.awt.*;

public class TransitionEffect {

    public enum Phase { IDLE, FADE_OUT, FADE_IN }
    private Phase phase = Phase.IDLE;
    private float alpha = 0f;
    private Runnable onMidpoint;

    public void startFade(Runnable onMidpoint) {
        this.onMidpoint = onMidpoint;
        phase = Phase.FADE_OUT;
        alpha = 0f;
    }

    public boolean isIdle() { return phase == Phase.IDLE; }

    public void update() {
        if (phase == Phase.FADE_OUT) {
            alpha += 0.05f;
            if (alpha >= 1f) {
                alpha = 1f;
                if (onMidpoint != null) onMidpoint.run();
                phase = Phase.FADE_IN;
            }
        } else if (phase == Phase.FADE_IN) {
            alpha -= 0.05f;
            if (alpha <= 0f) {
                alpha = 0f;
                phase = Phase.IDLE;
            }
        }
    }

    public void render(Graphics2D g, int w, int h) {
        if (phase == Phase.IDLE) return;
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, w, h);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    }
}

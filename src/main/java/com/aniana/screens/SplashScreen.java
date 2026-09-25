package com.aniana.screens;

import com.aniana.core.GameState;
import com.aniana.core.InputHandler;

import java.awt.*;
import java.awt.geom.Point2D;


public class SplashScreen {

    private float alpha    = 0f;
    private int   timer    = 0;
    private float pulse    = 0f;
    private float starAngle = 0f;

    public GameState update(InputHandler input) {
        timer++;
        if (alpha < 1f) alpha = Math.min(1f, alpha + 0.015f);
        pulse      = (float)(0.5 + 0.5 * Math.sin(timer * 0.06));
        starAngle += 0.4f;

        if (timer > 60 && (input.enter || input.space)) {
            input.consumeEnter();
            input.consumeSpace();
            return GameState.TITLE_MENU;
        }
        return GameState.SPLASH;
    }

    public void render(Graphics2D g, int w, int h) {
      
        GradientPaint grad = new GradientPaint(0, 0, new Color(5, 5, 30),
                                               0, h, new Color(20, 10, 60));
        g.setPaint(grad);
        g.fillRect(0, 0, w, h);

      
        g.setColor(new Color(255, 255, 255, 80));
        for (int i = 0; i < 60; i++) {
            int sx = (int)((Math.cos(i * 2.1 + starAngle * 0.01) * 0.5 + 0.5) * w);
            int sy = (int)((Math.sin(i * 1.7 + starAngle * 0.007) * 0.5 + 0.5) * h);
            g.fillOval(sx, sy, 2, 2);
        }
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        Font titleFont = new Font("Serif", Font.BOLD, 72);
        g.setFont(titleFont);

       
        g.setColor(new Color(80, 40, 160, 180));
        FontMetrics fm = g.getFontMetrics();
        int tx = (w - fm.stringWidth("ANIANA")) / 2;
        g.drawString("ANIANA", tx + 4, h / 2 - 60 + 4);
        GradientPaint titleGrad = new GradientPaint(tx, h/2 - 120,
                new Color(255, 210, 80), tx, h/2 - 60, new Color(200, 120, 255));
        g.setPaint(titleGrad);
        g.drawString("ANIANA", tx, h / 2 - 60);
        g.setFont(new Font("Serif", Font.ITALIC, 18));
        g.setColor(new Color(200, 180, 255));
        String sub = "The Journey of Anne and Kia";
        fm = g.getFontMetrics();
        g.drawString(sub, (w - fm.stringWidth(sub)) / 2, h / 2 - 20);
        g.setColor(new Color(180, 140, 255, 150));
        g.drawLine(w/2 - 120, h/2 - 5, w/2 + 120, h/2 - 5);
        if (timer > 60) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, pulse));
            g.setFont(new Font("Arial", Font.BOLD, 16));
            g.setColor(new Color(255, 240, 150));
            String prompt = "Press ENTER to Start";
            fm = g.getFontMetrics();
            g.drawString(prompt, (w - fm.stringWidth(prompt)) / 2, h / 2 + 50);
        }

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        g.setFont(new Font("Arial", Font.PLAIN, 11));
        g.setColor(new Color(150, 130, 180));
        g.drawString("© 2026 ANIANA Attack on Code  –  Built with Java & Tiled", 14, h - 12);
    }
}

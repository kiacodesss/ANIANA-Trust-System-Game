package com.aniana.ui;

import com.aniana.core.GameData;

import java.awt.*;


public class HUD {

    private static final Font LABEL = new Font("Arial", Font.BOLD, 12);
    private static final Font VALUE = new Font("Arial", Font.PLAIN, 12);

    public void render(Graphics2D g, GameData data, int screenW) {
       
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, screenW, 22);

        g.setFont(LABEL);
        g.setColor(new Color(100, 220, 100));
        g.drawString("TRUST:", 8, 15);
        g.setFont(VALUE);
        g.setColor(Color.WHITE);
        g.drawString(String.valueOf(data.trustPoints), 62, 15);

        g.setFont(LABEL);
        g.setColor(new Color(220, 180, 80));
        g.drawString("MORALITY:", 110, 15);
        g.setFont(VALUE);
        g.setColor(Color.WHITE);
        g.drawString(String.valueOf(data.moralityPoints), 182, 15);

        g.setFont(LABEL);
        g.setColor(new Color(150, 200, 255));
        g.drawString("MAP: " + data.currentMapId, 230, 15);
    }
}

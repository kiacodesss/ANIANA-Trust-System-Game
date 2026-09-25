package com.aniana.screens;

import com.aniana.core.GameData;
import com.aniana.core.GameState;
import com.aniana.core.InputHandler;

import java.awt.*;


public class GameEndScreen {

    private int   timer   = 0;
    private float alpha   = 0f;
    private float starAng = 0f;

    public void reset() { timer = 0; alpha = 0f; }

    public GameState update(InputHandler input, GameData data) {
        timer++;
        starAng += 0.5f;
        if (alpha < 1f) alpha = Math.min(1f, alpha + 0.01f);

        if (timer > 120 && (input.enter || input.space)) {
            input.consumeEnter();
           
            return GameState.TITLE_MENU;
        }
        return GameState.GAME_END;
    }

    public void render(Graphics2D g, int w, int h, GameData data) {
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

       
        GradientPaint bg = new GradientPaint(0, 0, new Color(5, 5, 30), 0, h, new Color(30, 10, 60));
        g.setPaint(bg);
        g.fillRect(0, 0, w, h);

        for (int i = 0; i < 40; i++) {
            float a = (float)(i * 1.57 + starAng * 0.02);
            int sx = (int)(w / 2 + Math.cos(a) * (100 + i * 5));
            int sy = (int)(h / 2 - 60 + Math.sin(a * 1.3) * (60 + i * 3));
            g.setColor(new Color(255, 230, 100, 80 + (i % 3) * 50));
            g.fillOval(sx, sy, 4, 4);
        }

        g.setFont(new Font("Serif", Font.BOLD, 52));
        GradientPaint titleG = new GradientPaint(0, 60, new Color(255, 210, 60),
                                                 0, 120, new Color(220, 120, 255));
        g.setPaint(titleG);
        String title = "Journey's End";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(title, (w - fm.stringWidth(title)) / 2, 90);

       
        g.setColor(new Color(200, 160, 255, 180));
        g.fillRect(w/2 - 150, 100, 300, 2);

       
        g.setColor(new Color(15, 15, 50, 220));
        g.fillRoundRect(w/2 - 180, 120, 360, 220, 16, 16);
        g.setColor(new Color(180, 160, 100));
        g.drawRoundRect(w/2 - 180, 120, 360, 220, 16, 16);

        int bx = w/2 - 150, by = 150;

        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.setColor(new Color(150, 230, 150));
        g.drawString("Trust Points Earned:", bx, by);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 22));
        g.drawString(String.valueOf(data.trustPoints), bx + 200, by);
        by += 36;

        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.setColor(new Color(230, 200, 100));
        g.drawString("Morality Points:", bx, by);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 22));
        g.drawString(String.valueOf(data.moralityPoints), bx + 200, by);
        by += 36;

        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.setColor(new Color(200, 160, 255));
        g.drawString("Total:", bx, by);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 22));
        g.drawString(String.valueOf(data.trustPoints + data.moralityPoints), bx + 200, by);
        by += 40;

       
        if (data.dragonDefeated) {
            g.setFont(new Font("Arial", Font.BOLD, 12));
            g.setColor(new Color(100, 200, 255));
            g.drawString("★ Blue Dragon Defeated!", bx, by);
            by += 24;
        }

       
        g.setFont(new Font("Serif", Font.BOLD, 20));
        g.setColor(new Color(255, 230, 80));
        String rating = data.getEndingRating();
        fm = g.getFontMetrics();
        g.drawString(rating, (w - fm.stringWidth(rating)) / 2, 370);

        
        g.setColor(new Color(200, 180, 255));
        g.setFont(new Font("Serif", Font.ITALIC, 16));
        String msg1 = "\"The bond you forged cannot be broken.\"";
        fm = g.getFontMetrics();
        g.drawString(msg1, (w - fm.stringWidth(msg1)) / 2, 410);

        // Credits
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        g.setColor(new Color(140, 120, 160));
        String credit = "Thank you for playing ANIANA";
        fm = g.getFontMetrics();
        g.drawString(credit, (w - fm.stringWidth(credit)) / 2, 450);

       
        if (timer > 120) {
            float blink = (float)(0.5 + 0.5 * Math.sin(timer * 0.07));
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, blink));
            g.setColor(new Color(220, 200, 100));
            g.setFont(new Font("Arial", Font.BOLD, 13));
            String pr = "Press ENTER to return to Main Menu";
            fm = g.getFontMetrics();
            g.drawString(pr, (w - fm.stringWidth(pr)) / 2, h - 20);
        }
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    }
}

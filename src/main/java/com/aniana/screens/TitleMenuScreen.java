package com.aniana.screens;

import com.aniana.core.GameData;
import com.aniana.core.GameState;
import com.aniana.core.InputHandler;

import java.awt.*;


public class TitleMenuScreen {

    private enum MenuStep { MODE_SELECT, CHAR_SELECT }
    private MenuStep step     = MenuStep.MODE_SELECT;
    private int      modeIdx  = 0; 
    private int      charIdx  = 0; 
    private int      timer    = 0;
    private float    pulse    = 0;

    public GameState update(InputHandler input, GameData data) {
        timer++;
        pulse = (float)(0.5 + 0.5 * Math.sin(timer * 0.08));

        if (step == MenuStep.MODE_SELECT) {
            if (input.p1Up || input.p2Up)     { modeIdx = Math.max(0, modeIdx - 1); }
            if (input.p1Down || input.p2Down)  { modeIdx = Math.min(1, modeIdx + 1); }

            if (input.enter) {
                input.consumeEnter();
                data.playerMode = (modeIdx == 0)
                        ? GameData.PlayerMode.ONE_PLAYER
                        : GameData.PlayerMode.TWO_PLAYER;

                if (data.playerMode == GameData.PlayerMode.ONE_PLAYER) {
                    step = MenuStep.CHAR_SELECT;
                } else {
                    data.p1Choice = GameData.CharacterChoice.KIA;
                    return GameState.PLAYING;
                }
            }
        } else {
            if (input.p1Up || input.p2Up)    { charIdx = Math.max(0, charIdx - 1); }
            if (input.p1Down || input.p2Down) { charIdx = Math.min(1, charIdx + 1); }

            if (input.enter) {
                input.consumeEnter();
                data.p1Choice = (charIdx == 0)
                        ? GameData.CharacterChoice.KIA
                        : GameData.CharacterChoice.ANNE;
                return GameState.PLAYING;
            }
            if (input.escape) {
                input.consumeEscape();
                step = MenuStep.MODE_SELECT;
            }
        }
        return GameState.TITLE_MENU;
    }

    public void render(Graphics2D g, int w, int h) {
      
        GradientPaint bg = new GradientPaint(0, 0, new Color(10, 5, 40), 0, h, new Color(30, 15, 70));
        g.setPaint(bg);
        g.fillRect(0, 0, w, h);
        g.setFont(new Font("Serif", Font.BOLD, 48));
        GradientPaint titleGrad = new GradientPaint(0, 50, new Color(255, 210, 80),
                                                    0, 100, new Color(200, 120, 255));
        g.setPaint(titleGrad);
        FontMetrics fm = g.getFontMetrics();
        g.drawString("ANIANA", (w - fm.stringWidth("ANIANA")) / 2, 80);

        if (step == MenuStep.MODE_SELECT) {
            g.setColor(new Color(200, 180, 255));
            g.setFont(new Font("Serif", Font.BOLD, 22));
            String sub = "Choose Game Mode";
            fm = g.getFontMetrics();
            g.drawString(sub, (w - fm.stringWidth(sub)) / 2, 130);

            drawOption(g, w, h, "1 PLAYER", 0, modeIdx, 190);
            drawOption(g, w, h, "2 PLAYER", 1, modeIdx, 240);

        
            g.setColor(new Color(20, 20, 50, 200));
            g.fillRoundRect(w/2 - 200, 290, 400, 90, 12, 12);
            g.setColor(new Color(180, 160, 100));
            g.drawRoundRect(w/2 - 200, 290, 400, 90, 12, 12);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 12));
            if (modeIdx == 0) {
                drawCentered(g, "Choose Kia or Anne as your character.", w, 315);
                drawCentered(g, "The other will become your AI companion,", w, 333);
                drawCentered(g, "following you and offering guidance.", w, 351);
                drawCentered(g, "Controls: ARROW KEYS", w, 369);
            } else {
                drawCentered(g, "Player 1 plays as Kia (ARROW KEYS).", w, 315);
                drawCentered(g, "Player 2 plays as Anne (WASD + F to interact).", w, 333);
                drawCentered(g, "Work together to earn Trust & Morality!", w, 351);
            }

            blinkHint(g, w, h, "UP/DOWN to select  –  ENTER to confirm", pulse);

        } else {
            g.setColor(new Color(200, 180, 255));
            g.setFont(new Font("Serif", Font.BOLD, 22));
            String sub = "Choose Your Character";
            fm = g.getFontMetrics();
            g.drawString(sub, (w - fm.stringWidth(sub)) / 2, 130);

            drawOption(g, w, h, "KIA ", 0, charIdx, 190);
            drawOption(g, w, h, "ANNE",     1, charIdx, 240);

            g.setColor(new Color(150, 200, 255));
            g.setFont(new Font("Arial", Font.ITALIC, 13));
            if (charIdx == 0) {
                drawCentered(g, "Kia leads with courage. Anne becomes your AI companion.", w, 295);
            } else {
                drawCentered(g, "Anne leads with wisdom. Kia becomes your AI companion.", w, 295);
            }

            blinkHint(g, w, h, "ENTER to confirm  –  ESC to go back", pulse);
        }
    }

    private void drawOption(Graphics2D g, int w, int h, String label, int idx, int selected, int y) {
        boolean sel = (idx == selected);
        g.setColor(sel ? new Color(255, 220, 60) : new Color(180, 180, 220));
        g.setFont(new Font("Arial", sel ? Font.BOLD : Font.PLAIN, 18));
        FontMetrics fm = g.getFontMetrics();
        int x = (w - fm.stringWidth(label)) / 2;
        if (sel) {
            g.setColor(new Color(60, 50, 120, 180));
            g.fillRoundRect(x - 20, y - 20, fm.stringWidth(label) + 40, 30, 8, 8);
            g.setColor(new Color(255, 220, 60));
            g.drawString("▶ " + label, x - 14, y);
        } else {
            g.setColor(new Color(180, 180, 220));
            g.drawString("  " + label, x - 14, y);
        }
    }

    private void blinkHint(Graphics2D g, int w, int h, String msg, float alpha) {
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g.setColor(new Color(200, 200, 150));
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        drawCentered(g, msg, w, h - 25);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    }

    private void drawCentered(Graphics2D g, String text, int w, int y) {
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, (w - fm.stringWidth(text)) / 2, y);
    }
}

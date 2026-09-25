package com.aniana.ui;

import com.aniana.core.GameData;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;


public class DialogueBox {

   
    public record DialogueLine(String speaker, String text, int trustReward, int moralityReward) {}

   
    public static class ChoiceNode {
        public final String            prompt;      
        public final List<DialogueLine> choices;    
        public final List<List<DialogueLine>> followUps; 

        public ChoiceNode(String prompt, List<DialogueLine> choices,
                          List<List<DialogueLine>> followUps) {
            this.prompt    = prompt;
            this.choices   = choices;
            this.followUps = followUps;
        }
    }

    
    public static class DialogueSequence {
        public final List<DialogueLine> opening;  
        public final ChoiceNode choice;           
        public final List<DialogueLine> closing;  

        public DialogueSequence(List<DialogueLine> opening, ChoiceNode choice,
                                List<DialogueLine> closing) {
            this.opening = opening;
            this.choice  = choice;
            this.closing = closing;
        }

        
        public DialogueSequence(List<DialogueLine> lines) {
            this(lines, null, List.of());
        }
    }

   

    private enum Phase { OPENING, CHOICE, CHOICE_FOLLOWUP, CLOSING, DONE }

    private DialogueSequence sequence;
    private Phase   phase         = Phase.DONE;
    private int     lineIdx       = 0;
    private int     choiceIdx     = 0;      
    private List<DialogueLine> followUpLines; 

    private boolean active = false;

   
    private static final int BOX_H        = 120;
    private static final int CHOICE_BOX_H = 200;
    private static final int MARGIN       = 14;
    private static final Font SPEAKER_FONT = new Font("Serif",  Font.BOLD,  14);
    private static final Font TEXT_FONT    = new Font("Serif",  Font.PLAIN, 13);
    private static final Font CHOICE_FONT  = new Font("Arial",  Font.PLAIN, 13);
    private static final Font HINT_FONT    = new Font("Arial",  Font.ITALIC, 10);
    private static final Font PROMPT_FONT  = new Font("Arial",  Font.BOLD,  13);

  

    public void startDialogue(DialogueSequence seq) {
        this.sequence     = seq;
        this.lineIdx      = 0;
        this.choiceIdx    = 0;
        this.followUpLines = null;
        this.active       = true;
        if (!seq.opening.isEmpty()) {
            phase = Phase.OPENING;
        } else if (seq.choice != null) {
            phase = Phase.CHOICE;
        } else if (!seq.closing.isEmpty()) {
            phase = Phase.CLOSING;
        } else {
            phase = Phase.DONE;
            active = false;
        }
    }

   
    public void startDialogue(List<DialogueLine> lines) {
        startDialogue(new DialogueSequence(lines));
    }

    public boolean isActive()      { return active; }
    public boolean isShowingChoice() { return phase == Phase.CHOICE; }

    
    public void choiceUp() {
        if (phase == Phase.CHOICE && sequence.choice != null) {
            choiceIdx = Math.max(0, choiceIdx - 1);
        }
    }

   
    public void choiceDown() {
        if (phase == Phase.CHOICE && sequence.choice != null) {
            choiceIdx = Math.min(sequence.choice.choices.size() - 1, choiceIdx + 1);
        }
    }

   
    public boolean advance(GameData data) {
        if (!active) return true;

        switch (phase) {
            case OPENING -> {
               
                DialogueLine line = sequence.opening.get(lineIdx);
                data.addTrust(line.trustReward);
                data.addMorality(line.moralityReward);
                lineIdx++;
                if (lineIdx >= sequence.opening.size()) {
                    lineIdx = 0;
                    if (sequence.choice != null) {
                        phase = Phase.CHOICE;
                    } else if (!sequence.closing.isEmpty()) {
                        phase = Phase.CLOSING;
                    } else {
                        finish();
                    }
                }
            }
            case CHOICE -> {
               
                ChoiceNode cn = sequence.choice;
                if (cn != null && !cn.choices.isEmpty()) {
                    DialogueLine chosen = cn.choices.get(choiceIdx);
                    data.addTrust(chosen.trustReward);
                    data.addMorality(chosen.moralityReward);
                    if (cn.followUps != null && choiceIdx < cn.followUps.size()) {
                        followUpLines = cn.followUps.get(choiceIdx);
                    } else {
                        followUpLines = List.of();
                    }
                    lineIdx = 0;

                    if (!followUpLines.isEmpty()) {
                        phase = Phase.CHOICE_FOLLOWUP;
                    } else if (!sequence.closing.isEmpty()) {
                        phase = Phase.CLOSING;
                    } else {
                        finish();
                    }
                }
            }
            case CHOICE_FOLLOWUP -> {
                DialogueLine line = followUpLines.get(lineIdx);
                data.addTrust(line.trustReward);
                data.addMorality(line.moralityReward);
                lineIdx++;
                if (lineIdx >= followUpLines.size()) {
                    lineIdx = 0;
                    if (!sequence.closing.isEmpty()) {
                        phase = Phase.CLOSING;
                    } else {
                        finish();
                    }
                }
            }
            case CLOSING -> {
                DialogueLine line = sequence.closing.get(lineIdx);
                data.addTrust(line.trustReward);
                data.addMorality(line.moralityReward);
                lineIdx++;
                if (lineIdx >= sequence.closing.size()) {
                    finish();
                }
            }
            case DONE -> { return true; }
        }
        return phase == Phase.DONE;
    }

    private void finish() {
        phase  = Phase.DONE;
        active = false;
    }

    public void render(Graphics2D g, int screenW, int screenH) {
        if (!active) return;

        if (phase == Phase.CHOICE) {
            renderChoicePanel(g, screenW, screenH);
        } else {
            renderLinePanel(g, screenW, screenH);
        }
    }

    private void renderLinePanel(Graphics2D g, int screenW, int screenH) {
        DialogueLine line = currentLine();
        if (line == null) return;

        int boxY = screenH - BOX_H - 6;
        drawBox(g, MARGIN, boxY, screenW - MARGIN * 2, BOX_H);
        g.setFont(SPEAKER_FONT);
        g.setColor(new Color(255, 220, 100));
        g.drawString(line.speaker, MARGIN + 10, boxY + 22);
        g.setFont(TEXT_FONT);
        g.setColor(Color.WHITE);
        drawWrappedText(g, line.text, MARGIN + 10, boxY + 42, screenW - MARGIN * 2 - 20);
        g.setFont(HINT_FONT);
        g.setColor(new Color(180, 180, 180));
        g.drawString("▼ Press ENTER to continue", screenW - MARGIN - 148, boxY + BOX_H - 8);
    }

    private void renderChoicePanel(Graphics2D g, int screenW, int screenH) {
        ChoiceNode cn = sequence.choice;
        if (cn == null) return;

        int boxH  = CHOICE_BOX_H;
        int boxY  = screenH - boxH - 6;
        int boxX  = MARGIN;
        int boxW  = screenW - MARGIN * 2;

        drawBox(g, boxX, boxY, boxW, boxH);
        g.setFont(PROMPT_FONT);
        g.setColor(new Color(255, 220, 100));
        g.drawString(cn.prompt, boxX + 10, boxY + 22);

        g.setColor(new Color(180, 160, 100));
        g.drawLine(boxX + 10, boxY + 28, boxX + boxW - 10, boxY + 28);
        int cy = boxY + 46;
        for (int i = 0; i < cn.choices.size(); i++) {
            DialogueLine ch = cn.choices.get(i);
            boolean sel = (i == choiceIdx);

            if (sel) {
                g.setColor(new Color(60, 50, 120, 180));
                g.fillRoundRect(boxX + 8, cy - 14, boxW - 16, 22, 6, 6);
                g.setColor(new Color(255, 220, 60));
                g.setFont(new Font("Arial", Font.BOLD, 13));
                g.drawString("▶ " + ch.text, boxX + 14, cy);
            } else {
                g.setColor(new Color(200, 200, 200));
                g.setFont(CHOICE_FONT);
                g.drawString("  " + ch.text, boxX + 14, cy);
            }

            if (ch.trustReward != 0 || ch.moralityReward != 0) {
                StringBuilder hint = new StringBuilder();
                if (ch.trustReward > 0)    hint.append("+").append(ch.trustReward).append(" Trust ");
                if (ch.trustReward < 0)    hint.append(ch.trustReward).append(" Trust ");
                if (ch.moralityReward > 0) hint.append("+").append(ch.moralityReward).append(" Moral");
                if (ch.moralityReward < 0) hint.append(ch.moralityReward).append(" Moral");
                g.setFont(new Font("Arial", Font.ITALIC, 10));
                g.setColor(ch.trustReward >= 0 && ch.moralityReward >= 0
                        ? new Color(100, 220, 100) : new Color(220, 100, 100));
                g.drawString(hint.toString().trim(), boxX + boxW - 120, cy);
            }

            cy += 30;
        }

        g.setFont(HINT_FONT);
        g.setColor(new Color(180, 180, 180));
        g.drawString("UP/DOWN to choose  –  ENTER to confirm",
                boxX + boxW - 210, boxY + boxH - 8);
    }

    private DialogueLine currentLine() {
        return switch (phase) {
            case OPENING         -> !sequence.opening.isEmpty() && lineIdx < sequence.opening.size()
                                    ? sequence.opening.get(lineIdx) : null;
            case CHOICE_FOLLOWUP -> followUpLines != null && lineIdx < followUpLines.size()
                                    ? followUpLines.get(lineIdx) : null;
            case CLOSING         -> !sequence.closing.isEmpty() && lineIdx < sequence.closing.size()
                                    ? sequence.closing.get(lineIdx) : null;
            default -> null;
        };
    }

    private void drawBox(Graphics2D g, int x, int y, int w, int h) {
        g.setColor(new Color(10, 10, 30, 220));
        g.fillRoundRect(x, y, w, h, 12, 12);
        g.setColor(new Color(180, 160, 100));
        g.drawRoundRect(x, y, w, h, 12, 12);
    }

    private void drawWrappedText(Graphics2D g, String text, int x, int y, int maxW) {
        FontMetrics fm = g.getFontMetrics();
        String[] words  = text.split(" ");
        StringBuilder line = new StringBuilder();
        int lineY = y;
        for (String word : words) {
            String test = line + (line.length() > 0 ? " " : "") + word;
            if (fm.stringWidth(test) > maxW && line.length() > 0) {
                g.drawString(line.toString(), x, lineY);
                lineY += fm.getHeight() + 2;
                line  = new StringBuilder(word);
            } else {
                if (line.length() > 0) line.append(" ");
                line.append(word);
            }
        }
        if (line.length() > 0) g.drawString(line.toString(), x, lineY);
    }
}

package com.aniana.battle;

import com.aniana.core.GameData;
import com.aniana.core.InputHandler;
import com.aniana.entities.Enemy;
import com.aniana.entities.Player;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class BattleSystem {

    public enum BattleState { PLAYER_TURN, ENEMY_TURN, WIN, LOSE, FLEE }

    private BattleState        state           = BattleState.PLAYER_TURN;
    private int                selection       = 0;
    private final List<String> log             = new ArrayList<>();
    private final Random       rng             = new Random();

    private Player   player;
    private Player   companion;
    private Enemy    enemy;
    private GameData data;

    private boolean waitingForInput  = false;  
    private boolean playerDefending  = false;
    private boolean exitReady        = false;
    private int     resultFrames     = 0;      
    private boolean enemyPending     = false;
    private int     enemyDelayFrames = 0;
    private static final int ENEMY_DELAY = 45; 
    private int inputFlushFrames = 0;
    private static final int INPUT_FLUSH = 10; 

    private static final String[] ACTIONS = {"ATTACK", "DEFEND", "USE ITEM", "FLEE"};

  

    public void start(Player player, Player companion, Enemy enemy, GameData data) {
        this.player    = player;
        this.companion = companion;
        this.enemy     = enemy;
        this.data      = data;

        state            = BattleState.PLAYER_TURN;
        selection        = 0;
        waitingForInput  = false;   
        playerDefending  = false;
        exitReady        = false;
        resultFrames     = 0;
        enemyPending     = false;
        enemyDelayFrames = 0;
        inputFlushFrames = 0;       

        log.clear();
        addLog("A wild BLUE DRAGON appeared!");
        addLog("Your turn — choose an action.");
    }

    
    public boolean isExitReady() { return exitReady; }
    public boolean playerWon()   { return state == BattleState.WIN;  }
    public boolean playerLost()  { return state == BattleState.LOSE; }
    public boolean playerFled()  { return state == BattleState.FLEE; }

   

    public void update(InputHandler input) {

       
        if (inputFlushFrames < INPUT_FLUSH) {
            inputFlushFrames++;
            input.consumeBattleNav();
            input.consumeBattleConfirm();
            input.consumeBattleConfirmExit();
            if (inputFlushFrames == INPUT_FLUSH) {
                waitingForInput = true; 
            }
            return;
        }

        
        if (isOver()) {
            resultFrames++;
            
            if (resultFrames >= 30 && input.battleConfirmExit) {
                input.consumeBattleConfirmExit();
                exitReady = true;
            }
            return;
        }

        
        if (enemyPending) {
            enemyDelayFrames++;
            if (enemyDelayFrames >= ENEMY_DELAY) {
                enemyPending     = false;
                enemyDelayFrames = 0;
                executeEnemyTurn();
            }
            return;
        }

       
        if (state != BattleState.PLAYER_TURN || !waitingForInput) return;

        if (input.battleUp)   { selection = Math.max(0, selection - 1); }
        if (input.battleDown) { selection = Math.min(ACTIONS.length - 1, selection + 1); }
        input.consumeBattleNav();

        if (input.battleConfirm) {
            input.consumeBattleConfirm();
            waitingForInput = false;
            executePlayerTurn(selection);
        }
    }



    private boolean isOver() {
        return state == BattleState.WIN
            || state == BattleState.LOSE
            || state == BattleState.FLEE;
    }

    private void addLog(String msg) {
        if (log.size() > 6) log.remove(0);
        log.add(msg);
    }

   
    private void executePlayerTurn(int action) {
        
        playerDefending = false;

        switch (action) {
            case 0 -> { 
                int dmg = player.attack + rng.nextInt(10);
                if (companion != null) dmg += 5;
                enemy.hp -= dmg;
                addLog(player.getName() + " attacks for " + dmg + " damage!");
                if (companion != null) addLog(companion.getName() + " assists!");
                if (enemy.hp <= 0) { enemy.hp = 0; endBattle(true); return; }
               
                queueEnemyTurn(1.0f);
            }
            case 1 -> { 
                playerDefending = true;
                addLog(player.getName() + " braces for impact! (Damage blocked)");
               
                if (companion != null) {
                    int healAmt = Math.max(1, player.maxHp / 5);
                    player.hp = Math.min(player.maxHp, player.hp + healAmt);
                    addLog(companion.getName() + " channels healing! +" + healAmt + " HP");
                }
                queueEnemyTurn(1.0f);
            }
            case 2 -> { 
                
                int itemDmg = Math.max(10, enemy.hp / 2);
                enemy.hp -= itemDmg;
                addLog(player.getName() + " hurls a Bomb Crystal! -" + itemDmg + " to Dragon!");
                if (companion != null) addLog(companion.getName() + " supercharges the blast!");
                if (enemy.hp <= 0) { enemy.hp = 0; endBattle(true); return; }
              
                queueEnemyTurn(0.60f);
            }
            case 3 -> { 
                data.addTrust(-5);
                data.addMorality(-5);
                addLog("You fled from battle! -5 Trust, -5 Morality");
                state = BattleState.FLEE;
            }
        }
    }

   
    private float pendingDamageMultiplier = 1.0f;

    private void queueEnemyTurn(float damageMultiplier) {
        state                   = BattleState.ENEMY_TURN;
        enemyPending            = true;
        enemyDelayFrames        = 0;
        pendingDamageMultiplier = damageMultiplier;
    }

   
    private void executeEnemyTurn() {
        if (playerDefending) {
          
            addLog("Dragon attacks — completely blocked by your defense!");
        } else {
           
            int rawDmg    = Math.max(1, enemy.damage + rng.nextInt(15) - 5);
            int scaledDmg = (int)(rawDmg * pendingDamageMultiplier);
            int cap       = Math.max(1, (int)(player.hp * 0.60f));
            int dmg       = Math.min(scaledDmg, cap);
            player.hp -= dmg;
            String suffix = (pendingDamageMultiplier < 1.0f) ? " (weakened!)" : "";
            addLog("Dragon attacks " + player.getName() + " for " + dmg + "!" + suffix);
            if (player.hp <= 0) { player.hp = 0; endBattle(false); return; }
        }
        pendingDamageMultiplier = 1.0f; 

        
        state           = BattleState.PLAYER_TURN;
        waitingForInput = true;
        addLog("Your turn — choose an action.");
    }

    private void endBattle(boolean win) {
        if (win) {
            state = BattleState.WIN;
            data.dragonDefeated = true;
            data.addTrust(15);
            data.addMorality(10);
            addLog("Dragon defeated! +15 Trust, +10 Morality");
        } else {
            state = BattleState.LOSE;
            player.hp = Math.max(1, player.maxHp / 2);
            addLog("You were defeated...");
        }
        
    }

   
    public void render(Graphics2D g, int screenW, int screenH) {
       
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRect(0, 0, screenW, screenH);

        
        g.setColor(new Color(100, 180, 255));
        g.setFont(new Font("Serif", Font.BOLD, 28));
        String title = "⚔ BATTLE ⚔";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(title, (screenW - fm.stringWidth(title)) / 2, 50);

        
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("BLUE DRAGON  HP: " + enemy.hp + "/" + enemy.maxHp, 30, 90);
        g.setColor(Color.RED);
        g.fillRect(30, 100, 300, 14);
        g.setColor(new Color(0x4466FF));
        g.fillRect(30, 100, Math.max(0, (int)((enemy.hp / (float)enemy.maxHp) * 300)), 14);

        
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.drawString(player.getName() + "  HP: " + player.hp + "/" + player.maxHp, 30, 135);
        g.setColor(Color.RED);
        g.fillRect(30, 142, 200, 10);
        g.setColor(Color.GREEN);
        g.fillRect(30, 142, Math.max(0, (int)((player.hp / (float)player.maxHp) * 200)), 10);

        if (companion != null) {
            g.setColor(new Color(200, 200, 255));
            g.setFont(new Font("Arial", Font.PLAIN, 12));
            g.drawString(companion.getName() + " (ally): assisting!", 30, 165);
        }

       
        String turnLabel;
        Color  turnColor;
        if (state == BattleState.PLAYER_TURN && waitingForInput) {
            turnLabel = "▶ YOUR TURN";
            turnColor = new Color(80, 220, 80);
        } else if (state == BattleState.ENEMY_TURN || enemyPending) {
            turnLabel = "▶ DRAGON'S TURN";
            turnColor = new Color(255, 80, 80);
        } else {
            turnLabel = "";
            turnColor = Color.WHITE;
        }
        if (!turnLabel.isEmpty()) {
            g.setColor(turnColor);
            g.setFont(new Font("Arial", Font.BOLD, 13));
            g.drawString(turnLabel, 30, 182);
        }

        
        if (playerDefending) {
            g.setColor(new Color(60, 180, 255));
            g.setFont(new Font("Arial", Font.BOLD, 12));
            g.drawString("[DEFENDING]", 160, 182);
        }

        
        g.setFont(new Font("Monospaced", Font.PLAIN, 11));
        int ly = 210;
        for (String line : log) {
            g.setColor(new Color(220, 220, 180));
            g.drawString(line, 30, ly);
            ly += 15;
        }

        
        if (state == BattleState.PLAYER_TURN && waitingForInput) {
            int ax = screenW - 210, ay = 80;
            g.setColor(new Color(20, 20, 60, 220));
            g.fillRoundRect(ax - 10, ay - 20, 200, 145, 10, 10);
            g.setColor(new Color(180, 160, 100));
            g.drawRoundRect(ax - 10, ay - 20, 200, 145, 10, 10);
            for (int i = 0; i < ACTIONS.length; i++) {
                if (i == selection) {
                    g.setColor(new Color(255, 220, 60));
                    g.setFont(new Font("Arial", Font.BOLD, 15));
                    g.drawString("▶ " + ACTIONS[i], ax, ay + i * 30);
                } else {
                    g.setColor(Color.WHITE);
                    g.setFont(new Font("Arial", Font.PLAIN, 15));
                    g.drawString("  " + ACTIONS[i], ax, ay + i * 30);
                }
            }
            g.setColor(new Color(160, 160, 160));
            g.setFont(new Font("Arial", Font.PLAIN, 10));
            g.drawString("↑↓ / WS  navigate", ax - 5, ay + 4 * 30 + 5);
            g.drawString("ENTER / F  confirm", ax - 5, ay + 4 * 30 + 18);
        }

        
        if (enemyPending) {
            int ax = screenW - 210, ay = 80;
            g.setColor(new Color(60, 20, 20, 220));
            g.fillRoundRect(ax - 10, ay - 20, 200, 60, 10, 10);
            g.setColor(new Color(255, 120, 120));
            g.drawRoundRect(ax - 10, ay - 20, 200, 60, 10, 10);
            g.setColor(new Color(255, 180, 180));
            g.setFont(new Font("Arial", Font.BOLD, 13));
            g.drawString("Dragon preparing...", ax, ay + 8);
            g.setColor(new Color(200, 140, 140));
            g.setFont(new Font("Arial", Font.PLAIN, 11));
            g.drawString("Brace yourself!", ax, ay + 28);
        }

        
        if (isOver()) {
            String resultMsg; Color resultCol; String subMsg;
            switch (state) {
                case WIN  -> { resultMsg = "VICTORY!";  resultCol = Color.YELLOW; subMsg = "+15 Trust  +10 Morality"; }
                case FLEE -> { resultMsg = "Fled...";   resultCol = Color.CYAN;   subMsg = "-5 Trust  -5 Morality";  }
                default   -> { resultMsg = "DEFEATED";  resultCol = Color.RED;    subMsg = "Returning to Main Menu..."; }
            }
            g.setColor(new Color(0, 0, 0, 160));
            g.fillRoundRect(screenW / 2 - 160, screenH / 2 + 50, 320, 110, 16, 16);
            g.setColor(resultCol);
            g.setFont(new Font("Serif", Font.BOLD, 38));
            fm = g.getFontMetrics();
            g.drawString(resultMsg, (screenW - fm.stringWidth(resultMsg)) / 2, screenH / 2 + 90);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 13));
            fm = g.getFontMetrics();
            g.drawString(subMsg, (screenW - fm.stringWidth(subMsg)) / 2, screenH / 2 + 112);
            if (resultFrames >= 30) {
                g.setColor(new Color(200, 200, 200));
                g.setFont(new Font("Arial", Font.ITALIC, 11));
                String hint = (state == BattleState.LOSE)
                        ? "Press ENTER to return to Main Menu"
                        : "Press ENTER to return to map";
                fm = g.getFontMetrics();
                g.drawString(hint, (screenW - fm.stringWidth(hint)) / 2, screenH / 2 + 138);
            }
        }
    }
}

package com.aniana.core;

import com.aniana.battle.BattleSystem;
import com.aniana.entities.*;
import com.aniana.maps.MapLoader;
import com.aniana.maps.MapObject;
import com.aniana.maps.TileRenderer;
import com.aniana.screens.SplashScreen;
import com.aniana.screens.TitleMenuScreen;
import com.aniana.screens.GameEndScreen;
import com.aniana.screens.TransitionEffect;
import com.aniana.ui.DialogueBox;
import com.aniana.ui.DialogueDatabase;
import com.aniana.ui.HUD;
import com.aniana.audio.GlobalMusic;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;


public class GamePanel extends JPanel implements Runnable {

    private static final int W   = GameWindow.SCREEN_W;
    private static final int H   = GameWindow.SCREEN_H;
    private static final int FPS = 60;

   
    private Thread         gameThread;
    private final InputHandler input    = new InputHandler();
    private GameState      state        = GameState.SPLASH;
    private final GameData gameData     = new GameData();

    
    private final SplashScreen     splash     = new SplashScreen();
    private final TitleMenuScreen  titleMenu  = new TitleMenuScreen();
    private final GameEndScreen    gameEnd    = new GameEndScreen();
    private final TransitionEffect transition = new TransitionEffect();

    
    private MapLoader    currentMap;
    private TileRenderer tileRenderer;
    private Camera       camera;

   
    private Player      player1;
    private Player      player2;   // null in 1P mode
    private AICompanion companion; // null in 2P mode
    private final List<NPC> npcs = new ArrayList<>();
    private Enemy       enemy;     // forest map only

    
    private final DialogueBox dialogueBox = new DialogueBox();
    private NPC               activeNPC   = null;
    private final HUD         hud         = new HUD();

   
    private final BattleSystem battle = new BattleSystem();

   
    private final BufferedImage offscreen;
    private final Graphics2D    og;

    public GamePanel() {
        setPreferredSize(new Dimension(W, H));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(input);
        
        GlobalMusic.playLoop("/audio/anianamusic.wav"); 
        
        offscreen    = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
        og           = offscreen.createGraphics();
        og.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        tileRenderer = new TileRenderer();
    }

    public void startGameLoop() {
        requestFocusInWindow();
        gameThread = new Thread(this, "GameLoop");
        gameThread.start();
    }

    @Override
    public void run() {
        long nsPerFrame = 1_000_000_000L / FPS;
        long lastTime   = System.nanoTime();
        while (gameThread != null) {
            long now   = System.nanoTime();
            long delta = now - lastTime;
            if (delta >= nsPerFrame) {
                lastTime = now;
                update();
                repaint();
            }
        }
    }

  
    private void update() {
        transition.update();
        switch (state) {
            case SPLASH     -> state = splash.update(input);
            case TITLE_MENU -> {
                GameState next = titleMenu.update(input, gameData);
                if (next == GameState.PLAYING) { initGame(); state = GameState.PLAYING; }
            }
            case PLAYING    -> updatePlaying();
            case DIALOGUE   -> updateDialogue();
            case BATTLE     -> updateBattle();
            case GAME_OVER  -> {
                if (input.enter || input.space) {
                    input.consumeEnter();
                    input.consumeSpace();
                    resetGameData();
                    state = GameState.TITLE_MENU;
                }
            }
            case GAME_END   -> {
                GameState next = gameEnd.update(input, gameData);
                if (next == GameState.TITLE_MENU) { resetGameData(); state = GameState.TITLE_MENU; }
            }
        }
    }

   
    private void initGame() {
        gameData.currentMapId          = "VILLAGE";
        gameData.trustPoints           = 0;
        gameData.moralityPoints        = 0;
        gameData.villagerQuestDone     = false;
        gameData.samuraiQuestDone      = false;
        gameData.witchQuestDone        = false;
        gameData.dragonDefeated        = false;
        gameData.elderConversationDone = false;
        loadMap("VILLAGE");
    }

    private void resetGameData() {
        gameData.trustPoints           = 0;
        gameData.moralityPoints        = 0;
        gameData.villagerQuestDone     = false;
        gameData.samuraiQuestDone      = false;
        gameData.witchQuestDone        = false;
        gameData.dragonDefeated        = false;
        gameData.elderConversationDone = false;
    }

    private void loadMap(String mapId) {
        gameData.currentMapId = mapId;
        String resource = switch (mapId) {
            case "VILLAGE" -> "/maps/village.tmj";
            case "FOREST"  -> "/maps/forest.tmj";
            case "SHRINE"  -> "/maps/shrine.tmj";
            default -> throw new IllegalArgumentException("Unknown map: " + mapId);
        };

        currentMap = new MapLoader();
        currentMap.load(resource);

        camera = new Camera(W, H,
                currentMap.width  * GameWindow.TILE_SIZE,
                currentMap.height * GameWindow.TILE_SIZE);

       
        MapObject spawn = currentMap.findObject("player_spawn");
        if (spawn == null) spawn = currentMap.findObject("playerSpawn");
        int spawnX = (spawn != null) ? spawn.x : 64;
        int spawnY = (spawn != null) ? spawn.y : 64;

        createPlayers(spawnX, spawnY);

       
        npcs.clear();
        enemy = null;
        for (MapObject obj : currentMap.objects) {
            String npcType = obj.getProp("npcType");
            String objName = obj.name.toLowerCase();

            
            boolean isDragon = npcType.equalsIgnoreCase("BLUE_DRAGON")
                    || objName.contains("dragon")
                    || obj.getProp("enemyType").equalsIgnoreCase("BLUE_DRAGON");

            if (isDragon) {
                if (!gameData.dragonDefeated) {
                    try { enemy = new Enemy(obj); }
                    catch (Exception ex) {
                        System.err.println("[GamePanel] Failed to create enemy: " + ex.getMessage());
                    }
                }
                continue; 
            }

           
            if ("npc".equals(obj.type) && !npcType.isEmpty()) {
                try {
                    NPC npc = new NPC(obj);
                    npc.interacted = isNpcDone(npc.npcId);
                    npcs.add(npc);
                } catch (Exception ex) {
                    System.err.println("[GamePanel] Failed to create NPC: " + obj.name + " – " + ex.getMessage());
                }
            }
        }
    }

    private void createPlayers(int spawnX, int spawnY) {
        GameData.CharacterChoice p1Char = gameData.p1Choice;
        GameData.CharacterChoice aiChar = (p1Char == GameData.CharacterChoice.KIA)
                ? GameData.CharacterChoice.ANNE : GameData.CharacterChoice.KIA;

        player1          = new Player(p1Char, true, input);
        player1.worldX   = spawnX;
        player1.worldY   = spawnY;

        if (gameData.playerMode == GameData.PlayerMode.TWO_PLAYER) {
           
            player2          = new Player(aiChar, false, input);
            player2.worldX   = spawnX + 36;
            player2.worldY   = spawnY;
            companion        = null;
        } else {
          
            player2          = null;
            companion        = new AICompanion(aiChar, player1, input);
            companion.worldX = spawnX + 36;
            companion.worldY = spawnY;
        }
    }

    private boolean isNpcDone(String npcId) {
        return switch (npcId) {
            case "npc_villager_1" -> gameData.villagerQuestDone;
            case "npc_samurai_1"  -> gameData.samuraiQuestDone;
            case "npc_witch_1"    -> gameData.witchQuestDone;
            case "npc_elf_1"      -> gameData.elderConversationDone;
            default -> false;
        };
    }

    
    private void updatePlaying() {
        if (!transition.isIdle()) return;

        player1.update(currentMap);
        if (companion != null) companion.update(currentMap);
        if (player2   != null) player2.update(currentMap);
        npcs.forEach(n -> n.update(currentMap));
        if (enemy != null) enemy.update(currentMap);

      
        camera.update(player1);

        checkNpcInteraction();
        checkEnemyCollision();
        checkExitPoints();
    }

    private void checkNpcInteraction() {
       
        boolean interact = input.p1Interact || input.p2Interact;
        if (!interact) return;

        
        Player interactor = player1;
        if (player2 != null && input.p2Interact) interactor = player2;

        for (NPC npc : npcs) {
            Rectangle pr    = interactor.getWorldHitbox();
            Rectangle nr    = npc.getWorldHitbox();
            Rectangle range = new Rectangle(nr.x - 24, nr.y - 24, nr.width + 48, nr.height + 48);
            if (pr.intersects(range)) {
                input.consumeEnter();
                startDialogue(npc);
                return;
            }
        }
    }

    private void checkEnemyCollision() {
        if (enemy == null || enemy.defeated) return;
        Rectangle ep = enemy.getWorldHitbox();
        if (player1.getWorldHitbox().intersects(ep)) { startBattle(); return; }
        if (player2 != null && player2.getWorldHitbox().intersects(ep)) { startBattle(); }
    }

    private void startDialogue(NPC npc) {
        activeNPC = npc;
        String companionName = (companion != null) ? companion.getName()
                : (player2 != null)  ? player2.getName()
                : player1.getName();
        DialogueBox.DialogueSequence seq = DialogueDatabase.get(npc.dialogueId, player1.getName(), companionName);
        dialogueBox.startDialogue(seq);
        state = GameState.DIALOGUE;
        if (companion != null) companion.showHint("Listen carefully!");
    }

    private void checkExitPoints() {
        for (MapObject obj : currentMap.findObjectsByType("exitPoint")) {
            Rectangle exit = new Rectangle(obj.x, obj.y, obj.width, obj.height);
            boolean p1On   = player1.getWorldHitbox().intersects(exit);
            boolean p2On   = player2 != null && player2.getWorldHitbox().intersects(exit);
            if (p1On || p2On) {
                String targetMap = obj.getProp("targetMap");
                if (!targetMap.isEmpty()) { doMapTransition(targetMap); return; }
            }
        }
    }

    private void doMapTransition(String targetMapId) {
        transition.startFade(() -> {
            loadMap(targetMapId);
            state = GameState.PLAYING;
        });
    }

   
    private void updateDialogue() {
        
        if (dialogueBox.isShowingChoice()) {
            if (input.dialogueUp)   { dialogueBox.choiceUp();   input.consumeDialogueNav(); }
            if (input.dialogueDown) { dialogueBox.choiceDown(); input.consumeDialogueNav(); }
        }

      
        if (input.enter || input.space) {
            input.consumeEnter();
            input.consumeSpace();
            boolean done = dialogueBox.advance(gameData);
            if (done) {
                markNpcDone();
                boolean triggerEnd = (activeNPC != null
                        && "npc_elf_1".equals(activeNPC.npcId)
                        && gameData.elderConversationDone);
                activeNPC = null;
                state     = GameState.PLAYING;
                if (triggerEnd) {
                    gameEnd.reset();
                    state = GameState.GAME_END;
                }
            }
        }
    }

    private void markNpcDone() {
        if (activeNPC == null) return;
        activeNPC.interacted = true;
        switch (activeNPC.npcId) {
            case "npc_villager_1" -> gameData.villagerQuestDone     = true;
            case "npc_samurai_1"  -> gameData.samuraiQuestDone      = true;
            case "npc_witch_1"    -> gameData.witchQuestDone        = true;
            case "npc_elf_1"      -> gameData.elderConversationDone = true;
        }
    }

   
    private void startBattle() {
        Player comp2 = (companion != null) ? companion : player2;
        battle.start(player1, comp2, enemy, gameData);
        state = GameState.BATTLE;
    }

    private void updateBattle() {
       
        battle.update(input);

      
        if (!battle.isExitReady()) return;

        if (battle.playerWon()) {
            
            enemy.defeated = true;
            enemy          = null;
            state = GameState.PLAYING;

        } else if (battle.playerLost()) {
           
            state = GameState.GAME_OVER;

        } else {
            
            MapObject spawn = currentMap.findObject("player_spawn");
            if (spawn == null) spawn = currentMap.findObject("playerSpawn");
            if (spawn != null) {
                player1.worldX = spawn.x;
                player1.worldY = spawn.y;
                if (player2 != null) {
                    player2.worldX = spawn.x + 36;
                    player2.worldY = spawn.y;
                }
                if (companion != null) {
                    companion.worldX = spawn.x + 36;
                    companion.worldY = spawn.y;
                }
            }
            state = GameState.PLAYING;
        }
    }

    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        og.setColor(Color.BLACK);
        og.fillRect(0, 0, W, H);

        switch (state) {
            case SPLASH     -> splash.render(og, W, H);
            case TITLE_MENU -> titleMenu.render(og, W, H);
            case PLAYING, DIALOGUE, BATTLE -> renderGame();
            case GAME_OVER  -> renderDefeatScreen();
            case GAME_END   -> gameEnd.render(og, W, H, gameData);
        }

        transition.render(og, W, H);
        g.drawImage(offscreen, 0, 0, null);
    }

    private void renderGame() {
        if (currentMap == null) return;
        tileRenderer.renderLayer(og, currentMap.groundLayer,     currentMap.width, currentMap.height, camera);
        tileRenderer.renderLayer(og, currentMap.collisionLayer,  currentMap.width, currentMap.height, camera);
        tileRenderer.renderLayer(og, currentMap.structureLayer,  currentMap.width, currentMap.height, camera); 
        tileRenderer.renderLayer(og, currentMap.decorationLayer, currentMap.width, currentMap.height, camera);

        if (companion != null) companion.render(og, camera);
        if (player2   != null) player2.render(og, camera);
        npcs.forEach(n -> n.render(og, camera));
        if (enemy != null) enemy.render(og, camera);
        player1.render(og, camera);

       
        hud.render(og, gameData, W);

        drawPlayerLabel(og, player1, camera);
        if (companion != null) drawPlayerLabel(og, companion, camera);
        if (player2   != null) drawPlayerLabel(og, player2, camera);

      
        if (gameData.playerMode == GameData.PlayerMode.TWO_PLAYER) {
            og.setFont(new Font("Arial", Font.PLAIN, 10));
            og.setColor(new Color(200, 200, 200, 160));
            og.drawString("P1: Arrows+Enter | P2: WASD+F", 4, H - 4);
        }

        if (state == GameState.DIALOGUE) dialogueBox.render(og, W, H);
        if (state == GameState.BATTLE)   battle.render(og, W, H);

       
        renderExitHints();
    }

    private void drawPlayerLabel(Graphics2D g, Entity e, Camera cam) {
        String name = switch (e) {
            case AICompanion a -> a.getName() + " [AI]";
            case Player p      -> p.getName() + (p.isP1 ? " [P1]" : " [P2]");
            default            -> "";
        };
        g.setFont(new Font("Arial", Font.BOLD, 9));
        g.setColor(Color.WHITE);
        int sx = cam.toScreenX(e.worldX);
        int sy = cam.toScreenY(e.worldY) - 2;
        g.drawString(name, sx + 1, sy);
    }

    
    private void renderDefeatScreen() {
        
        og.setColor(new Color(30, 0, 0));
        og.fillRect(0, 0, W, H);

       
        og.setColor(new Color(180, 0, 0, 120));
        og.setStroke(new BasicStroke(8f));
        og.drawRect(4, 4, W - 8, H - 8);
        og.setStroke(new BasicStroke(1f));

        
        og.setFont(new Font("Serif", Font.BOLD, 64));
        og.setColor(new Color(220, 40, 40));
        String msg = "DEFEATED";
        FontMetrics fm = og.getFontMetrics();
        og.drawString(msg, (W - fm.stringWidth(msg)) / 2, H / 2 - 40);

        
        og.setFont(new Font("Arial", Font.ITALIC, 18));
        og.setColor(new Color(200, 150, 150));
        String sub = "You fell in battle, but your story is not over.";
        fm = og.getFontMetrics();
        og.drawString(sub, (W - fm.stringWidth(sub)) / 2, H / 2 + 10);

        
        og.setFont(new Font("Arial", Font.PLAIN, 14));
        og.setColor(new Color(200, 200, 200));
        String prompt = "Press ENTER to return to Main Menu";
        fm = og.getFontMetrics();
        og.drawString(prompt, (W - fm.stringWidth(prompt)) / 2, H / 2 + 55);
    }

    private void renderExitHints() {
        for (MapObject obj : currentMap.findObjectsByType("exitPoint")) {
            int sx = camera.toScreenX(obj.x);
            int sy = camera.toScreenY(obj.y);
            og.setColor(new Color(255, 255, 80, 120));
            og.drawRect(sx, sy, obj.width, obj.height);
            og.setFont(new Font("Arial", Font.BOLD, 9));
            og.setColor(new Color(255, 255, 80));
            og.drawString("EXIT →", sx + 2, sy + obj.height / 2);
        }
    }
}

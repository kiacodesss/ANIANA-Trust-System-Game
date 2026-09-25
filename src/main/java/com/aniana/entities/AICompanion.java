package com.aniana.entities;

import com.aniana.core.Camera;
import com.aniana.core.GameData;
import com.aniana.core.InputHandler;
import com.aniana.maps.MapLoader;

import java.awt.*;

public class AICompanion extends Player {

    private final Player leader;

   
    private static final int FOLLOW_DIST   = 48;  
    private static final int STOP_DIST     = 20; 
    private static final int FOLLOW_OFFSET = 28; 
    private int  stuckTimer  = 0;
    private int  stuckX, stuckY;
    private static final int STUCK_THRESHOLD = 30; 
    private static final int STUCK_POS_TOL   = 2; 
    private int wallHugDir   = 1;
    private int wallHugTimer = 0;
    private static final int WALL_HUG_TICKS = 60; 
    private String hint      = "";
    private int    hintTimer = 0;

    public AICompanion(GameData.CharacterChoice character, Player leader, InputHandler dummy) {
        super(character, false, dummy);
        this.leader = leader;
        this.speed  = 2;
    }

    
    @Override
    public void update(MapLoader map) {

        
        int targetX = leader.worldX;
        int targetY = leader.worldY;

        switch (leader.dirRow) {
            case 0 -> targetY += FOLLOW_OFFSET; 
            case 1 -> targetX -= FOLLOW_OFFSET; 
            case 2 -> targetY -= FOLLOW_OFFSET; 
            case 3 -> targetX += FOLLOW_OFFSET; 
        }

        int dx   = targetX - worldX;
        int dy   = targetY - worldY;
        double dist = Math.sqrt((double) dx * dx + (double) dy * dy);

        
        if (dist <= STOP_DIST) {
            animFrame  = 0;
            stuckTimer = 0;
            if (hintTimer > 0) hintTimer--;
            return;
        }

      
        int moveX = 0, moveY = 0;
        if (Math.abs(dx) > 1) moveX = (dx > 0) ? speed : -speed;
        if (Math.abs(dy) > 1) moveY = (dy > 0) ? speed : -speed;

        
        boolean movedEnough = (Math.abs(worldX - stuckX) > STUCK_POS_TOL
                            || Math.abs(worldY - stuckY) > STUCK_POS_TOL);

        if (movedEnough) {
            stuckTimer   = 0;
            stuckX       = worldX;
            stuckY       = worldY;
            wallHugTimer = 0;
        } else {
            stuckTimer++;
        }

       
        if (stuckTimer < STUCK_THRESHOLD) {
            
            move(moveX, moveY, map);

        } else {
            
            wallHugTimer++;
            if (wallHugTimer > WALL_HUG_TICKS) {
                wallHugDir   = -wallHugDir; 
                wallHugTimer = 0;
            }

          
            int perpX, perpY;
            if (Math.abs(dx) >= Math.abs(dy)) {
                perpX = 0;
                perpY = speed * wallHugDir;
            } else {
                perpX = speed * wallHugDir;
                perpY = 0;
            }

            int beforeX = worldX, beforeY = worldY;
            move(moveX, moveY, map);

            
            if (worldX == beforeX && worldY == beforeY) {
                move(perpX, perpY, map);
            }

           
            if (worldX != beforeX || worldY != beforeY) {
                stuckTimer = 0;
                stuckX = worldX;
                stuckY = worldY;
            }
        }

        if (hintTimer > 0) hintTimer--;
    }

   
    public void showHint(String message) {
        hint      = message;
        hintTimer = 180;
    }

    
    @Override
    public void render(Graphics2D g, Camera camera) {
        super.render(g, camera);
        if (hintTimer > 0) {
            int sx = camera.toScreenX(worldX) - 10;
            int sy = camera.toScreenY(worldY) - 28;
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRoundRect(sx - 2, sy - 14, hint.length() * 6 + 8, 18, 6, 6);
            g.setColor(Color.YELLOW);
            g.setFont(new Font("Arial", Font.PLAIN, 10));
            g.drawString(hint, sx + 2, sy);
        }
    }
}

package com.aniana.entities;

import com.aniana.core.Camera;
import com.aniana.maps.MapLoader;
import com.aniana.maps.MapObject;
import com.aniana.utils.SpriteSheet;

import java.awt.*;


public class Enemy extends Entity {

    private static final int FRAME_SIZE   = 112;
    private static final int DISPLAY_SIZE = 80;

    public int  hp;
    public int  maxHp;
    public int  damage;
    public int  aggressiveRange;
    public boolean defeated = false;
    public int expReward;

    public Enemy(MapObject obj) {
        this.hp             = Integer.parseInt(obj.getProp("hp"));
        this.maxHp          = this.hp;
        this.damage         = Integer.parseInt(obj.getProp("damage"));
        this.aggressiveRange = Integer.parseInt(obj.getProp("aggressiveRange"));
        this.expReward      = Integer.parseInt(obj.getProp("expReward"));
        this.worldX         = obj.x;
        this.worldY         = obj.y;

        spriteSheet = new SpriteSheet("/sprites/enemy.png", FRAME_SIZE, FRAME_SIZE);
    }

    @Override
    public void update(MapLoader map) {
        if (defeated) return;
        animate();
    }

    @Override
    public void render(Graphics2D g, Camera camera) {
        if (defeated) return;
        if (spriteSheet != null && spriteSheet.isLoaded()) {
            var frame = spriteSheet.getFrame(animFrame, 0);
            if (frame != null) {
                int tileSize = com.aniana.core.GameWindow.TILE_SIZE;

                int sx = camera.toScreenX(worldX) - (DISPLAY_SIZE - tileSize) / 2;
                int sy = camera.toScreenY(worldY) - (DISPLAY_SIZE - tileSize) / 2;

                g.drawImage(frame, sx, sy, DISPLAY_SIZE, DISPLAY_SIZE, null);
            }
        } else {
            g.setColor(new Color(0x4444FF));
            g.fillRect(camera.toScreenX(worldX), camera.toScreenY(worldY), DISPLAY_SIZE, DISPLAY_SIZE);
        }

        
        int tileSize = com.aniana.core.GameWindow.TILE_SIZE;

        int sx = camera.toScreenX(worldX) - (DISPLAY_SIZE - tileSize) / 2;
        int sy = camera.toScreenY(worldY) - (DISPLAY_SIZE - tileSize) / 2;
        g.setColor(Color.RED);
        g.fillRect(sx, sy - 8, DISPLAY_SIZE, 5);
        g.setColor(new Color(0x00AAFF));
        int w = (int)((hp / (float)maxHp) * DISPLAY_SIZE);
        g.fillRect(sx, sy - 8, w, 5);

        
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 9));
        g.drawString("DRAGON", sx, sy - 10);
    }
}

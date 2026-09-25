package com.aniana.entities;

import com.aniana.maps.MapLoader;
import com.aniana.utils.SpriteSheet;

import java.awt.*;
import java.awt.image.BufferedImage;


public abstract class Entity {

    public int worldX, worldY;
    public int speed = 1;
    public boolean active = true;


    protected SpriteSheet spriteSheet;
    protected int displaySize = 32;
    protected int animFrame   = 0;
    protected int animTimer   = 0;
    protected int animDelay   = 12; 
    protected int dirRow      = 2;  

    
    public Rectangle hitbox = new Rectangle(8, 16, 16, 16);

    public Rectangle getWorldHitbox() {
        return new Rectangle(worldX + hitbox.x, worldY + hitbox.y,
                             hitbox.width, hitbox.height);
    }

    protected void animate() {
        animTimer++;
        if (animTimer >= animDelay) {
            animTimer = 0;
            animFrame = (animFrame + 1) % 4;
        }
    }

    protected void move(int dx, int dy, MapLoader map) {
        if (dx == 0 && dy == 0) { animFrame = 0; return; }

       
        if      (dy < 0) dirRow = 0; 
        else if (dx > 0) dirRow = 1; 
        else if (dy > 0) dirRow = 2; 
        else if (dx < 0) dirRow = 3; 

        animate();

        
        int newX = worldX + dx;
        Rectangle testX = new Rectangle(newX + hitbox.x, worldY + hitbox.y,
                                        hitbox.width, hitbox.height);
        if (!collidesWithMap(testX, map)) worldX = newX;

        
        int newY = worldY + dy;
        Rectangle testY = new Rectangle(worldX + hitbox.x, newY + hitbox.y,
                                        hitbox.width, hitbox.height);
        if (!collidesWithMap(testY, map)) worldY = newY;
    }

    private boolean collidesWithMap(Rectangle box, MapLoader map) {
        if (map == null) return false;
        int ts = com.aniana.core.GameWindow.TILE_SIZE;
        int left   = box.x / ts;
        int right  = (box.x + box.width - 1) / ts;
        int top    = box.y / ts;
        int bottom = (box.y + box.height - 1) / ts;
        return map.isSolid(left, top)   || map.isSolid(right, top)
            || map.isSolid(left, bottom)|| map.isSolid(right, bottom);
    }

    public abstract void update(MapLoader map);
    public abstract void render(Graphics2D g, com.aniana.core.Camera camera);

    protected void drawSprite(Graphics2D g, com.aniana.core.Camera camera, int col, int row) {
        if (spriteSheet == null || !spriteSheet.isLoaded()) {
            
            g.setColor(Color.MAGENTA);
            g.fillRect(camera.toScreenX(worldX), camera.toScreenY(worldY), 32, 32);
            return;
        }
        BufferedImage frame = spriteSheet.getFrame(col, row);
        if (frame != null) {
            int tileSize = com.aniana.core.GameWindow.TILE_SIZE;

            int sx = camera.toScreenX(worldX) - (displaySize - tileSize) / 2;
            int sy = camera.toScreenY(worldY) - (displaySize - tileSize) / 2;
            g.drawImage(frame, sx, sy, displaySize, displaySize, null);
        }
    }
}

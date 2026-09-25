package com.aniana.entities;

import com.aniana.core.Camera;
import com.aniana.core.GameData;
import com.aniana.core.InputHandler;
import com.aniana.maps.MapLoader;
import com.aniana.utils.SpriteSheet;

import java.awt.*;


public class Player extends Entity {

    private static final int FRAME_SIZE = 96; 
    public static final int DISPLAY_SIZE = 96;

    public boolean isP1;
    private final InputHandler input;
    private final GameData.CharacterChoice character;

    
    public int hp    = 100;
    public int maxHp = 100;
    public int attack = 20;

    public Player(GameData.CharacterChoice character, boolean isP1, InputHandler input) {
        this.character = character;
        this.isP1      = isP1;
        this.input     = input;
        this.speed     = 1;
        this.displaySize = DISPLAY_SIZE;

        String spriteName = (character == GameData.CharacterChoice.KIA) ? "kia" : "anne";
        spriteSheet = new SpriteSheet("/sprites/" + spriteName + ".png", FRAME_SIZE, FRAME_SIZE);
    }

    @Override
    public void update(MapLoader map) {
        int dx = 0, dy = 0;

        if (isP1) {
            if (input.p1Up)    dy = -speed;
            if (input.p1Down)  dy =  speed;
            if (input.p1Left)  dx = -speed;
            if (input.p1Right) dx =  speed;
        } else {
            if (input.p2Up)    dy = -speed;
            if (input.p2Down)  dy =  speed;
            if (input.p2Left)  dx = -speed;
            if (input.p2Right) dx =  speed;
        }

       
        if (dx != 0 && dy != 0) { dx /= 2; dy /= 2; }

        move(dx, dy, map);
    }

    @Override
    public void render(Graphics2D g, Camera camera) {
        drawSprite(g, camera, animFrame, dirRow);
      

        int barWidth = 40; 

        int tileSize = com.aniana.core.GameWindow.TILE_SIZE;
        int sx = camera.toScreenX(worldX) - (DISPLAY_SIZE - tileSize) / 2 + (DISPLAY_SIZE - barWidth) / 2;
        int sy = camera.toScreenY(worldY) - (DISPLAY_SIZE - tileSize) / 2;

        g.setColor(Color.RED);
        g.fillRect(sx, sy - 6, barWidth, 4);

        g.setColor(Color.GREEN);
        int hpWidth = (int) ((hp / (float) maxHp) * barWidth);
        g.fillRect(sx, sy - 6, hpWidth, 4);
    }

    public String getName() {
        return character == GameData.CharacterChoice.KIA ? "Kia" : "Anne";
    }
}

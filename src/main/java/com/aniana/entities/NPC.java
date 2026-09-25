package com.aniana.entities;

import com.aniana.core.Camera;
import com.aniana.maps.MapLoader;
import com.aniana.maps.MapObject;
import com.aniana.utils.SpriteSheet;

import java.awt.*;


public class NPC extends Entity {

    private static final int FRAME_SIZE   = 112;
    private static final int displaySize = 80;

    public enum NpcType {
        WOUNDED_VILLAGER, SAMURAI_WARRIOR, WITCH, ELDER_ELF, SLUM_NPC
    }

    public final NpcType    npcType;
    public final String     dialogueId;
    public final String     npcId;
    public       boolean    interacted = false;

    private int idleTimer = 0;

    public NPC(MapObject obj) {
        this.npcId      = obj.getProp("npcId");
        this.dialogueId = obj.getProp("dialogueId");
        String typeStr  = obj.getProp("npcType");
        this.npcType    = parseType(typeStr);

        this.worldX = obj.x;
        this.worldY = obj.y;

       
        String spriteRes = resolveSprite(npcType);
        spriteSheet = new SpriteSheet(spriteRes, FRAME_SIZE, FRAME_SIZE);
        dirRow = 0; 
    }

    private NpcType parseType(String s) {
        return switch (s) {
            case "WOUNDED_VILLAGER" -> NpcType.WOUNDED_VILLAGER;
            case "SAMURAI_WARRIOR"  -> NpcType.SAMURAI_WARRIOR;
            case "WITCH"            -> NpcType.WITCH;
            case "ELDER_ELF"        -> NpcType.ELDER_ELF;
            default                 -> NpcType.SLUM_NPC;
        };
    }

    private String resolveSprite(NpcType type) {
        return switch (type) {
            case WOUNDED_VILLAGER -> "/sprites/npc1.png";
            case SAMURAI_WARRIOR  -> "/sprites/npc2.png";
            case WITCH            -> "/sprites/slumnpc.png";
            case ELDER_ELF        -> "/sprites/npc3.png";
            case SLUM_NPC         -> "/sprites/slumnpc.png";
        };
    }

    @Override
    public void update(MapLoader map) {
        
        idleTimer++;
        if (idleTimer >= animDelay) {
            idleTimer = 0;
            animFrame = (animFrame + 1) % 4;
        }
    }

    @Override
    public void render(Graphics2D g, Camera camera) {
        if (spriteSheet != null && spriteSheet.isLoaded()) {
            var frame = spriteSheet.getFrame(animFrame, 0);
            if (frame != null) {
                int tileSize = com.aniana.core.GameWindow.TILE_SIZE;

                int sx = camera.toScreenX(worldX) - (displaySize - tileSize) / 2;
                int sy = camera.toScreenY(worldY) - (displaySize - tileSize) / 2;

                g.drawImage(frame, sx, sy, displaySize, displaySize, null);
            }
        } else {
            
            g.setColor(Color.CYAN);
            g.fillRect(camera.toScreenX(worldX), camera.toScreenY(worldY), displaySize, displaySize);
        }

       
        if (!interacted) {
            int sx = camera.toScreenX(worldX) + 10;
            int sy = camera.toScreenY(worldY) - 8;
            g.setColor(Color.YELLOW);
            g.setFont(new Font("Arial", Font.BOLD, 14));
            g.drawString("!", sx, sy);
        }
    }

    public String getDisplayName() {
        return switch (npcType) {
            case WOUNDED_VILLAGER -> "Wounded Villager";
            case SAMURAI_WARRIOR  -> "Samurai Warrior";
            case WITCH            -> "Forest Witch";
            case ELDER_ELF        -> "Elder Elf";
            case SLUM_NPC         -> "Slum Dweller";
        };
    }
}

package com.aniana.maps;

import com.aniana.core.Camera;
import com.aniana.core.GameWindow;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;


public class TileRenderer {

    private static final int TILE_SIZE    = GameWindow.TILE_SIZE;
    private static final int TILESET_COLS = 16;  
    private static final int TILESET_ROWS = 12;   
    private static final int TILE_MAX     = TILESET_COLS * TILESET_ROWS; 

    private BufferedImage tileset;

    public TileRenderer() {
        loadTileset();
    }

    private void loadTileset() {
        try {
            InputStream is = getClass().getResourceAsStream("/sprites/tileset.png");
            if (is == null) {
                System.err.println("WARNING: /sprites/tileset.png not found in resources.");
                return;
            }
            tileset = ImageIO.read(is);
            System.out.println("[TileRenderer] Tileset loaded: "
                    + tileset.getWidth() + "x" + tileset.getHeight()
                    + " -> " + (tileset.getWidth()/TILE_SIZE) + " cols x "
                    + (tileset.getHeight()/TILE_SIZE) + " rows");
        } catch (IOException e) {
            System.err.println("Failed to load tileset: " + e.getMessage());
        }
    }

    
    public void renderLayer(Graphics2D g, int[] data, int mapW, int mapH, Camera camera) {
        if (data == null || tileset == null) return;

      
        int startCol = Math.max(0, camera.x / TILE_SIZE);
        int startRow = Math.max(0, camera.y / TILE_SIZE);
        int endCol   = Math.min(mapW, (camera.x + GameWindow.SCREEN_W) / TILE_SIZE + 2);
        int endRow   = Math.min(mapH, (camera.y + GameWindow.SCREEN_H) / TILE_SIZE + 2);

        for (int row = startRow; row < endRow; row++) {
            for (int col = startCol; col < endCol; col++) {
                int idx  = row * mapW + col;
                if (idx < 0 || idx >= data.length) continue;

                int tile = data[idx];
                if (tile < 0 || tile >= TILE_MAX) continue; 

                
                int tileCol = tile % TILESET_COLS;
                int tileRow = tile / TILESET_COLS;
                int srcX = tileCol * TILE_SIZE;
                int srcY = tileRow * TILE_SIZE;

                
                int dstX = camera.toScreenX(col * TILE_SIZE);
                int dstY = camera.toScreenY(row * TILE_SIZE);

                g.drawImage(tileset,
                        dstX, dstY, dstX + TILE_SIZE, dstY + TILE_SIZE,
                        srcX, srcY, srcX + TILE_SIZE, srcY + TILE_SIZE,
                        null);
            }
        }
    }

   
    public void renderLayerFallback(Graphics2D g, int[] data, int mapW, int mapH,
                                    Camera camera, Color color) {
        if (data == null) return;
        g.setColor(color);
        for (int row = 0; row < mapH; row++) {
            for (int col = 0; col < mapW; col++) {
                int tile = data[row * mapW + col];
                if (tile < 0) continue;
                int dstX = camera.toScreenX(col * TILE_SIZE);
                int dstY = camera.toScreenY(row * TILE_SIZE);
                g.fillRect(dstX + 1, dstY + 1, TILE_SIZE - 2, TILE_SIZE - 2);
            }
        }
    }

    public boolean isLoaded() { return tileset != null; }
}

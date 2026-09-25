package com.aniana.core;

import com.aniana.entities.Player;


public class Camera {

    public int x, y; 

    private final int viewW;
    private final int viewH;
    private final int mapPixelW;
    private final int mapPixelH;

    public Camera(int viewW, int viewH, int mapPixelW, int mapPixelH) {
        this.viewW      = viewW;
        this.viewH      = viewH;
        this.mapPixelW  = mapPixelW;
        this.mapPixelH  = mapPixelH;
    }

    public void update(Player player) {
       
        x = player.worldX - viewW / 2 + GameWindow.TILE_SIZE / 2;
        y = player.worldY - viewH / 2 + GameWindow.TILE_SIZE / 2;

       
        x = Math.max(0, Math.min(x, mapPixelW - viewW));
        y = Math.max(0, Math.min(y, mapPixelH - viewH));
    }

    
    public int toScreenX(int worldX) { return worldX - x; }
    public int toScreenY(int worldY) { return worldY - y; }
}

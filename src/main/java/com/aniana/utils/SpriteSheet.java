package com.aniana.utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;


public class SpriteSheet {

    private final BufferedImage sheet;
    public final int frameW;
    public final int frameH;
    public final int cols;
    public final int rows;

    public SpriteSheet(String resourcePath, int frameW, int frameH) {
        this.frameW = frameW;
        this.frameH = frameH;
        BufferedImage img = null;
        try {
            InputStream is = getClass().getResourceAsStream(resourcePath);
            if (is != null) {
                img = ImageIO.read(is);
            } else {
                System.err.println("SpriteSheet not found: " + resourcePath);
            }
        } catch (IOException e) {
            System.err.println("Failed to load sprite: " + resourcePath + " – " + e.getMessage());
        }
        this.sheet = img;
        this.cols  = (img != null) ? img.getWidth()  / frameW : 1;
        this.rows  = (img != null) ? img.getHeight() / frameH : 1;
    }

  
    public BufferedImage getFrame(int col, int row) {
        if (sheet == null) return null;
        col = Math.min(col, cols - 1);
        row = Math.min(row, rows - 1);
        return sheet.getSubimage(col * frameW, row * frameH, frameW, frameH);
    }

    public boolean isLoaded() { return sheet != null; }
}

package com.aniana.core;

import javax.swing.*;
import java.awt.*;
import com.aniana.audio.GlobalMusic; 


public class GameWindow extends JFrame {

    public static final int TILE_SIZE   = 32;
    public static final int MAP_COLS    = 20;
    public static final int MAP_ROWS    = 16;
    public static final int SCREEN_W    = TILE_SIZE * MAP_COLS; 
    public static final int SCREEN_H    = TILE_SIZE * MAP_ROWS; 

    private final GamePanel gamePanel;

    public GameWindow() {
        setTitle("ANIANA: The Journey of Anne and Kia");

        
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        setResizable(false);

        gamePanel = new GamePanel();
        add(gamePanel);

        pack();
        setLocationRelativeTo(null);

      
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {

               
                GlobalMusic.stop();

                
                dispose();

                
                System.exit(0);
            }
        });
    }

    public void startGame() {
        gamePanel.startGameLoop();
    }
}
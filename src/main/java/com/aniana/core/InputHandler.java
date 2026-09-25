package com.aniana.core;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;


public class InputHandler extends KeyAdapter {

   
    public volatile boolean p1Up, p1Down, p1Left, p1Right, p1Interact;
    public volatile boolean p2Up, p2Down, p2Left, p2Right, p2Interact;
    public volatile boolean enter, escape, space;
    public volatile boolean dialogueUp, dialogueDown;
    public volatile boolean battleUp, battleDown, battleConfirm, battleConfirmExit;
    private boolean rawUp, rawDown, rawEnter;

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
           
            case KeyEvent.VK_UP -> {
                p1Up = true;
                dialogueUp = true;
                if (!rawUp) { rawUp = true; battleUp = true; }
            }
            case KeyEvent.VK_DOWN -> {
                p1Down = true;
                dialogueDown = true;
                if (!rawDown) { rawDown = true; battleDown = true; }
            }
            case KeyEvent.VK_LEFT  -> p1Left  = true;
            case KeyEvent.VK_RIGHT -> p1Right = true;
            case KeyEvent.VK_ENTER -> {
                p1Interact = true;
                enter = true;
                if (!rawEnter) {
                    rawEnter = true;
                    battleConfirm = true;
                    battleConfirmExit = true;
                }
            }

          
            case KeyEvent.VK_W -> {
                p2Up = true;
                dialogueUp = true;
                if (!rawUp) { rawUp = true; battleUp = true; }
            }
            case KeyEvent.VK_S -> {
                p2Down = true;
                dialogueDown = true;
                if (!rawDown) { rawDown = true; battleDown = true; }
            }
            case KeyEvent.VK_A -> p2Left  = true;
            case KeyEvent.VK_D -> p2Right = true;
            case KeyEvent.VK_F -> {
                p2Interact = true;
                enter = true;
                if (!rawEnter) {
                    rawEnter = true;
                    battleConfirm = true;
                    battleConfirmExit = true;
                }
            }

            
            case KeyEvent.VK_ESCAPE -> escape = true;
            case KeyEvent.VK_SPACE  -> space  = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP    -> { p1Up    = false; dialogueUp   = false; rawUp    = false; }
            case KeyEvent.VK_DOWN  -> { p1Down  = false; dialogueDown = false; rawDown  = false; }
            case KeyEvent.VK_LEFT  -> p1Left   = false;
            case KeyEvent.VK_RIGHT -> p1Right  = false;
            case KeyEvent.VK_ENTER -> { p1Interact = false; enter = false; rawEnter = false; }

            case KeyEvent.VK_W -> { p2Up    = false; dialogueUp   = false; rawUp    = false; }
            case KeyEvent.VK_S -> { p2Down  = false; dialogueDown = false; rawDown  = false; }
            case KeyEvent.VK_A -> p2Left   = false;
            case KeyEvent.VK_D -> p2Right  = false;
            case KeyEvent.VK_F -> { p2Interact = false; enter = false; rawEnter = false; }

            case KeyEvent.VK_ESCAPE -> escape = false;
            case KeyEvent.VK_SPACE  -> space  = false;
        }
    }

   
    public void consumeEnter()        { enter = false; p1Interact = false; p2Interact = false; }
    public void consumeSpace()        { space = false; }
    public void consumeEscape()       { escape = false; }
    public void consumeDialogueNav()  { dialogueUp = false; dialogueDown = false; }
    public void consumeBattleNav()    { battleUp = false; battleDown = false; }
    public void consumeBattleConfirm(){ battleConfirm = false; }
    public void consumeBattleConfirmExit() { battleConfirmExit = false; }
}

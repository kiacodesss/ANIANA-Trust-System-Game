package com.aniana.core;


public class GameData {

    public enum PlayerMode { ONE_PLAYER, TWO_PLAYER }
    public enum CharacterChoice { KIA, ANNE }

    public PlayerMode    playerMode       = PlayerMode.ONE_PLAYER;
    public CharacterChoice p1Choice      = CharacterChoice.KIA;

    
    public int trustPoints               = 0;
    public int moralityPoints            = 0;

    
    public boolean villagerQuestDone     = false;
    public boolean samuraiQuestDone      = false;
    public boolean witchQuestDone        = false;
    public boolean dragonDefeated        = false;
    public boolean elderConversationDone = false;

   
    public String currentMapId           = "VILLAGE";

    public void addTrust(int amount)    { trustPoints    += amount; }
    public void addMorality(int amount) { moralityPoints += amount; }

   
    public String getEndingRating() {
        int total = trustPoints + moralityPoints;
        if (total >= 120) return "LEGEND – Your bond was unbreakable.";
        if (total >= 80)  return "HERO – You earned their trust.";
        if (total >= 50)  return "WANDERER – You helped when it mattered.";
        return "LOST – Your path was uncertain.";
    }
}

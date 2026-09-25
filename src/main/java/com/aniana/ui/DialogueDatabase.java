package com.aniana.ui;

import java.util.List;

public class DialogueDatabase {

    public static DialogueBox.DialogueSequence get(String dialogueId,
                                                    String playerName,
                                                    String companionName) {
        return switch (dialogueId) {

          
            case "wounded_villager_1" -> new DialogueBox.DialogueSequence(
               
                List.of(
                    new DialogueBox.DialogueLine("Wounded Villager",
                        "Please... help me. Bandits attacked our village last night.", 0, 0),
                    new DialogueBox.DialogueLine("Wounded Villager",
                        "I managed to escape, but my friends are still trapped in the east house.", 0, 0)
                ),
               
                new DialogueBox.ChoiceNode(
                    "What do you say to the wounded villager?",
                    List.of(
                        new DialogueBox.DialogueLine(playerName,
                            "We'll help you right away. Stay here and rest!", +8, +8),
                        new DialogueBox.DialogueLine(playerName,
                            "We can try, but it sounds dangerous. We'll do our best.", +4, +4),
                        new DialogueBox.DialogueLine(playerName,
                            "We're busy right now. Maybe later.", -4, -6),
                        new DialogueBox.DialogueLine(playerName,
                            "Not our problem. Find someone else.", -8, -10)
                    ),
                  
                    List.of(
                        List.of(new DialogueBox.DialogueLine("Wounded Villager",
                            "Bless you! The forest path leads east, but beware what lurks there.", +3, +2)),
                        List.of(new DialogueBox.DialogueLine("Wounded Villager",
                            "I understand... just please, if you can, help them.", +1, +1)),
                        List.of(new DialogueBox.DialogueLine("Wounded Villager",
                            "Please... there is no one else. They're counting on you.", 0, 0)),
                        List.of(new DialogueBox.DialogueLine("Wounded Villager",
                            "...I see. I pray someone kinder finds them.", -2, -2))
                    )
                ),
         
                List.of(
                    new DialogueBox.DialogueLine(companionName,
                        "The villager's wounds look serious. Let's move quickly.", 0, +2)
                )
            );

           
            case "forest_samurai_1" -> new DialogueBox.DialogueSequence(
                List.of(
                    new DialogueBox.DialogueLine("Samurai Warrior",
                        "Halt, traveler. This forest is no place for the weak-hearted.", 0, 0),
                    new DialogueBox.DialogueLine("Samurai Warrior",
                        "A Blue Dragon guards the ruins ahead. Its rage has grown since the village fell.", 0, 0),
                    new DialogueBox.DialogueLine("Samurai Warrior",
                        "Prove your courage by defeating it, and I will guide you to the Shrine of Aniana.", 0, 0)
                ),
                new DialogueBox.ChoiceNode(
                    "How do you respond to the Samurai's challenge?",
                    List.of(
                        new DialogueBox.DialogueLine(playerName,
                            "We accept! We'll defeat the dragon and protect these people!", +8, +8),
                        new DialogueBox.DialogueLine(playerName,
                            "We seek passage through, we mean no harm. We'll try our best.", +4, +2),
                        new DialogueBox.DialogueLine(playerName,
                            "Is there another way past? We don't want to fight unnecessarily.", +2, +4),
                        new DialogueBox.DialogueLine(playerName,
                            "Stand aside. We'll deal with this however we choose.", -6, -4)
                    ),
                    List.of(
                        List.of(new DialogueBox.DialogueLine("Samurai Warrior",
                            "That is the spirit of a true warrior! I'll wait here for your victory.", +5, +3)),
                        List.of(new DialogueBox.DialogueLine("Samurai Warrior",
                            "Words alone are not enough. Show me your deeds.", +2, +1)),
                        List.of(new DialogueBox.DialogueLine("Samurai Warrior",
                            "Wisdom too is a form of courage. Very well, but the dragon must be dealt with.", +3, +4)),
                        List.of(new DialogueBox.DialogueLine("Samurai Warrior",
                            "Arrogance will get you killed in there. Tread carefully.", -2, -2))
                    )
                ),
                List.of(
                    new DialogueBox.DialogueLine(companionName,
                        "A dragon... This is our chance to protect the people here!", +3, +5)
                )
            );

           
            case "forest_witch_1" -> new DialogueBox.DialogueSequence(
                List.of(
                    new DialogueBox.DialogueLine("Forest Witch",
                        "Heheheh... young ones wandering into my swamp. How curious.", 0, 0),
                    new DialogueBox.DialogueLine("Forest Witch",
                        "The dragon you seek feeds on fear. Show it none, and you may survive.", 0, 0)
                ),
                new DialogueBox.ChoiceNode(
                    "The witch offers to share her knowledge. How do you respond?",
                    List.of(
                        new DialogueBox.DialogueLine(playerName,
                            "Please tell us everything you know about the Shrine and the dragon.", +6, +6),
                        new DialogueBox.DialogueLine(playerName,
                            "Do you know anything about the Shrine of Aniana?", +3, +2),
                        new DialogueBox.DialogueLine(playerName,
                            "We don't need your help, witch. Stay out of our way.", -4, -6),
                        new DialogueBox.DialogueLine(playerName,
                            "What do you want in return for this information?", +1, -2)
                    ),
                    List.of(
                        List.of(new DialogueBox.DialogueLine("Forest Witch",
                            "Smart child! The Shrine holds Aniana's spirit, only true bonds may enter its heart. Trust is your weapon.", +5, +4)),
                        List.of(new DialogueBox.DialogueLine("Forest Witch",
                            "The Shrine holds the spirit of Aniana herself. Only those with true bonds may enter its heart.", +3, +3)),
                        List.of(new DialogueBox.DialogueLine("Forest Witch",
                            "Hmph! Prideful fools rarely survive what awaits. But suit yourself.", -2, -3)),
                        List.of(new DialogueBox.DialogueLine("Forest Witch",
                            "Nothing... I only wish to see young ones succeed where so many before have failed.", +2, +1))
                    )
                ),
                List.of(
                    new DialogueBox.DialogueLine("Forest Witch",
                        "Trust... it is the rarest thing in these cursed lands.", +4, +3)
                )
            );

           
            case "shrine_elder_final" -> new DialogueBox.DialogueSequence(
                List.of(
                    new DialogueBox.DialogueLine("Elder Elf",
                        "So... you have arrived at last. The spirit of Aniana has watched your journey.", 0, 0),
                    new DialogueBox.DialogueLine("Elder Elf",
                        "From the village wounded to the forest's darkness, every choice you made echoes here.", 0, 0)
                ),
                new DialogueBox.ChoiceNode(
                    "The Elder asks: What drove you on this journey?",
                    List.of(
                        new DialogueBox.DialogueLine(playerName,
                            "We came to help the people we met. Their suffering mattered to us.", +10, +10),
                        new DialogueBox.DialogueLine(playerName,
                            "We only wanted to protect " + companionName + " and find peace.", +6, +8),
                        new DialogueBox.DialogueLine(playerName,
                            "We needed to prove ourselves. To become stronger.", +4, +2),
                        new DialogueBox.DialogueLine(playerName,
                            "Honestly? We just wanted to get here and be done with it.", -2, -4)
                    ),
                    List.of(
                        List.of(new DialogueBox.DialogueLine("Elder Elf",
                            "A selfless heart... Aniana's blessing is yours by right.", +8, +8)),
                        List.of(new DialogueBox.DialogueLine("Elder Elf",
                            "Love for another is a sacred bond. Aniana honors that.", +6, +6)),
                        List.of(new DialogueBox.DialogueLine("Elder Elf",
                            "Strength in service of others is noble. A good path.", +4, +3)),
                        List.of(new DialogueBox.DialogueLine("Elder Elf",
                            "At least you are honest. Honesty too has its own grace.", +1, +2))
                    )
                ),
                List.of(
                    new DialogueBox.DialogueLine("Elder Elf",
                        "Aniana's blessing is granted. Go in peace, the world is a little brighter for your deeds.", +5, +5),
                    new DialogueBox.DialogueLine(companionName,
                        playerName + "... I'm proud of everything we did together. This is what true trust feels like.", +10, +10)
                )
            );

            default -> new DialogueBox.DialogueSequence(
                List.of(new DialogueBox.DialogueLine("???", "...", 0, 0))
            );
        };
    }
}

package com.alkateca.lendasdapoeira.engine;


import com.alkateca.lendasdapoeira.entity.Card;
import com.alkateca.lendasdapoeira.entity.HeroCard;
import com.alkateca.lendasdapoeira.entity.Player;
import com.alkateca.lendasdapoeira.enums.TurnPhase;
import com.alkateca.lendasdapoeira.enums.ZoneId;
import lombok.*;

import java.util.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GameEngine {

    private Player player1;
    private Player player2;
    private List<Card> cardsOnBoard;
    private ResolutionQueue resolutionQueue = new ResolutionQueue();
    private UUID activePlayerId;
    private boolean isPlayer1Ready = false;
    private boolean isPlayer2Ready = false;

    private TurnPhase turnPhase;


    public GameEngine(Player player1, Player player2) {
        this.player1 = player1;
        this.player2 = player2;
        this.cardsOnBoard = new ArrayList<>();
    }

    private void drawInitialHand(Player player) {
        int drawn = 0;

        while (drawn < 5) {
            Card cardToDraw = player.getCurrentDeck().getCardList().stream()
                    .filter(c -> c.getZoneId() == ZoneId.DECK)
                    .findFirst()
                    .orElse(null);

            if (cardToDraw != null) {
                cardToDraw.setZoneId(ZoneId.HAND);
                drawn++;
            } else {
                return;
            }
        }

        System.out.println("Jogador " + player.getName() + " comprou " + drawn + " carta(s).");
    }

    public void startGame() {
        System.out.println("--- INICIANDO A PARTIDA ---");

        Random random = new Random();
        boolean player1Starts = random.nextBoolean();

        if (player1Starts) {
            this.activePlayerId = player1.getUuid();
            System.out.println("=> Sorteio: " + player1.getName() + " ganhou no cara ou coroa e começa jogando!");
        } else {
            this.activePlayerId = player2.getUuid();
            System.out.println("=> Sorteio: " + player2.getName() + " ganhou no cara ou coroa e começa jogando!");
        }

        player1.getCurrentDeck().shuffleDeck();
        player2.getCurrentDeck().shuffleDeck();

        drawInitialHand(player1);
        drawInitialHand(player2);

        for(Card heroCard : player1.getCurrentDeck().getCardList()){
            heroCard.setZoneId(ZoneId.BENCH);
        }

        for(Card heroCard : player2.getCurrentDeck().getCardList()){
            heroCard.setZoneId(ZoneId.BENCH);
        }


    }

    public void selectHeroCard() {}

    public void physicalCombat() {

    }

    public void effectsResolution(){}

    public void startOfCombatEffects(){}

    public void endOfCombatEffects(){}

    public void discard(){}

    public void gameOver(){}

    public void gamePhase(){
        switch (turnPhase) {
            case GAME_START:
                startGame();
                break;
            case CHOICE:
                break;
            case PREPARATION:
                selectHeroCard();
                break;
            case COMBAT_START:
                startOfCombatEffects();
                break;
            case RESOLUTION:
                effectsResolution();
                break;
            case COMBAT:
                physicalCombat();
                break;
            case COMBAT_END:
                endOfCombatEffects();
                break;
            case DISCARD:
                discard();
                break;
            case GAME_OVER:
                gameOver();
                break;
        }
    }


}


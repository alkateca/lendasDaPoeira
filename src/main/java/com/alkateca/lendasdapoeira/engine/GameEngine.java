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

    public GameEngine(Player player1, Player player2) {
        this.player1 = player1;
        this.player2 = player2;
        this.cardsOnBoard = new ArrayList<>();
    }

    private void shuffle(Player player){
        player.getCurrentDeck().shuffleDeck();
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

    public void selectHeroCard(HeroCard heroCard) {

        if (player1.getUuid().equals(heroCard.getOwnerId()) && heroCard.getZoneId() == ZoneId.BENCH
                && heroCard.getEstaAtivo() == true) {
            heroCard.setZoneId(ZoneId.BATTLE);
        }

        if (player2.getUuid().equals(heroCard.getOwnerId()) && heroCard.getZoneId() == ZoneId.BENCH
                && heroCard.getEstaAtivo() == true) {
            heroCard.setZoneId(ZoneId.BATTLE);
        }

    }

    public void physicalCombat() {

    }

    public void effectsResolution(){}



}


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

    private GameState gameState;
    private Player player1;
    private Player player2;
    private List<Card> cardsOnBoard;
    private ResolutionQueue resolutionQueue = new ResolutionQueue();
    private UUID activePlayerId;
    private boolean isPlayer1Ready = false;
    private boolean isPlayer2Ready = false;

    private TurnPhase turnPhase;


    public GameEngine(Player player1, Player player2) {
        //this.gameState = new GameState();
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
            if (this.gameState != null) this.gameState.setActiveTurnPlayerId(player1.getUuid());
            System.out.println("=> Sorteio: " + player1.getName() + " ganhou no cara ou coroa e começa jogando!");
        } else {
            this.activePlayerId = player2.getUuid();
            if (this.gameState != null) this.gameState.setActiveTurnPlayerId(player2.getUuid());
            System.out.println("=> Sorteio: " + player2.getName() + " ganhou no cara ou coroa e começa jogando!");
        }

        player1.getCurrentDeck().shuffleDeck();
        player2.getCurrentDeck().shuffleDeck();

        drawInitialHand(player1);
        drawInitialHand(player2);

        for(Card heroCard : player1.getCurrentDeck().getHeroList()){
            heroCard.setZoneId(ZoneId.BENCH);
        }

        for(Card heroCard : player2.getCurrentDeck().getHeroList()){
            heroCard.setZoneId(ZoneId.BENCH);
        }

        changePhase(TurnPhase.CHOICE);

    }

    public void choice() {

        changePhase(TurnPhase.COMBAT_START);

    }

    public void combatStart(){

        changePhase(TurnPhase.RESOLUTION);
    }

    public void resolution() {

        List<Card> firstPlayerCards = gameState.getActiveTurnPlayerId().equals(gameState.getPlayer1().getUuid())
                ? gameState.getPlayer1PlayedCards()
                : gameState.getPlayer2PlayedCards();

        List<Card> secondPlayerCards = gameState.getActiveTurnPlayerId().equals(gameState.getPlayer1().getUuid())
                ? gameState.getPlayer2PlayedCards()
                : gameState.getPlayer1PlayedCards();

        int maxCards = Math.max(firstPlayerCards.size(), secondPlayerCards.size());

        for (int i = 0; i < maxCards; i++) {
            if (i < firstPlayerCards.size()) {
                Card c1 = firstPlayerCards.get(i);
                if (c1.getEffects() != null) {
                    c1.getEffects().forEach(effect -> resolutionQueue.enqueue(effect, c1));
                }
            }
            if (i < secondPlayerCards.size()) {
                Card c2 = secondPlayerCards.get(i);
                if (c2.getEffects() != null) {
                    c2.getEffects().forEach(effect -> resolutionQueue.enqueue(effect, c2));
                }
            }
        }

        resolutionQueue.resolveAll(this);

        gameState.getPlayer1PlayedCards().clear();
        gameState.getPlayer2PlayedCards().clear();

        changePhase(TurnPhase.COMBAT);
    }

    public void combat() {

        changePhase(TurnPhase.COMBAT_END);
    }


    public void combatEnd() {

        checkBoardRefresh(gameState.getPlayer1());
        checkBoardRefresh(gameState.getPlayer2());

        if (checkGameOver()) {
            changePhase(TurnPhase.GAME_OVER);
        } else {
            changePhase(TurnPhase.DISCARD);
        }
    }

    public void discard(){

        if(gameState.getActiveTurnPlayerId().equals(gameState.getPlayer1().getUuid())) {
            gameState.setActiveTurnPlayerId(gameState.getPlayer2().getUuid());
        } else {
            gameState.setActiveTurnPlayerId(gameState.getPlayer1().getUuid());
        }

        changePhase(TurnPhase.CHOICE);
    }

    public void gameOver() {
        System.out.println("--- PARTIDA ENCERRADA ---");
    }

    private boolean checkGameOver() {
        return false;
    }

    private void changePhase(TurnPhase newPhase) {
        this.turnPhase = newPhase;
        if (this.gameState != null) {
            this.gameState.setTurnPhase(newPhase);
        }
    }

    private void checkBoardRefresh(Player player) {

        boolean hasActiveHeroes = player.getCurrentDeck().getHeroList().stream()
                .filter(c -> c instanceof HeroCard && c.getZoneId() == ZoneId.BENCH)
                .anyMatch(c -> ((HeroCard) c).getEstaAtivo().equals(true));

        if (!hasActiveHeroes) {
            player.getCurrentDeck().getHeroList().stream()
                    .filter(c -> c instanceof HeroCard && c.getZoneId() == ZoneId.BENCH  && ((HeroCard) c).getEstaAtivo().equals(true))
                    .forEach(c -> ((HeroCard) c).setEstaAtivo(true));
        }
    }

    public void gamePhase(){
        switch (turnPhase) {
            case GAME_START:
                startGame();
                break;
            case CHOICE:
                break;
            case PREPARATION:
                choice();
                break;
            case COMBAT_START:
                combatStart();
                break;
            case RESOLUTION:
                resolution();
                break;
            case COMBAT:
                combat();
                break;
            case COMBAT_END:
                combatEnd();
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


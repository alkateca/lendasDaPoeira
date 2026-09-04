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

    public GameEngine(Player player1, Player player2) {
        this.player1 = player1;
        this.player2 = player2;
        this.cardsOnBoard = new ArrayList<>();
    }

    private void shuffle(Player player){
        player.getCurrentDeck().shuffleDeck();
    }

    public void changePhase(TurnPhase turnPhase) {
        // 1. Atualiza o estado interno da Engine (CORREÇÃO AQUI)
        this.currentPhase = turnPhase;

        System.out.println("Fase mudou para: " + this.currentPhase);

        // 2. Avisa as cartas da mesa
        for (Card card : cardsOnBoard) {
            card.onPhaseChange(turnPhase, resolutionQueue);
        }

        // 3. Resolve a fila
        resolutionQueue.resolveAll(this);
    }

    public void startGame() {
        System.out.println("--- INICIANDO A PARTIDA ---");

        this.cardsOnBoard = new ArrayList<>();

        // Prepara os baralhos e as mãos
        setupPlayer(player1);
        setupPlayer(player2);

        // 1. O Sorteio (Coin Toss)
        Random random = new Random();
        boolean player1Starts = random.nextBoolean(); // Retorna true ou false (50/50)

        if (player1Starts) {
            this.activePlayerId = player1.getUuid();
            System.out.println("=> Sorteio: " + player1.getName() + " ganhou no cara ou coroa e começa jogando!");
        } else {
            this.activePlayerId = player2.getUuid();
            System.out.println("=> Sorteio: " + player2.getName() + " ganhou no cara ou coroa e começa jogando!");
        }
        changePhase(TurnPhase.GAME_START);
        // 2. Agora sim, o jogo começa sabendo quem manda na fase CHOICE
        changePhase(TurnPhase.CHOICE);
    }

    private void setupPlayer(Player player) {
        System.out.println("Preparando o campo para o jogador: " + player.getName());

        for (Card hero : player.getCurrentDeck().getHeroList()) {
            hero.setZoneId(ZoneId.BENCH);

            this.cardsOnBoard.add(hero);
        }

        List<Card> regularCards = player.getCurrentDeck().getCardList();
        for (Card card : regularCards) {
            card.setZoneId(ZoneId.DECK);
        }

        player.getCurrentDeck().shuffleDeck();

        drawInitialHand(player, 5);
    }

    private void drawInitialHand(Player player, int amount) {
        List<Card> deck = player.getCurrentDeck().getCardList();
        int drawn = 0;

        for (Card card : deck) {
            if (card.getZoneId() == ZoneId.DECK) {

                card.setZoneId(ZoneId.HAND);
                drawn++;

                if (drawn == amount) {
                    break;
                }
            }
        }
        System.out.println("Jogador comprou " + drawn + " cartas.");
    }

    private UUID activePlayerId;
    private TurnPhase currentPhase;


    private boolean isPlayer1Ready = false;
    private boolean isPlayer2Ready = false;

    public void nextPhase() {
        switch (currentPhase) {
            case CHOICE:
                // O jogador ativo escolheu quem vai lutar. Avança para preparação.
                changePhase(TurnPhase.PREPARATION);
                break;

            case PREPARATION:
                // Na preparação, AMBOS os jogadores jogam cartas secretamente.
                // A Engine só pode avançar para a RESOLUTION se os dois confirmarem a jogada.
                if (isPlayer1Ready && isPlayer2Ready) {
                    // Reseta os status para o próximo turno
                    isPlayer1Ready = false;
                    isPlayer2Ready = false;
                    changePhase(TurnPhase.RESOLUTION);
                } else {
                    System.out.println("Aguardando o outro jogador terminar de preparar suas cartas...");
                }
                break;

            case RESOLUTION:
                // As cartas são reveladas e os efeitos (sua ResolutionQueue) acontecem
                changePhase(TurnPhase.COMBAT);
                break;

            case COMBAT:
                // O cálculo de dano físico é aplicado
                changePhase(TurnPhase.COMBAT_END);
                break;

            case COMBAT_END:
                // Efeitos de "após o combate" ou limpeza
                changePhase(TurnPhase.DISCARD);
                break;

            case DISCARD:
                // Cartas usadas vão para o cemitério e o turno vira
                passTurn();
                break;
        }
    }

    private void passTurn() {
        System.out.println("--- FIM DO TURNO ---");

        // Se era o player 1, vira o player 2. Se não, vira o player 1.
        if (this.activePlayerId.equals(player1.getUuid())) {
            this.activePlayerId = player2.getUuid();
        } else {
            this.activePlayerId = player1.getUuid();
        }

        System.out.println("=> O turno passou! Jogador ativo agora é o ID: " + this.activePlayerId);

        // O ciclo recomeça na fase CHOICE, mas agora com o outro jogador no controle
        changePhase(TurnPhase.CHOICE);
    }

    public void chooseCombatant(UUID playerId, UUID heroCardId) {
        if (currentPhase != TurnPhase.CHOICE) {
            throw new IllegalStateException("Não estamos na fase de escolha!");
        }
        if (!playerId.equals(activePlayerId)) {
            throw new IllegalStateException("Apenas o jogador ativo pode fazer a escolha!");
        }

        // Lógica de definir o herói ativo...
        System.out.println("Jogador " + playerId + " escolheu o herói " + heroCardId);

        // Como ele já escolheu, a mesa avança automaticamente
        nextPhase();
    }

    public void setPlayerReady(UUID playerId) {
        if (currentPhase != TurnPhase.PREPARATION) {
            throw new IllegalStateException("Não é momento de preparação!");
        }

        if (playerId.equals(player1.getUuid())) {
            isPlayer1Ready = true;
        } else if (playerId.equals(player2.getUuid())) {
            isPlayer2Ready = true;
        }

        // Tenta avançar a fase. Se o outro ainda não estiver pronto, o nextPhase() vai segurar o jogo.
        nextPhase();
    }

    public void playCard(UUID playerId, UUID cardId, UUID targetHeroId) {
        // 1. Validação de Fase
        if (this.currentPhase != TurnPhase.PREPARATION) {
            throw new IllegalStateException("Você só pode jogar cartas na fase de PREPARATION!");
        }

        // 2. Encontrar o Jogador e a Carta
        Player player = playerId.equals(player1.getUuid()) ? player1 : player2;

        // Busca a carta na lista geral do deck usando Streams (recurso muito útil do Java)
        Card cardToPlay = player.getCurrentDeck().getCardList().stream()
                .filter(c -> c.getUuid().equals(cardId) && c.getZoneId() == ZoneId.HAND)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Carta não encontrada na sua mão!"));

        // 3. Busca o Herói Alvo na mesa
        Card targetHero = cardsOnBoard.stream()
                .filter(c -> c.getUuid().equals(targetHeroId) && c.getZoneId() == ZoneId.BENCH)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Herói alvo inválido!"));

        // 4. Executa a jogada
        System.out.println("=> Jogador " + player.getName() + " jogou a carta [" + cardToPlay.getCardName() + "] no herói [" + targetHero.getCardName() + "].");

        // A carta sai da MÃO e vai para a BATTLE (Mesa)
        cardToPlay.setZoneId(ZoneId.BATTLE);

        // A Engine agora precisa gerenciar essa carta recém-jogada
        this.cardsOnBoard.add(cardToPlay);
    }

    public HeroCard getActiveEnemyHero(UUID myPlayerId) {

        return this.cardsOnBoard.stream()
                // 1. Garante que estamos olhando apenas para Heróis
                .filter(card -> card instanceof HeroCard)

                // 2. Garante que o dono da carta NÃO seja quem está atacando (ou seja, é o inimigo)
                .filter(card -> !card.getOwnerId().equals(myPlayerId))

                // 3. Garante que o herói está na linha de frente (BATTLE) e não no banco (BENCH)
                .filter(card -> card.getZoneId() == ZoneId.BATTLE)

                // 4. Converte o tipo Card genérico para HeroCard
                .map(card -> (HeroCard) card)

                // 5. Pega o primeiro que encontrar (já que é 1x1 no turno)
                .findFirst()

                // 6. Retorna nulo por segurança, caso o oponente não tenha heróis vivos
                .orElse(null);
    }
}

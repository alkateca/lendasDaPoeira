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
        int drawn = 0;

        while (drawn < amount) {
            Card cardToDraw = player.getCurrentDeck().getCardList().stream()
                    .filter(c -> c.getZoneId() == ZoneId.DECK)
                    .findFirst()
                    .orElse(null);

            if (cardToDraw != null) {
                cardToDraw.setZoneId(ZoneId.HAND);
                drawn++;
            } else {
                // O baralho secou antes de terminar a compra exigida!
                System.out.println("⚠️ O baralho de " + player.getName() + " esgotou!");
                triggerSuddenDeath(player);
                return; // Interrompe o método de compra
            }
        }

        System.out.println("Jogador " + player.getName() + " comprou " + drawn + " carta(s).");
    }

    private void triggerSuddenDeath(Player defeatedPlayer) {
        Player winner = defeatedPlayer.getUuid().equals(player1.getUuid()) ? player2 : player1;

        System.out.println("☠️ MORTE SÚBITA POR FADIGA!");
        System.out.println("🏆 " + winner.getName() + " VENCEU A PARTIDA!");

        // * Lembrete: Adicione GAME_OVER no seu enum TurnPhase se ainda não existir.
        changePhase(TurnPhase.GAME_OVER);
    }

    private UUID activePlayerId;

    private TurnPhase currentPhase;

    private boolean isPlayer1Ready = false;
    private boolean isPlayer2Ready = false;

    // ... variáveis existentes (player1, player2, cardsOnBoard, etc) ...

    private int scorePlayer1 = 0;
    private int scorePlayer2 = 0;

    /**
     * Adiciona pontos (dano ou cura) ao placar de um jogador específico.
     */
    public void addScore(UUID playerId, int amount) {
        if (playerId.equals(player1.getUuid())) {
            this.scorePlayer1 += amount;
        } else if (playerId.equals(player2.getUuid())) {
            this.scorePlayer2 += amount;
        }
    }

    public void nextPhase() {
        switch (this.currentPhase) {

            case CHOICE:
                // O jogador ativo escolheu quem vai lutar.
                changePhase(TurnPhase.PREPARATION);
                break;

            case PREPARATION:
                // Na preparação, AMBOS jogam cartas.
                if (isPlayer1Ready && isPlayer2Ready) {
                    isPlayer1Ready = false;
                    isPlayer2Ready = false;
                    changePhase(TurnPhase.RESOLUTION);
                } else {
                    System.out.println("Aguardando o outro jogador terminar de preparar suas cartas...");
                }
                break;

            case RESOLUTION:
                System.out.println("✨ Resolvendo feitiços e ações...");

                // Pega todas as cartas que foram jogadas nesta rodada
                List<Card> cardsInResolve = this.cardsOnBoard.stream()
                        .filter(c -> c.getZoneId() == ZoneId.RESOLVE)
                        .toList();

                // Aqui você chamaria os efeitos delas (ex: card.applyEffect(this))

                // Após o efeito, envia magias/ações para o descarte.
                // (Se for um Item Permanente, o efeito dela deve ter mudado a zona para ATTACHED)
                cardsInResolve.stream()
                        .filter(c -> c.getZoneId() == ZoneId.RESOLVE)
                        .forEach(c -> c.setZoneId(ZoneId.DISCARD));

                changePhase(TurnPhase.COMBAT);
                break;

            case COMBAT:
                // 2. Porrada Física
                resolvePhysicalCombat(); // Calcula o dano (Aceita HP negativo)

                changePhase(TurnPhase.COMBAT_END);
                break;

            case COMBAT_END:
                // 3. Limpeza e Mortes

                // O "Ceifador" entra AQUI, matando quem ficou com HP <= 0
                processCasualtiesAndFatigue();

                // Retorna os sobreviventes inativos para o banco
                this.cardsOnBoard.stream()
                        .filter(c -> c.getZoneId() == ZoneId.BATTLE)
                        .forEach(c -> c.setZoneId(ZoneId.BENCH));

                changePhase(TurnPhase.DISCARD);
                break;

            case DISCARD:
                // Cartas usadas vão para o cemitério e o turno vira
                processPermanents();
                passTurn(); // Retorna para GAME_START ou CHOICE
                break;
        }
    }

    private void passTurn() {
        System.out.println("--- FIM DO TURNO ---");

        // 1. Contagem de Sobreviventes
        long p1HeroesAlive = this.cardsOnBoard.stream()
                .filter(c -> c instanceof HeroCard && c.getOwnerId().equals(player1.getUuid()))
                .map(c -> (HeroCard) c)
                .filter(HeroCard::getEstaVivo)
                .count();

        long p2HeroesAlive = this.cardsOnBoard.stream()
                .filter(c -> c instanceof HeroCard && c.getOwnerId().equals(player2.getUuid()))
                .map(c -> (HeroCard) c)
                .filter(HeroCard::getEstaVivo)
                .count();

        // 2. Condições de Fim de Jogo
        if (p1HeroesAlive == 0 && p2HeroesAlive == 0) {
            System.out.println("☠️ DESTRUIÇÃO MÚTUA! Todos os heróis caíram em batalha.");
            System.out.println("📊 PONTUAÇÃO FINAL (Dano + Cura) -> " + player1.getName() + ": " + scorePlayer1 + " | " + player2.getName() + ": " + scorePlayer2);

            if (scorePlayer1 > scorePlayer2) {
                System.out.println("🏆 " + player1.getName() + " VENCEU PELO DESEMPATE DE PONTOS!");
            } else if (scorePlayer2 > scorePlayer1) {
                System.out.println("🏆 " + player2.getName() + " VENCEU PELO DESEMPATE DE PONTOS!");
            } else {
                System.out.println("🤝 EMPATE! As forças se anularam de forma idêntica.");
            }

            changePhase(TurnPhase.GAME_OVER);
            return;
        }
        else if (p1HeroesAlive == 0) {
            System.out.println("🏆 " + player2.getName() + " VENCEU A PARTIDA!");
            changePhase(TurnPhase.GAME_OVER);
            return;
        }
        else if (p2HeroesAlive == 0) {
            System.out.println("🏆 " + player1.getName() + " VENCEU A PARTIDA!");
            changePhase(TurnPhase.GAME_OVER);
            return;
        }

        // 3. Troca o jogador ativo
        this.activePlayerId = this.activePlayerId.equals(player1.getUuid()) ? player2.getUuid() : player1.getUuid();
        System.out.println("=> O turno passou! Jogador ativo agora é: " + this.activePlayerId);

        // ... (o resto da Fase de Compra e Exaustão que fizemos anteriormente continua igual) ...

        // 3. NOVA REGRA: Reposição de Mão (Ambos os jogadores compram até ter 5)
        System.out.println("🃏 Fase de Compra (Draw):");
        replenishHand(player1);
        if (this.currentPhase == TurnPhase.GAME_OVER) return;
        replenishHand(player2);
        if (this.currentPhase == TurnPhase.GAME_OVER) return;

        // 4. Exaustão e Despertar de Esquadrão
        Player activePlayer = this.activePlayerId.equals(player1.getUuid()) ? player1 : player2;
        long activeHeroesCount = this.cardsOnBoard.stream()
                .filter(c -> c instanceof HeroCard && c.getOwnerId().equals(activePlayerId))
                .map(c -> (HeroCard) c)
                .filter(h -> h.getEstaVivo() && h.getEstaAtivo())
                .count();

        if (activeHeroesCount == 0) {
            System.out.println("🔄 O esquadrão de " + activePlayer.getName() + " estava totalmente exausto. Todos recuperam suas energias!");
            this.cardsOnBoard.stream()
                    .filter(c -> c instanceof HeroCard && c.getOwnerId().equals(activePlayerId))
                    .map(c -> (HeroCard) c)
                    .filter(HeroCard::getEstaVivo)
                    .forEach(h -> h.setEstaAtivo(true));
        } else {
            System.out.println("⚡ " + activePlayer.getName() + " ainda possui " + activeHeroesCount + " herói(s) disposto(s) a lutar no banco.");
        }

        // 5. O ciclo recomeça
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

        Player player = playerId.equals(player1.getUuid()) ? player1 : player2;

        // 2. Tira a carta da MÃO
        Card cardToPlay = player.getCurrentDeck().getCardList().stream()
                .filter(c -> c.getUuid().equals(cardId) && c.getZoneId() == ZoneId.HAND)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Carta não encontrada na sua mão!"));

        // 3. Busca o Herói Alvo na mesa (MUDANÇA: Ele agora precisa estar na BATTLE)
        Card targetHero = cardsOnBoard.stream()
                .filter(c -> c.getUuid().equals(targetHeroId) && c.getZoneId() == ZoneId.BATTLE)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Herói alvo inválido ou não está em combate!"));

        // 4. Grava a intenção e move para a zona de resolução
        System.out.println("=> Jogador " + player.getName() + " preparou [" + cardToPlay.getCardName() + "] para agir sobre [" + targetHero.getCardName() + "].");

        cardToPlay.setAttachedToCardId(targetHeroId);

        // NOVA ZONA: A carta fica "flutuando" aguardando a fase RESOLUTION
        cardToPlay.setZoneId(ZoneId.RESOLVE);

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

    private void processPermanents() {
        System.out.println("--- Checando a duração das cartas anexadas ---");

        List<Card> toDiscard = new ArrayList<>();

        for (Card card : cardsOnBoard) {
            if (card.getZoneId() == ZoneId.ATTACHED && card.getTurnsRemaining() != null) {

                // Diminui um turno da vida útil da carta
                card.setTurnsRemaining(card.getTurnsRemaining() - 1);

                if (card.getTurnsRemaining() <= 0) {
                    System.out.println("O efeito/item [" + card.getCardName() + "] expirou e foi para o cemitério.");
                    card.setZoneId(ZoneId.DISCARD);
                    card.setAttachedToCardId(null); // Desgruda do herói
                    toDiscard.add(card);
                }
            }
        }

        // Remove da mesa (se você tiver uma lista separada de cemitério, adicione lá)
        cardsOnBoard.removeAll(toDiscard);
    }

    public void chooseHeroForCombat(UUID playerId, UUID heroCardId) {
        if (this.currentPhase != TurnPhase.CHOICE) {
            throw new IllegalStateException("Não estamos na fase de escolha de heróis!");
        }

        Card chosenHero = this.cardsOnBoard.stream()
                .filter(c -> c.getUuid().equals(heroCardId))
                .filter(c -> c.getOwnerId().equals(playerId))
                .filter(c -> c.getZoneId() == ZoneId.BENCH)
                .filter(c -> {
                    // Garante que o herói está vivo e ativo antes de ir pro combate
                    if (c instanceof HeroCard) {
                        HeroCard h = (HeroCard) c;
                        return h.getEstaVivo() && h.getEstaAtivo();
                    }
                    return false;
                })
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Herói inválido, exausto ou já está em combate!"));

        chosenHero.setZoneId(ZoneId.BATTLE);
        System.out.println("=> O jogador " + playerId + " enviou o herói [" + chosenHero.getCardName() + "] para o BATTLE!");
    }

    public void resolvePhysicalCombat() {
        if (this.currentPhase != TurnPhase.COMBAT) {
            throw new IllegalStateException("O combate físico só pode ocorrer na fase COMBAT!");
        }

        HeroCard heroP1 = (HeroCard) this.cardsOnBoard.stream()
                .filter(c -> c.getZoneId() == ZoneId.BATTLE && c.getOwnerId().equals(player1.getUuid()))
                .findFirst()
                .orElse(null);

        HeroCard heroP2 = (HeroCard) this.cardsOnBoard.stream()
                .filter(c -> c.getZoneId() == ZoneId.BATTLE && c.getOwnerId().equals(player2.getUuid()))
                .findFirst()
                .orElse(null);

        if (heroP1 == null || heroP2 == null) return;

        int danoEmP2 = Math.max(0, heroP1.getAtaque() - heroP2.getDefesa());
        int danoEmP1 = Math.max(0, heroP2.getAtaque() - heroP1.getDefesa());

        heroP1.setVidaAtual(heroP1.getVidaAtual() - danoEmP1);
        heroP2.setVidaAtual(heroP2.getVidaAtual() - danoEmP2);

        // NOVA REGRA: Acumula a pontuação de dano realizado
        addScore(player1.getUuid(), danoEmP2);
        addScore(player2.getUuid(), danoEmP1);

        System.out.println("⚔️ COMBATE: [" + heroP1.getCardName() + "] (" + heroP1.getVidaAtual() + " HP) vs [" + heroP2.getCardName() + "] (" + heroP2.getVidaAtual() + " HP)");
    }

    private void processCasualtiesAndFatigue() {
        System.out.println("☠️ Checando baixas da rodada...");

        this.cardsOnBoard.stream()
                .filter(c -> c instanceof HeroCard)
                .map(c -> (HeroCard) c)
                .forEach(hero -> {
                    // 1. Processa a Morte
                    if (hero.getVidaAtual() <= 0 && hero.getEstaVivo()) {
                        System.out.println("💀 [" + hero.getCardName() + "] não resistiu aos ferimentos e morreu!");

                        hero.setEstaVivo(false);
                        hero.setEstaAtivo(false);

                        // O herói morto volta e permanece no banco!
                        hero.setZoneId(ZoneId.BENCH);

                        limparEquipamentos(hero.getUuid());
                    }
                    // 2. Processa a Fadiga (Sobreviveu, mas lutou)
                    else if (hero.getVidaAtual() > 0 && hero.getZoneId() == ZoneId.BATTLE) {
                        hero.setEstaAtivo(false);
                        System.out.println("💤 [" + hero.getCardName() + "] sobreviveu ao combate, mas está exausto (inativo).");
                    }
                });
    }

    private void limparEquipamentos(UUID deadHeroId) {
        this.cardsOnBoard.stream()
                .filter(c -> c.getZoneId() == ZoneId.ATTACHED && deadHeroId.equals(c.getAttachedToCardId()))
                .forEach(c -> {
                    c.setZoneId(ZoneId.DISCARD);
                    c.setAttachedToCardId(null);
                    System.out.println(" ↳ Equipamento [" + c.getCardName() + "] foi destruído junto com o herói.");
                });
    }

    public void executeCombatPhaseSequence() {

        // 1. Resolve Magias (Pode dar dano, mas NINGUÉM MORRE AQUI)
        this.currentPhase = TurnPhase.RESOLUTION;
        // resolveSpells();

        // 2. Porrada Física (Pode deixar HP negativo, NINGUÉM MORRE AQUI)
        this.currentPhase = TurnPhase.COMBAT;
        this.resolvePhysicalCombat();

        // 3. Efeitos Pós-Combate (Sangramentos, Curas finais)
        this.currentPhase = TurnPhase.COMBAT_END;
        // resolveCombatEndEffects();

        // 4. A GUILHOTINA: Resolve as mortes e inativa quem lutou
        this.processCasualtiesAndFatigue();

        // 5. Libera o tabuleiro para o jogador fazer o descarte com as mortes já confirmadas
        this.currentPhase = TurnPhase.DISCARD;

        // Manda os sobreviventes cansados de volta pro BENCH
        this.cardsOnBoard.stream()
                .filter(c -> c.getZoneId() == ZoneId.BATTLE)
                .forEach(c -> c.setZoneId(ZoneId.BENCH));
    }

    public void discardCard(UUID playerId, UUID cardId) {
        if (this.currentPhase != TurnPhase.DISCARD) {
            throw new IllegalStateException("Descartes só podem ser feitos na fase DISCARD.");
        }

        Player player = playerId.equals(player1.getUuid()) ? player1 : player2;

        Card cardToDiscard = player.getCurrentDeck().getCardList().stream()
                .filter(c -> c.getUuid().equals(cardId) && c.getZoneId() == ZoneId.HAND)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Carta não encontrada na mão!"));

        cardToDiscard.setZoneId(ZoneId.DISCARD);
        System.out.println("🗑️ O jogador " + player.getName() + " descartou a carta [" + cardToDiscard.getCardName() + "].");
    }

    private void replenishHand(Player player) {
        long currentHandSize = this.cardsOnBoard.stream()
                .filter(c -> c.getOwnerId().equals(player.getUuid()) && c.getZoneId() == ZoneId.HAND)
                .count();

        int cardsToDraw = 5 - (int) currentHandSize;

        if (cardsToDraw > 0) {
            System.out.println("Repondo a mão de " + player.getName() + ": precisa comprar " + cardsToDraw + " carta(s).");
            // Reutiliza o método que você já criou para o começo do jogo
            drawInitialHand(player, cardsToDraw);
        }
    }
}

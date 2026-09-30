package com.alkateca.lendasdapoeira;

import com.alkateca.lendasdapoeira.engine.CardFactory;
import com.alkateca.lendasdapoeira.engine.GameEngine;
import com.alkateca.lendasdapoeira.engine.GameState;
import com.alkateca.lendasdapoeira.entity.Card;
import com.alkateca.lendasdapoeira.entity.Deck;
import com.alkateca.lendasdapoeira.entity.HeroCard;
import com.alkateca.lendasdapoeira.entity.Player;
import com.alkateca.lendasdapoeira.enums.TurnPhase;
import com.alkateca.lendasdapoeira.enums.ZoneId;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SpringBootApplication
public class LendasDaPoeiraApplication {

    public static void main(String[] args) {
        SpringApplication.run(LendasDaPoeiraApplication.class, args);
        System.out.println(">>> Servidor do Jogo Rodando na porta 8080! <<<");

        // 1. Setup dos Decks e Jogadores
        Player p1 = createMockPlayer("Jogador 1 - Alka");
        Player p2 = createMockPlayer("Jogador 2 - Moyra");

        // 2. Inicializando o Motor do Jogo
        GameEngine engine = new GameEngine(p1, p2);

        // Inicializa o GameState que faltou no construtor padrão da engine
        GameState state = new GameState();
        state.setPlayer1(p1);
        state.setPlayer2(p2);
        engine.setGameState(state);

        // 3. Iniciando a Partida
        engine.startGame(); // Vai definir quem começa e mudar a fase para CHOICE

        // --- LOOP PRINCIPAL DO PROTÓTIPO ---
        // Simulação de turnos até um "game over" ou limite de turnos para teste
        int turnos = 1;
        while (engine.getGameState().getTurnPhase() != TurnPhase.GAME_OVER && turnos <= 3) {
            System.out.println("\n===== INÍCIO DO TURNO " + turnos + " =====");

            // FASE DE ESCOLHA (CHOICE) -> O Jogador Ativo escolhe os combatentes
            if (engine.getGameState().getTurnPhase() == TurnPhase.CHOICE) {
                System.out.println("Fase: CHOICE - Jogador " + getActivePlayerName(engine) + " está escolhendo os heróis.");

                // Mock: Seleciona o primeiro herói vivo e ativo de cada banco
                HeroCard heroP1 = getFirstAvailableHero(p1);
                HeroCard heroP2 = getFirstAvailableHero(p2);

                engine.getGameState().setActiveHeroPlayer1Id(heroP1 != null ? heroP1.getUuid() : null);
                engine.getGameState().setActiveHeroPlayer2Id(heroP2 != null ? heroP2.getUuid() : null);

                System.out.println("Combate definido: " + (heroP1 != null ? heroP1.getCardName() : "Nenhum") + " VS " + (heroP2 != null ? heroP2.getCardName() : "Nenhum"));

                engine.choice(); // Muda fase para COMBAT_START
            }

            // FASE PREPARATÓRIA (COMBAT_START) -> Jogadores escolhem cartas da mão para jogar
            if (engine.getGameState().getTurnPhase() == TurnPhase.COMBAT_START) {
                System.out.println("Fase: COMBAT_START - Jogadores preparam suas cartas e efeitos de Início de Combate ativam.");

                // 1. Busca os heróis que estão lutando nesta rodada no GameState
                HeroCard activeHeroP1 = (HeroCard) p1.getCurrentDeck().getHeroList().stream()
                        .filter(h -> h.getUuid().equals(engine.getGameState().getActiveHeroPlayer1Id())).findFirst().orElse(null);

                HeroCard activeHeroP2 = (HeroCard) p2.getCurrentDeck().getHeroList().stream()
                        .filter(h -> h.getUuid().equals(engine.getGameState().getActiveHeroPlayer2Id())).findFirst().orElse(null);

                // 2. Dispara os efeitos deles (se a Moyra estiver aqui, ela vai enfileirar o dano de início de combate)
                if (activeHeroP1 != null) activeHeroP1.onPhaseChange(TurnPhase.COMBAT_START, engine.getResolutionQueue());
                if (activeHeroP2 != null) activeHeroP2.onPhaseChange(TurnPhase.COMBAT_START, engine.getResolutionQueue());

                // Mock: Cartas da mão
                playCardsFromHand(engine, p1, engine.getGameState().getPlayer1PlayedCards());
                playCardsFromHand(engine, p2, engine.getGameState().getPlayer2PlayedCards());

                engine.combatStart(); // Muda fase para RESOLUTION
            }

            // FASE DE RESOLUÇÃO (RESOLUTION) -> Pilha FIFO
            if (engine.getGameState().getTurnPhase() == TurnPhase.RESOLUTION) {
                System.out.println("Fase: RESOLUTION - Resolvendo efeitos das cartas...");
                engine.resolution(); // Executa a pilha e vai para COMBAT
            }

            // FASE DE COMBATE (COMBAT) -> Cálculo de dano (não implementado na engine, então apenas avança)
            if (engine.getGameState().getTurnPhase() == TurnPhase.COMBAT) {
                System.out.println("Fase: COMBAT - Heróis trocam golpes físicos.");
                engine.combat(); // Muda fase para COMBAT_END
            }

            // FASE DE ENCERRAMENTO (COMBAT_END) -> Verifica mortos e inativos
            if (engine.getGameState().getTurnPhase() == TurnPhase.COMBAT_END) {
                System.out.println("Fase: COMBAT_END - Avaliando mortes e status dos heróis...");
                engine.combatEnd(); // Muda fase para GAME_OVER ou DISCARD[cite: 6]
            }

            // FASE DE DESCARTE (DISCARD)
            if (engine.getGameState().getTurnPhase() == TurnPhase.DISCARD) {
                System.out.println("Fase: DISCARD - Jogadores descartam cartas. Passando o turno...");
                engine.discard(); // Alterna a prioridade e volta para CHOICE[cite: 6]
            }

            turnos++;
        }

        System.out.println("\nFim do teste do protótipo.");
    }

    // Métodos Auxiliares de Mocking para a Main

    private static Player createMockPlayer(String name) {
        Player player = new Player();
        player.setUuid(UUID.randomUUID());
        player.setName(name);

        Deck deck = new Deck();
        deck.setUuid(UUID.randomUUID());

        List<Card> heroList = new ArrayList<>();

        // 1. Instanciando a Moyra com os dados do seu JSON
        HeroCard moyra = new HeroCard();
        moyra.setUuid(UUID.randomUUID());
        moyra.setOwnerId(player.getUuid()); // Essencial para o efeito alvejar o oponente certo
        moyra.setCardName("Moyra, Aprendiz da Santa");
        moyra.setVidaMaxima(13);
        moyra.setVidaAtual(13);
        moyra.setAtaque(4);
        moyra.setEspirito(1);
        moyra.setZoneId(ZoneId.BENCH);
        moyra.setEstaAtivo(true);
        moyra.setEstaVivo(true);

        // 2. Injetando a lógica! Isso vai adicionar o StartOfCombatDamageEffect nela
        CardFactory.buildCardLogic(moyra);

        heroList.add(moyra);

        // 3. Adicionando 2 heróis mockados para fechar os 3 heróis do jogador
        for (int i = 2; i <= 3; i++) {
            HeroCard hero = new HeroCard();
            hero.setUuid(UUID.randomUUID());
            hero.setOwnerId(player.getUuid());
            hero.setCardName("Herói " + i + " de " + name);
            hero.setZoneId(ZoneId.BENCH);
            hero.setEstaAtivo(true);
            hero.setEstaVivo(true);
            heroList.add(hero);
        }

        deck.setHeroList(heroList);

        List<Card> cardList = new ArrayList<>();
        // Adicionando 10 cartas de ação/magia no deck principal
        for (int i = 1; i <= 10; i++) {
            Card card = new Card();
            card.setUuid(UUID.randomUUID());
            card.setOwnerId(player.getUuid());
            card.setCardName("Carta de Ação " + i + " de " + name);
            card.setZoneId(ZoneId.DECK);
            cardList.add(card);
        }
        deck.setCardList(cardList);

        player.setCurrentDeck(deck);
        return player;
    }

    private static String getActivePlayerName(GameEngine engine) {
        if (engine.getGameState().getActiveTurnPlayerId() == null) return "Desconhecido";
        if (engine.getGameState().getActiveTurnPlayerId().equals(engine.getPlayer1().getUuid())) {
            return engine.getPlayer1().getName();
        }
        return engine.getPlayer2().getName();
    }

    private static HeroCard getFirstAvailableHero(Player player) {
        return (HeroCard) player.getCurrentDeck().getHeroList().stream()
                .filter(c -> c.getZoneId() == ZoneId.BENCH && ((HeroCard)c).getEstaVivo() && ((HeroCard)c).getEstaAtivo())
                .findFirst()
                .orElse(null);
    }

    private static void playCardsFromHand(GameEngine engine, Player player, List<Card> playedCardsList) {
        Card cardToPlay = player.getCurrentDeck().getCardList().stream()
                .filter(c -> c.getZoneId() == ZoneId.HAND)
                .findFirst()
                .orElse(null);

        if (cardToPlay != null) {
            System.out.println("  -> " + player.getName() + " jogou: " + cardToPlay.getCardName());
            playedCardsList.add(cardToPlay);
            cardToPlay.setZoneId(ZoneId.RESOLVE); // Remove da mão e joga na mesa
        } else {
            System.out.println("  -> " + player.getName() + " não tem cartas na mão para jogar.");
        }
    }
}
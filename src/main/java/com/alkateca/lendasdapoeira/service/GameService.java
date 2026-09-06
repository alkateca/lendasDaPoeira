package com.alkateca.lendasdapoeira.service;

import com.alkateca.lendasdapoeira.DTO.CardDTO;
import com.alkateca.lendasdapoeira.DTO.GameStateDTO;
import com.alkateca.lendasdapoeira.engine.GameEngine;
import com.alkateca.lendasdapoeira.entity.Card;
import com.alkateca.lendasdapoeira.entity.Deck;
import com.alkateca.lendasdapoeira.entity.HeroCard;
import com.alkateca.lendasdapoeira.entity.Player;
import com.alkateca.lendasdapoeira.effects.CardFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GameService {

    private GameEngine engine;

    public void startNewGame() {
        // IDs fixos (Mockados) para baterem exatamente com o HTML!
        UUID p1Id = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID p2Id = UUID.randomUUID();

        // 1. Heróis do Jogador 1
        List<Card> heroListP1 = new ArrayList<>();
        HeroCard isenora = CardFactory.createIsenora(p1Id);
        isenora.setUuid(UUID.fromString("33333333-3333-3333-3333-333333333333")); // ID da Isenora do HTML
        heroListP1.add(isenora);

        HeroCard arqueiro = new HeroCard();
        arqueiro.setUuid(UUID.fromString("22222222-2222-2222-2222-222222222222")); // ID do Arqueiro do HTML
        arqueiro.setOwnerId(p1Id);
        arqueiro.setCardName("Arqueiro");
        heroListP1.add(arqueiro);

        // 2. Cartas da Mão (Deck) do Jogador 1
        List<Card> deckListP1 = new ArrayList<>();
        Card espada = CardFactory.createEspadaLonga(p1Id);
        espada.setUuid(UUID.fromString("44444444-4444-4444-4444-444444444444")); // ID da Espada do HTML
        deckListP1.add(espada);

        Card magia = new Card();
        magia.setUuid(UUID.fromString("55555555-5555-5555-5555-555555555555")); // ID da Bola de Fogo
        magia.setOwnerId(p1Id);
        magia.setCardName("Bola de Fogo");
        deckListP1.add(magia);

        Deck deckP1 = new Deck("deck1", deckListP1, heroListP1);
        Player player1 = new Player(p1Id, "José", deckP1);

        // 3. Jogador 2 (Oponente)
        List<Card> heroListP2 = new ArrayList<>();
        HeroCard orc = new HeroCard();
        orc.setUuid(UUID.fromString("66666666-6666-6666-6666-666666666666")); // ID do Orc
        orc.setOwnerId(p2Id);
        orc.setCardName("Orc Inimigo");
        heroListP2.add(orc);

        Deck deckP2 = new Deck("deck2", new ArrayList<>(), heroListP2);
        Player player2 = new Player(p2Id, "Oponente", deckP2);

        // 4. Instancia a Engine e dá o Start!
        this.engine = new GameEngine(player1, player2);
        this.engine.startGame(); // Vai disparar o GAME_START e parar em CHOICE

        // Força a compra das cartas para a mão do jogador para o teste
        engine.getCardsOnBoard().addAll(deckListP1);
        deckListP1.forEach(c -> c.setZoneId(com.alkateca.lendasdapoeira.enums.ZoneId.HAND));

        System.out.println("Partida iniciada no Servidor com UUIDs sincronizados com o Frontend!");
    }

    public GameEngine getEngine() {
        if (engine == null) {
            throw new IllegalStateException("A partida ainda não começou!");
        }
        return engine;
    }

    public GameStateDTO getGameStateAsDTO() {
        if (this.engine == null) {
            throw new IllegalStateException("O jogo não iniciou.");
        }

        GameStateDTO state = new GameStateDTO();
        state.setCurrentPhase(this.engine.getCurrentPhase().name());

        // Mapeia todas as cartas da mesa para DTOs
        List<CardDTO> dtos = this.engine.getCardsOnBoard().stream().map(card -> {
            CardDTO dto = new CardDTO();
            dto.setId(card.getUuid().toString());
            dto.setName(card.getCardName());
            dto.setType(card.getCardType().name());
            dto.setZone(card.getZoneId().name());
            dto.setOwnerId(card.getOwnerId().toString());

            // Se for Herói, extraímos os status reais de RPG
            if (card instanceof HeroCard) {
                HeroCard hero = (HeroCard) card;
                dto.setHp(hero.getVidaAtual());
                dto.setMaxHp(hero.getVidaMaxima());
                dto.setAttack(hero.getAtaque());
                dto.setDefense(hero.getDefesa());
                dto.setSpirit(hero.getEspirito());

                // Conta quantos itens estão na mesa anexados a este herói
                long attached = this.engine.getCardsOnBoard().stream()
                        .filter(c -> c.getZoneId() == com.alkateca.lendasdapoeira.enums.ZoneId.ATTACHED)
                        .filter(c -> hero.getUuid().equals(c.getAttachedToCardId()))
                        .count();
                dto.setAttachedCount((int) attached);
            }
            return dto;
        }).collect(Collectors.toList());

        state.setCardsOnBoard(dtos);
        return state;
    }
}
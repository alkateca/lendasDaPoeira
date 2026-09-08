package com.alkateca.lendasdapoeira.service;

import com.alkateca.lendasdapoeira.DTO.CardDTO;
import com.alkateca.lendasdapoeira.DTO.GameStateDTO;
import com.alkateca.lendasdapoeira.engine.GameEngine;
import com.alkateca.lendasdapoeira.entity.Card;
import com.alkateca.lendasdapoeira.entity.Deck;
import com.alkateca.lendasdapoeira.entity.HeroCard;
import com.alkateca.lendasdapoeira.entity.Player;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GameService {

    private GameEngine engine;

    public void startNewGame() {
        UUID p1Id = UUID.randomUUID();
        UUID p2Id = UUID.randomUUID();

        List<Card> heroListP1 = new ArrayList<>();
        List<Card> deckListP1 = new ArrayList<>();
        Deck deckP1 = new Deck("deck1", deckListP1, heroListP1);
        Player player1 = new Player(p1Id, "José", deckP1);

        List<Card> heroListP2 = new ArrayList<>();
        List<Card> deckListP2 = new ArrayList<>();
        Deck deckP2 = new Deck("deck2", deckListP2, heroListP2);
        Player player2 = new Player(p2Id, "Oponente", deckP2);


        this.engine = new GameEngine(player1, player2);
        this.engine.startGame();

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
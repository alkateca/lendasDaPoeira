package com.alkateca.lendasdapoeira.service;

import com.alkateca.lendasdapoeira.DTO.CardDTO;
import com.alkateca.lendasdapoeira.DTO.GameStateDTO;
import com.alkateca.lendasdapoeira.engine.CardFactory;
import com.alkateca.lendasdapoeira.engine.GameEngine;
import com.alkateca.lendasdapoeira.entity.Card;
import com.alkateca.lendasdapoeira.entity.Deck;
import com.alkateca.lendasdapoeira.entity.HeroCard;
import com.alkateca.lendasdapoeira.entity.Player;

import com.alkateca.lendasdapoeira.enums.TurnPhase;
import com.alkateca.lendasdapoeira.enums.ZoneId;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;

@Service
public class GameService {

    private Map<UUID, GameEngine> activeGames = new HashMap<>();
    private Map<UUID, UUID> playerToGameMap = new HashMap<>();
    private List<HeroCard> allHeroesDb = new ArrayList<>();
    private List<Card> allCardsDb = new ArrayList<>();

    @PostConstruct
    public void carregarBancoDeDados() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            // Lê os arquivos da pasta src/main/resources/
            InputStream heroisStream = new ClassPathResource("heros.json").getInputStream();
            InputStream cartasStream = new ClassPathResource("cards.json").getInputStream();

            this.allHeroesDb = mapper.readValue(heroisStream, new TypeReference<List<HeroCard>>(){});
            this.allCardsDb = mapper.readValue(cartasStream, new TypeReference<List<Card>>(){});

            System.out.println("📚 Banco de dados carregado: " + allHeroesDb.size() + " Heróis e " + allCardsDb.size() + " Cartas de Baralho.");
        } catch (Exception e) {
            System.err.println("⚠️ Erro ao desserializar os JSONs: " + e.getMessage());
        }
    }

    public void createGame(Player p1, Player p2) {
        Deck deckP1 = p1.getCurrentDeck();
        Player player1 = new Player(p1.getUuid(), p1.getName(), deckP1);

        Deck deckP2 = p2.getCurrentDeck();
        Player player2 = new Player(p2.getUuid(), p2.getName(), deckP2);

        GameEngine engine = new GameEngine(player1, player2);
        engine.startGame();

        UUID gameId = UUID.randomUUID();
        activeGames.put(gameId, engine);
        playerToGameMap.put(p1.getUuid(), gameId);
        playerToGameMap.put(p2.getUuid(), gameId);

        System.out.println("Nova partida iniciada! ID: " + gameId);
    }


    private Deck loadPlayerDeck(UUID ownerId, String deckId, List<String> heroNames, List<String> cardNames, List<String> extraNames) {
        List<Card> heroList = new ArrayList<>();
        List<Card> deckList = new ArrayList<>();
        List<Card> extraList = new ArrayList<>();

        for (String name : heroNames) {
            HeroCard baseHero = allHeroesDb.stream()
                    .filter(h -> h.getCardName().equals(name))
                    .findFirst()
                    .orElse(null);

            if (baseHero != null) {
                HeroCard clone = cloneHero(baseHero);
                clone.setOwnerId(ownerId);
                CardFactory.buildCardLogic(clone); // Injeta passivas (se houver)
                heroList.add(clone);
            }
        }

        for (String name : cardNames) {
            Card baseCard = allCardsDb.stream()
                    .filter(c -> c.getCardName().equals(name))
                    .findFirst()
                    .orElse(null);

            if (baseCard != null) {
                Card clone = cloneCard(baseCard);
                clone.setOwnerId(ownerId);
                CardFactory.buildCardLogic(clone); // Injeta comportamento jogável
                deckList.add(clone);
            }
        }

        if (extraNames != null) {
            for (String name : extraNames) {
                Card baseCard = allCardsDb.stream()
                        .filter(c -> c.getCardName().equals(name))
                        .findFirst()
                        .orElse(null);

                if (baseCard != null) {
                    Card clone = cloneCard(baseCard);
                    clone.setOwnerId(ownerId);
                    CardFactory.buildCardLogic(clone);
                    extraList.add(clone);
                }
            }
        }

        return new Deck(deckId, deckList, heroList, extraList);
    }

    /**
     * Clona um HeroCard a partir da base do JSON para que os jogadores
     * tenham instâncias únicas na memória durante a partida.
     */
    private HeroCard cloneHero(HeroCard base) {
        HeroCard clone = new HeroCard();
        clone.setUuid(UUID.randomUUID());
        clone.setCardName(base.getCardName());
        clone.setCardType(base.getCardType());
        clone.setColor(base.getColor());
        clone.setDescricao(base.getDescricao());
        clone.setEspirito(base.getEspirito());
        clone.setAtaque(base.getAtaque());
        clone.setDefesa(base.getDefesa());
        clone.setVidaMaxima(base.getVidaMaxima());
        clone.setVidaAtual(base.getVidaMaxima());
        clone.setMaxWeaponSlots(base.getMaxWeaponSlots());
        clone.setReducaoDano(base.getReducaoDano());
        clone.setDanoBonus(base.getDanoBonus());
        clone.setVulnerabilidade(base.getVulnerabilidade());
        clone.setEstaVivo(base.getEstaVivo());
        clone.setEstaAtivo(base.getEstaAtivo());

        // Copia as listas (importante instanciar um novo ArrayList)
        clone.setRacas(base.getRacas() != null ? new ArrayList<>(base.getRacas()) : new ArrayList<>());
        clone.setOrdem(base.getOrdem() != null ? new ArrayList<>(base.getOrdem()) : new ArrayList<>());
        clone.setClasse(base.getClasse() != null ? new ArrayList<>(base.getClasse()) : new ArrayList<>());
        clone.setAfinidades(base.getAfinidades() != null ? new ArrayList<>(base.getAfinidades()) : new ArrayList<>());

        return clone;
    }

    /**
     * Clona uma Carta padrão (Ação, Item, Feitiço).
     */
    private Card cloneCard(Card base) {
        Card clone = new Card();
        clone.setUuid(UUID.randomUUID());
        clone.setCardName(base.getCardName());
        clone.setCardType(base.getCardType());
        clone.setColor(base.getColor());
        clone.setItemType(base.getItemType());
        clone.setEmpunhadura(base.getEmpunhadura());
        clone.setDescricao(base.getDescricao());

        // Copia as listas de restrições de uso
        clone.setRacas(base.getRacas() != null ? new ArrayList<>(base.getRacas()) : new ArrayList<>());
        clone.setOrdem(base.getOrdem() != null ? new ArrayList<>(base.getOrdem()) : new ArrayList<>());
        clone.setClasse(base.getClasse() != null ? new ArrayList<>(base.getClasse()) : new ArrayList<>());
        clone.setAfinidades(base.getAfinidades() != null ? new ArrayList<>(base.getAfinidades()) : new ArrayList<>());

        return clone;
    }

    public UUID getGameIdByPlayerId(UUID playerId) {
        return playerToGameMap.get(playerId);
    }

    public GameEngine getEngineByPlayerId(UUID playerId) {
        UUID gameId = getGameIdByPlayerId(playerId);
        if (gameId == null) {
            throw new IllegalStateException("Jogador não está em uma partida!");
        }
        return activeGames.get(gameId);
    }

    public GameStateDTO getGameStateAsDTO(UUID playerId) {
        GameEngine engine = getEngineByPlayerId(playerId);
        if (engine == null) {
            throw new IllegalStateException("O jogo não iniciou.");
        }

        GameStateDTO state = new GameStateDTO();
        state.setCurrentPhase(engine.getCurrentPhase().name());
        state.setActivePlayerId(engine.getActivePlayerId());
        state.setResolvingCardId(engine.getResolvingCardId());
        state.setWaitingEffectChoice(engine.isWaitingEffectChoice());
        state.setPendingChoiceType(engine.getPendingChoiceType());
        state.setHasPendingEffects(!engine.getResolutionQueue().isEmpty());

        List<CardDTO> dtos = engine.getCardsOnBoard().stream().map(card -> {
            CardDTO dto = new CardDTO();
            dto.setId(card.getUuid().toString());
            dto.setOwnerId(card.getOwnerId().toString());
            dto.setZone(card.getZoneId().name());

            // Esconder nome/tipo se a carta estiver na zona de resolução em preparação e não for minha
            if (engine.getCurrentPhase() == TurnPhase.PREPARATION 
                && card.getZoneId() == ZoneId.RESOLVE 
                && !card.getOwnerId().equals(playerId)) {
                
                dto.setName("Carta Oculta");
                dto.setType("UNKNOWN");
                dto.setDescription("Oponente está preparando algo...");
            } else {
                dto.setName(card.getCardName());
                dto.setType(card.getCardType().name());
                dto.setDescription(card.getDescricao());
            }

            if (card instanceof HeroCard) {
                HeroCard hero = (HeroCard) card;
                dto.setHp(hero.getVidaAtual());
                dto.setMaxHp(hero.getVidaMaxima());
                dto.setAttack(hero.getAtaque());
                dto.setDefense(hero.getDefesa());
                dto.setSpirit(hero.getEspirito());
                dto.setActive(hero.getEstaAtivo());

                long attached = engine.getCardsOnBoard().stream()
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
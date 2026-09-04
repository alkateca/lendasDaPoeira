package com.alkateca.lendasdapoeira;

import com.alkateca.lendasdapoeira.effects.CardFactory;
import com.alkateca.lendasdapoeira.engine.GameEngine;
import com.alkateca.lendasdapoeira.entity.Card;
import com.alkateca.lendasdapoeira.entity.Deck;
import com.alkateca.lendasdapoeira.entity.HeroCard;
import com.alkateca.lendasdapoeira.entity.Player;
import com.alkateca.lendasdapoeira.effects.Effect;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Main {
    public static void main(String[] args) {

        // 1. GERAMOS OS IDs DOS JOGADORES PRIMEIRO
        UUID p1Id = UUID.randomUUID();
        UUID p2Id = UUID.randomUUID();

        // 2. Montando o Jogador 1 (Usando a Factory para a Isenora!)
        List<Card> heroListP1 = new ArrayList<>();

        // ---> AQUI ENTRA A FACTORY <---
        heroListP1.add(CardFactory.createIsenora(p1Id));

        // Os outros heróis aliados (mocks) que vão receber o buff da Isenora
        heroListP1.add(createMockHero("Arqueiro Aliado", p1Id, new ArrayList<>()));
        heroListP1.add(createMockHero("Guerreiro Aliado", p1Id, new ArrayList<>()));

        List<Card> deckListP1 = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            deckListP1.add(createMockNormalCard("Magia J1 - " + i, p1Id));
        }

        Deck deckP1 = new Deck("deck1", deckListP1, heroListP1);
        Player player1 = new Player(p1Id, "José", deckP1);

        // 3. Montando o Jogador 2 (Oponente)
        List<Card> heroListP2 = new ArrayList<>();
        heroListP2.add(createMockHero("Inimigo 1", p2Id, new ArrayList<>()));
        heroListP2.add(createMockHero("Inimigo 2", p2Id, new ArrayList<>()));
        heroListP2.add(createMockHero("Inimigo 3", p2Id, new ArrayList<>()));

        List<Card> deckListP2 = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            deckListP2.add(createMockNormalCard("Magia J2 - " + i, p2Id));
        }

        Deck deckP2 = new Deck("deck2", deckListP2, heroListP2);
        Player player2 = new Player(p2Id, "Oponente", deckP2);

        // 4. Rodando o Jogo!
        GameEngine engine = new GameEngine(player1, player2);

        // O startGame() deve chamar a fase GAME_START internamente, engatilhando a Isenora
        engine.startGame();
    }

    // --- MÉTODOS AUXILIARES ATUALIZADOS ---

    // Cria um Herói (Terá Ataque/Defesa e receberá os Buffs)
    private static HeroCard createMockHero(String name, UUID ownerId, List<Effect> effects) {
        HeroCard hero = new HeroCard();
        hero.setUuid(UUID.randomUUID());
        hero.setOwnerId(ownerId);
        hero.setCardName(name);

        // Status base ruins só para vermos o buff funcionando
        hero.setAtaque(1);
        hero.setDefesa(1);
        hero.setEspirito(1);

        hero.setEffects(effects);
        return hero;
    }

    // Cria uma carta normal (Magia/Ação) - Não é um HeroCard, logo não receberá buff!
    private static Card createMockNormalCard(String name, UUID ownerId) {
        Card card = new Card();
        card.setUuid(UUID.randomUUID());
        card.setOwnerId(ownerId);
        card.setCardName(name);
        card.setEffects(new ArrayList<>());
        return card;
    }
}
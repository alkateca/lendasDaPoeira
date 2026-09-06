package com.alkateca.lendasdapoeira.effects;

import com.alkateca.lendasdapoeira.engine.GameEngine;
import com.alkateca.lendasdapoeira.entity.Card;
import com.alkateca.lendasdapoeira.entity.HeroCard;
import com.alkateca.lendasdapoeira.enums.TurnPhase;
import com.alkateca.lendasdapoeira.enums.ZoneId;

import java.util.UUID;

public class EquipBuffEffect implements Effect {

    private int buffEsp;
    private int buffAtk;
    private int buffDef;

    // Construtor genérico: você pode passar valores positivos para itens bons,
    // ou valores negativos se for uma Teurgia/Veneno jogada no inimigo!
    public EquipBuffEffect(int buffEsp, int buffAtk, int buffDef) {
        this.buffEsp = buffEsp;
        this.buffAtk = buffAtk;
        this.buffDef = buffDef;
    }

    @Override
    public boolean predicate(Card sourceCard, TurnPhase turnPhase) {
        // Engatilha o efeito na fase de RESOLUTION
        return turnPhase == TurnPhase.RESOLUTION;
    }

    @Override
    public void resolve(GameEngine engine, Card sourceCard) {

        // 1. Descobre quem é o alvo que definimos na fase de preparação
        UUID targetHeroId = sourceCard.getAttachedToCardId();

        if (targetHeroId == null) {
            System.out.println("Erro: A carta [" + sourceCard.getCardName() + "] não tem um alvo definido!");
            return;
        }

        // 2. Procura o herói alvo na mesa (cardsOnBoard)
        Card targetCard = engine.getCardsOnBoard().stream()
                .filter(c -> c.getUuid().equals(targetHeroId))
                .findFirst()
                .orElse(null);

        // 3. Aplica os atributos
        if (targetCard instanceof HeroCard) {
            HeroCard hero = (HeroCard) targetCard;

            hero.setEspirito(hero.getEspirito() + this.buffEsp);
            hero.setAtaque(hero.getAtaque() + this.buffAtk);
            hero.setDefesa(hero.getDefesa() + this.buffDef);

            // 4. Oficializa a carta como um equipamento fixo na mesa
            sourceCard.setZoneId(ZoneId.ATTACHED);

            System.out.println(">>> EFEITO RESOLVIDO: [" + sourceCard.getCardName() +
                    "] foi equipada em [" + hero.getCardName() +
                    "] (+" + buffAtk + " Atk / +" + buffDef + " Def / +" + buffEsp + " Esp)");
        }
    }
}
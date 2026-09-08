package com.alkateca.lendasdapoeira.effects.buffs;

import com.alkateca.lendasdapoeira.effects.Effect;
import com.alkateca.lendasdapoeira.engine.GameEngine;
import com.alkateca.lendasdapoeira.entity.Card;
import com.alkateca.lendasdapoeira.entity.HeroCard;
import com.alkateca.lendasdapoeira.enums.TurnPhase;

import java.util.UUID;

public class EquipBuffEffect implements Effect {

    private int buffEsp;
    private int buffAtk;
    private int buffDef;

    public EquipBuffEffect(int buffEsp, int buffAtk, int buffDef) {
        this.buffEsp = buffEsp;
        this.buffAtk = buffAtk;
        this.buffDef = buffDef;
    }

    @Override
    public boolean predicate(Card sourceCard, TurnPhase turnPhase) {
        return turnPhase == TurnPhase.RESOLUTION;
    }

    @Override
    public void resolve(GameEngine engine, Card sourceCard) {

        UUID targetHeroId = sourceCard.getAttachedToCardId();
        if (targetHeroId == null) return;

        HeroCard hero = (HeroCard) engine.getCardsOnBoard().stream()
                .filter(c -> c.getUuid().equals(targetHeroId))
                .findFirst()
                .orElse(null);

        if (hero == null) return;

        engine.equipItem(sourceCard, hero);

        hero.setEspirito(hero.getEspirito() + this.buffEsp);
        hero.setAtaque(hero.getAtaque() + this.buffAtk);
        hero.setDefesa(hero.getDefesa() + this.buffDef);

        System.out.println(">>> EFEITO RESOLVIDO: [" + sourceCard.getCardName() +
                "] foi equipado em [" + hero.getCardName() +
                "] (+" + buffAtk + " Atk / +" + buffDef + " Def / +" + buffEsp + " Esp)");
    }

    @Override
    public void onUnequip(GameEngine engine, Card sourceCard) {

        UUID targetHeroId = sourceCard.getAttachedToCardId();
        if (targetHeroId == null) return;

        HeroCard hero = (HeroCard) engine.getCardsOnBoard().stream()
                .filter(c -> c.getUuid().equals(targetHeroId))
                .findFirst()
                .orElse(null);

        if (hero != null) {
            hero.setEspirito(hero.getEspirito() - this.buffEsp);
            hero.setAtaque(hero.getAtaque() - this.buffAtk);
            hero.setDefesa(hero.getDefesa() - this.buffDef);

            System.out.println("<<< EFEITO REMOVIDO: [" + sourceCard.getCardName() +
                    "] foi desequipado de [" + hero.getCardName() + "]. Status subtraídos.");
        }
    }
}
package com.alkateca.lendasdapoeira.effects.debuffs;

import com.alkateca.lendasdapoeira.effects.Effect;
import com.alkateca.lendasdapoeira.engine.GameEngine;
import com.alkateca.lendasdapoeira.entity.Card;
import com.alkateca.lendasdapoeira.entity.HeroCard;
import com.alkateca.lendasdapoeira.enums.TurnPhase;
import com.alkateca.lendasdapoeira.enums.ZoneId;

import java.util.UUID;

public class TemporaryCombatDebuffEffect implements Effect {

    private int debuffAtk;
    private int debuffDef;
    private int debuffEsp;

    private boolean isDebuffApplied = false;

    // Guardamos o quanto realmente conseguimos subtrair para devolver exatamente o mesmo valor
    private int appliedDebuffAtk = 0;
    private int appliedDebuffDef = 0;
    private int appliedDebuffEsp = 0;

    public TemporaryCombatDebuffEffect(int debuffAtk, int debuffDef, int debuffEsp) {
        this.debuffAtk = debuffAtk;
        this.debuffDef = debuffDef;
        this.debuffEsp = debuffEsp;
    }

    @Override
    public boolean predicate(Card sourceCard, TurnPhase turnPhase) {
        return turnPhase == TurnPhase.RESOLUTION || turnPhase == TurnPhase.COMBAT_END;
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

        // --- PARTE 1: APLICANDO O DEBUFF (Fase de Resolução) ---
        if (engine.getCurrentPhase() == TurnPhase.RESOLUTION && !isDebuffApplied) {

            // Calcula o quanto podemos tirar sem deixar o status menor que zero
            this.appliedDebuffAtk = Math.min(hero.getAtaque(), this.debuffAtk);
            this.appliedDebuffDef = Math.min(hero.getDefesa(), this.debuffDef);
            this.appliedDebuffEsp = Math.min(hero.getEspirito(), this.debuffEsp);

            hero.setAtaque(hero.getAtaque() - this.appliedDebuffAtk);
            hero.setDefesa(hero.getDefesa() - this.appliedDebuffDef);
            hero.setEspirito(hero.getEspirito() - this.appliedDebuffEsp);

            isDebuffApplied = true;

            System.out.println("☠️ DEBUFF TEMPORÁRIO: [" + hero.getCardName() + "] perdeu -" + appliedDebuffAtk + " Atk para este combate!");
        }

        // --- PARTE 2: REMOVENDO O DEBUFF (Fim do Combate) ---
        else if (engine.getCurrentPhase() == TurnPhase.COMBAT_END && isDebuffApplied) {

            // Devolve exatamente o que foi tirado
            hero.setAtaque(hero.getAtaque() + this.appliedDebuffAtk);
            hero.setDefesa(hero.getDefesa() + this.appliedDebuffDef);
            hero.setEspirito(hero.getEspirito() + this.appliedDebuffEsp);

            isDebuffApplied = false;

            sourceCard.setAttachedToCardId(null);
            sourceCard.setZoneId(ZoneId.DISCARD);

            System.out.println("💨 O Debuff Temporário expirou. [" + hero.getCardName() + "] recuperou seus status normais.");
        }
    }
}
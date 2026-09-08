package com.alkateca.lendasdapoeira.effects.buffs;

import com.alkateca.lendasdapoeira.effects.Effect;
import com.alkateca.lendasdapoeira.engine.GameEngine;
import com.alkateca.lendasdapoeira.entity.Card;
import com.alkateca.lendasdapoeira.entity.HeroCard;
import com.alkateca.lendasdapoeira.enums.TurnPhase;
import com.alkateca.lendasdapoeira.enums.ZoneId;

import java.util.UUID;

public class TemporaryCombatBuffEffect implements Effect {

    private int tempAtk;
    private int tempDef;
    private int tempEsp;

    // Flag de segurança parecida com o seu "self.efeitoDoTurno = true" do Lua
    private boolean isBuffApplied = false;

    public TemporaryCombatBuffEffect(int tempAtk, int tempDef, int tempEsp) {
        this.tempAtk = tempAtk;
        this.tempDef = tempDef;
        this.tempEsp = tempEsp;
    }

    @Override
    public boolean predicate(Card sourceCard, TurnPhase turnPhase) {
        // A MÁGICA AQUI: Esta carta reage a DUAS fases do jogo!
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

        // --- PARTE 1: APLICANDO O BUFF (Fase de Resolução) ---
        if (engine.getCurrentPhase() == TurnPhase.RESOLUTION && !isBuffApplied) {

            hero.setAtaque(hero.getAtaque() + this.tempAtk);
            hero.setDefesa(hero.getDefesa() + this.tempDef);
            hero.setEspirito(hero.getEspirito() + this.tempEsp);

            isBuffApplied = true;

            // A carta fica colada no herói provisoriamente para a Engine saber que ela está ativa no combate
            sourceCard.setZoneId(ZoneId.ATTACHED);

            System.out.println("⚡ EFEITO TEMPORÁRIO: [" + hero.getCardName() + "] recebeu +" + tempAtk + " Atk para este combate!");
        }

        // --- PARTE 2: REMOVENDO O BUFF (Fim do Combate) ---
        else if (engine.getCurrentPhase() == TurnPhase.COMBAT_END && isBuffApplied) {

            hero.setAtaque(hero.getAtaque() - this.tempAtk);
            hero.setDefesa(hero.getDefesa() - this.tempDef);
            hero.setEspirito(hero.getEspirito() - this.tempEsp);

            isBuffApplied = false;

            // O feitiço perdeu o efeito, então desgruda do herói e vai pro cemitério
            sourceCard.setAttachedToCardId(null);
            sourceCard.setZoneId(ZoneId.DISCARD);

            System.out.println("💨 O Efeito Temporário expirou. [" + hero.getCardName() + "] perdeu os +" + tempAtk + " Atk.");
        }
    }
}
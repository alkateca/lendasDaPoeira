package com.alkateca.lendasdapoeira.effects;

import com.alkateca.lendasdapoeira.effects.Effect;
import com.alkateca.lendasdapoeira.engine.GameEngine;
import com.alkateca.lendasdapoeira.entity.Card;
import com.alkateca.lendasdapoeira.entity.HeroCard;
import com.alkateca.lendasdapoeira.enums.TurnPhase;

public class MagicDamageEffect implements Effect {

    private int baseDamage;

    public MagicDamageEffect(int baseDamage) {
        this.baseDamage = baseDamage;
    }

    @Override
    public boolean predicate(Card sourceCard, TurnPhase turnPhase) {
        // Equivale ao seu "efeitoFinalDoCombate" do Lua
        return turnPhase == TurnPhase.COMBAT_END;
    }

    @Override
    public void resolve(GameEngine engine, Card sourceCard) {
        // Como o sourceCard é genérico, precisamos dizer ao Java que ele é um HeroCard
        HeroCard atacante = (HeroCard) sourceCard;

        // A Engine precisa nos dar o herói ativo do oponente para batermos nele
        HeroCard inimigo = engine.getActiveEnemyHero(atacante.getOwnerId());

        if (inimigo != null) {
            // Tradução exata da sua fórmula matemática do Lua
            int danoMagico = (this.baseDamage + atacante.getDanoBonus()) -
                    (inimigo.getEspirito() + inimigo.getReducaoDano() + inimigo.getVulnerabilidade());

            if (danoMagico > 0) {
                inimigo.setVidaAtual(inimigo.getVidaAtual() - danoMagico);
                System.out.println(atacante.getCardName() + " causou " + danoMagico + " de dano mágico!");
            }
        }
    }
}
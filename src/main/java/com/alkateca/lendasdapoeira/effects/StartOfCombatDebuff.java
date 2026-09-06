package com.alkateca.lendasdapoeira.effects;

import com.alkateca.lendasdapoeira.engine.GameEngine;
import com.alkateca.lendasdapoeira.entity.Card;
import com.alkateca.lendasdapoeira.entity.HeroCard;
import com.alkateca.lendasdapoeira.enums.TurnPhase;

public class StartOfCombatDebuff implements Effect {

    private int debuffEsp;
    private int debuffAtk;
    private int debuffDef;
    private int debuffHp;
    private int debuffMaxHp;

    public StartOfCombatDebuff(int debuffEsp, int debuffAtk, int debuffDef, int debuffHp, int debuffMaxHp) {
        this.debuffEsp = debuffEsp;
        this.debuffAtk = debuffAtk;
        this.debuffDef = debuffDef;
        this.debuffHp = debuffHp;
        this.debuffMaxHp = debuffMaxHp;
    }

    @Override
    public boolean predicate(Card sourceCard, TurnPhase turnPhase) {
        return turnPhase == TurnPhase.COMBAT;
    }

    @Override
    public void resolve(GameEngine engine, Card sourceCard) {

        if (!(sourceCard instanceof HeroCard)) {
            return;
        }

        HeroCard atacante = (HeroCard) sourceCard;
        HeroCard inimigo = engine.getActiveEnemyHero(atacante.getOwnerId());

        if (inimigo != null) {

            // Trava 2: Math.max garante que o status nunca será menor que zero
            int esp = Math.max(0, inimigo.getEspirito() - this.debuffEsp);
            inimigo.setEspirito(esp);

            int atk = Math.max(0, inimigo.getAtaque() - this.debuffAtk);
            inimigo.setAtaque(atk);

            int def = Math.max(0, inimigo.getDefesa() - this.debuffDef);
            inimigo.setDefesa(def);

            // Vida pode ficar abaixo de zero (o que significa que o herói morre),
            // então não precisa de Math.max para a vida atual.
            int hp = inimigo.getVidaAtual() - this.debuffHp;
            inimigo.setVidaAtual(hp);

            int maxHp = Math.max(1, inimigo.getVidaMaxima() - this.debuffMaxHp);
            inimigo.setVidaMaxima(maxHp);

            System.out.println(">>> EFEITO ATIVADO: [" + atacante.getCardName() + "] enfraqueceu [" + inimigo.getCardName() + "]!");
        }

    }

}

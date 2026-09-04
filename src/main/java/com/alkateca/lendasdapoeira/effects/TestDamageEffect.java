package com.alkateca.lendasdapoeira.effects;

import com.alkateca.lendasdapoeira.engine.GameEngine;
import com.alkateca.lendasdapoeira.entity.Card;
import com.alkateca.lendasdapoeira.enums.TurnPhase;

public class TestDamageEffect implements Effect {

    private int damageAmount;

    public TestDamageEffect(int damageAmount) {
        this.damageAmount = damageAmount;
    }

    @Override
    public boolean predicate(Card sourceCard, TurnPhase turnPhase) {
        // O efeito diz para a carta: "Eu só funciono na fase RESOLUTION"
        return turnPhase == TurnPhase.RESOLUTION;
    }

    @Override
    public void resolve(GameEngine gameEngine, Card sourceCard) {
        // Quando a Fila de Resolução chamar este método, ele executará a ação
        System.out.println(">>> EFEITO ATIVADO: A carta [" + sourceCard.getCardName() + "] causou " + damageAmount + " de dano!");
    }
}
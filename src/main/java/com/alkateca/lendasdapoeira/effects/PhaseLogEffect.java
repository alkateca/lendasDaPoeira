package com.alkateca.lendasdapoeira.effects;

import com.alkateca.lendasdapoeira.engine.GameEngine;
import com.alkateca.lendasdapoeira.entity.Card;
import com.alkateca.lendasdapoeira.enums.TurnPhase;

public class PhaseLogEffect implements Effect {

    private TurnPhase triggerPhase;
    private String logMessage;

    public PhaseLogEffect(TurnPhase triggerPhase, String logMessage) {
        this.triggerPhase = triggerPhase;
        this.logMessage = logMessage;
    }

    @Override
    public boolean predicate(Card sourceCard, TurnPhase turnPhase) {
        // Só engatilha se a fase atual for igual a fase que configuramos no construtor
        return turnPhase == this.triggerPhase;
    }

    @Override
    public void resolve(GameEngine gameEngine, Card sourceCard) {
        System.out.println(">>> [" + triggerPhase + " EFEITO] Carta [" + sourceCard.getCardName() + "]: " + logMessage);
    }
}
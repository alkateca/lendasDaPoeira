package com.alkateca.lendasdapoeira.effects;

import com.alkateca.lendasdapoeira.engine.GameEngine;
import com.alkateca.lendasdapoeira.entity.Card;
import com.alkateca.lendasdapoeira.enums.TurnPhase;

public interface Effect {

    boolean predicate(Card sourceCard, TurnPhase turnPhase);

    void resolve(GameEngine gameEngine, Card sourceCard);

}


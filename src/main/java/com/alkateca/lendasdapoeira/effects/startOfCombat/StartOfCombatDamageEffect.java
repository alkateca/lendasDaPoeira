package com.alkateca.lendasdapoeira.effects.startOfCombat;

import com.alkateca.lendasdapoeira.effects.Effect;
import com.alkateca.lendasdapoeira.engine.GameEngine;
import com.alkateca.lendasdapoeira.entity.Card;
import com.alkateca.lendasdapoeira.enums.TurnPhase;


public class StartOfCombatDamageEffect implements Effect {

    @Override
    public boolean predicate(Card sourceCard, TurnPhase turnPhase){

        return turnPhase.equals(TurnPhase.COMBAT);

    }

    @Override
    public void resolve(GameEngine gameEngine, Card sourceCard){



    };

}

package com.alkateca.lendasdapoeira.effects;

import com.alkateca.lendasdapoeira.engine.GameEngine;
import com.alkateca.lendasdapoeira.entity.Card;
import com.alkateca.lendasdapoeira.entity.HeroCard;
import com.alkateca.lendasdapoeira.enums.TurnPhase;

public class GlobalBuffEffect implements Effect {

    private int buff;

    public GlobalBuffEffect(int buff) {
        this.buff = buff;
    }

    @Override
    public boolean predicate(Card sourceCard, TurnPhase turnPhase) {
        return turnPhase == TurnPhase.GAME_START;
    }

    @Override
    public void resolve(GameEngine gameEngine, Card sourceCard) {
        System.out.println(">>> EFEITO ATIVADO: [" + sourceCard.getCardName() + "] irradiou sua benção!");

        // 1. Itera por todas as cartas na mesa
        for (Card cardOnBoard : gameEngine.getCardsOnBoard()) {

            // 2. Verifica se a carta pertence ao mesmo jogador
            if (cardOnBoard.getOwnerId().equals(sourceCard.getOwnerId())) {

                // 3. Verifica se a carta é um Herói (pois apenas heróis têm Ataque/Defesa)
                if (cardOnBoard instanceof HeroCard) {

                    // Converte a carta genérica para HeroCard
                    HeroCard ally = (HeroCard) cardOnBoard;

                    // Aplica o buff nos atributos de RPG
                    ally.setEspirito(ally.getEspirito() + buff);
                    ally.setAtaque(ally.getAtaque() + buff);
                    ally.setDefesa(ally.getDefesa() + buff);

                    System.out.println("    -> O aliado [" + ally.getCardName() + "] recebeu os buffs! (Ataque agora é: " + ally.getAtaque() + ")");
                }
            }
        }
    }

}

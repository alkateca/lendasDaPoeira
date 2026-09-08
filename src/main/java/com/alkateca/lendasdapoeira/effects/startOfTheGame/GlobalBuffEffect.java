package com.alkateca.lendasdapoeira.effects.startOfTheGame;

import com.alkateca.lendasdapoeira.effects.Effect;
import com.alkateca.lendasdapoeira.engine.GameEngine;
import com.alkateca.lendasdapoeira.entity.Card;
import com.alkateca.lendasdapoeira.entity.HeroCard;
import com.alkateca.lendasdapoeira.enums.TurnPhase;

public class GlobalBuffEffect implements Effect {

    private int buffEsp;
    private int buffAtk;
    private int buffDef;

    public GlobalBuffEffect(int buffEsp, int buffAtk, int buffDef) {
        this.buffEsp = buffEsp;
        this.buffAtk = buffAtk;
        this.buffDef = buffDef;
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
                    ally.setEspirito(ally.getEspirito() + buffEsp);
                    ally.setAtaque(ally.getAtaque() + buffAtk);
                    ally.setDefesa(ally.getDefesa() + buffDef);

                    System.out.println("    -> O aliado [" + ally.getCardName() + "] recebeu os buffs! (Ataque agora é: " + ally.getEspirito() + ")" +
                            "(Ataque agora é: " + ally.getAtaque() + ")" +
                            "(Ataque agora é: " + ally.getDefesa() + ")");
                }
            }
        }
    }

}

package com.alkateca.lendasdapoeira.effects.buffs;

import com.alkateca.lendasdapoeira.effects.Effect;
import com.alkateca.lendasdapoeira.engine.GameEngine;
import com.alkateca.lendasdapoeira.entity.Card;
import com.alkateca.lendasdapoeira.entity.HeroCard;
import com.alkateca.lendasdapoeira.enums.*;

import java.util.UUID;

public class HealAndBuffSpecificGroupEffect implements Effect {

    private int buffEsp;
    private int buffAtk;
    private int buffDef;
    private int buffMaxHp;
    private Ordem ordem;
    private Colors colors;
    private Classe classe;
    private Afinidade afinidade;


    public HealAndBuffSpecificGroupEffect(int buffEsp, int buffAtk, int buffDef, int buffMaxHp,  Ordem ordem, Colors colors, Afinidade afinidade, Classe classe) {
        this.buffEsp = buffEsp;
        this.buffAtk = buffAtk;
        this.buffDef = buffDef;
        this.buffMaxHp = buffMaxHp;
        this.ordem = ordem;
        this.colors = colors;
        this.classe = classe;
        this.afinidade = afinidade;
    }

    @Override
    public boolean predicate(Card sourceCard, TurnPhase turnPhase) {
        return turnPhase == TurnPhase.RESOLUTION;
    }

    @Override
    public void resolve(GameEngine engine, Card sourceCard) {

        UUID targetHeroId = sourceCard.getAttachedToCardId();

        if (targetHeroId == null) {
            System.out.println("Erro: A carta [" + sourceCard.getCardName() + "] não tem um alvo definido!");
            return;
        }

        Card targetCard = engine.getCardsOnBoard().stream()
                .filter(c -> c.getUuid().equals(targetHeroId))
                .findFirst()
                .orElse(null);

        if (targetCard instanceof HeroCard) {
            HeroCard hero = (HeroCard) targetCard;

            boolean matchAfinidade = this.afinidade != null && hero.getAfinidades() != null && hero.getAfinidades().contains(this.afinidade);
            boolean matchCasta = this.ordem != null && hero.getOrdem() != null && hero.getOrdem().contains(this.ordem);
            boolean matchClasse = this.classe != null && hero.getClasse() != null && hero.getClasse().contains(this.classe);
            boolean matchColor = this.colors != null && this.colors.equals(hero.getColor());

            if (matchAfinidade || matchCasta || matchClasse || matchColor) {

                hero.setEspirito(hero.getEspirito() + this.buffEsp);
                hero.setAtaque(hero.getAtaque() + this.buffAtk);
                hero.setDefesa(hero.getDefesa() + this.buffDef);

                hero.setVidaMaxima(hero.getVidaMaxima() + this.buffMaxHp);
                hero.setVidaAtual(hero.getVidaAtual() + this.buffMaxHp);


                System.out.println(">>> EFEITO RESOLVIDO: [" + sourceCard.getCardName() +
                        "] foi equipada em [" + hero.getCardName() +
                        "] (+" + buffAtk + " Atk / +" + buffDef + " Def / +" + buffEsp + " Esp)");
            }


        }
    }
}

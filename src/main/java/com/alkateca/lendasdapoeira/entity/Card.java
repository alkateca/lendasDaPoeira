package com.alkateca.lendasdapoeira.entity;

import com.alkateca.lendasdapoeira.effects.Effect;
import com.alkateca.lendasdapoeira.engine.GameEngine;
import com.alkateca.lendasdapoeira.engine.ResolutionQueue;
import com.alkateca.lendasdapoeira.enums.*;
import lombok.*;

import java.awt.*;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Card {

    private UUID uuid;
    private String cardName;
    private CardType cardType;
    private Color color;
    private UUID ownerId;
    private ZoneId zoneId;
    private ItemType itemType = ItemType.NENHUM;
    private int empunhadura = 0;

    private List<Raca> racas;
    private List<Afinidade> afinidades;
    private List<Ordem> ordem;
    private List<Classe> classe;


    private UUID attachedToCardId;

    private Integer turnsRemaining;

    private List<Effect> effects;

    private String descricao;

    public void onPhaseChange(TurnPhase currentPhase, ResolutionQueue queue) {

        if (effects == null) return;

        for (Effect effect : effects) {
            if (effect.predicate(this, currentPhase)) {
                queue.enqueue(effect, this);
            }
        }
    }

    public boolean canBePlayedBy(HeroCard targetHero) {

        // 1. EXCEÇÃO ABSOLUTA: Constructos e Itens
        // Se a carta é um Item e o herói é um Constructo, ignora todas as outras restrições.
        if (this.cardType == CardType.ITEM) {
            if (targetHero.getRacas() != null && targetHero.getRacas().contains("Constructo")) {
                return true;
            }
        }

        // 2. RESTRIÇÕES PADRÃO (Ordem, Raça, Classe)
        // Ações (ACTION) e demais cartas não-isentas passam por aqui normalmente.
        if (this.ordem != null && !this.ordem.isEmpty()) {
            boolean matchOrdem = this.ordem.stream().anyMatch(o -> targetHero.getOrdem().contains(o));
            if (!matchOrdem) return false;
        }

        if (this.racas != null && !this.racas.isEmpty()) {
            boolean matchRaca = this.racas.stream().anyMatch(r -> targetHero.getRacas().contains(r));
            if (!matchRaca) return false;
        }

        if (this.classe != null && !this.classe.isEmpty()) {
            boolean matchClasse = this.classe.stream().anyMatch(c -> targetHero.getClasse().contains(c));
            if (!matchClasse) return false;
        }

        // 3. REGRAS ESPECÍFICAS POR TIPO DE CARTA

        // Teurgias: Exigem estritamente a classe Transformador
        if (this.cardType == CardType.THEURGY) {
            if (targetHero.getClasse() == null || !targetHero.getClasse().contains("Transformador")) {
                return false;
            }
        }

        // Magias: Exigem Afinidade, com exceção para Goblins
        if (this.cardType == CardType.SPELL) {

            // Goblins jogam magias sem limite de afinidade
            if (targetHero.getRacas() != null && targetHero.getRacas().contains("Goblin")) {
                return true;
            }

            // Emissores e demais heróis precisam respeitar a afinidade da magia
            if (this.afinidades != null && !this.afinidades.isEmpty()) {
                boolean matchAfinidade = this.afinidades.stream().anyMatch(a -> targetHero.getAfinidades().contains(a));
                if (!matchAfinidade) {
                    return false;
                }
            }
        }

        // Se chegou até aqui (como as cartas ACTION que passaram no bloco 2), a jogada é válida.
        return true;
    }

}

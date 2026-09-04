package com.alkateca.lendasdapoeira.entity;

import com.alkateca.lendasdapoeira.effects.Effect;
import com.alkateca.lendasdapoeira.engine.GameEngine;
import com.alkateca.lendasdapoeira.engine.ResolutionQueue;
import com.alkateca.lendasdapoeira.enums.CardType;
import com.alkateca.lendasdapoeira.enums.TurnPhase;
import lombok.*;

import com.alkateca.lendasdapoeira.enums.ZoneId;
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
    private UUID ownerId;
    private ZoneId zoneId;


    private List<Effect> effects;

    public void onPhaseChange(TurnPhase currentPhase, ResolutionQueue queue) {

        if (effects == null) return;

        for (Effect effect : effects) {
            if (effect.predicate(this, currentPhase)) {
                queue.enqueue(effect, this);
            }
        }
    }

}

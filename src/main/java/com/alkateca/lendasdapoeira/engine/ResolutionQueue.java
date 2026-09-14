package com.alkateca.lendasdapoeira.engine;

import com.alkateca.lendasdapoeira.effects.Effect;
import com.alkateca.lendasdapoeira.entity.Card;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.LinkedList;
import java.util.Queue;

public class ResolutionQueue {

    private Queue<PendingEffect> queue = new LinkedList<>();

    @Data
    @AllArgsConstructor
    private static class PendingEffect {
        private Effect effect;
        private Card sourceCard;
    }

    public void enqueue(Effect effect, Card sourceCard) {
        queue.add(new PendingEffect(effect, sourceCard));
    }

    public void resolveNext(GameEngine engine) {
        if (!queue.isEmpty()) {
            PendingEffect pending = queue.peek();
            engine.setResolvingCardId(pending.getSourceCard().getUuid());
            
            pending.getEffect().resolve(engine, pending.getSourceCard());
            
            if (!engine.isWaitingEffectChoice()) {
                queue.poll();
                if (queue.isEmpty()) {
                    engine.setResolvingCardId(null);
                }
            }
        } else {
            engine.setResolvingCardId(null);
        }
    }

    public void resolveAll(GameEngine engine) {
        while (!queue.isEmpty()) {
            PendingEffect pending = queue.poll();
            pending.getEffect().resolve(engine, pending.getSourceCard());
        }
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }
}
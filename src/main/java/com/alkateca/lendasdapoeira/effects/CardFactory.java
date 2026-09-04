package com.alkateca.lendasdapoeira.effects;

import com.alkateca.lendasdapoeira.entity.HeroCard;
import com.alkateca.lendasdapoeira.enums.CardType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CardFactory {

    public static HeroCard createIsenora(UUID ownerId) {
        HeroCard isenora = new HeroCard();
        isenora.setUuid(UUID.randomUUID());
        isenora.setOwnerId(ownerId); // O MAIS IMPORTANTE: Define de quem é a carta
        isenora.setCardName("Isenora, Santa das Laminas");

        // Atributos base
        isenora.setAtaque(5);
        isenora.setDefesa(1);
        isenora.setEspirito(2);
        isenora.setVidaMaxima(13);
        isenora.setVidaAtual(13);

        // Adiciona o Efeito de Início de Partida (Buff de +1 em tudo)
        List<Effect> efeitos = new ArrayList<>();
        efeitos.add(new GlobalBuffEffect(1));
        isenora.setEffects(efeitos);

        return isenora;
    }
}
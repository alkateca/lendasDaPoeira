package com.alkateca.lendasdapoeira.effects;

import com.alkateca.lendasdapoeira.entity.Card;
import com.alkateca.lendasdapoeira.entity.HeroCard;
import com.alkateca.lendasdapoeira.enums.CardType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CardFactory {

    public static Card createEspadaLonga(UUID ownerId) {
        Card espada = new Card();
        espada.setUuid(UUID.randomUUID());
        espada.setOwnerId(ownerId);
        espada.setCardName("Espada Longa");
        espada.setCardType(CardType.ITEM);
        espada.setTurnsRemaining(2); // Vai durar 2 turnos e quebrar

        List<Effect> efeitos = new ArrayList<>();

        // Concede 0 Esp, +3 Atk, 0 Def
        efeitos.add(new EquipBuffEffect(0, 0, 3));

        espada.setEffects(efeitos);
        return espada;
    }

    public static HeroCard createIsenora(UUID ownerId) {
        HeroCard isenora = new HeroCard();
        isenora.setUuid(UUID.randomUUID());
        isenora.setOwnerId(ownerId); // O MAIS IMPORTANTE: Define de quem é a carta
        isenora.setCardName("Isenora, Santa das Laminas");

        // Atributos base
        isenora.setEspirito(2);
        isenora.setAtaque(5);
        isenora.setDefesa(1);
        isenora.setVidaMaxima(13);
        isenora.setVidaAtual(13);

        // Adiciona o Efeito de Início de Partida (Buff de +1 em tudo)
        List<Effect> efeitos = new ArrayList<>();
        efeitos.add(new GlobalBuffEffect(1,1,1));
        efeitos.add(new EndOfCombatMagicalDamage(5));
        isenora.setEffects(efeitos);

        return isenora;
    }
}
package com.alkateca.lendasdapoeira.DTO;

import lombok.Data;
import java.util.UUID;

@Data
public class PlayerActionDTO {
    private UUID playerId;

    // Ex: "CHOOSE_HERO", "PLAY_CARD", "SET_READY", "DISCARD_CARD"
    private String actionType;

    // A carta que está sendo usada (ex: a Espada Longa da mão, ou a Isenora no banco)
    private UUID sourceCardId;

    // O alvo da ação (pode ser nulo, por exemplo, ao clicar em "Pronto")
    private UUID targetCardId;

    // Cartas preparadas para a fase de resolução
    private java.util.List<UUID> stagedCards;
}

package com.alkateca.lendasdapoeira.DTO;

import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class PlayerSetupDTO {
    private UUID playerId;
    private String playerName;

    private List<String> heroCardIds;
    private List<String> deckCardIds;
}

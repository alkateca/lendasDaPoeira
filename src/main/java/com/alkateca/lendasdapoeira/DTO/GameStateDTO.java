package com.alkateca.lendasdapoeira.DTO;

import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class GameStateDTO {
    private String currentPhase;
    private UUID activePlayerId;
    private List<CardDTO> cardsOnBoard;
}
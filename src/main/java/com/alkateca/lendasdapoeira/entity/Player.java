package com.alkateca.lendasdapoeira.entity;

import com.alkateca.lendasdapoeira.enums.TurnPhase;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Player {

    private UUID uuid;
    private String name;

    //private List<Deck> decks;

    //private Integer currency;

    private Deck currentDeck;

//    private Integer elo;

    //private Integer energy;

    //private TurnPhase turnPhase;



}

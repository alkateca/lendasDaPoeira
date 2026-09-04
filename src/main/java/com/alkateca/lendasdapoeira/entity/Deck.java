package com.alkateca.lendasdapoeira.entity;

import lombok.*;

import java.util.Collections;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Deck {

    private String deckName;

    private List<Card> cardList;
    private List<Card> heroList;

    public void shuffleDeck(){
        Collections.shuffle(cardList);
    }

}

package com.alkateca.lendasdapoeira.engine;

import com.alkateca.lendasdapoeira.effects.Effect;
import com.alkateca.lendasdapoeira.entity.Card;

import java.util.ArrayList;
import java.util.List;

public class CardFactory {


    public static Card buildCardLogic(Card rawCardFromJson) {

        List<Effect> effects = new ArrayList<>();

        switch (rawCardFromJson.getCardName()) {

            case "Quebra de Armadura":

                break;
        }


        rawCardFromJson.setEffects(effects);

        return rawCardFromJson;
    }
}
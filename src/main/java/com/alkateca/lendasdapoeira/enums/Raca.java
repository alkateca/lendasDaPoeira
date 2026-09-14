package com.alkateca.lendasdapoeira.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Raca {
    HUMANO("Humano"),
    GOBLIN("Goblin"),
    CONSTRUCTO("Constructo"),
    QUIMERA("Quimera"),
    ZUMBI("Zumbi"),
    CRISTAL("Cristal"),
    CAVALEIRO("Cavaleiro"),
    GUARDIOES_DO_TRONO("Guardiões do Trono");

    private final String nomeFormatado;

    Raca(String nomeFormatado) {
        this.nomeFormatado = nomeFormatado;
    }

    @JsonValue
    public String getNomeFormatado() {
        return nomeFormatado;
    }
}
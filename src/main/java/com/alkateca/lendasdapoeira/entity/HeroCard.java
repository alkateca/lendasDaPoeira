package com.alkateca.lendasdapoeira.entity;

import com.alkateca.lendasdapoeira.enums.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class HeroCard extends Card{

    private int espirito;
    private int ataque;
    private int defesa;
    private int vidaMaxima;
    private int vidaAtual;
    private int reducaoDano;
    private int danoBonus;
    private int vulnerabilidade;

    private List<Raca> racas;
    private List<Afinidade> afinidades;
    private List<Casta> casta;
    private List<Classe> classe;

    private Boolean estaVivo;
    private Boolean estaAtivo;

    private String descricao;


}

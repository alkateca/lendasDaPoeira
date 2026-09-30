package com.alkateca.lendasdapoeira.engine;

import com.alkateca.lendasdapoeira.effects.Effect;
import com.alkateca.lendasdapoeira.effects.startOfCombat.StartOfCombatMagicDamageEffect;
import com.alkateca.lendasdapoeira.entity.Card;

import java.util.ArrayList;
import java.util.List;

public class CardFactory {

    public static Card buildCardLogic(Card rawCardFromJson) {

        // Garante que a lista de efeitos nunca seja nula
        List<Effect> effects = new ArrayList<>();

        if (rawCardFromJson.getCardName() == null) {
            rawCardFromJson.setEffects(effects);
            return rawCardFromJson;
        }

        switch (rawCardFromJson.getCardName()) {

            // ==========================================
            // HERÓIS
            // ==========================================
            case "Alucinação Cintlante":
            case "Esquadrão Goblin":
            case "Rei Goblin":
            case "Traidor Goblin":
            case "Rainha Goblin":
            case "Quimera Carniceira":
            case "Necromante das Areias":
            case "Isenora, Santa das Laminas":
                break;
            case "Moyra, Aprendiz da Santa":
                effects.add(new StartOfCombatMagicDamageEffect(3));
                break;
            case "Naelis, Grande Artesã de Cristais":
                 break;
            case "Alka, Lutador do Grupo dos Heróis":
            case "Leone, Clériga do Grupo dos Heróis":
            case "Enamor,O Grande Cavaleiro":
                break;
            case "Ageus, Constructo Cristalino":
                break;
            case "Cavaleiro Onear":
                break;
            case "Moyra, Santa das Laminas":
                break;
            case "Heróis Lendários dos Goblin":
            case "Kael, Domador de Feras":
            case "Behemoth-29A":
            case "Rapinária-7B3":
            case "Kael, Cavaleiro Bestial":
            case "Hati, Constructo Gélido":
            case "Skoll, Espreitador Solar":
            case "Fenrir, o Grande Devorador":
            case "Oráculo Real":
            case "Portador da Chama":
            case "Constructo Fantasmagórico Guardião Perpétuo":
            case "Xia, Caçador de Bestas":
            case "Asahum, Caçadora de Bestas":
            case "Constructo de Combate, Arsenal Vivo":
            case "Xamã das cinzas":
            case "Alix, Dragão Flamejante":
            case "Constructo das Chamas, Sif":
                // TODO: Adicionar efeitos de Herói
                break;

            // ==========================================
            // ITENS (Armas, Joias, Ferramentas, etc)
            // ==========================================
            case "Quimera":
                break;
            case "Broche de Cristal":
                break;
            case "Lamina de Cristal":
                break;
            case "Dragão de Cristal":
                break;
            case "Homunculo Carniceiro":
            case "Quimera Negra":
            case "Dragast":
                break;
            case "Fragmento Afiado":
                break;
            case "Rapieira de Cristal":
                break;
            case "Garra espectral":
            case "Machado de Gelo":
            case "Grande Machado Sombrio":
            case "Artefato Cortante":
            case "Sica":
            case "Sino do Caçador":
            case "Arpão terrestre":
            case "Bomba de fumaça":
            case "Tesouro Cristalino dos Caçadores":
            case "Peitoral de brasas":
            case "Escudo ardente":
                // TODO: Adicionar efeitos de Equipamento
                break;

            // ==========================================
            // FEITIÇOS (Spells)
            // ==========================================
            case "Bola de fogo":
            case "Maestria com Chamas":
            case "Estatica":
            case "Para-raios":
            case "Atmosfera Pesada":
            case "Vendaval Arcano":
            case "Quebra!":
            case "Barreira de Gelo":
            case "Mordida Glacial":
            case "Contra Ataque":
                break;
            case "Ataque Mágico":
                break;
            case "Golpes Pesados":
                break;
            case "Massacre Cristalino":
                break;
            case "Ponto Final":
                break;
            case "Auto Defesa Magica":
                break;
            case "Muralha de Fogo":
            case "Cauterizar":
                // TODO: Adicionar efeitos Mágicos
                break;

            // ==========================================
            // AÇÕES (Actions)
            // ==========================================
            case "Disciplina":
            case "Ração de emergencia":
            case "Determinação Cristalina":
                break;
            case "Convocação":
            case "Mirar na cabeça":
            case "Ritos Fúnebres":
            case "Exumação":
            case "Banir!":
            case "Impulso de Poder":
                break;
            case "Tristeza":
            case "Luz da Lua":
            case "Afie suas Lâminas":
            case "Estilo Secreto do Caçador":
            case "Corte seus Pescoços":
                // TODO: Adicionar efeitos de Ação
                break;

            // ==========================================
            // TEURGIAS (Theurgies)
            // ==========================================
            case "Risada Sarcástica":
            case "Alma em Chamas":
            case "Fortaleça sua Fé":
            case "Saque Rápido":
            case "Ordem Divina":
                // TODO: Adicionar efeitos de Teurgia
                break;

            default:
                // Cartas sem nome mapeado passarão por aqui sem adicionar efeitos.
                break;
        }

        // Injeta a lista de efeitos na carta
        rawCardFromJson.setEffects(effects);

        return rawCardFromJson;
    }
}
package com.alkateca.lendasdapoeira.controller;

import com.alkateca.lendasdapoeira.DTO.GameStateDTO;
import com.alkateca.lendasdapoeira.DTO.PlayerActionDTO;
import com.alkateca.lendasdapoeira.DTO.PlayerSetupDTO;
import com.alkateca.lendasdapoeira.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/game")
@CrossOrigin("*")
public class GameController {

    @Autowired
    private GameService gameService;

    @PostMapping("/setup")
    public GameStateDTO setupPlayer(@RequestBody PlayerSetupDTO setup) {

        gameService.startNewGame();
        return gameService.getGameStateAsDTO();
    }


    @PostMapping("/action")
    public GameStateDTO performAction(@RequestBody PlayerActionDTO action) {

        switch (action.getActionType()) {

            case "CHOOSE_HERO":
                gameService.getEngine().chooseHeroForCombat(action.getPlayerId(), action.getSourceCardId());
                break;

            case "PLAY_CARD":
                gameService.getEngine().playCard(action.getPlayerId(), action.getSourceCardId(), action.getTargetCardId());
                break;

            case "SET_READY":
                gameService.getEngine().setPlayerReady(action.getPlayerId());
                break;

            default:
                throw new IllegalArgumentException("Tipo de ação inválida ou não implementada: " + action.getActionType());
        }

        return gameService.getGameStateAsDTO();
    }

    @GetMapping("/state")
    public GameStateDTO getState() {
        return gameService.getGameStateAsDTO();
    }
}
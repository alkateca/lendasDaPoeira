package com.alkateca.lendasdapoeira.controller;

import com.alkateca.lendasdapoeira.DTO.GameStateDTO;
import com.alkateca.lendasdapoeira.DTO.PlayerActionDTO;
import com.alkateca.lendasdapoeira.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping("/api/game")
@CrossOrigin(origins = "*") // Libera o CORS para o seu frontend (React, Vanilla JS, etc.) conseguir fazer as requisiÃ§Ãµes
public class GameController {

    @Autowired
    private GameService gameService;



}

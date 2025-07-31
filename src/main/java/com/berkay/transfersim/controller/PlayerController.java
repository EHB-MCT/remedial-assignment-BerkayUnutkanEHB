package com.berkay.transfersim.controller;

import com.berkay.transfersim.model.Player;
import com.berkay.transfersim.repository.PlayerRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/players")
@CrossOrigin
public class PlayerController {

    private final PlayerRepository playerRepository;

    public PlayerController(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @GetMapping
    public List<Player> getAllPlayers() {
        return playerRepository.findAll();
    }

    @PostMapping
    public Player addPlayer(@RequestBody Player player) {
        return playerRepository.save(player);
    }
}

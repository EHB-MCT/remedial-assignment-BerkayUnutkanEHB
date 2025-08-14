package com.berkay.transfersim.controller;

import com.berkay.transfersim.model.Player;
import com.berkay.transfersim.repository.PlayerRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/players")
@CrossOrigin
public class PlayerController {
    private final PlayerRepository repo;

    public PlayerController(PlayerRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Player> all() {
        return repo.findAll();
    }

    @GetMapping("/byClub/{club}")
    public List<Player> byClub(@PathVariable String club) {
        return repo.findByClub(club);
    }

    @PostMapping
    public Player create(@RequestBody Player p) {
        if (p.getName() == null || p.getName().isBlank()) {
            throw new RuntimeException("Player.name is required");
        }
        if (p.getMarketValue() < 0) p.setMarketValue(0);
        return repo.save(p);
    }
}

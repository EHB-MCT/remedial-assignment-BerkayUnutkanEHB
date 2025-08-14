package com.berkay.transfersim.controller;

import com.berkay.transfersim.model.Club;
import com.berkay.transfersim.repository.ClubRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clubs")
@CrossOrigin
public class ClubController {
    private final ClubRepository repo;

    public ClubController(ClubRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Club> all() {
        return repo.findAll();
    }

    @GetMapping("/{name}")
    public Club byName(@PathVariable String name) {
        return repo.findByName(name).orElseThrow(() -> new RuntimeException("Club not found: " + name));
    }

    @PostMapping
    public Club create(@RequestBody Club club) {
        if (club.getName() == null || club.getName().isBlank()) {
            throw new RuntimeException("Club.name is required");
        }
        if (club.getBudget() < 0) club.setBudget(0);
        return repo.save(club);
    }

    @PutMapping("/{name}/budget/{amount}")
    public Club updateBudget(@PathVariable String name, @PathVariable double amount) {
        var c = repo.findByName(name).orElseThrow(() -> new RuntimeException("Club not found: " + name));
        c.setBudget(amount);
        return repo.save(c);
    }

    @PostMapping("/{name}/players")
    public Club addPlayer(@PathVariable String name, @RequestBody String playerName) {
        var club = repo.findByName(name).orElseThrow(() -> new RuntimeException("Club not found: " + name));
        club.getPlayers().add(playerName);
        return repo.save(club);
    }

    @PostMapping("/{name}/simulateTick")
    public Club simulateTick(@PathVariable String name) {
        var club = repo.findByName(name).orElseThrow(() -> new RuntimeException("Club not found: " + name));
        double income = club.getTicketRevenueRate() + club.getSponsorRevenueRate();
        club.setBudget(club.getBudget() + income);
        return repo.save(club);
    }
}

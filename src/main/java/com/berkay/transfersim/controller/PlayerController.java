package com.berkay.transfersim.controller;

import com.berkay.transfersim.model.Player;
import com.berkay.transfersim.repository.PlayerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

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

    @PutMapping("/{id}")
    public Player updatePlayer(@PathVariable String id, @RequestBody Player updatedPlayer) {
        return playerRepository.findById(id)
                .map(player -> {
                    player.setName(updatedPlayer.getName());
                    player.setMarketValue(updatedPlayer.getMarketValue());
                    player.setClub(updatedPlayer.getClub());
                    return playerRepository.save(player);
                })
                .orElseThrow(() -> new RuntimeException("Player not found with id " + id));
    }

    @DeleteMapping("/{id}")
    public String deletePlayer(@PathVariable String id) {
        if (playerRepository.existsById(id)) {
            playerRepository.deleteById(id);
            return "Player with ID " + id + " deleted successfully.";
        } else {
            throw new RuntimeException("Player not found with id " + id);
        }
    }

    // 1️⃣ Transfer endpoint
    @PostMapping("/transfer")
    public ResponseEntity<Player> transferPlayer(@RequestBody TransferRequest request) {
        Optional<Player> playerOpt = playerRepository.findById(request.getPlayerId());

        if (playerOpt.isPresent()) {
            Player player = playerOpt.get();
            player.setMarketValue(player.getMarketValue() + request.getTransferFee() * 0.05);
            player.setClub(request.getNewClub());

            Player updatedPlayer = playerRepository.save(player);
            return ResponseEntity.ok(updatedPlayer);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // 2️⃣ GET endpoint om spelers van een specifieke club op te halen
    @GetMapping("/club/{clubName}")
    public List<Player> getPlayersByClub(@PathVariable String clubName) {
        return playerRepository.findByClubIgnoreCase(clubName);
    }

    // Inner class voor transfer data
    public static class TransferRequest {
        private String playerId;
        private String newClub;
        private double transferFee;

        public String getPlayerId() {
            return playerId;
        }
        public void setPlayerId(String playerId) {
            this.playerId = playerId;
        }
        public String getNewClub() {
            return newClub;
        }
        public void setNewClub(String newClub) {
            this.newClub = newClub;
        }
        public double getTransferFee() {
            return transferFee;
        }
        public void setTransferFee(double transferFee) {
            this.transferFee = transferFee;
        }
    }
}

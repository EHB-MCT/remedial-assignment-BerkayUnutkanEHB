package com.berkay.transfersim.controller;

import com.berkay.transfersim.dto.TransferRequest;
import com.berkay.transfersim.model.Player;
import com.berkay.transfersim.repository.PlayerRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/transfers")
@CrossOrigin
public class TransferController {

    private final PlayerRepository playerRepository;

    public TransferController(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @PostMapping
    public Player transferPlayer(@RequestBody TransferRequest request) {
        Optional<Player> playerOpt = playerRepository.findById(request.getPlayerId());

        if (playerOpt.isEmpty()) {
            throw new RuntimeException("Speler niet gevonden");
        }

        Player player = playerOpt.get();
        player.setClub(request.getNewClub());

        // Marktwaarde aanpassen (gemiddelde van huidige waarde en transferprijs)
        double nieuweWaarde = (player.getMarketValue() + request.getTransferFee()) / 2;
        player.setMarketValue(nieuweWaarde);

        return playerRepository.save(player);
    }
}

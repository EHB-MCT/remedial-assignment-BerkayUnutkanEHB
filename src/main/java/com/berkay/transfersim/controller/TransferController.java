package com.berkay.transfersim.controller;

import com.berkay.transfersim.dto.TransferRequest;
import com.berkay.transfersim.model.Club;
import com.berkay.transfersim.model.Player;
import com.berkay.transfersim.model.Transfer;
import com.berkay.transfersim.repository.ClubRepository;
import com.berkay.transfersim.repository.PlayerRepository;
import com.berkay.transfersim.repository.TransferRepository;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/transfers")
@CrossOrigin
public class TransferController {

    private final PlayerRepository playerRepository;
    private final ClubRepository clubRepository;
    private final TransferRepository transferRepository;

    public TransferController(PlayerRepository playerRepository,
                              ClubRepository clubRepository,
                              TransferRepository transferRepository) {
        this.playerRepository = playerRepository;
        this.clubRepository = clubRepository;
        this.transferRepository = transferRepository;
    }

    /** Manual transfer via API (you already had this) */
    @PostMapping
    public Player transfer(@RequestBody @Valid TransferRequest request) {
        Player player = playerRepository.findById(request.getPlayerId())
                .orElseThrow(() -> new RuntimeException("Speler niet gevonden: " + request.getPlayerId()));

        String fromClubName = player.getClub();
        String toClubName = request.getNewClub();

        Club fromClub = clubRepository.findByName(fromClubName)
                .orElseThrow(() -> new RuntimeException("Verkopende club niet gevonden: " + fromClubName));
        Club toClub = clubRepository.findByName(toClubName)
                .orElseThrow(() -> new RuntimeException("Kopende club niet gevonden: " + toClubName));

        double fee = request.getTransferFee();
        if (fee < 0) throw new RuntimeException("Fee kan niet negatief zijn");
        if (toClub.getBudget() < fee) {
            throw new RuntimeException("Onvoldoende budget bij " + toClubName);
        }

        // Geldstromen
        toClub.setBudget(toClub.getBudget() - fee);
        fromClub.setBudget(fromClub.getBudget() + fee);
        clubRepository.save(toClub);
        clubRepository.save(fromClub);

        // Speler updaten
        player.setClub(toClubName);
        double nieuweWaarde = Math.max(1_000_000, (player.getMarketValue() * 0.6) + fee * 0.4);
        player.setMarketValue(nieuweWaarde);
        playerRepository.save(player);

        // Logboek
        Transfer t = new Transfer();
        t.setPlayerId(player.getId());
        t.setFromClub(fromClubName);
        t.setToClub(toClubName);
        t.setFee(fee);
        t.setCompletedAt(Instant.now());
        transferRepository.save(t);

        return player;
    }

    /** NEW: list all transfers (for grading / inspection) */
    @GetMapping
    public List<Transfer> all() {
        return transferRepository.findAll();
    }
}

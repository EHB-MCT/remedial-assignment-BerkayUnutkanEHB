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
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

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

    /** Manual transfer (jouw bestaande endpoint) */
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

        // Speler updaten (lichte waarde-aanpassing)
        player.setClub(toClubName);
        double nieuweWaarde = Math.max(1_000_000, (player.getMarketValue() * 0.6) + fee * 0.4);
        player.setMarketValue(nieuweWaarde);
        playerRepository.save(player);

        // Logboek
        Transfer t = new Transfer();
        t.setPlayerId(player.getId());
        t.setPlayerName(player.getName());
        t.setFromClub(fromClubName);
        t.setToClub(toClubName);
        t.setFee(fee);
        t.setCompletedAt(Instant.now());
        transferRepository.save(t);

        return player;
    }

    /** Alle transfers, nieuwste eerst */
    @GetMapping
    public List<Transfer> all() {
        List<Transfer> list = transferRepository.findAllByOrderByCompletedAtDesc();
        // fallback als completedAt null zou zijn
        list.sort(Comparator.comparing(Transfer::getCompletedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed());
        return list;
    }

    /** Laatste N transfers (default 10) */
    @GetMapping("/recent")
    public List<Transfer> recent(@RequestParam(name = "limit", defaultValue = "10") int limit) {
        List<Transfer> list = transferRepository.findAllByOrderByCompletedAtDesc();
        return list.stream().limit(Math.max(0, limit)).toList();
    }

    /** Transfers van/naar een club */
    @GetMapping("/byClub/{club}")
    public List<Transfer> byClub(@PathVariable String club) {
        return transferRepository.findByFromClubOrToClubOrderByCompletedAtDesc(club, club);
    }

    /** Transfers van een specifieke speler */
    @GetMapping("/byPlayer/{playerId}")
    public List<Transfer> byPlayer(@PathVariable String playerId) {
        return transferRepository.findByPlayerIdOrderByCompletedAtDesc(playerId);
    }

    /** “Pretty” logformaat als array van strings (lekker leesbaar in Postman) */
    @GetMapping("/pretty")
    public List<String> pretty(@RequestParam(name = "limit", defaultValue = "10") int limit) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                .withZone(ZoneId.systemDefault());
        return transferRepository.findAllByOrderByCompletedAtDesc()
                .stream()
                .limit(Math.max(0, limit))
                .map(t -> fmt.format(t.getCompletedAt()) + " — "
                        + (t.getPlayerName() != null ? t.getPlayerName() : t.getPlayerId())
                        + ": " + t.getFromClub() + " → " + t.getToClub()
                        + " | €" + formatMoney(t.getFee()))
                .toList();
    }

    private String formatMoney(double v) {
        // Engels locale voor 1,234,567.89
        return String.format(Locale.US, "%,.2f", v);
    }
}

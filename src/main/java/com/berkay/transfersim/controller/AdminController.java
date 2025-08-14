package com.berkay.transfersim.controller;

import com.berkay.transfersim.model.Club;
import com.berkay.transfersim.model.Player;
import com.berkay.transfersim.repository.ClubRepository;
import com.berkay.transfersim.repository.PlayerRepository;
import com.berkay.transfersim.repository.TransferRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin
public class AdminController {

    private final ClubRepository clubs;
    private final PlayerRepository players;
    private final TransferRepository transfers;

    public AdminController(ClubRepository clubs, PlayerRepository players, TransferRepository transfers) {
        this.clubs = clubs;
        this.players = players;
        this.transfers = transfers;
    }

    /** Alle data wissen (pas op!) */
    @DeleteMapping("/wipe")
    public String wipe() {
        transfers.deleteAll();
        players.deleteAll();
        clubs.deleteAll();
        return "All collections cleared.";
    }

    /** Basis seed toevoegen (2 clubs + 2 spelers) */
    @PostMapping("/seed")
    public String seed() {
        if (clubs.count() == 0) {
            var fener = new Club();
            fener.setName("Fenerbahce");
            fener.setLeague("Süper Lig");
            fener.setBudget(80_000_000);
            fener.setTicketRevenueRate(120_000);
            fener.setSponsorRevenueRate(250_000);

            var gala = new Club();
            gala.setName("Galatasaray");
            gala.setLeague("Süper Lig");
            gala.setBudget(70_000_000);
            gala.setTicketRevenueRate(110_000);
            gala.setSponsorRevenueRate(230_000);

            clubs.save(fener);
            clubs.save(gala);
        }

        if (players.count() == 0) {
            var p1 = new Player();
            p1.setName("Star FW");
            p1.setAge(26);
            p1.setPosition("FW");
            p1.setMarketValue(25_000_000);
            p1.setClub("Fenerbahce");

            var p2 = new Player();
            p2.setName("Playmaker MF");
            p2.setAge(27);
            p2.setPosition("MF");
            p2.setMarketValue(18_000_000);
            p2.setClub("Galatasaray");

            players.save(p1);
            players.save(p2);
        }
        return "Seed done.";
    }
}

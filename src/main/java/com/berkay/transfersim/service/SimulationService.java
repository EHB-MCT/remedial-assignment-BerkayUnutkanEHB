package com.berkay.transfersim.service;

import com.berkay.transfersim.model.Club;
import com.berkay.transfersim.model.Player;
import com.berkay.transfersim.repository.ClubRepository;
import com.berkay.transfersim.repository.PlayerRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
public class SimulationService {
    private final PlayerRepository playerRepository;
    private final ClubRepository clubRepository;
    private final Random rnd = new Random();

    public SimulationService(PlayerRepository playerRepository, ClubRepository clubRepository) {
        this.playerRepository = playerRepository;
        this.clubRepository = clubRepository;
    }

    /** Eén tick:
     *  - marktwaarde fluctuatie per speler
     *  - clubs krijgen inkomsten (ticket/sponsor)
     */
    public String tick() {
        List<Player> players = playerRepository.findAll();
        for (Player p : players) {
            // Fluctuatie ~ [-2%, +4%]
            double factor = 0.98 + rnd.nextDouble() * 0.06; // 0.98..1.04
            double newVal = Math.max(500_000, p.getMarketValue() * factor);
            p.setMarketValue(newVal);
        }
        playerRepository.saveAll(players);

        List<Club> clubs = clubRepository.findAll();
        for (Club c : clubs) {
            double income = c.getTicketRevenueRate() + c.getSponsorRevenueRate();
            c.setBudget(Math.max(0, c.getBudget() + income));
        }
        clubRepository.saveAll(clubs);

        return "Tick applied to " + players.size() + " players and " + clubs.size() + " clubs.";
    }
}

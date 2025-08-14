package com.berkay.transfersim.service;

import com.berkay.transfersim.model.Club;
import com.berkay.transfersim.model.Player;
import com.berkay.transfersim.model.Transfer;
import com.berkay.transfersim.repository.ClubRepository;
import com.berkay.transfersim.repository.PlayerRepository;
import com.berkay.transfersim.repository.TransferRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SimulationService {

    private final PlayerRepository playerRepository;
    private final ClubRepository clubRepository;
    private final TransferRepository transferRepository;
    private final Random rnd = new Random();

    // ======= Config via application.properties =======
    @Value("${app.sim.auto:true}")
    private boolean autoEnabled;

    @Value("${app.sim.applyIncome:true}")
    private boolean applyIncome;

    @Value("${app.sim.transferEnabled:true}")
    private boolean transferEnabled;

    @Value("${app.sim.factor.min:0.97}")
    private double factorMin; // symmetrisch rond 1.0

    @Value("${app.sim.factor.max:1.03}")
    private double factorMax;

    @Value("${app.sim.minPlayerValue:500000}")
    private double minPlayerValue;

    @Value("${app.sim.maxPlayerValue:250000000}")
    private double maxPlayerValue; // cap, voorkomt miljarden

    @Value("${app.sim.maxTransfersPerTick:3}")
    private int maxTransfersPerTick;

    @Value("${app.sim.transfer.mult.min:0.9}")
    private double transferMultMin;

    @Value("${app.sim.transfer.mult.max:1.2}")
    private double transferMultMax;

    public SimulationService(PlayerRepository playerRepository,
                             ClubRepository clubRepository,
                             TransferRepository transferRepository) {
        this.playerRepository = playerRepository;
        this.clubRepository = clubRepository;
        this.transferRepository = transferRepository;
    }

    /** Eén tick uitvoeren (handmatig of automatisch) */
    public String tick() {
        int nPlayers = updatePlayerValues();
        int nClubs = applyClubIncomeIfEnabled();
        int nTransfers = transferEnabled ? simulateTransfers() : 0;
        return "Tick applied to " + nPlayers + " players and " + nClubs + " clubs. Transfers: " + nTransfers + ".";
    }

    // ---- Spelerwaardes updaten (met cap en symmetrische fluctuatie) ----
    private int updatePlayerValues() {
        List<Player> players = playerRepository.findAll();
        for (Player p : players) {
            double oldVal = p.getMarketValue();
            double factor = factorMin + rnd.nextDouble() * (factorMax - factorMin);
            double newVal = clamp(Math.max(minPlayerValue, oldVal * factor), minPlayerValue, maxPlayerValue);
            p.setMarketValue(newVal);
            System.out.printf("[PLAYER] %s: %.2f → %.2f%n", p.getName(), oldVal, newVal);
        }
        playerRepository.saveAll(players);
        return players.size();
    }

    // ---- Club-inkomsten toepassen (optioneel) ----
    private int applyClubIncomeIfEnabled() {
        List<Club> clubs = clubRepository.findAll();
        if (!applyIncome) {
            // Alleen loggen dat we niets aanpassen
            for (Club c : clubs) {
                System.out.printf("[CLUB] %s budget (unchanged): %.2f%n", c.getName(), c.getBudget());
            }
            return clubs.size();
        }

        for (Club c : clubs) {
            double oldBudget = c.getBudget();
            double income = c.getTicketRevenueRate() + c.getSponsorRevenueRate();
            double newBudget = Math.max(0, oldBudget + income);
            c.setBudget(newBudget);
            System.out.printf("[CLUB] %s budget: %.2f → %.2f (+%.2f)%n", c.getName(), oldBudget, newBudget, income);
        }
        clubRepository.saveAll(clubs);
        return clubs.size();
    }

    // ---- Transfers simuleren (optioneel) ----
    private int simulateTransfers() {
        List<Club> clubs = clubRepository.findAll();
        if (clubs.size() < 2) return 0;

        Map<String, List<Player>> playersByClub = playerRepository.findAll()
                .stream()
                .collect(Collectors.groupingBy(Player::getClub));

        int transfersCount = 0;
        int attempts = 0;

        while (transfersCount < maxTransfersPerTick && attempts < 10) {
            attempts++;

            Club buyer = clubs.get(rnd.nextInt(clubs.size()));
            Club seller = clubs.get(rnd.nextInt(clubs.size()));
            if (buyer.getName().equals(seller.getName())) continue;

            List<Player> sellerPlayers = playersByClub.getOrDefault(seller.getName(), Collections.emptyList());
            if (sellerPlayers.isEmpty()) continue;
            Player target = sellerPlayers.get(rnd.nextInt(sellerPlayers.size()));

            double mult = transferMultMin + rnd.nextDouble() * (transferMultMax - transferMultMin);
            double fee = Math.max(minPlayerValue, target.getMarketValue() * mult);

            if (buyer.getBudget() < fee) continue;

            double buyerOld = buyer.getBudget();
            double sellerOld = seller.getBudget();

            buyer.setBudget(buyer.getBudget() - fee);
            seller.setBudget(seller.getBudget() + fee);
            clubRepository.save(buyer);
            clubRepository.save(seller);

            target.setClub(buyer.getName());
            // kleine waardecorrectie na transfer + cap
            double adjusted = (target.getMarketValue() * 0.6) + fee * 0.4;
            target.setMarketValue(clamp(adjusted, minPlayerValue, maxPlayerValue));
            playerRepository.save(target);

            sellerPlayers.remove(target);
            playersByClub.computeIfAbsent(buyer.getName(), k -> new ArrayList<>()).add(target);

            Transfer t = new Transfer();
            t.setPlayerId(target.getId());
            t.setPlayerName(target.getName());
            t.setFromClub(seller.getName());
            t.setToClub(buyer.getName());
            t.setFee(fee);
            t.setCompletedAt(Instant.now());
            transferRepository.save(t);

            transfersCount++;
            System.out.printf("[TRANSFER] %s: %s → %s | fee: %.2f | buyer %.2f→%.2f | seller %.2f→%.2f%n",
                    target.getName(), seller.getName(), buyer.getName(), fee,
                    buyerOld, buyer.getBudget(), sellerOld, seller.getBudget());
        }

        if (transfersCount == 0) {
            System.out.println("[TRANSFER] No transfers this tick (budget/selection constraints).");
        }
        return transfersCount;
    }

    private double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }

    /** Automatisch ticken – enkel actief als app.sim.auto=true */
    @Scheduled(fixedDelayString = "${app.sim.fixedDelayMs:10000}")
    public void autoTick() {
        if (!autoEnabled) return;
        String result = tick();
        System.out.println("[AUTO TICK] " + result);
    }
}

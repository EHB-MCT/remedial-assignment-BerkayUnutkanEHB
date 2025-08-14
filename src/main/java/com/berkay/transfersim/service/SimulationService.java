package com.berkay.transfersim.service;

import com.berkay.transfersim.model.Club;
import com.berkay.transfersim.model.Player;
import com.berkay.transfersim.model.Transfer;
import com.berkay.transfersim.repository.ClubRepository;
import com.berkay.transfersim.repository.PlayerRepository;
import com.berkay.transfersim.repository.TransferRepository;
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

    // Simulatie parameters
    private static final int MAX_TRANSFERS_PER_TICK = 3;
    private static final double MIN_TRANSFER_MULT = 0.9; // 90% van marktwaarde
    private static final double MAX_TRANSFER_MULT = 1.2; // 120% van marktwaarde
    private static final double MIN_PLAYER_VALUE = 500_000;

    public SimulationService(PlayerRepository playerRepository,
                             ClubRepository clubRepository,
                             TransferRepository transferRepository) {
        this.playerRepository = playerRepository;
        this.clubRepository = clubRepository;
        this.transferRepository = transferRepository;
    }

    /** Eén tick:
     *  - marktwaarde fluctuatie per speler
     *  - clubs krijgen inkomsten (ticket/sponsor)
     *  - voer 0..3 transfers uit (als budget/aanbod het toelaat)
     */
    public String tick() {
        int nPlayers = updatePlayerValues();
        int nClubs = applyClubIncome();
        int nTransfers = simulateTransfers();

        return "Tick applied to " + nPlayers + " players and " + nClubs + " clubs. Transfers: " + nTransfers + ".";
    }

    // ---- Player waarde update ----
    private int updatePlayerValues() {
        List<Player> players = playerRepository.findAll();
        for (Player p : players) {
            double oldVal = p.getMarketValue();
            double factor = 0.98 + rnd.nextDouble() * 0.06; // [-2%, +4%]
            double newVal = Math.max(MIN_PLAYER_VALUE, oldVal * factor);
            p.setMarketValue(newVal);
            System.out.printf("[PLAYER] %s: %.2f → %.2f%n", p.getName(), oldVal, newVal);
        }
        playerRepository.saveAll(players);
        return players.size();
    }

    // ---- Club inkomsten ----
    private int applyClubIncome() {
        List<Club> clubs = clubRepository.findAll();
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

    // ---- Transfer simulatie ----
    private int simulateTransfers() {
        List<Club> clubs = clubRepository.findAll();
        if (clubs.size() < 2) return 0;

        // Map clubName -> spelers
        Map<String, List<Player>> playersByClub = playerRepository.findAll()
                .stream()
                .collect(Collectors.groupingBy(Player::getClub));

        int transfersCount = 0;
        int attempts = 0;

        while (transfersCount < MAX_TRANSFERS_PER_TICK && attempts < 10) {
            attempts++;

            // kies koper en verkoper (verschillende clubs)
            Club buyer = clubs.get(rnd.nextInt(clubs.size()));
            Club seller = clubs.get(rnd.nextInt(clubs.size()));
            if (buyer.getName().equals(seller.getName())) continue;

            // kies speler uit verkoper
            List<Player> sellerPlayers = playersByClub.getOrDefault(seller.getName(), Collections.emptyList());
            if (sellerPlayers.isEmpty()) continue;
            Player target = sellerPlayers.get(rnd.nextInt(sellerPlayers.size()));

            // bereken transfersom
            double mult = MIN_TRANSFER_MULT + rnd.nextDouble() * (MAX_TRANSFER_MULT - MIN_TRANSFER_MULT);
            double fee = Math.max(MIN_PLAYER_VALUE, target.getMarketValue() * mult);

            // voldoende budget?
            if (buyer.getBudget() < fee) continue;

            // voer transfer door
            double buyerOld = buyer.getBudget();
            double sellerOld = seller.getBudget();

            buyer.setBudget(buyer.getBudget() - fee);
            seller.setBudget(seller.getBudget() + fee);
            clubRepository.save(buyer);
            clubRepository.save(seller);

            // update speler club
            target.setClub(buyer.getName());
            playerRepository.save(target);

            // update cache map
            sellerPlayers.remove(target);
            playersByClub.computeIfAbsent(buyer.getName(), k -> new ArrayList<>()).add(target);

            // log transfer record
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

    /** Automatisch elke 10 seconden uitvoeren – kun je uitzetten door deze methode te verwijderen of te commenten. */
    @Scheduled(fixedRate = 10_000)
    public void autoTick() {
        String result = tick();
        System.out.println("[AUTO TICK] " + result);
    }
}

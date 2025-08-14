package com.berkay.transfersim.controller;

import com.berkay.transfersim.model.Club;
import com.berkay.transfersim.model.Player;
import com.berkay.transfersim.repository.ClubRepository;
import com.berkay.transfersim.repository.PlayerRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin
public class AdminController {

    private final PlayerRepository playerRepository;
    private final ClubRepository clubRepository;

    public AdminController(PlayerRepository playerRepository, ClubRepository clubRepository) {
        this.playerRepository = playerRepository;
        this.clubRepository = clubRepository;
    }

    // Helpers
    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
    private static double roundToMillions(double v) {
        return Math.round(v / 1_000_000d) * 1_000_000d;
    }

    // ---------- PLAYERS ----------

    /** Schaal ALLE spelerswaardes met factor; optioneel clamp + afronden op miljoenen. */
    @PostMapping("/players/scale")
    public String scalePlayers(
            @RequestParam(name = "factor", defaultValue = "1.0") double factor,
            @RequestParam(name = "min", defaultValue = "500000") double min,
            @RequestParam(name = "max", defaultValue = "250000000") double max,
            @RequestParam(name = "roundMillions", defaultValue = "true") boolean roundMillions
    ) {
        List<Player> players = playerRepository.findAll();
        for (Player p : players) {
            double nv = p.getMarketValue() * factor;
            nv = clamp(nv, min, max);
            if (roundMillions) nv = roundToMillions(nv);
            p.setMarketValue(nv);
        }
        playerRepository.saveAll(players);
        return "Scaled " + players.size() + " players by factor=" + factor + " (clamp [" + min + "," + max + "])"
                + (roundMillions ? " with million rounding" : "");
    }

    /** Normaliseer ALLE spelerswaardes naar [min,max]; optioneel afronden op miljoenen. */
    @PostMapping("/players/normalize")
    public String normalizePlayers(
            @RequestParam(name = "min", defaultValue = "500000") double min,
            @RequestParam(name = "max", defaultValue = "250000000") double max,
            @RequestParam(name = "roundMillions", defaultValue = "true") boolean roundMillions
    ) {
        List<Player> players = playerRepository.findAll();
        for (Player p : players) {
            double nv = clamp(p.getMarketValue(), min, max);
            if (roundMillions) nv = roundToMillions(nv);
            p.setMarketValue(nv);
        }
        playerRepository.saveAll(players);
        return "Normalized " + players.size() + " players to [" + min + "," + max + "]"
                + (roundMillions ? " with million rounding" : "");
    }

    // ---------- CLUBS ----------

    /** Schaal ALLE clubbudgetten met factor; optioneel clamp + afronden op miljoenen. */
    @PostMapping("/clubs/scale")
    public String scaleClubs(
            @RequestParam(name = "factor", defaultValue = "1.0") double factor,
            @RequestParam(name = "min", required = false) Double min,
            @RequestParam(name = "max", required = false) Double max,
            @RequestParam(name = "roundMillions", defaultValue = "true") boolean roundMillions
    ) {
        List<Club> clubs = clubRepository.findAll();
        for (Club c : clubs) {
            double nb = c.getBudget() * factor;
            if (min != null || max != null) {
                double lo = (min == null ? Double.NEGATIVE_INFINITY : min);
                double hi = (max == null ? Double.POSITIVE_INFINITY : max);
                nb = clamp(nb, lo, hi);
            }
            if (roundMillions) nb = roundToMillions(nb);
            c.setBudget(nb);
        }
        clubRepository.saveAll(clubs);
        return "Scaled " + clubs.size() + " clubs by factor=" + factor
                + (min != null || max != null ? " with clamp" : "")
                + (roundMillions ? " and million rounding" : "");
    }

    /** Normaliseer ALLE clubbudgetten; optioneel afronden op miljoenen. */
    @PostMapping("/clubs/normalize")
    public String normalizeClubs(
            @RequestParam(name = "min", required = false) Double min,
            @RequestParam(name = "max", required = false) Double max,
            @RequestParam(name = "roundMillions", defaultValue = "true") boolean roundMillions
    ) {
        List<Club> clubs = clubRepository.findAll();
        for (Club c : clubs) {
            double nb = c.getBudget();
            if (min != null || max != null) {
                double lo = (min == null ? Double.NEGATIVE_INFINITY : min);
                double hi = (max == null ? Double.POSITIVE_INFINITY : max);
                nb = clamp(nb, lo, hi);
            }
            if (roundMillions) nb = roundToMillions(nb);
            c.setBudget(nb);
        }
        clubRepository.saveAll(clubs);
        return "Normalized " + clubs.size() + " clubs"
                + (min != null || max != null ? " to range [" + (min==null?"-inf":min) + "," + (max==null?"+inf":max) + "]" : "")
                + (roundMillions ? " with million rounding" : "");
    }
}

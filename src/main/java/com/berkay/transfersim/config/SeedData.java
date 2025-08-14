package com.berkay.transfersim.config;

import com.berkay.transfersim.model.Club;
import com.berkay.transfersim.model.Player;
import com.berkay.transfersim.repository.ClubRepository;
import com.berkay.transfersim.repository.PlayerRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SeedData {
    @Bean
    CommandLineRunner init(ClubRepository clubs, PlayerRepository players) {
        return args -> {
            if (clubs.count() == 0) {
                Club fener = new Club();
                fener.setName("Fenerbahce");
                fener.setLeague("Süper Lig");
                fener.setBudget(80_000_000);
                fener.setTicketRevenueRate(120_000);
                fener.setSponsorRevenueRate(250_000);

                Club gala = new Club();
                gala.setName("Galatasaray");
                gala.setLeague("Süper Lig");
                gala.setBudget(70_000_000);
                gala.setTicketRevenueRate(110_000);
                gala.setSponsorRevenueRate(230_000);

                clubs.save(fener);
                clubs.save(gala);
            }

            if (players.count() == 0) {
                Player p1 = new Player();
                p1.setName("Star FW");
                p1.setAge(26);
                p1.setPosition("FW");
                p1.setMarketValue(25_000_000);
                p1.setClub("Fenerbahce");

                Player p2 = new Player();
                p2.setName("Playmaker MF");
                p2.setAge(27);
                p2.setPosition("MF");
                p2.setMarketValue(18_000_000);
                p2.setClub("Galatasaray");

                players.save(p1);
                players.save(p2);
            }
        };
    }
}

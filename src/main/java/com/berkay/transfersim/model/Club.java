package com.berkay.transfersim.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document("clubs")
public class Club {
    @Id
    private String id;

    @Indexed(unique = true)
    private String name;

    private String league;
    private double budget;            // transferbudget
    private double ticketRevenueRate; // per tick
    private double sponsorRevenueRate;// per tick

    private List<String> players = new ArrayList<>();

    // getters/setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLeague() { return league; }
    public void setLeague(String league) { this.league = league; }

    public double getBudget() { return budget; }
    public void setBudget(double budget) { this.budget = budget; }

    public double getTicketRevenueRate() { return ticketRevenueRate; }
    public void setTicketRevenueRate(double ticketRevenueRate) { this.ticketRevenueRate = ticketRevenueRate; }

    public double getSponsorRevenueRate() { return sponsorRevenueRate; }
    public void setSponsorRevenueRate(double sponsorRevenueRate) { this.sponsorRevenueRate = sponsorRevenueRate; }

    public List<String> getPlayers() { return players; }
    public void setPlayers(List<String> players) { this.players = players; }
}

package com.berkay.transfersim.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document("transfers")
public class Transfer {
    @Id
    private String id;

    private String playerId;
    private String playerName;
    private String fromClub;
    private String toClub;
    private double fee;
    private Instant completedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPlayerId() { return playerId; }
    public void setPlayerId(String playerId) { this.playerId = playerId; }

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public String getFromClub() { return fromClub; }
    public void setFromClub(String fromClub) { this.fromClub = fromClub; }

    public String getToClub() { return toClub; }
    public void setToClub(String toClub) { this.toClub = toClub; }

    public double getFee() { return fee; }
    public void setFee(double fee) { this.fee = fee; }

    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
}

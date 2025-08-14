package com.berkay.transfersim.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class TransferRequest {
    @NotBlank
    private String playerId;

    @NotBlank
    private String newClub;

    @NotNull
    @Min(0)
    private Double transferFee;

    private Double proposedWage; // optioneel

    // getters/setters
    public String getPlayerId() { return playerId; }
    public void setPlayerId(String playerId) { this.playerId = playerId; }

    public String getNewClub() { return newClub; }
    public void setNewClub(String newClub) { this.newClub = newClub; }

    public Double getTransferFee() { return transferFee; }
    public void setTransferFee(Double transferFee) { this.transferFee = transferFee; }

    public Double getProposedWage() { return proposedWage; }
    public void setProposedWage(Double proposedWage) { this.proposedWage = proposedWage; }
}

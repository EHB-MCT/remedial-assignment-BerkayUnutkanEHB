package com.berkay.transfersim.dto;

public class TransferRequest {
    private String playerId;
    private String newClub;
    private double transferFee;

    public String getPlayerId() {
        return playerId;
    }
    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }
    public String getNewClub() {
        return newClub;
    }
    public void setNewClub(String newClub) {
        this.newClub = newClub;
    }
    public double getTransferFee() {
        return transferFee;
    }
    public void setTransferFee(double transferFee) {
        this.transferFee = transferFee;
    }
}

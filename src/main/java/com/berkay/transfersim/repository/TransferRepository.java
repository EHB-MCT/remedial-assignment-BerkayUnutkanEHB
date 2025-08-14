package com.berkay.transfersim.repository;

import com.berkay.transfersim.model.Transfer;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TransferRepository extends MongoRepository<Transfer, String> {
    List<Transfer> findAllByOrderByCompletedAtDesc();
    List<Transfer> findByFromClubOrToClubOrderByCompletedAtDesc(String fromClub, String toClub);
    List<Transfer> findByPlayerIdOrderByCompletedAtDesc(String playerId);
}

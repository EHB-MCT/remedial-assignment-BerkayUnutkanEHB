package com.berkay.transfersim.repository;

import com.berkay.transfersim.model.Player;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PlayerRepository extends MongoRepository<Player, String> {
}

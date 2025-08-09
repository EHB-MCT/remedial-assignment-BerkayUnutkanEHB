package com.berkay.transfersim.repository;

import com.berkay.transfersim.model.Player;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface PlayerRepository extends MongoRepository<Player, String> {
    List<Player> findByClubIgnoreCase(String club);
}

package com.berkay.transfersim.repository;

import com.berkay.transfersim.model.Club;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface ClubRepository extends MongoRepository<Club, String> {
    Optional<Club> findByName(String name);
}

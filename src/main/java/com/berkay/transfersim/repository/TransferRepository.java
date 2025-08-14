package com.berkay.transfersim.repository;

import com.berkay.transfersim.model.Transfer;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TransferRepository extends MongoRepository<Transfer, String> {
}

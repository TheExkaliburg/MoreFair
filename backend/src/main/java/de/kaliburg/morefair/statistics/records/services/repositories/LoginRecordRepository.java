package de.kaliburg.morefair.statistics.records.services.repositories;

import de.kaliburg.morefair.statistics.records.model.LoginRecordEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface LoginRecordRepository extends MongoRepository<LoginRecordEntity, Long> {

}

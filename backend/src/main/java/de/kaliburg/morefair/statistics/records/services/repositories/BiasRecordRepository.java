package de.kaliburg.morefair.statistics.records.services.repositories;

import de.kaliburg.morefair.statistics.records.model.BiasRecordEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BiasRecordRepository extends MongoRepository<BiasRecordEntity, Long> {

}

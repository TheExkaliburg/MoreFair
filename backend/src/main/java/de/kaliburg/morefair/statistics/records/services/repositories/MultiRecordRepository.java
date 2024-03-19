package de.kaliburg.morefair.statistics.records.services.repositories;

import de.kaliburg.morefair.statistics.records.model.MultiRecordEntity;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface MultiRecordRepository extends MongoRepository<MultiRecordEntity, Long> {

}

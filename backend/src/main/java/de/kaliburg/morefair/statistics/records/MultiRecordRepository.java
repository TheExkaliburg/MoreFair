package de.kaliburg.morefair.statistics.records;

import org.springframework.data.mongodb.repository.MongoRepository;


public interface MultiRecordRepository extends MongoRepository<MultiRecordEntity, Long> {

}

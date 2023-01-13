package de.kaliburg.morefair.statistics;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface LoginRecordRepository extends MongoRepository<LoginRecordEntity, Long> {

}

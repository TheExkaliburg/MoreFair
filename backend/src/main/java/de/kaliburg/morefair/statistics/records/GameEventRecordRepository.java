package de.kaliburg.morefair.statistics.records;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface GameEventRecordRepository<T extends AbstractGameEventRecord> extends
    MongoRepository<T, Long> {

}

package de.kaliburg.morefair.statistics.records.services.repositories;

import de.kaliburg.morefair.statistics.records.model.AbstractGameEventRecord;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GameEventRecordRepository<T extends AbstractGameEventRecord> extends
    MongoRepository<T, Long> {

}

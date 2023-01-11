package de.kaliburg.morefair.statistics;

import org.springframework.data.mongodb.repository.MongoRepository;


public interface MultiRepository extends MongoRepository<MultiEntity, Long> {

}

package de.kaliburg.morefair.statistics;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface LoginRepository extends MongoRepository<LoginEntity, Long> {

}

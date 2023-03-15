package de.kaliburg.morefair.statistics.results;

import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ActivityAnalysisRepository extends MongoRepository<ActivityAnalysisEntity, Long> {

  Optional<ActivityAnalysisEntity> findTopByOrderByCreatedOnDesc();
}

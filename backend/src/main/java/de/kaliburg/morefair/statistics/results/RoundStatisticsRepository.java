package de.kaliburg.morefair.statistics.results;

import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RoundStatisticsRepository extends MongoRepository<RoundStatisticsEntity, Long> {

  Optional<RoundStatisticsEntity> findByRoundId(Integer id);
}

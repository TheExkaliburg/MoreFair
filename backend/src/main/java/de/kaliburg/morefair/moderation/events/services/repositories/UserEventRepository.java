package de.kaliburg.morefair.moderation.events.services.repositories;

import de.kaliburg.morefair.moderation.events.model.UserEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserEventRepository extends JpaRepository<UserEventEntity, Long> {

}

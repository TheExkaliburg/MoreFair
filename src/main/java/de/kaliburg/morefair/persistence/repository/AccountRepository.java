package de.kaliburg.morefair.persistence.repository;

import de.kaliburg.morefair.persistence.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Integer countAccountByIsAsshole(Boolean isAsshole);

    Account findByUuid(UUID uuid);

    @Query("SELECT MAX(a.timesAsshole) FROM Account a")
    Integer findTopByTimesAsshole();
}

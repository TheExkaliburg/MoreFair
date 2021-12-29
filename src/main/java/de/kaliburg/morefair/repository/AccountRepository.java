package de.kaliburg.morefair.repository;

import de.kaliburg.morefair.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    @Query("SELECT a FROM Account a WHERE a.uuid = :uuid")
    Account findByUUID(@Param("uuid") UUID uuid);
}

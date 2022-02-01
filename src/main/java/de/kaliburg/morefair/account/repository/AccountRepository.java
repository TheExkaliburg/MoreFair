package de.kaliburg.morefair.account.repository;

import de.kaliburg.morefair.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Integer countAccountByIsAsshole(Boolean isAsshole);

    @Query("SELECT a FROM Account a LEFT JOIN FETCH a.rankers WHERE a.uuid = :uuid")
    Account findByUuid(@Param("uuid") UUID uuid);

    @Query("SELECT MAX(a.timesAsshole) FROM Account a")
    Integer findMaxTimesAsshole();

    @Query("SELECT a FROM Account a LEFT JOIN FETCH a.rankers")
    Set<Account> findAllAccountsJoinedWithRankers();
}

package com.stgsporting.piehmecup.repositories;

import com.stgsporting.piehmecup.dtos.users.UserCoinsDTO;
import com.stgsporting.piehmecup.entities.*;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findUserById(long id);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findUserByIdWithLock(@Param("id") Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE User u SET u.coins = u.coins - :amount WHERE u.id = :id AND u.coins >= :amount")
    int debitCoinsIfEnough(@Param("id") Long id, @Param("amount") Integer amount);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE User u SET u.coins = u.coins - :amount WHERE u.id = :id")
    int forceDebitCoins(@Param("id") Long id, @Param("amount") Integer amount);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE User u SET u.coins = u.coins + :amount WHERE u.id = :id")
    int creditCoins(@Param("id") Long id, @Param("amount") Integer amount);
    Optional<User> findUsersByUsername(String username);

    @Query("SELECT u FROM User u WHERE u.username in :usernames")
    List<User> findUsersByUsername(List<String> usernames);

    @Query("SELECT op.player FROM OwnedPlayer op WHERE op.user.id = :userId")
    List<Player> findPlayersByUserId(@Param("userId") Long userId);

    boolean existsByUsername(String username);

    @Query("SELECT u.icons FROM User u WHERE u.id = :userId")
    List<Icon> findIconsByUserId(@Param("userId") Long userId);

    @Query("SELECT u FROM User u LEFT JOIN u.positions WHERE u.id = :id")
    Optional<User> findUserByIdWithPositions(Long id);

    @Query("SELECT u FROM User u WHERE u.quizId = :quizId")
    Optional<User> findUserByQuizId(Long quizId);

    @Query("SELECT u FROM User u LEFT JOIN u.totalChemistry tc WHERE u.schoolYear = :schoolYear " +
            "AND u.leaderboardBoolean = true AND u.lineupRating.lineupRating > 4.55 " +
            "ORDER BY (u.lineupRating.lineupRating + COALESCE(tc.totalChemistry, 0)) desc, u.id asc")
    List<User> findUsersBySchoolYear(SchoolYear schoolYear);

    @Query("SELECT u FROM User u LEFT JOIN u.totalChemistry tc WHERE u.schoolYear = :schoolYear and u.username " +
            "LIKE :search ORDER BY (u.lineupRating.lineupRating + COALESCE(tc.totalChemistry, 0)) desc, u.id asc")
    Page<User> findUsersBySchoolYearPaginated(SchoolYear schoolYear, String search, Pageable pageable);

    @Query("""
            SELECT new com.stgsporting.piehmecup.dtos.users.UserCoinsDTO(
                u,
                (
                    SELECT COALESCE(SUM(ct.amount), 0)
                    FROM TRANSACTIONS ct
                    WHERE ct.user = u AND ct.type = 'CREDIT' AND (
                        ct.description LIKE '%Admin%' OR
                        ct.description LIKE '%Attended%' OR
                        ct.description LIKE '%Question%' OR
                        ct.description LIKE '%Quiz%'
                    )
                ) - (
                    SELECT COALESCE(SUM(dt.amount), 0)
                    FROM TRANSACTIONS dt
                    WHERE dt.user = u AND dt.type = 'DEBIT' AND (
                        dt.description LIKE '%Admin%' OR
                        dt.description LIKE '%Quiz%' OR
                        dt.description LIKE '%deleted%'
                    )
                )
            )
            FROM User u
            WHERE u.schoolYear = :schoolYear and u.username
            LIKE :search
            ORDER BY (
                (
                    SELECT COALESCE(SUM(ct.amount), 0)
                    FROM TRANSACTIONS ct
                    WHERE ct.user = u AND ct.type = 'CREDIT' AND (
                        ct.description LIKE '%Admin%' OR
                        ct.description LIKE '%Attended%' OR
                        ct.description LIKE '%Question%' OR
                        ct.description LIKE '%Quiz%'
                    )
                ) - (
                    SELECT COALESCE(SUM(dt.amount), 0)
                    FROM TRANSACTIONS dt
                    WHERE dt.user = u AND dt.type = 'DEBIT' AND (
                        dt.description LIKE '%Admin%' OR
                        dt.description LIKE '%Quiz%' OR
                        dt.description LIKE '%deleted%'
                    )
                )
            ) DESC, u.id ASC
            """)
    Page<UserCoinsDTO> findUsersBySchoolYearPaginatedAndCoins(SchoolYear schoolYear, String search, Pageable pageable);

    List<User> findAllBySchoolYear(SchoolYear schoolYear);
}

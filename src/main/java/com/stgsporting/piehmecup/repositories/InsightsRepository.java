package com.stgsporting.piehmecup.repositories;

import com.stgsporting.piehmecup.dtos.insights.BestSellerDTO;
import com.stgsporting.piehmecup.dtos.insights.UserLongMetricDTO;
import com.stgsporting.piehmecup.dtos.insights.UserSpendValueDTO;
import com.stgsporting.piehmecup.entities.Player;
import com.stgsporting.piehmecup.entities.SchoolYear;
import com.stgsporting.piehmecup.entities.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InsightsRepository extends JpaRepository<Player, Long> {

    @Query("SELECT new com.stgsporting.piehmecup.dtos.insights.BestSellerDTO(COUNT(op), p.name) " +
            "FROM OwnedPlayer op JOIN op.player p " +
            "WHERE (:levelId IS NULL OR p.level.id = :levelId) " +
            "GROUP BY p.id, p.name " +
            "ORDER BY COUNT(op) DESC")
    List<BestSellerDTO> findBestSeller(@Param("levelId") Long levelId);

    @Query("""
            SELECT u FROM User u
            LEFT JOIN u.totalChemistry tc
            WHERE u.schoolYear = :schoolYear
            ORDER BY (u.lineupRating.lineupRating + COALESCE(tc.totalChemistry, 0)) DESC, u.id ASC
            """)
    List<User> findTopUsersByOverallScore(@Param("schoolYear") SchoolYear schoolYear, Pageable pageable);

    @Query("""
            SELECT new com.stgsporting.piehmecup.dtos.insights.UserSpendValueDTO(
                u,
                (
                    SELECT COALESCE(SUM(dt.amount), 0)
                    FROM TRANSACTIONS dt
                    WHERE dt.user = u
                    AND dt.type = 'DEBIT'
                    AND dt.description LIKE 'Player purchase:%'
                ) * 1.0,
                (
                    (
                        u.lineupRating.lineupRating + COALESCE(tc.totalChemistry, 0)
                    ) / NULLIF(
                        (
                            SELECT COALESCE(SUM(dt.amount), 0)
                            FROM TRANSACTIONS dt
                            WHERE dt.user = u
                            AND dt.type = 'DEBIT'
                            AND dt.description LIKE 'Player purchase:%'
                        ) * 1.0,
                        0.0
                    )
                ) * 1000.0
            )
            FROM User u
            LEFT JOIN u.totalChemistry tc
            WHERE u.schoolYear = :schoolYear
            AND (
                SELECT COALESCE(SUM(dt.amount), 0)
                FROM TRANSACTIONS dt
                WHERE dt.user = u
                AND dt.type = 'DEBIT'
                AND dt.description LIKE 'Player purchase:%'
            ) > 0
            ORDER BY (
                (
                    u.lineupRating.lineupRating + COALESCE(tc.totalChemistry, 0)
                ) / NULLIF(
                    (
                        SELECT COALESCE(SUM(dt.amount), 0)
                        FROM TRANSACTIONS dt
                        WHERE dt.user = u
                        AND dt.type = 'DEBIT'
                        AND dt.description LIKE 'Player purchase:%'
                    ) * 1.0,
                    0.0
                )
            ) DESC, u.id ASC
            """)
    List<UserSpendValueDTO> findTopUsersByValue(@Param("schoolYear") SchoolYear schoolYear, Pageable pageable);

    @Query("""
            SELECT new com.stgsporting.piehmecup.dtos.insights.UserLongMetricDTO(u, COUNT(a))
            FROM ATTENDANCE a
            JOIN a.user u
            WHERE u.schoolYear = :schoolYear
            AND a.approved = true
            GROUP BY u
            ORDER BY COUNT(a) DESC, u.id ASC
            """)
    List<UserLongMetricDTO> findTopUsersByApprovedAttendances(@Param("schoolYear") SchoolYear schoolYear, Pageable pageable);

    @Query("""
            SELECT COUNT(a)
            FROM ATTENDANCE a
            WHERE a.user.schoolYear = :schoolYear
            AND a.approved = true
            """)
    Long countApprovedAttendancesBySchoolYear(@Param("schoolYear") SchoolYear schoolYear);
}

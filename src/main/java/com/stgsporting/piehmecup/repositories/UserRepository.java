package com.stgsporting.piehmecup.repositories;

import com.stgsporting.piehmecup.entities.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findUserById(long id);
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

    @Query("SELECT u FROM User u WHERE u.schoolYear = :schoolYear AND u.leaderboardBoolean = true " +
            "AND u.lineupRating.lineupRating > 4.55 ORDER BY u.lineupRating.lineupRating desc, u.id asc")
    List<User> findUsersBySchoolYear(SchoolYear schoolYear);

    @Query("SELECT u FROM User u WHERE u.schoolYear = :schoolYear and u.username LIKE :search ORDER BY u.lineupRating.lineupRating desc, u.id asc")
    Page<User> findUsersBySchoolYearPaginated(SchoolYear schoolYear, String search, Pageable pageable);

    List<User> findAllBySchoolYear(SchoolYear schoolYear);
}

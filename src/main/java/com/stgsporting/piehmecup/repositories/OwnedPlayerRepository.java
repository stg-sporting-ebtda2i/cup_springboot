package com.stgsporting.piehmecup.repositories;

import com.stgsporting.piehmecup.entities.OwnedPlayer;
import com.stgsporting.piehmecup.entities.Player;
import com.stgsporting.piehmecup.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OwnedPlayerRepository extends JpaRepository<OwnedPlayer, Long> {
    Optional<OwnedPlayer> findByUserAndPlayer(User user, Player player);
    List<OwnedPlayer> findByUser(User user);
}

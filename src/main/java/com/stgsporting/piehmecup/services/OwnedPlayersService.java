package com.stgsporting.piehmecup.services;

import com.stgsporting.piehmecup.dtos.players.PlayerDTO;
import com.stgsporting.piehmecup.entities.OwnedPlayer;
import com.stgsporting.piehmecup.entities.Player;
import com.stgsporting.piehmecup.entities.User;
import com.stgsporting.piehmecup.exceptions.PlayerAlreadyPurchasedException;
import com.stgsporting.piehmecup.exceptions.PlayerNotFoundException;
import com.stgsporting.piehmecup.exceptions.UserNotFoundException;
import com.stgsporting.piehmecup.repositories.OwnedPlayerRepository;
import com.stgsporting.piehmecup.repositories.PlayerRepository;
import com.stgsporting.piehmecup.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OwnedPlayersService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private WalletService walletService;
    @Autowired
    private PlayerService playerService;
    @Autowired
    private OwnedPlayerRepository ownedPlayerRepository;

    public List<PlayerDTO> getLineup(){
        try {
            Long userId = userService.getAuthenticatableId();
            List<Player> players = userRepository.findPlayersByUserId(userId);
            List<PlayerDTO> playerDTOS = new ArrayList<>();
            for(Player player : players)
                playerDTOS.add(playerService.playerToDTO(player));

            return playerDTOS;
        } catch (UserNotFoundException e) {
            throw new UserNotFoundException();
        }
    }

    public List<PlayerDTO> getLineup(Long userId){
        try {
            List<Player> players = userRepository.findPlayersByUserId(userId);
            List<PlayerDTO> playerDTOS = new ArrayList<>();
            for(Player player : players)
                playerDTOS.add(playerService.playerToDTO(player));

            return playerDTOS;
        } catch (UserNotFoundException e) {
            throw new UserNotFoundException("User not found");
        }
    }

    @Transactional
    public void addPlayerToUser(Long playerId) {
        Long userId = userService.getAuthenticatableId();
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        Player player = playerRepository.findById(playerId).orElseThrow(PlayerNotFoundException::new);

        if (user.getPlayers().contains(player)) {
            throw new PlayerAlreadyPurchasedException();
        }
        if (!player.getPosition().getName().equals("CM") && !player.getPosition().getName().equals("CB")) {
            for(Player p : user.getPlayers()) {
                if(p.getPosition().equals(player.getPosition())) {
                    throw new PlayerAlreadyPurchasedException("Player of this position already purchased");
                }
                if (p.getName().equals(player.getName())) {
                    throw new PlayerAlreadyPurchasedException("Cannot purchase same player twice");
                }
            }
        } else {
            int count = 0;
            for(Player p : user.getPlayers()) {
                if(p.getPosition().equals(player.getPosition())) {
                    count++;
                }
            }
            if(count >= 2) {
                throw new PlayerAlreadyPurchasedException("Cannot purchase more than 2 players of this position");
            }
        }
        if(user.getSelectedPosition() != null && user.getSelectedPosition().getName().equals(player.getPosition().getName())) {
            throw new PlayerAlreadyPurchasedException("Cannot purchase player of this position");
        }

        walletService.debit(user, player.getPrice(), "Player purchase: " + player.getId());

        int playerChemistry = 0;
        for (Player p : user.getPlayers()) {
            if (p.getClub().equals(player.getClub()) || p.getNationality().equals(player.getNationality()) || p.getLeague().equals(player.getLeague())) {
                playerChemistry++;
                updateChemistry(p.getId(),true);
            }
        }

        user.addPlayer(player, Math.min(playerChemistry, 3));
        userRepository.save(user);
    }

    @Transactional
    public void removePlayerFromUser(Long playerId) {
        Long userId = userService.getAuthenticatableId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new PlayerNotFoundException("Player not found"));

        if (!user.getPlayers().contains(player)) {
            throw new PlayerNotFoundException("User does not own player");
        }

        walletService.credit(user, player.getPrice(), "Player sale: " + player.getId());

        for (Player p : user.getPlayers()) {
            if (p.getClub().equals(player.getClub()) || p.getNationality().equals(player.getNationality()) || p.getLeague().equals(player.getLeague())) {
                updateChemistry(p.getId(),false);
            }
        }

        user.removePlayer(player);
        userRepository.save(user);
    }

    @Transactional
    public void updateChemistry(Long playerId, boolean increase) {
        Long userId = userService.getAuthenticatableId();
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        Player player = playerRepository.findById(playerId).orElseThrow(PlayerNotFoundException::new);
        OwnedPlayer ownedPlayer = ownedPlayerRepository.findByUserAndPlayer(user, player)
                .orElseThrow(PlayerNotFoundException::new);
        if (increase) {
            if (ownedPlayer.getChemistry() < 3) {
                ownedPlayer.setChemistry(ownedPlayer.getChemistry() + 1);
                ownedPlayerRepository.save(ownedPlayer);
            }
        } else {
            if (ownedPlayer.getChemistry() > 0) {
                ownedPlayer.setChemistry(ownedPlayer.getChemistry() - 1);
                ownedPlayerRepository.save(ownedPlayer);
            }
        }
        ownedPlayerRepository.save(ownedPlayer);
    }
}

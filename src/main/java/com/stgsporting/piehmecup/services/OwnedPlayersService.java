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
import org.jetbrains.annotations.NotNull;
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

    @NotNull
    private List<PlayerDTO> getPlayerDTOS(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty())
            throw new UserNotFoundException();

        List<Player> players = userRepository.findPlayersByUserId(userId);
        List<PlayerDTO> playerDTOS = new ArrayList<>();
        for(Player player : players) {
            Optional<OwnedPlayer> op =
                    ownedPlayerRepository.findByUserAndPlayer(user.get(),player);
            if (op.isEmpty())
                throw new PlayerNotFoundException();
            playerDTOS.add(playerService.ownedPlayerToDTO(player,op.get()));
        }

        return playerDTOS;
    }

    public List<PlayerDTO> getLineup(){
        try {
            Long userId = userService.getAuthenticatableId();
            return getPlayerDTOS(userId);
        } catch (UserNotFoundException e) {
            throw new UserNotFoundException("User not found");
        } catch (PlayerNotFoundException e) {
            throw new PlayerNotFoundException("Player not found in owned players");
        }
    }

    public List<PlayerDTO> getLineup(Long userId){
        try {
            return getPlayerDTOS(userId);
        } catch (UserNotFoundException e) {
            throw new UserNotFoundException("User not found");
        } catch (PlayerNotFoundException e) {
            throw new PlayerNotFoundException("Player not found in owned players");
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

        user.addPlayer(player, 0);
        userRepository.save(user);

        recalculateAllUserChemistry(user);
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

        user.removePlayer(player);
        userRepository.save(user);

        recalculateAllUserChemistry(user);
    }

    @Transactional
    public void recalculateAllUserChemistry(User user) {
        List<Player> allPlayers = user.getPlayers();
        List<OwnedPlayer> ownedPlayers = ownedPlayerRepository.findByUser(user);

        for (OwnedPlayer ownedPlayer : ownedPlayers) {
            Player playerToCalculate = ownedPlayer.getPlayer();

            int newChemistry = calculateChemistryForPlayer(playerToCalculate, allPlayers);

            ownedPlayer.setChemistry(newChemistry);
            ownedPlayerRepository.save(ownedPlayer);
        }
    }

    private int calculateChemistryForPlayer(Player playerToCalculate, List<Player> allPlayers) {
        boolean isCalculatingForIcon = "icon".equals(playerToCalculate.getLeague());
        if (isCalculatingForIcon) {
            return 3;
        }

        int clubCount = 0;
        int leagueCount = 0;
        int nationCount = 0;

        for (Player otherPlayer : allPlayers) {
            boolean isOtherPlayerIcon = "icon".equals(otherPlayer.getLeague());

            if (isOtherPlayerIcon) {
                leagueCount++;
                if (otherPlayer.getNationality().equals(playerToCalculate.getNationality())) {
                    nationCount += 2;
                }
            } else {
                if (otherPlayer.getClub().equals(playerToCalculate.getClub())) {
                    clubCount++;
                }
                if (otherPlayer.getLeague().equals(playerToCalculate.getLeague())) {
                    leagueCount++;
                }
                if (otherPlayer.getNationality().equals(playerToCalculate.getNationality())) {
                    nationCount++;
                }
            }
        }

        int totalPoints = 0;
        totalPoints += getClubPoints(clubCount);
        totalPoints += getLeaguePoints(leagueCount);
        totalPoints += getNationPoints(nationCount);

        return Math.min(3, totalPoints);
    }

    private int getClubPoints(int count) {
        if (count >= 7) return 3;
        if (count >= 4) return 2;
        if (count >= 2) return 1;
        return 0;
    }

    private int getLeaguePoints(int count) {
        if (count >= 8) return 3;
        if (count >= 5) return 2;
        if (count >= 3) return 1;
        return 0;
    }

    private int getNationPoints(int count) {
        if (count >= 8) return 3;
        if (count >= 5) return 2;
        if (count >= 2) return 1;
        return 0;
    }
}

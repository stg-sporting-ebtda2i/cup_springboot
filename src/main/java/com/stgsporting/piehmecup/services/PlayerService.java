package com.stgsporting.piehmecup.services;

import com.stgsporting.piehmecup.authentication.Authenticatable;
import com.stgsporting.piehmecup.dtos.players.PlayerDTO;
import com.stgsporting.piehmecup.dtos.players.PlayerUploadDTO;
import com.stgsporting.piehmecup.entities.Level;
import com.stgsporting.piehmecup.entities.OwnedPlayer;
import com.stgsporting.piehmecup.entities.Player;
import com.stgsporting.piehmecup.entities.Position;
import com.stgsporting.piehmecup.enums.Positions;
import com.stgsporting.piehmecup.exceptions.PlayerNotFoundException;
import com.stgsporting.piehmecup.repositories.PlayerRepository;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class PlayerService {
    private final PlayerRepository playerRepository;
    private final FileService fileService;
    private final PositionService positionService;
    private final AdminService adminService;

    public PlayerService(PlayerRepository playerRepository, FileService fileService, PositionService positionService, AdminService adminService) {
        this.playerRepository = playerRepository;
        this.fileService = fileService;
        this.positionService = positionService;
        this.adminService = adminService;
    }

    public void createPlayer(PlayerUploadDTO player) {
        Player newPlayer = dtoToPlayer(player);
        Authenticatable authenticatable = adminService.getAuthenticatable();
        newPlayer.setLevel(authenticatable.getSchoolYear().getLevel());

        playerRepository.save(newPlayer);
    }

    private Player dtoToPlayer(PlayerUploadDTO player) {
        Player newPlayer = new Player();
        newPlayer.setName(player.getName());

        newPlayer.setPosition(positionService.getPositionByName(player.getPosition()));

        newPlayer.setAvailable(player.getAvailable());
        newPlayer.setPrice(player.getPrice());
        newPlayer.setClub(player.getClub());
        newPlayer.setLeague(player.getLeague());
        newPlayer.setNationality(player.getNationality());

        String key = fileService.uploadFile(player.getImage(), "/players");

        newPlayer.setImgLink(key);
        newPlayer.setRating(player.getRating());
        return newPlayer;
    }

    public PlayerDTO getPlayerByName(String name) {
        Player player = playerRepository.findPlayerByName(name).orElseThrow(
                () -> new PlayerNotFoundException("Player with name " + name + " not found")
        );

        return playerToDTO(player);
    }

    public PlayerDTO getPlayerById(Long id) {
        Player player = playerRepository.findById(id)
                .orElseThrow(PlayerNotFoundException::new);

        return playerToDTO(player);
    }

    public Page<PlayerDTO> getPlayers(Pageable pageable, String search, Level level) {
        if(search == null) {
            search = "";
        }
        search = search + "%";

        return playerRepository.findPlayersByLevel(pageable, search, level).map(this::playerToDTO);
    }

    public PlayerDTO playerToDTO(Player player) {
        return getDto(player);
    }

    public PlayerDTO ownedPlayerToDTO(Player player, OwnedPlayer op) {
        PlayerDTO playerDTO = getDto(player);
        playerDTO.setChemistry(op.getChemistry());
        return playerDTO;
    }

    @NotNull
    private PlayerDTO getDto(Player player) {
        PlayerDTO playerDTO = new PlayerDTO();
        playerDTO.setId(player.getId());
        playerDTO.setName(player.getName());
        playerDTO.setPosition(player.getPosition().getName());
        playerDTO.setAvailable(player.getAvailable());
        playerDTO.setPrice(player.getPrice());
        playerDTO.setClub(player.getClub());
        playerDTO.setLeague(player.getLeague());
        playerDTO.setNationality(player.getNationality());

        String url = fileService.generateSignedUrl(player.getImgLink());

        playerDTO.setImageUrl(url);
        playerDTO.setImageKey(player.getImgLink());

        playerDTO.setRating(player.getRating());
        return playerDTO;
    }

    public void updatePlayer(Long playerId, PlayerUploadDTO playerDTO) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(PlayerNotFoundException::new);

        Player updatedPlayer = dtoToPlayer(playerDTO);

        if(playerDTO.getImage() != null && !player.getImgLink().equals(updatedPlayer.getImgLink()))
            fileService.deleteFile(player.getImgLink());
        else
            updatedPlayer.setImgLink(player.getImgLink());

        updatedPlayer.setId(player.getId());
        updatedPlayer.setLevel(player.getLevel());
        playerRepository.save(updatedPlayer);
    }

    public void deletePlayer(Long playerId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(PlayerNotFoundException::new);

        fileService.deleteFile(player.getImgLink());

        playerRepository.delete(player);
    }

    public List<PlayerDTO> getPlayersByPosition(String positionName, Level level){
        Position position = positionService.getPositionByName(positionName);

        List<Player> players = playerRepository.findPlayersByPositionAndLevel(position, level);

        List<PlayerDTO> playerDTOs = new ArrayList<>();
        for(Player player : players)
            playerDTOs.add(playerToDTO(player));

        return playerDTOs;
    }
}

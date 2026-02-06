package com.stgsporting.piehmecup.dtos.users;

import com.stgsporting.piehmecup.dtos.attendances.AttendanceUserDTO;
import com.stgsporting.piehmecup.entities.Icon;
import com.stgsporting.piehmecup.entities.User;
import com.stgsporting.piehmecup.services.FileService;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class UserDetailsDTO {

    private Long id;
    private String username;
    private Integer coins;
    private Integer cardRating;
    private Double lineupRating;
    private Boolean leaderboardBoolean;
    private String imageKey;
    private String imageUrl;
    private String selectedIcon;
    private Boolean confirmed;
    private List<AttendanceUserDTO> attendances;
    private Integer chemistry;


    public UserDetailsDTO(User user, FileService fileService) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.coins = user.getCoins();
        this.cardRating = user.getCardRating();
        this.lineupRating = user.getLineupRating();
        this.chemistry = user.getTotalChemistry();
        Icon selectedIcon = user.getSelectedIcon();
        this.confirmed = user.getConfirmed();
        this.leaderboardBoolean = user.getLeaderboardBoolean();
        this.imageKey = user.getImgLink();
        this.imageUrl = fileService.generateSignedUrl(user.getImgLink());

        this.attendances = user.getAttendances() != null ? user.getAttendances().stream().map(AttendanceUserDTO::new).toList() : new ArrayList<>();

        this.selectedIcon = selectedIcon != null ? selectedIcon.getImgLink() : null;
    }
}

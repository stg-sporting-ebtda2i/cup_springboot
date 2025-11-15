package com.stgsporting.piehmecup.dtos.users;

import com.stgsporting.piehmecup.entities.Icon;
import com.stgsporting.piehmecup.entities.User;
import com.stgsporting.piehmecup.services.FileService;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserInListDTO {

    private Long id;
    private String username;
    private Integer coins;
    private Integer cardRating;
    private Double lineupRating;
    private String imageUrl;
    private String imageKey;
    private Integer chemistry;
    private String selectedIcon;
    private Boolean confirmed;

    public UserInListDTO(User user, FileService fileService) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.coins = user.getCoins();
        this.cardRating = user.getCardRating();
        this.lineupRating = user.getLineupRating();
        this.confirmed = user.getConfirmed();
        Icon selectedIcon = user.getSelectedIcon();
        this.chemistry = user.getTotalChemistry();

        this.imageKey = user.getImgLink();
        this.imageUrl = fileService.generateSignedUrl(user.getImgLink());

        this.selectedIcon = selectedIcon != null ? selectedIcon.getImgLink() : null;
    }
}

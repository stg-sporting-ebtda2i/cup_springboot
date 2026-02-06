package com.stgsporting.piehmecup.dtos.users;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserInLeaderboardDTO {
    private Long id;
    private String name;
    private Integer cardRating;
    private Integer chemistry;
    private String imageUrl;
    private String imageKey;
    private String Position;
    private Double lineupRating;
    private String iconUrl;
    private String iconKey;
    private Boolean confirmed;
}

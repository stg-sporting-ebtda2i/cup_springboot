package com.stgsporting.piehmecup.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserCardDTO {
    private Long id;
    private String name;
    private Integer cardRating;
    private Double lineupRating;
    private int chemistry;
    private String imageUrl;
    private String imageKey;
    private String iconUrl;
    private String iconKey;
    private String position;
}

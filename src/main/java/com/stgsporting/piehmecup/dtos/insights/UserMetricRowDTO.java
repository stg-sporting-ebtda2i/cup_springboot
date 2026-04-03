package com.stgsporting.piehmecup.dtos.insights;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserMetricRowDTO {
    private Long rank;
    private Long userId;
    private String username;
    private String imageUrl;
    private String imageKey;
    private Double lineupRating;
    private Integer chemistry;
    private Double overallScore;
    private Integer currentCoins;
    private Integer totalCoinsEarned;
    private Double metricValue;
}

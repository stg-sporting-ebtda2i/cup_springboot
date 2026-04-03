package com.stgsporting.piehmecup.dtos.insights;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AttemptedAllQuizUserDTO {
    private Long userId;
    private String username;
    private String imageUrl;
    private String imageKey;
    private Double overallScore;
    private Integer totalCoinsEarned;
    private Long attemptedQuizzesCount;
    private Long publishedQuizzesCount;
}

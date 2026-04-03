package com.stgsporting.piehmecup.dtos.insights;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class QuizDifficultyDTO {
    private Long quizId;
    private String quizSlug;
    private String quizName;
    private Long submissionsCount;
    private Long totalAnswers;
    private Long correctAnswers;
    private Double accuracy;
}

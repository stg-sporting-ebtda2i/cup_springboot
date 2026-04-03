package com.stgsporting.piehmecup.dtos.insights;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class HardestQuestionDTO {
    private Long quizId;
    private String quizSlug;
    private String quizName;
    private Long questionId;
    private String questionTitle;
    private String questionType;
    private Long attempts;
    private Long correctAnswers;
    private Double accuracy;
}

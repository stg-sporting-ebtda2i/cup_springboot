package com.stgsporting.piehmecup.dtos.insights;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.minidev.json.JSONObject;

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

    public static QuizDifficultyDTO fromJson(JSONObject json) {
        return new QuizDifficultyDTO(
                json.getAsNumber("quizId").longValue(),
                json.getAsString("quizSlug"),
                json.getAsString("quizName"),
                json.getAsNumber("submissionsCount").longValue(),
                json.getAsNumber("totalAnswers").longValue(),
                json.getAsNumber("correctAnswers").longValue(),
                json.getAsNumber("accuracy").doubleValue()
        );
    }
}

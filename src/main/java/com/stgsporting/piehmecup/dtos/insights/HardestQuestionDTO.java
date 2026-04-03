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

    public static HardestQuestionDTO fromJson(JSONObject json) {
        return new HardestQuestionDTO(
                json.getAsNumber("quizId").longValue(),
                json.getAsString("quizSlug"),
                json.getAsString("quizName"),
                json.getAsNumber("questionId").longValue(),
                json.getAsString("questionTitle"),
                json.getAsString("questionType"),
                json.getAsNumber("attempts").longValue(),
                json.getAsNumber("correctAnswers").longValue(),
                json.getAsNumber("accuracy").doubleValue()
        );
    }
}

package com.stgsporting.piehmecup.dtos.insights;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class HardestQuestionsByQuizDTO {
    private Long quizId;
    private String quizSlug;
    private String quizName;
    private List<HardestQuestionDTO> questions;

    public static HardestQuestionsByQuizDTO fromJson(JSONObject json) {
        JSONArray questions = (JSONArray) json.get("questions");
        return new HardestQuestionsByQuizDTO(
                json.getAsNumber("quizId").longValue(),
                json.getAsString("quizSlug"),
                json.getAsString("quizName"),
                questions == null
                        ? List.of()
                        : questions.stream()
                        .map(JSONObject.class::cast)
                        .map(HardestQuestionDTO::fromJson)
                        .toList()
        );
    }
}

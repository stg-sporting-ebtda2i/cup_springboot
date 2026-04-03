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
public class ChoiceDistributionDTO {
    private Long quizId;
    private String quizSlug;
    private String quizName;
    private Long questionId;
    private String questionTitle;
    private Long totalResponses;
    private List<ChoiceDistributionOptionDTO> options;

    public static ChoiceDistributionDTO fromJson(JSONObject json) {
        JSONArray options = (JSONArray) json.get("options");
        return new ChoiceDistributionDTO(
                json.getAsNumber("quizId").longValue(),
                json.getAsString("quizSlug"),
                json.getAsString("quizName"),
                json.getAsNumber("questionId").longValue(),
                json.getAsString("questionTitle"),
                json.getAsNumber("totalResponses").longValue(),
                options == null
                        ? List.of()
                        : options.stream()
                        .map(JSONObject.class::cast)
                        .map(ChoiceDistributionOptionDTO::fromJson)
                        .toList()
        );
    }
}

package com.stgsporting.piehmecup.dtos.insights;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
}

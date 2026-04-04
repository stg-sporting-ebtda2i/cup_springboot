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
public class QuestionDistributionDTO {
    private String distributionType;
    private Long quizId;
    private String quizSlug;
    private String quizName;
    private Long questionId;
    private String questionTitle;
    private String questionType;
    private Long totalResponses;
    private Long distinctAnswers;
    private List<ChoiceDistributionOptionDTO> options;
    private List<WrittenAnswerGroupDTO> answers;
    private List<ReorderPermutationDTO> permutations;
}

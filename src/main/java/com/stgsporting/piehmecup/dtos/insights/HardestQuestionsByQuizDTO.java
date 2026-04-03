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
public class HardestQuestionsByQuizDTO {
    private Long quizId;
    private String quizSlug;
    private String quizName;
    private List<HardestQuestionDTO> questions;
}

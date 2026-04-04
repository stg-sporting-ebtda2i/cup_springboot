package com.stgsporting.piehmecup.dtos.insights;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WrittenAnswerGroupDTO {
    private String normalizedAnswer;
    private String displayAnswer;
    private Long count;
    private Double percentage;
}

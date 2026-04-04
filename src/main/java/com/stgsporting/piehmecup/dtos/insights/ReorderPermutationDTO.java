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
public class ReorderPermutationDTO {
    private List<Long> optionOrders;
    private String sequenceLabel;
    private Long count;
    private Double percentage;
    private Boolean correct;
}

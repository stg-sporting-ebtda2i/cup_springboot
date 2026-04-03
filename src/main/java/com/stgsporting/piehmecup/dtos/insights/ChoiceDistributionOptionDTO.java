package com.stgsporting.piehmecup.dtos.insights;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChoiceDistributionOptionDTO {
    private Long optionId;
    private String optionName;
    private Long optionOrder;
    private Long picksCount;
    private Double picksPercentage;
    private Boolean correct;
}

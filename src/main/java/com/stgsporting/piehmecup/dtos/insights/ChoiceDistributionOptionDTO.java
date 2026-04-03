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
public class ChoiceDistributionOptionDTO {
    private Long optionId;
    private String optionName;
    private Long optionOrder;
    private Long picksCount;
    private Double picksPercentage;
    private Boolean correct;

    public static ChoiceDistributionOptionDTO fromJson(JSONObject json) {
        return new ChoiceDistributionOptionDTO(
                json.getAsNumber("optionId").longValue(),
                json.getAsString("optionName"),
                json.getAsNumber("optionOrder").longValue(),
                json.getAsNumber("picksCount").longValue(),
                json.getAsNumber("picksPercentage").doubleValue(),
                (Boolean) json.get("correct")
        );
    }
}

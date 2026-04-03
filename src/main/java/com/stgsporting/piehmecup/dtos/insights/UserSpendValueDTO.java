package com.stgsporting.piehmecup.dtos.insights;

import com.stgsporting.piehmecup.entities.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSpendValueDTO {
    private User user;
    private Double totalSpent;
    private Double metricValue;

    public UserSpendValueDTO(User user, Double totalSpent, Double metricValue) {
        this.user = user;
        this.totalSpent = totalSpent;
        this.metricValue = metricValue;
    }
}

package com.stgsporting.piehmecup.dtos.insights;

import com.stgsporting.piehmecup.entities.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserLongMetricDTO {
    private User user;
    private Long metricValue;

    public UserLongMetricDTO(User user, Long metricValue) {
        this.user = user;
        this.metricValue = metricValue;
    }
}

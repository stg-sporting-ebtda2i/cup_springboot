package com.stgsporting.piehmecup.dtos.insights;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StatsSummaryDTO {
    private Integer usersCount;
    private Integer quizzesCount;
    private Integer questionsCount;
    private Integer approvedAttendancesCount;
}

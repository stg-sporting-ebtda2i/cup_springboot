package com.stgsporting.piehmecup.dtos.attendances;

import lombok.Getter;
import lombok.Setter;

import java.sql.Date;
import java.util.List;

@Setter
@Getter
public class BulkAttendanceDTO {
    private Date date;
    private String liturgyName;
    private List<Long> userIds;
}

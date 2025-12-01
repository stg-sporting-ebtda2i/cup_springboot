package com.stgsporting.piehmecup.dtos.attendances;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BulkAttendanceResponseDTO {
    private List<String> failedUsers;
    private List<String> approvedUsers;
}

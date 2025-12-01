package com.stgsporting.piehmecup.exceptions;

import com.stgsporting.piehmecup.entities.Attendance;
import java.util.List;

public class DuplicateAttendanceException extends RuntimeException {
    private Attendance attendance;

    public DuplicateAttendanceException(String message) {
        super(message);
    }

    public DuplicateAttendanceException(Attendance attendance) {
        super("You can't attend the same liturgy twice in the same week");
        this.attendance = attendance;
    }

    public Attendance getAttendance() {
        return attendance;
    }
}

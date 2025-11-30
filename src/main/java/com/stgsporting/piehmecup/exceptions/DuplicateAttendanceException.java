package com.stgsporting.piehmecup.exceptions;

import java.util.List;

public class DuplicateAttendanceException extends RuntimeException {
    public DuplicateAttendanceException(String message) {
        super(message);
    }

    public DuplicateAttendanceException(List<String> usernames) {
        super("(DUPLICATE ATTENDANCE) Failed to add attendance for users: " + String.join(", ", usernames));
    }
}

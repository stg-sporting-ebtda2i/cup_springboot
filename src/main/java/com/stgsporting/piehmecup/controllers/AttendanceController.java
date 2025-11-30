package com.stgsporting.piehmecup.controllers;

import com.stgsporting.piehmecup.dtos.PaginationDTO;
import com.stgsporting.piehmecup.dtos.attendances.BulkAttendanceDTO;
import com.stgsporting.piehmecup.dtos.attendances.RequestAttendanceDTO;
import com.stgsporting.piehmecup.entities.SchoolYear;
import com.stgsporting.piehmecup.entities.User;
import com.stgsporting.piehmecup.services.AdminService;
import com.stgsporting.piehmecup.services.AttendanceService;
import com.stgsporting.piehmecup.services.UserService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping
public class AttendanceController {
    private final AttendanceService attendanceService;
    private final AdminService adminService;
    private final UserService userService;

    public AttendanceController(AttendanceService attendanceService, AdminService adminService, UserService userService) {
        this.attendanceService = attendanceService;
        this.adminService = adminService;
        this.userService = userService;
    }

    @GetMapping("/ostaz/attendances")
    public ResponseEntity<Object> getUnapprovedAttendances(@RequestParam(required = false) Integer page) {
        SchoolYear schoolYear = adminService.getAuthenticatable().getSchoolYear();

        Pageable pageable = PageRequest.of(page == null ? 0 : page, 20);

        return ResponseEntity.ok(
                new PaginationDTO<>(attendanceService.getUnapprovedAttendances(pageable, schoolYear))
        );
    }

    @PatchMapping("ostaz/attendances/{attendanceId}")
    public ResponseEntity<Object> approveAttendance(@PathVariable Long attendanceId) {
        attendanceService.approveAttendance(attendanceId);
        return ResponseEntity.ok().body(new HashMap<>(Map.of("message", "Attendance approved")));
    }

    @DeleteMapping("attendances/{attendanceId}")
    public ResponseEntity<Object> deleteAttendanceUser(@PathVariable Long attendanceId) {
        attendanceService.deleteAttendanceForUser(attendanceId, (User) userService.getAuthenticatable());
        return ResponseEntity.ok().body(Map.of("message", "Attendance deleted"));
    }

    @DeleteMapping("ostaz/attendances/{attendanceId}")
    public ResponseEntity<Object> deleteAttendanceAdmin(@PathVariable Long attendanceId) {
        attendanceService.deleteAttendance(attendanceId);
        return ResponseEntity.ok().body(Map.of("message", "Attendance deleted"));
    }

    @PostMapping("ostaz/attendances")
    public ResponseEntity<Object> addBulkAttendance(@ModelAttribute BulkAttendanceDTO attendanceDTO) {
        attendanceService.addBulkAttendance(attendanceDTO);
        return ResponseEntity.ok().body(new HashMap<>(Map.of("message", "Attendances added")));
    }

    @PatchMapping("attendance/{liturgyName}")
    public ResponseEntity<Object> requestAttendance(@PathVariable String liturgyName, @RequestBody RequestAttendanceDTO attendanceDTO) {
        attendanceService.requestAttendance(liturgyName, attendanceDTO.getDate());
        return ResponseEntity.ok().body(Map.of("message", "Attendance requested"));
    }

    @GetMapping("attendances/approved")
    public ResponseEntity<Object> getApprovedAttendances(@RequestParam(required = false) Integer page) {
        Pageable pageable = PageRequest.of(page == null ? 0 : page, 20);

        return ResponseEntity.ok(
                new PaginationDTO<>(attendanceService.getApprovedAttendancesOfUser(pageable))
        );
    }

    @GetMapping("attendances/approved/{userId}")
    public ResponseEntity<Object> getApprovedAttendances(@PathVariable Long userId, @RequestParam(required = false) Integer page) {
        Pageable pageable = PageRequest.of(page == null ? 0 : page, 20);

        return ResponseEntity.ok(
                new PaginationDTO<>(attendanceService.getApprovedAttendancesOfUser(pageable, userId))
        );
    }

    @GetMapping("attendances")
    public ResponseEntity<Object> getAttendancesByUserId(@RequestParam(required = false) Integer page) {
        Pageable pageable = PageRequest.of(page == null ? 0 : page, 20);

        return ResponseEntity.ok(
                new PaginationDTO<>(attendanceService.getAllAttendancesOfUser(pageable))
        );
    }

    @GetMapping("attendances/pending")
    public ResponseEntity<Object> getPendingAttendances(@RequestParam(required = false) Integer page) {
        Pageable pageable = PageRequest.of(page == null ? 0 : page, 20);

        return ResponseEntity.ok(
                new PaginationDTO<>(attendanceService.getPendingAttendancesOfUser(pageable))
        );
    }

    @GetMapping("attendances/pending/{userId}")
    public ResponseEntity<Object> getPendingAttendances(@PathVariable Long userId, @RequestParam(required = false) Integer page) {
        Pageable pageable = PageRequest.of(page == null ? 0 : page, 20);

        return ResponseEntity.ok(
                new PaginationDTO<>(attendanceService.getPendingAttendancesOfUser(pageable, userId))
        );
    }
}

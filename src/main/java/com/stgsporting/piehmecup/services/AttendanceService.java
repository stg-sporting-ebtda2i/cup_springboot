package com.stgsporting.piehmecup.services;

import com.stgsporting.piehmecup.authentication.Authenticatable;
import com.stgsporting.piehmecup.dtos.attendances.AttendanceDTO;
import com.stgsporting.piehmecup.entities.*;
import com.stgsporting.piehmecup.exceptions.*;
import com.stgsporting.piehmecup.repositories.AttendanceRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.Objects;

@Service
public class AttendanceService {
    private final AttendanceRepository attendanceRepository;
    private final UserService userService;
    private final WalletService walletService;
    private final PriceService priceService;
    private final AdminService adminService;

    @Value("${system.event_start_date}")
    private String eventStartDate;

    public AttendanceService(AttendanceRepository attendanceRepository, UserService userService, WalletService walletService, PriceService priceService, AdminService adminService) {
        this.attendanceRepository = attendanceRepository;
        this.userService = userService;
        this.walletService = walletService;
        this.priceService = priceService;
        this.adminService = adminService;
    }

    public void requestAttendance(String liturgyName, Date date) {
        long userId = userService.getAuthenticatableId();
        User user = userService.findOrFail(userId);
        Price price = priceService.getPrice(liturgyName, user.getSchoolYear().getLevel());
        validateAttendance(price, date, user);

        // Remove this spagetti hardcoded trash on finishing this season
        validateJ2(user, liturgyName);

        saveAttendance(liturgyName, date, user);
    }

    // Remove this spagetti hardcoded trash on finishing this season
    private void validateJ2(User user, String liturgyName) {
        if (user.getSchoolYear().getId() != 9
                && Objects.equals(liturgyName, "Osret El-Alhan (for j2 only)")) {
            throw new InvalidAttendanceException("Not valid to your school year");
        }
    }

    private void validateAttendance(Price price, Date date, User user) {
        if (date == null) throw new InvalidAttendanceException("Date is required");

        if (date.before(Date.valueOf(eventStartDate)))
            throw new InvalidAttendanceException("This date is before the mosab2a start date");

        ZoneId zoneId = ZoneId.of("Africa/Cairo");

        Timestamp timestamp = new Timestamp(date.getTime());
        LocalDateTime givenDateTime = timestamp.toInstant().
                atZone(zoneId).toLocalDateTime();

        LocalDate previousSaturday = givenDateTime
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
                .toLocalDate();

        LocalDate nextSunday = givenDateTime
                .with(TemporalAdjusters.next(DayOfWeek.SUNDAY)).toLocalDate();

        Boolean alreadyExists = attendanceRepository.existsAttendancesBetween(user, price, previousSaturday, nextSunday);

        if (alreadyExists) throw new InvalidAttendanceException("You can't attend the same liturgy twice in the same week");
    }

    private void saveAttendance(String liturgyName, Date date, User user) {
        Attendance attendance = new Attendance();
        attendance.setPrice(priceService.getPrice(liturgyName, user.getSchoolYear().getLevel()));
        attendance.setUser(user);
        attendance.setDate(date);
        attendance.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        attendance.setApproved(false);
        attendanceRepository.save(attendance);
    }

    @Transactional
    public void approveAttendance(Long attendanceId) {
        Authenticatable admin = adminService.getAuthenticatable();

        Attendance attendance = attendanceRepository.findById(attendanceId).orElseThrow(AttendanceNotFoundException::new);

        if (!attendance.getUser().getSchoolYear().getId().equals(admin.getSchoolYear().getId()))
            throw new AttendanceNotFoundException();

        if (attendance.getApproved())
            throw new AttendanceAlreadyApproved("Attendance already approved");

        attendance.setApproved(true);

        Price price = attendance.getPrice();

        walletService.credit(attendance.getUser(), price.getCoins(), "Attended" + price.getName());

        attendanceRepository.save(attendance);
    }

    @Transactional
    public void deleteAttendance(Long attendanceId) {
        Attendance attendance = attendanceRepository.findById(attendanceId).orElseThrow(AttendanceNotFoundException::new);

        if (attendance.getApproved()) {
            Price price = attendance.getPrice();
            walletService.forceDebit(attendance.getUser(), price.getCoins(), price.getName() + " deleted");
        }

        attendanceRepository.delete(attendance);
    }

    @Transactional
    public void deleteAttendanceForUser(Long attendanceId, User user) {
        Attendance attendance = attendanceRepository
                .findByIdForUser(attendanceId, user)
                .orElseThrow(AttendanceNotFoundException::new);

        if (attendance.getApproved()) {
            throw new AttendanceAlreadyApproved("Cannot delete approved attendance");
        }

        attendanceRepository.delete(attendance);
    }

    public Page<AttendanceDTO> getUnapprovedAttendances(Pageable pageable, SchoolYear schoolYear) {
        Page<Attendance> unapprovedAttendances = attendanceRepository
                .findByApprovedAndUserContainingSchoolYear(pageable, false, schoolYear);

        return unapprovedAttendances.map(AttendanceDTO::new);
    }

    public Page<AttendanceDTO> getApprovedAttendancesOfUser(Pageable pageable) {
        Long userId = userService.getAuthenticatableId();
        return getAttendanceDTOS(userId, pageable, true);
    }

    public Page<AttendanceDTO> getApprovedAttendancesOfUser(Pageable pageable, Long userId) {
        return getAttendanceDTOS(userId, pageable, true);
    }

    public Page<AttendanceDTO> getPendingAttendancesOfUser(Pageable pageable) {
        Long userId = userService.getAuthenticatableId();
        return getAttendanceDTOS(userId, pageable, false);
    }

    public Page<AttendanceDTO> getPendingAttendancesOfUser(Pageable pageable, Long userId) {
        return getAttendanceDTOS(userId, pageable, false);
    }

    public Page<AttendanceDTO> getAllAttendancesOfUser(Pageable pageable) {
        long userId = userService.getAuthenticatableId();
        User user = userService.findOrFail(userId);
        Page<Attendance> allAttendances = attendanceRepository.findAttendanceByUser(user, pageable);
        return allAttendances.map(AttendanceDTO::new);
    }

    @NotNull
    private Page<AttendanceDTO> getAttendanceDTOS(Long userId, Pageable pageable, boolean approved) {
        User user = userService.findOrFail(userId);

        Page<Attendance> approvedAttendances = attendanceRepository
                .findByApprovedAndUser(pageable, approved, user);

        return approvedAttendances.map(AttendanceDTO::new);
    }
}

package com.stgsporting.piehmecup.repositories;

import com.stgsporting.piehmecup.entities.Attendance;
import com.stgsporting.piehmecup.entities.Price;
import com.stgsporting.piehmecup.entities.SchoolYear;
import com.stgsporting.piehmecup.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    @Query("SELECT a FROM ATTENDANCE a WHERE a.approved = :approved AND a.user.schoolYear = :schoolYear order by a.id desc")
    Page<Attendance> findByApprovedAndUserContainingSchoolYear(Pageable pageable, @Param("approved") Boolean approved, @Param("schoolYear") SchoolYear schoolYear);

    @Query("SELECT a FROM ATTENDANCE a WHERE a.approved = :approved AND a.user = :user order by a.id desc")
    Page<Attendance> findByApprovedAndUser(Pageable pageable, @Param("approved") Boolean approved, @Param("user") User user);

    @Query("SELECT a FROM ATTENDANCE a WHERE a.user = :user order by a.id desc")
    Page<Attendance> findAttendanceByUser(@Param("user") User user, Pageable pageable);

    @Query("SELECT a FROM ATTENDANCE a WHERE a.id = :attendanceId AND a.user = :user")
    Optional<Attendance> findByIdForUser(Long attendanceId, User user);

    @Query("SELECT count(a) > 0  FROM ATTENDANCE a WHERE a.date >= :startDate AND a.date < :endDate AND a.user = :user AND a.price = :price")
    Boolean existsAttendancesBetween(User user, Price price, LocalDate startDate, LocalDate endDate);

    @Query("SELECT a FROM ATTENDANCE a WHERE a.date >= :startDate AND a.date < :endDate AND a.user = :user AND a.price = :price")
    Optional<Attendance> findAttendanceBetween(User user, Price price, LocalDate startDate, LocalDate endDate);
}

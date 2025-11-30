package com.stgsporting.piehmecup.exceptions;

import net.minidev.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class RestResponseEntityExceptionResolver extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value
            = { NotFoundException.class, UserNotFoundException.class, SchoolYearNotFound.class, PlayerNotFoundException.class, IconNotFoundException.class, PositionNotFoundException.class, AttendanceNotFoundException.class, LiturgyNotFoundException.class, PriceNotFoundException.class })
    protected ResponseEntity<Object> handleNotFound(
            NotFoundException ex, WebRequest request) {
        return handleExceptionDefault(ex, HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(value = { UsernameTakenException.class })
    protected ResponseEntity<Object> handleUsernameTaken(UsernameTakenException ex, WebRequest request) {
        return handleExceptionDefault(ex, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(value = { UnownedPositionException.class, UnownedIconException.class, UnownedException.class })
    protected ResponseEntity<Object> handleUnowned(UnownedException ex, WebRequest request) {
        return handleExceptionDefault(ex, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(value = { IconAlreadyPurchasedException.class, PlayerAlreadyPurchasedException.class })
    protected ResponseEntity<Object> handleAlreadyPurchased(AlreadyPurchasedException ex, WebRequest request) {
        return handleExceptionDefault(ex, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(value = { AttendanceAlreadyApproved.class })
    protected ResponseEntity<Object> handleAttendanceAlreadyApproved(AttendanceAlreadyApproved ex, WebRequest request) {
        return handleExceptionDefault(ex, HttpStatus.BAD_REQUEST, request);
    }


    @ExceptionHandler(value = { IllegalArgumentException.class })
    protected ResponseEntity<Object> handleIllegal(IllegalArgumentException ex, WebRequest request) {
        return handleExceptionDefault(ex, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(value = { UserDeleteException.class })
    protected ResponseEntity<Object> handleIllegal(UserDeleteException ex, WebRequest request) {
        return handleExceptionDefault(ex, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(value = { InsufficientCoinsException.class })
    protected ResponseEntity<Object> handleInsufficientFunds(InsufficientCoinsException ex, WebRequest request) {
        return handleExceptionDefault(ex, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(value = { PositionOccupiedException.class })
    protected ResponseEntity<Object> handlePositionOccupied(PositionOccupiedException ex, WebRequest request) {
        return handleExceptionDefault(ex, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(value = { InvalidCredentialsException.class })
    protected ResponseEntity<Object> handleInvalidCred(InvalidCredentialsException ex, WebRequest request) {
        return handleExceptionDefault(ex, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(value = { MinRatingException.class })
    protected ResponseEntity<Object> handleMinRating(MinRatingException ex, WebRequest request) {
        return handleExceptionDefault(ex, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(value = { InvalidAttendanceException.class })
    protected ResponseEntity<Object> handleInvalidAttendance(InvalidAttendanceException ex, WebRequest request) {
        return handleExceptionDefault(ex, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(value = { IllegalSellingException.class })
    protected ResponseEntity<Object> handleIllegalSelling(IllegalSellingException ex, WebRequest request) {
        return handleExceptionDefault(ex, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(value = { UnauthorizedAccessException.class })
    protected ResponseEntity<Object> handleUnauthorized(UnauthorizedAccessException ex, WebRequest request) {
        return handleExceptionDefault(ex, HttpStatus.UNAUTHORIZED, request);
    }

    @ExceptionHandler(value = { UserNotInSameSchoolYearException.class })
    protected ResponseEntity<Object> handleNotSameSchoolYear(UserNotInSameSchoolYearException ex, WebRequest request) {
        return handleExceptionDefault(ex, HttpStatus.FORBIDDEN, request);
    }

    @ExceptionHandler(value = { UserAlreadyExistException.class })
    protected ResponseEntity<Object> handleAlreadyExists(UserAlreadyExistException ex, WebRequest request) {
        return handleExceptionDefault(ex, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(value = { ChangePasswordException.class })
    protected ResponseEntity<Object> handleChangePassword(ChangePasswordException ex, WebRequest request) {
        return handleExceptionDefault(ex, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(value = { DuplicateAttendanceException.class })
    protected ResponseEntity<Object> handleDuplicateAttendance(DuplicateAttendanceException ex, WebRequest request) {
        return handleExceptionDefault(ex, HttpStatus.BAD_REQUEST, request);
    }

    private ResponseEntity<Object> handleExceptionDefault(Exception ex, HttpStatus status, WebRequest request) {
        JSONObject response = new JSONObject();
        response.put("message", ex.getMessage());

        return handleExceptionInternal(ex, response, new HttpHeaders(), status, request);
    }
}

package com.szu.mallsystem.common;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusiness(BusinessException ex) {
        log.warn("Business exception: {}", ex.getMessage());
        return Result.failure(ex.getErrorCode(), ex.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public Result<Void> handleValidation(Exception ex) {
        String message;
        if (ex instanceof MethodArgumentNotValidException manve) {
            message = manve.getBindingResult()
                    .getFieldErrors()
                    .stream()
                    .map(err -> err.getField() + " " + err.getDefaultMessage())
                    .collect(Collectors.joining("; "));
        } else if (ex instanceof BindException be) {
            message = be.getBindingResult()
                    .getFieldErrors()
                    .stream()
                    .map(err -> err.getField() + " " + err.getDefaultMessage())
                    .collect(Collectors.joining("; "));
        } else {
            message = "Validation failed";
        }
        return Result.failure(ErrorCode.VALIDATION_ERROR, message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolation(ConstraintViolationException ex) {
        return Result.failure(ErrorCode.VALIDATION_ERROR, ex.getMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    public Result<Void> handleAuth(AuthenticationException ex) {
        return Result.failure(ErrorCode.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public Result<Void> handleAccessDenied(AccessDeniedException ex) {
        return Result.failure(ErrorCode.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleBadRequest(HttpMessageNotReadableException ex) {
        return Result.failure(ErrorCode.BAD_REQUEST, "Invalid request payload");
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleOther(Exception ex) {
        log.error("Unexpected error", ex);
        return Result.failure(ErrorCode.SERVER_ERROR, ErrorCode.SERVER_ERROR.getMessage());
    }
}

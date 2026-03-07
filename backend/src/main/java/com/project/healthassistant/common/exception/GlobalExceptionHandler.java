package com.project.healthassistant.common.exception;

import com.project.healthassistant.common.api.ApiResponse;
import com.project.healthassistant.common.api.ResultCode;
import com.project.healthassistant.common.trace.TraceIdHolder;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusinessException(BusinessException ex) {
        return ApiResponse.failure(ex.getCode(), ex.getMessage(), TraceIdHolder.getTraceId());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class, ConstraintViolationException.class, HttpMessageNotReadableException.class})
    public ApiResponse<Void> handleValidationException(Exception ex) {
        return ApiResponse.failure(ResultCode.INVALID_PARAM.getCode(), ex.getMessage(), TraceIdHolder.getTraceId());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ApiResponse<Void> handleAccessDeniedException(AccessDeniedException ex) {
        return ApiResponse.failure(ResultCode.FORBIDDEN, TraceIdHolder.getTraceId());
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception ex) {
        return ApiResponse.failure(ResultCode.SYSTEM_ERROR.getCode(), ex.getMessage(), TraceIdHolder.getTraceId());
    }
}

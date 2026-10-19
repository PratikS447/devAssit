package com.devopsbuddy;

import com.devopsbuddy.model.ApiError;
import com.devopsbuddy.service.CommandExecutor;
import com.devopsbuddy.service.OllamaService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.UUID;

@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> invalidRequest(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> unreadableRequest(HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", request);
    }

    @ExceptionHandler(OllamaService.OllamaUnavailableException.class)
    public ResponseEntity<ApiError> ollamaUnavailable(HttpServletRequest request) {
        return error(HttpStatus.SERVICE_UNAVAILABLE, "AI_SERVICE_UNAVAILABLE", request);
    }

    @ExceptionHandler(CommandExecutor.ExecutionDisabledException.class)
    public ResponseEntity<ApiError> executionDisabled(HttpServletRequest request) {
        return error(HttpStatus.SERVICE_UNAVAILABLE, "COMMAND_EXECUTION_DISABLED", request);
    }

    @ExceptionHandler(CommandExecutor.UnknownOperationException.class)
    public ResponseEntity<ApiError> unknownOperation(HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, "UNKNOWN_COMMAND_OPERATION", request);
    }

    @ExceptionHandler(CommandExecutor.ApprovalRequiredException.class)
    public ResponseEntity<ApiError> approvalRequired(HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "EXPLICIT_APPROVAL_REQUIRED", request);
    }

    @ExceptionHandler(CommandExecutor.CommandTimeoutException.class)
    public ResponseEntity<ApiError> commandTimeout(HttpServletRequest request) {
        return error(HttpStatus.GATEWAY_TIMEOUT, "COMMAND_TIMED_OUT", request);
    }

    @ExceptionHandler(CommandExecutor.CommandExecutionException.class)
    public ResponseEntity<ApiError> commandFailed(HttpServletRequest request) {
        return error(HttpStatus.BAD_GATEWAY, "COMMAND_EXECUTION_FAILED", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> unexpectedError(Exception exception, HttpServletRequest request) {
        String requestId = UUID.randomUUID().toString();
        log.error("Unhandled API error {} ({})", requestId, exception.getClass().getSimpleName());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiError("INTERNAL_ERROR", requestId));
    }

    private ResponseEntity<ApiError> error(HttpStatus status, String code, HttpServletRequest request) {
        return ResponseEntity.status(status).body(new ApiError(code, requestId(request)));
    }

    private String requestId(HttpServletRequest request) {
        Object requestId = request.getAttribute("requestId");
        if (requestId instanceof String value && !value.isBlank()) {
            return value;
        }
        String generated = UUID.randomUUID().toString();
        request.setAttribute("requestId", generated);
        return generated;
    }
}

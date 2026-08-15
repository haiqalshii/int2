package com.int2.common;

import com.int2.candidate.CvTooLargeException;
import com.int2.candidate.InvalidCvException;
import com.int2.email.InvalidEmailContextException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ApiError> notFound(ResourceNotFoundException exception, HttpServletRequest request) {
        return response(HttpStatus.NOT_FOUND, exception.getMessage(), request);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, jakarta.validation.ConstraintViolationException.class})
    ResponseEntity<ApiError> invalidRequest(Exception exception, HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, "Invalid request.", request);
    }

    @ExceptionHandler({CvTooLargeException.class, MaxUploadSizeExceededException.class})
    ResponseEntity<ApiError> tooLarge(Exception exception, HttpServletRequest request) {
        return response(HttpStatus.PAYLOAD_TOO_LARGE, "CV files must not exceed 10 MB.", request);
    }

    @ExceptionHandler(InvalidCvException.class)
    ResponseEntity<ApiError> invalidCv(InvalidCvException exception, HttpServletRequest request) {
        return response(HttpStatus.UNPROCESSABLE_ENTITY, exception.getMessage(), request);
    }

    @ExceptionHandler(InvalidEmailContextException.class)
    ResponseEntity<ApiError> invalidEmailContext(InvalidEmailContextException exception, HttpServletRequest request) {
        return response(HttpStatus.UNPROCESSABLE_ENTITY, exception.getMessage(), request);
    }

    @ExceptionHandler(ExternalServiceException.class)
    ResponseEntity<ApiError> providerFailure(ExternalServiceException exception, HttpServletRequest request) {
        log.error("External provider failure for {}: {}", request.getRequestURI(), exception.getMessage());
        return response(HttpStatus.BAD_GATEWAY, exception.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> unexpected(Exception exception, HttpServletRequest request) {
        log.error("Unexpected API error for {}", request.getRequestURI(), exception);
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.", request);
    }

    private ResponseEntity<ApiError> response(HttpStatus status, String message, HttpServletRequest request) {
        return ResponseEntity.status(status).body(new ApiError(
                Instant.now(), status.value(), status.getReasonPhrase(), message, request.getRequestURI()));
    }
}

package com.bms.account.exception;

import com.bms.account.dtos.accountPin.PinErrorResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<ErrorResponseDTO> handleAllExceptions(Exception ex, WebRequest request) {
//        ErrorResponseDTO error = new ErrorResponseDTO(
//                LocalDateTime.now(),
//                HttpStatus.INTERNAL_SERVER_ERROR.value(),
//                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
//                ex.getMessage(),
//                request.getDescription(false)
//        );
//        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
//    }
//
//    // Handle validation errors (DTO @Valid)
//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public ResponseEntity<ErrorResponseDTO> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
//        String errors = ex.getBindingResult()
//                .getFieldErrors()
//                .stream()
//                .map(err -> err.getField() + ": " + err.getDefaultMessage())
//                .collect(Collectors.joining(", "));
//
//        ErrorResponseDTO error = new ErrorResponseDTO(
//                LocalDateTime.now(),
//                HttpStatus.BAD_REQUEST.value(),
//                HttpStatus.BAD_REQUEST.getReasonPhrase(),
//                errors,
//                request.getDescription(false)
//        );
//        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
//    }
//
//    // Handle custom NotFoundException (you can create this later)
//    @ExceptionHandler(ResourceNotFoundException.class)
//    public ResponseEntity<ErrorResponseDTO> handleNotFound(ResourceNotFoundException ex, WebRequest request) {
//        ErrorResponseDTO error = new ErrorResponseDTO(
//                LocalDateTime.now(),
//                HttpStatus.NOT_FOUND.value(),
//                HttpStatus.NOT_FOUND.getReasonPhrase(),
//                ex.getMessage(),
//                request.getDescription(false)
//        );
//        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
//    }

    // Handle Resource Not Found (404)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("Resource Not Found: {}", ex.getMessage());
        return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND, request.getRequestURI());
    }

    // Handle Duplicate Account (409)
    @ExceptionHandler(AccountAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDTO> handleAccountAlreadyExists(AccountAlreadyExistsException ex, HttpServletRequest request) {
        log.warn("Duplicate Account: {}", ex.getMessage());
        return buildErrorResponse(ex.getMessage(), HttpStatus.CONFLICT, request.getRequestURI());
    }

    // Handle Invalid PIN (403)
    @ExceptionHandler(InvalidPinException.class)
    public ResponseEntity<?> handleInvalidPin(InvalidPinException ex) {

        PinErrorResponse response = PinErrorResponse.builder()
                .message(ex.getMessage())
                .attempts(ex.getAttempts())
                .attemptsRemaining(ex.getRemaining())
                .locked(ex.isLocked())
                .lockExpiresInSeconds(ex.getLockExpiresInSeconds())
                .build();

        if (ex.isLocked()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        return ResponseEntity.badRequest().body(response);
    }

    // Handle Insufficient Balance (400)
    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ErrorResponseDTO> handleInsufficientBalance(InsufficientBalanceException ex, HttpServletRequest request) {
        log.warn("Insufficient Funds: {}", ex.getMessage());
        return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST, request.getRequestURI());
    }

    // Handle KYC Errors (400)
    @ExceptionHandler(KycValidationException.class)
    public ResponseEntity<ErrorResponseDTO> handleKycValidation(KycValidationException ex, HttpServletRequest request) {
        log.warn("KYC Validation Failed: {}", ex.getMessage());
        return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST, request.getRequestURI());
    }

    // Handle invalid input/logic errors (400)
    @ExceptionHandler(AccountStateException.class)
    public ResponseEntity<ErrorResponseDTO> handleAccountState(
            AccountStateException ex,
            HttpServletRequest request) {

        ErrorResponseDTO error = new ErrorResponseDTO(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    // Handle validation errors (DTO field validation)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String validationErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        log.warn("Validation Failed: {}", validationErrors);
        return buildErrorResponse(validationErrors, HttpStatus.BAD_REQUEST, request.getRequestURI());
    }

    // Handle all OTHER unexpected exceptions (500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGlobalException(Exception ex, HttpServletRequest request) {
        log.error("Unexpected Error at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return buildErrorResponse("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR, request.getRequestURI());
    }
    // 🔥 When Feign throws 400 Bad Request from another service
    @ExceptionHandler(FeignException.BadRequest.class)
    public ResponseEntity<ErrorResponseDTO> handleFeignBadRequest(
            FeignException.BadRequest ex,
            WebRequest request
    ) {
        String message = extractMessageFromFeign(ex);

        ErrorResponseDTO error = new ErrorResponseDTO(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                message,
                request.getDescription(false)
        );

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ErrorResponseDTO> handleFeignException(
            FeignException ex,
            WebRequest request
    ) {
        HttpStatus status = switch (ex.status()) {
            case 404 -> HttpStatus.NOT_FOUND;
            case 400 -> HttpStatus.BAD_REQUEST;
            case 401 -> HttpStatus.UNAUTHORIZED;
            case 403 -> HttpStatus.FORBIDDEN;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };

        ErrorResponseDTO response = new ErrorResponseDTO(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(response, status);
    }

    private String extractMessageFromFeign(FeignException ex) {
        try {
            String json = ex.contentUTF8();
            JsonNode node = new ObjectMapper().readTree(json);

            if (node.has("message")) {
                return node.get("message").asText();
            }
            return json;
        } catch (Exception e) {
            return ex.getMessage();
        }
    }
    // ------------ HELPER --------------
    private ResponseEntity<ErrorResponseDTO> buildErrorResponse(String message, HttpStatus status, String path) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path
        );
        return new ResponseEntity<>(errorResponse, status);
    }
}

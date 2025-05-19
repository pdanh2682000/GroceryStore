package com.danhuy.common_service.exception;

import com.danhuy.common_service.enums.MessageEnum;
import com.danhuy.common_service.exception.ex.AppException;
import com.danhuy.common_service.exception.ex.NotFoundException;
import com.danhuy.common_service.response.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  /**
   * Resource not found exception (url,...)
   *
   * @param ex NoResourceFoundException
   * @return exception
   */
  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<ApiResponse<?>> handleNoResourceFoundException(
      NoResourceFoundException ex) {
    log.error("No resource found exception: {}", ex.getMessage());

    ApiResponse<?> apiResponse = new ApiResponse<>();
    apiResponse.setCode(MessageEnum.RESOURCE_NOT_FOUND_EXCEPTION.getCode());
    apiResponse.setMessage(MessageEnum.RESOURCE_NOT_FOUND_EXCEPTION.getMessage());

    return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
  }

  /**
   * Not found exception
   *
   * @param ex NotFoundException
   * @return exception
   */
  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<ApiResponse<?>> handleEntityNotFoundException(
      NotFoundException ex) {
    log.error("Entity not found exception: {}", ex.getMessage());

    ApiResponse<?> apiResponse = new ApiResponse<>();
    apiResponse.setCode(MessageEnum.NOT_FOUND_EXCEPTION.getCode());
    apiResponse.setMessage(MessageEnum.NOT_FOUND_EXCEPTION.getMessage());

    return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
  }

  /**
   * Validation error
   *
   * @param ex MethodArgumentNotValidException
   * @return exception
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(
      MethodArgumentNotValidException ex) {
    log.error("Validation exception: {}", ex.getMessage());

    Map<String, String> errors = new HashMap<>();
    ApiResponse<Map<String, String>> apiResponse = new ApiResponse<>();
    apiResponse.setCode(MessageEnum.VALIDATION_EXCEPTION.getCode());
    apiResponse.setMessage(MessageEnum.VALIDATION_EXCEPTION.getMessage());

    ex.getBindingResult().getAllErrors().forEach((error) -> {
      String fieldName = ((FieldError) error).getField();
      String errorMessage = error.getDefaultMessage();
      errors.put(fieldName, errorMessage);
    });

    apiResponse.setResult(errors);

    return ResponseEntity.badRequest().body(apiResponse);
  }

  /**
   * Violation exception
   *
   * @param ex ConstraintViolationException
   * @return exception
   */
  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ApiResponse<Map<String, String>>> handleConstraintViolationException(
      ConstraintViolationException ex) {
    log.error("Constraint violation exception: {}", ex.getMessage());

    Map<String, String> errors = new HashMap<>();
    ApiResponse<Map<String, String>> apiResponse = new ApiResponse<>();
    apiResponse.setCode(MessageEnum.VIOLATION_EXCEPTION.getCode());
    apiResponse.setMessage(MessageEnum.VIOLATION_EXCEPTION.getMessage());

    ex.getConstraintViolations().forEach(violation -> {
      String propertyPath = violation.getPropertyPath().toString();
      String message = violation.getMessage();
      errors.put(propertyPath, message);
    });

    apiResponse.setResult(errors);

    return ResponseEntity.badRequest().body(apiResponse);
  }

  /**
   * App exception
   *
   * @param ex AppException
   * @return exception
   */
  @ExceptionHandler(AppException.class)
  public ResponseEntity<ApiResponse<?>> handleAppException(AppException ex) {
    log.error("App exception: {}", ex.getMessage());
    MessageEnum messageEnum = ex.getMessageEnum();
    Object[] args = ex.getArgsFormated();

    ApiResponse<?> apiResponse = new ApiResponse<>();
    apiResponse.setCode(messageEnum.getCode());
    apiResponse.setMessage(MessageFormat.format(messageEnum.getMessage(), args));

    return ResponseEntity.badRequest().body(apiResponse);
  }

  /**
   * DateTime Parse Exception
   *
   * @param ex HttpMessageNotReadableException
   * @return exception
   */
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ApiResponse<?>> handleDateTimeParseException(
      HttpMessageNotReadableException ex) {
    log.error("DateTimeParseException: {}", ex.getMessage());

    ApiResponse<?> apiResponse = new ApiResponse<>();
    apiResponse.setCode(MessageEnum.UNCATEGORIZED_EXCEPTION.getCode());
    apiResponse.setMessage(ex.getMessage());

    return ResponseEntity.badRequest().body(apiResponse);
  }

  /**
   * Server error
   *
   * @param ex Exception
   * @return ex
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<?>> handleGenericException(Exception ex) {
    log.error("Unhandled exception occurred: ", ex);

    ApiResponse<?> apiResponse = new ApiResponse<>();
    apiResponse.setCode(MessageEnum.UNCATEGORIZED_EXCEPTION.getCode());
    apiResponse.setMessage(MessageEnum.UNCATEGORIZED_EXCEPTION.getMessage());

    return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}

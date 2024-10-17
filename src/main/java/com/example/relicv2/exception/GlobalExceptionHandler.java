package com.example.relicv2.exception;

import com.example.relicv2.dto.ErrorDetailsDto;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors()
                .stream().map(FieldError::getDefaultMessage).collect(Collectors.toList());
        return new ResponseEntity<>(getErrorsMap(errors), new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    private Map<String, List<String>> getErrorsMap(List<String> errors) {
        Map<String, List<String>> errorResponse = new HashMap<>();
        errorResponse.put("errors", errors);
        return errorResponse;
    }

    @ExceptionHandler(value = AlreadyExistsException.class)
    public ResponseEntity<ErrorDetailsDto> handleAlreadyExistsException(AlreadyExistsException exception){
        ErrorDetailsDto errorDetails = new ErrorDetailsDto(exception.getMessage(),
                HttpStatus.CONFLICT, new Date());
        return new ResponseEntity<>(errorDetails, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(value = ResourceNotFoundException.class)
    public ResponseEntity<ErrorDetailsDto> handleResourceNotFoundException(ResourceNotFoundException exception){
        ErrorDetailsDto errorDetails = new ErrorDetailsDto(exception.getMessage(),
                HttpStatus.NOT_FOUND, new Date());
        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = CustomException.class)
    public ResponseEntity<ErrorDetailsDto> handleCustomException(CustomException exception){
        ErrorDetailsDto errorDetails = new ErrorDetailsDto(exception.getMessage(),
                HttpStatus.BAD_REQUEST, new Date());
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = BiddingUnauthorizedException.class)
    public ResponseEntity<ErrorDetailsDto> handleBiddingUnauthorizedException(
            BiddingUnauthorizedException biddingUnauthorizedException) {
        ErrorDetailsDto errorDetails = new ErrorDetailsDto(biddingUnauthorizedException.getMessage(),
                HttpStatus.UNAUTHORIZED, new Date());
        return new ResponseEntity<>(errorDetails, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(value = MediaUploadException.class)
    public ResponseEntity<ErrorDetailsDto> handleMediaUploadException(MediaUploadException mediaUploadException) {
        ErrorDetailsDto errorDetails = new ErrorDetailsDto(mediaUploadException.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR, new Date());
        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

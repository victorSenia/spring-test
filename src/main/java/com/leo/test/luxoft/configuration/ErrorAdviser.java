package com.leo.test.luxoft.configuration;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;

/**
 * @author Senchenko Victor
 */
@RestControllerAdvice
public class ErrorAdviser {
    @ExceptionHandler({ResourceAccessException.class})
    public ResponseEntity<String> handleRequest(ResourceAccessException ex) {
        return new ResponseEntity<>(ex.getLocalizedMessage(), HttpStatus.SERVICE_UNAVAILABLE);
    }

}

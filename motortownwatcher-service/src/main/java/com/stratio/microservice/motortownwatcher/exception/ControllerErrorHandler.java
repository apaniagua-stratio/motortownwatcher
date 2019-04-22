package com.stratio.microservice.motortownwatcher.exception;

import com.stratio.microservice.motortownwatcher.generated.rest.model.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;


@ControllerAdvice
@RequestMapping
@Slf4j
public class ControllerErrorHandler {

    private static final String BAD_REQUEST_MSG = "Bad request";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> badRequest(Exception e) {
        log.error(e.getMessage());
        return new ResponseEntity<>(new ErrorResponse().error(BAD_REQUEST_MSG).data(e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> CustomException(
        CustomException e) {
        log.error(e.getMessage());
        return new ResponseEntity<>(new ErrorResponse().error(e.getData()).data(e.getError()), e.getStatus());
    }

}

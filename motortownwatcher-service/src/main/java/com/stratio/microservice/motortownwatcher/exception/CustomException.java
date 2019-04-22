package com.stratio.microservice.motortownwatcher.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatus;


@Data
@EqualsAndHashCode(callSuper=false)
@AllArgsConstructor
public class CustomException extends RuntimeException {

    private String data;
    private String error;
    private HttpStatus status;


}

package com.TaskFlow.UserService.exception;

import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Data
public class ApiError {

    private LocalDateTime timestamp;
    private String error;
    private HttpStatus statusCode;

    public ApiError(){this.timestamp = LocalDateTime.now();}

    public ApiError(String e, HttpStatus code){
        this();
        this.error = e;
        this.statusCode = code;
    }

}

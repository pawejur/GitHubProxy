package com.example.githubProxy;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
public class GithubExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    DtoError userNotFound(UserNotFoundException exception) {
        return new DtoError(HttpStatus.NOT_FOUND.value(), exception.getMessage());
    }
}

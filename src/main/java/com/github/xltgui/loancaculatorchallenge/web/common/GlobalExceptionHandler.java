package com.github.xltgui.loancaculatorchallenge.web.common;

import com.github.xltgui.loancaculatorchallenge.api.CustomFieldError;
import com.github.xltgui.loancaculatorchallenge.api.ResponseError;
import com.github.xltgui.loancaculatorchallenge.exception.InvalidDateDateException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidDateDateException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ResponseError handleInvalidDateDateException(InvalidDateDateException ex){
        return ResponseError.defaultResponse(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ResponseError handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        List<FieldError> fieldErrors = e.getFieldErrors();

        List<CustomFieldError> errorsList = fieldErrors.stream().
                map(fe -> new CustomFieldError(fe.getField(), fe.getDefaultMessage()))
                .collect(Collectors.toList());

        return new ResponseError("Validation error", errorsList);

    }
}

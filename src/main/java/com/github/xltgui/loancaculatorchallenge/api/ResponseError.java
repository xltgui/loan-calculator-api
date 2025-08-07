package com.github.xltgui.loancaculatorchallenge.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ResponseError(
        String message,
        List<CustomFieldError> errorsList
) {

    public static ResponseError defaultResponse(String message) {
        return new ResponseError(message, List.of());
    }
}

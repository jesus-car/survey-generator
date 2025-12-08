package com.surveygenerator.surveygenerator.utils;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ValidatorResult<T> {

    private final boolean valid;
    private final String errorMessage;
    private final T data;

    public static <T> ValidatorResult<T> success(T data) {
        return new ValidatorResult<>(true, null, data);
    }

    public static <T> ValidatorResult<T> error(String errorMessage) {
        return new ValidatorResult<>(false, errorMessage, null);
    }

    public boolean isValid() {
        return valid;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public T getData() {
        return data;
    }
}
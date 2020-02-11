package uk.gov.companieshouse.orders.api.controller;

import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;

public class ApiError {
    private final HttpStatus status;
    private List<String> errors = new ArrayList<>();

    public ApiError(final HttpStatus status, final List<String> errors) {
        this.status = status;
        this.errors = errors;
    }

    public ApiError(final HttpStatus status, final String error) {
        this.status = status;
        this.errors.add(error);
    }

    public HttpStatus getStatus() {
        return status;
    }

    public List<String> getErrors() {
        return errors;
    }
}

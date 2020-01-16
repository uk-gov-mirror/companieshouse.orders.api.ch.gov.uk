package uk.gov.companieshouse.orders.api.model;

import org.springframework.http.HttpStatus;

import java.util.List;

public class ApiError {

    private final HttpStatus status;

    private final List<String> errors;

    public ApiError(final HttpStatus status, final List<String> errors) {
        super();
        this.status = status;
        this.errors = errors;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public List<String> getErrors() {
        return errors;
    }

}

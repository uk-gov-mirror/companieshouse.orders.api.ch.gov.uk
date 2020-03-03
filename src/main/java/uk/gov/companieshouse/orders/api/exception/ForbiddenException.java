package uk.gov.companieshouse.orders.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        this(message, null);
    }

    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }
}

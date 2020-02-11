package uk.gov.companieshouse.orders.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ConflictException extends RuntimeException {
    private static final long serialVersionUID = 7992904489502842099L;

    public ConflictException() {
        this("Conflict");
    }

    public ConflictException(String message) {
        this(message, (Throwable)null);
    }

    public ConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
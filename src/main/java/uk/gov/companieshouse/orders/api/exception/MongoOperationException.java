package uk.gov.companieshouse.orders.api.exception;

public class MongoOperationException extends RuntimeException {
    public MongoOperationException(String message, Throwable cause) { super(message, cause); }
}

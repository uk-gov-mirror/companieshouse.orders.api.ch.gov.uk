package uk.gov.companieshouse.orders.api.exception;

public class KafkaMessagingException extends RuntimeException {

    public KafkaMessagingException(String message, Throwable cause) {
        super(message, cause);
    }

}

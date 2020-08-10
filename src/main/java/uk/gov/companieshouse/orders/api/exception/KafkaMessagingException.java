package uk.gov.companieshouse.orders.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class KafkaMessagingException extends RuntimeException {

    public KafkaMessagingException(String message) {
        super(message);
    }

}

package uk.gov.companieshouse.orders.api.converter;

import static uk.gov.companieshouse.orders.api.OrdersApiApplication.APPLICATION_NAMESPACE;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

public class LocalDateTimeConverter {
    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAMESPACE);
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
    
    public static String convertLocalDateTimeToString(LocalDateTime localDateTime) {
        return localDateTime.format(FORMATTER);
    }
    
    public static LocalDateTime convertStringToLocalDateTime(String string) throws DateTimeParseException {
        try {
            return LocalDateTime.parse(string);
        } catch(DateTimeParseException dtpe) {
            LOGGER.error("Parsing error when trying to convert string to date", dtpe);
            throw dtpe;
        }
    }

}

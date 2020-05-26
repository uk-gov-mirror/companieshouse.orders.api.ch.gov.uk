package uk.gov.companieshouse.orders.api.converter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeConverter {
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
    
    private LocalDateTimeConverter() {
        // empty constructor
    }
    
    public static String convertLocalDateTimeToString(LocalDateTime localDateTime) {
        return localDateTime.format(FORMATTER);
    }
    
    public static LocalDateTime convertStringToLocalDateTime(String string) {
        return LocalDateTime.parse(string);
    }

}

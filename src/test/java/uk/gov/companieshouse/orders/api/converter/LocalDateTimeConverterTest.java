package uk.gov.companieshouse.orders.api.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class LocalDateTimeConverterTest {
    
    private final static String BST_STRING = "2020-04-21T10:11:12.345";
    private final static String GMT_STRING = "2020-01-21T10:11:12.345";
    private final static LocalDateTime GMT = LocalDateTime.parse(GMT_STRING);
    private final static LocalDateTime BST = LocalDateTime.parse(BST_STRING);

    @Test
    @DisplayName("Test to convert a String containing a BST date to a LocalDateTime object")
    public void successfulConvertSummerTimeDateStringToLocalDateTime() {
        LocalDateTime result = LocalDateTimeConverter.convertStringToLocalDateTime(BST_STRING);
        assertEquals(2020, result.getYear());
        assertEquals(4, result.getMonthValue());
        assertEquals(21, result.getDayOfMonth());
        assertEquals(10, result.getHour());
        assertEquals(11, result.getMinute());
        assertEquals(12, result.getSecond());
    }
    
    @Test
    @DisplayName("Test to convert a String containing a GMT date to a LocalDateTime object")
    public void successfulConvertGMTDateStringToLocalDateTime() {
        LocalDateTime result = LocalDateTimeConverter.convertStringToLocalDateTime(GMT_STRING);
        assertEquals(2020, result.getYear());
        assertEquals(1, result.getMonthValue());
        assertEquals(21, result.getDayOfMonth());
        assertEquals(10, result.getHour());
        assertEquals(11, result.getMinute());
        assertEquals(12, result.getSecond());
    }
    
    @Test
    @DisplayName("Test to convert a LocalDateTime object representing a BST date to a String")
    public void successfulConvertSummerTimeDateLocalDateTimeToString() {
        String result = LocalDateTimeConverter.convertLocalDateTimeToString(BST);
        assertEquals(BST_STRING, result);
    }
   
    @Test
    @DisplayName("Test to convert a LocalDateTime object representing a GMT date to a String")
    public void successfulConvertGMTDateLocalDateTimeToString() {
        String result = LocalDateTimeConverter.convertLocalDateTimeToString(GMT);
        assertEquals(GMT_STRING, result);
    }

    @Test
    @DisplayName("Test to convert a String causes parsing exception")
    public void convertStringToLocalDateTimeCausesException() {
        String time = "Time";
        assertThrows(DateTimeParseException.class, () -> {
            LocalDateTimeConverter.convertStringToLocalDateTime(time);
        });
    }

}

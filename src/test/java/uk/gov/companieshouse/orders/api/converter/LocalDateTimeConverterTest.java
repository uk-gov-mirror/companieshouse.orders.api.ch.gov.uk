package uk.gov.companieshouse.orders.api.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class LocalDateTimeConverterTest {
    
    private final String bstString = "2020-04-21T10:11:12.345";
    private final String gmtString = "2020-01-21T10:11:12.345";
    private final LocalDateTime gmt = LocalDateTime.parse(gmtString);
    private final LocalDateTime bst = LocalDateTime.parse(bstString);

    @Test
    @DisplayName("Test to convert a String containing a BST date to a LocalDateTime object")
    public void successfulConvertSummerTimeDateStringToLocalDateTime() {
        LocalDateTime result = LocalDateTimeConverter.convertStringToLocalDateTime(bstString);
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
        LocalDateTime result = LocalDateTimeConverter.convertStringToLocalDateTime(gmtString);
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
        String result = LocalDateTimeConverter.convertLocalDateTimeToString(bst);
        assertEquals(bstString, result);
    }
   
    @Test
    @DisplayName("Test to convert a LocalDateTime object representing a GMT date to a String")
    public void successfulConvertGMTDateLocalDateTimeToString() {
        String result = LocalDateTimeConverter.convertLocalDateTimeToString(gmt);
        assertEquals(gmtString, result);
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

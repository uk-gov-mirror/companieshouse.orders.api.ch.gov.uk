package uk.gov.companieshouse.orders.api.converter;

import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;

public class EnumValueConverterTest {

    enum COLOUR {
        RED,
        YELLOW,
        BABY_BLUE
    }

    @Test
    public void successfullyConvertsEnumsToKebabAndLowerCase() {
        assertEquals("baby-blue", EnumValueNameConverter.convertEnumValueNameToJson(COLOUR.BABY_BLUE));
        assertEquals("red", EnumValueNameConverter.convertEnumValueNameToJson(COLOUR.RED));
        assertEquals("yellow", EnumValueNameConverter.convertEnumValueNameToJson(COLOUR.YELLOW));
    }

    @Test
    public void successfullyConvertsKebabAndLowerCaseToEnum() {
        assertEquals(COLOUR.BABY_BLUE.toString(), EnumValueNameConverter.convertEnumValueJsonToName("baby-blue"));
        assertEquals(COLOUR.RED.toString(), EnumValueNameConverter.convertEnumValueJsonToName("red"));
        assertEquals(COLOUR.YELLOW.toString(), EnumValueNameConverter.convertEnumValueJsonToName("yellow"));
    }


}

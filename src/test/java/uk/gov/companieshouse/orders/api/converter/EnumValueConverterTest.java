package uk.gov.companieshouse.orders.api.converter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.companieshouse.orders.api.util.EricHeaderHelper;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.orders.api.util.TestConstants.ERIC_IDENTITY_HEADER_NAME;
import static uk.gov.companieshouse.orders.api.util.TestConstants.ERIC_IDENTITY_VALUE;

@RunWith(MockitoJUnitRunner.class)
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

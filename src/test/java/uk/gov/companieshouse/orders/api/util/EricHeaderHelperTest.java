package uk.gov.companieshouse.orders.api.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.orders.api.util.TestConstants.*;

@ExtendWith(MockitoExtension.class)
public class EricHeaderHelperTest {

    @Mock
    private HttpServletRequest request;

    @Test
    public void testGetIdentity() {
        when(request.getHeader(ERIC_IDENTITY_HEADER_NAME)).thenReturn(ERIC_IDENTITY_VALUE);
        assertEquals(ERIC_IDENTITY_VALUE, EricHeaderHelper.getIdentity(request));
    }

    @Test
    public void testGetIdentityType() {
        when(request.getHeader(ERIC_IDENTITY_TYPE_HEADER_NAME)).thenReturn(ERIC_IDENTITY_TYPE_VALUE);
        assertEquals(ERIC_IDENTITY_TYPE_VALUE, EricHeaderHelper.getIdentityType(request));
    }

}

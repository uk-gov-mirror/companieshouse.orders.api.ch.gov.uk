package uk.gov.companieshouse.orders.api.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
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
        when(request.getHeader(ERIC_IDENTITY_TYPE_HEADER_NAME)).thenReturn(ERIC_IDENTITY_OAUTH2_TYPE_VALUE);
        assertEquals(ERIC_IDENTITY_OAUTH2_TYPE_VALUE, EricHeaderHelper.getIdentityType(request));
    }

    @Test
    public void testGetAuthorisedUser() {
        when(request.getHeader(ERIC_AUTHORISED_USER_HEADER_NAME)).thenReturn(ERIC_AUTHORISED_USER_VALUE);
        assertEquals(ERIC_AUTHORISED_USER_VALUE, EricHeaderHelper.getAuthorisedUser(request));
    }

    @Test
    @DisplayName("getIdentity returns null where identity header is blank")
    public void blankIdentityGotAsNull() {
        when(request.getHeader(ERIC_IDENTITY_HEADER_NAME)).thenReturn("");
        assertThat(EricHeaderHelper.getIdentity(request), is(nullValue()));
    }

    @Test
    @DisplayName("getIdentityType returns null where identity type header is blank")
    public void blankIdentityTypeGotAsNull() {
        when(request.getHeader(ERIC_IDENTITY_TYPE_HEADER_NAME)).thenReturn("");
        assertThat(EricHeaderHelper.getIdentityType(request), is(nullValue()));
    }

    @Test
    @DisplayName("getAuthorisedUser returns null where authorised user header is blank")
    public void blankAuthorisedUserGotAsNull() {
        when(request.getHeader(ERIC_AUTHORISED_USER_HEADER_NAME)).thenReturn("");
        assertThat(EricHeaderHelper.getAuthorisedUser(request), is(nullValue()));
    }

}

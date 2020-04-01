package uk.gov.companieshouse.orders.api.interceptor;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.test.context.EmbeddedKafka;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpMethod.*;
import static uk.gov.companieshouse.orders.api.interceptor.UserAuthenticationInterceptor.*;
import static uk.gov.companieshouse.orders.api.util.EricHeaderHelper.*;
import static uk.gov.companieshouse.orders.api.util.TestConstants.ERIC_IDENTITY_VALUE;

/**
 * Unit/integration tests the {@link UserAuthenticationInterceptor} class.
 */
@SpringBootTest
@EmbeddedKafka
public class UserAuthenticationInterceptorTests {

    @Autowired
    private UserAuthenticationInterceptor interceptorUnderTest;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Object handler;

    @Test
    @DisplayName("preHandle accepts a request it has not been configured to authenticate")
    void preHandleAcceptsUnknownRequest() {

        // Given
        givenRequest(DELETE, "/unknown");

        // When and then
        assertThat(interceptorUnderTest.preHandle(request, response, handler), is(true));
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    @DisplayName("preHandle rejects a request it has been configured to authenticate that lacks required headers")
    void preHandleRejectsUnauthenticatedRequest() {

        // Given
        givenRequest(POST, "/basket/items");

        // When and then
        assertThat(interceptorUnderTest.preHandle(request, response, handler), is(false));
        verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    @DisplayName("preHandle accepts a request it has been configured to authenticate that has the required headers")
    void preHandleAcceptsAuthenticatedRequest() {

        // Given
        givenRequest(POST, "/basket/items");
        givenRequestHasSignedInUser();

        // When and then
        assertThat(interceptorUnderTest.preHandle(request, response, handler), is(true));
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    @DisplayName("getRequestMappingInfo gets the add item request mapping")
    void getRequestMappingInfoGetsAddItem() {

        // Given
        givenRequest(POST, "/basket/items");

        // When and then
        assertThat(interceptorUnderTest.getRequestMapping(request).getName(), is(ADD_ITEM));
    }

    @Test
    @DisplayName("getRequestMappingInfo gets the checkout basket request mapping")
    void getRequestMappingInfoGetsCheckoutBasket() {

        // Given
        givenRequest(POST, "/basket/checkouts");

        // When and then
        assertThat(interceptorUnderTest.getRequestMapping(request).getName(), is(CHECKOUT_BASKET));
    }

    @Test
    @DisplayName("getRequestMappingInfo gets the get payment details request mapping")
    void getRequestMappingInfoGetsGetPaymentDetails() {

        // Given
        givenRequest(GET, "/basket/checkouts/{checkoutId}/payment");

        // When and then
        assertThat(interceptorUnderTest.getRequestMapping(request).getName(), is(GET_PAYMENT_DETAILS));
    }

    @Test
    @DisplayName("getRequestMappingInfo gets the patch basket request mapping")
    void getRequestMappingInfoGetsPatchBasket() {

        // Given
        givenRequest(PATCH, "/basket");

        // When and then
        assertThat(interceptorUnderTest.getRequestMapping(request).getName(), is(PATCH_BASKET));
    }

    @Test
    @DisplayName("getRequestMappingInfo gets the patch payment details request mapping")
    void getRequestMappingInfoGetsPatchPaymentDetails() {

        // Given
        givenRequest(PATCH, "/basket/checkouts/{id}/payment");

        // When and then
        assertThat(interceptorUnderTest.getRequestMapping(request).getName(), is(PATCH_PAYMENT_DETAILS));
    }

    @Test
    @DisplayName("getRequestMappingInfo gets the get order request mapping")
    void getRequestMappingInfoGetsGetOrder() {

        // Given
        givenRequest(GET, "/orders/{id}");

        // When and then
        assertThat(interceptorUnderTest.getRequestMapping(request).getName(), is(GET_ORDER));
    }

    @Test
    @DisplayName("getRequestMappingInfo returns null where no mapping found")
    void getRequestMappingInfoReturnsNullWhereNoMappingFound() {

        // Given
        givenRequest(DELETE, "/unknown/uri");

        // When and then
        assertThat(interceptorUnderTest.getRequestMapping(request), is(nullValue()));
    }

    /**
     * Sets up request givens.
     * @param method the HTTP request method
     * @param uri the request URI
     */
    private void givenRequest(final HttpMethod method, final String uri) {
        when(request.getMethod()).thenReturn(method.name());
        when(request.getRequestURI()).thenReturn(uri);
        when(request.getContextPath()).thenReturn("");
        when(request.getServletPath()).thenReturn("");
    }

    /**
     * Sets up request with required header values to represent a signed in user.
     */
   private void givenRequestHasSignedInUser() {
        when(request.getHeader(ERIC_IDENTITY_TYPE)).thenReturn(OAUTH2_IDENTITY_TYPE);
       when(request.getHeader(ERIC_IDENTITY)).thenReturn(ERIC_IDENTITY_VALUE);
   }

}

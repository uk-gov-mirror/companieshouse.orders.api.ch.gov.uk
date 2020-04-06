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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpMethod.PATCH;
import static uk.gov.companieshouse.api.util.security.EricConstants.ERIC_AUTHORISED_KEY_ROLES;
import static uk.gov.companieshouse.api.util.security.SecurityConstants.INTERNAL_USER_ROLE;

/**
 * Unit/integration tests the {@link UserAuthorisationInterceptor} class.
 */
@SpringBootTest
@EmbeddedKafka
public class UserAuthorisationInterceptorTests {

    @Autowired
    private UserAuthorisationInterceptor interceptorUnderTest;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Object handler;

    @Test
    @DisplayName("preHandle accepts patch payment details request that has the required headers")
    void preHandleAcceptsAuthorisedPatchPaymentDetailsRequest() {

        // Given
        givenRequest(PATCH, "/basket/checkouts/1234/payment");
        givenRequestHasInternalUserRole();

        // When and then
        thenRequestIsAccepted();
    }

    @Test
    @DisplayName("preHandle rejects patch payment details request that lacks the required headers")
    void preHandleRejectsUnauthorisedPatchPaymentDetailsRequest() {

        // Given
        givenRequest(PATCH, "/basket/checkouts/1234/payment");

        // When and then
        thenRequestIsRejected();
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
     * Sets up the request with an internal user role.
     */
    private void givenRequestHasInternalUserRole() {
        when(request.getHeader(ERIC_AUTHORISED_KEY_ROLES)).thenReturn(INTERNAL_USER_ROLE);
    }

    /**
     * Verifies that the authorisation interceptor does not reject the request as unauthorised.
     */
    private void thenRequestIsAccepted() {
        assertThat(interceptorUnderTest.preHandle(request, response, handler), is(true));
        verify(response, never()).setStatus(anyInt());
    }

    /**
     * Verifies that the authorisation interceptor blocks the request as unauthorised.
     */
    private void thenRequestIsRejected() {
        assertThat(interceptorUnderTest.preHandle(request, response, handler), is(false));
        verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
    }
}

package uk.gov.companieshouse.orders.api.interceptor;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.test.context.EmbeddedKafka;
import uk.gov.companieshouse.orders.api.model.Checkout;
import uk.gov.companieshouse.orders.api.model.Order;
import uk.gov.companieshouse.orders.api.repository.CheckoutRepository;
import uk.gov.companieshouse.orders.api.repository.OrderRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.singletonMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.web.servlet.HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE;
import static uk.gov.companieshouse.api.util.security.EricConstants.ERIC_AUTHORISED_KEY_ROLES;
import static uk.gov.companieshouse.api.util.security.SecurityConstants.INTERNAL_USER_ROLE;
import static uk.gov.companieshouse.orders.api.controller.BasketController.CHECKOUT_ID_PATH_VARIABLE;
import static uk.gov.companieshouse.orders.api.controller.OrderController.ORDER_ID_PATH_VARIABLE;
import static uk.gov.companieshouse.orders.api.util.EricHeaderHelper.*;
import static uk.gov.companieshouse.orders.api.util.TestConstants.ERIC_IDENTITY_VALUE;
import static uk.gov.companieshouse.orders.api.util.TestConstants.WRONG_ERIC_IDENTITY_VALUE;

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

    @MockBean
    private CheckoutRepository checkoutRepository;

    @Mock
    private Checkout checkout;

    @MockBean
    private OrderRepository orderRepository;

    @Mock
    private Order order;

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

    @Test
    @DisplayName("preHandle accepts get payment details internal API request that has the required headers")
    void preHandleAcceptsAuthorisedInternalApiGetPaymentDetailsRequest() {

        // Given
        givenRequest(GET, "/basket/checkouts/1234/payment");
        givenRequestHasInternalUserRole();

        // When and then
        thenRequestIsAccepted();
    }

    @Test
    @DisplayName("preHandle accepts get payment details user request that has the required headers")
    void preHandleAcceptsAuthorisedUserGetPaymentDetailsRequest() {

        // Given
        givenRequest(GET, "/basket/checkouts/1234/payment");
        givenRequestHasSignedInUser(ERIC_IDENTITY_VALUE);
        givenGetPaymentDetailsCheckoutIdPathVariableIsPopulated(ERIC_IDENTITY_VALUE);

        // When and then
        thenRequestIsAccepted();
    }

    @Test
    @DisplayName("preHandle accepts get payment details internal API request that has the required headers")
    void preHandleRejectsUnauthorisedInternalApiGetPaymentDetailsRequest() {

        // Given
        givenRequest(GET, "/basket/checkouts/1234/payment");
        givenRequestDoesNotHaveInternalUserRole();

        // When and then
        thenRequestIsRejected();
    }

    @Test
    @DisplayName("preHandle rejects get payment details user request that lacks the required headers")
    void preHandleRejectsUnauthorisedUserGetPaymentDetailsRequest() {

        // Given
        givenRequest(GET, "/basket/checkouts/1234/payment");
        givenRequestHasSignedInUser(WRONG_ERIC_IDENTITY_VALUE);
        givenGetPaymentDetailsCheckoutIdPathVariableIsPopulated(ERIC_IDENTITY_VALUE);

        // When and then
        thenRequestIsRejected();
    }

    @Test
    @DisplayName("preHandle errors clearly if URI path variables are not present in get payment details request")
    void preHandleErrorsClearlyIfUriPathVariablesNotPresentInGetPaymentDetailsRequest() {

        // Given
        givenRequest(GET, "/basket/checkouts/1234/payment");
        givenRequestHasSignedInUser(ERIC_IDENTITY_VALUE);

        // When and then
        final IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> interceptorUnderTest.preHandle(request, response, handler));
        assertEquals("No URI template path variables found in the request!", exception.getMessage());
    }

    @Test
    @DisplayName("preHandle accepts get order internal API request that has the required headers")
    void preHandleAcceptsAuthorisedInternalApiGetOrderRequest() {

        // Given
        givenRequest(GET, "/orders/1234");
        givenRequestHasInternalUserRole();

        // When and then
        thenRequestIsAccepted();
    }

    @Test
    @DisplayName("preHandle accepts get order user request that has the required headers")
    void preHandleAcceptsAuthorisedUserGetOrderRequest() {

        // Given
        givenRequest(GET, "/orders/1234");
        givenRequestHasSignedInUser(ERIC_IDENTITY_VALUE);
        givenGetOrderOrderIdPathVariableIsPopulated(ERIC_IDENTITY_VALUE);

        // When and then
        thenRequestIsAccepted();
    }

    @Test
    @DisplayName("preHandle rejects get order internal API request that lacks the required headers")
    void preHandleRejectsUnauthorisedInternalApiGetOrderRequest() {

        // Given
        givenRequest(GET, "/orders/1234");
        givenRequestDoesNotHaveInternalUserRole();

        // When and then
        thenRequestIsRejected();
    }

    @Test
    @DisplayName("preHandle rejects get order user request that lacks the required headers")
    void preHandleRejectsUnauthorisedUserGetOrderRequest() {

        // Given
        givenRequest(GET, "/orders/1234");
        givenRequestHasSignedInUser(WRONG_ERIC_IDENTITY_VALUE);
        givenGetOrderOrderIdPathVariableIsPopulated(ERIC_IDENTITY_VALUE);

        // When and then
        thenRequestIsRejected();
    }

    @Test
    @DisplayName("preHandle errors clearly if URI path variables are not present in get order request")
    void preHandleErrorsClearlyIfUriPathVariablesNotPresentInGetOrderRequest() {

        // Given
        givenRequest(GET, "/orders/1234");
        givenRequestHasSignedInUser(ERIC_IDENTITY_VALUE);

        // When and then
        final IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> interceptorUnderTest.preHandle(request, response, handler));
        assertEquals("No URI template path variables found in the request!", exception.getMessage());
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
     * Sets up the request with the checkout ID path variable as Spring does.
     * @param checkoutOwnerId the user ID value on the retrieved checkout
     */
    private void givenGetPaymentDetailsCheckoutIdPathVariableIsPopulated(final String checkoutOwnerId) {
        givenPathVariable(CHECKOUT_ID_PATH_VARIABLE, "1");
        when(checkoutRepository.findById("1")).thenReturn(Optional.of(checkout));
        when(checkout.getUserId()).thenReturn(checkoutOwnerId);
    }

    /**
     * Sets up the request with the order ID path variable as Spring does.
     * @param orderOwnerId the user ID value on the retrieved order
     */
    private void givenGetOrderOrderIdPathVariableIsPopulated(final String orderOwnerId) {
        givenPathVariable(ORDER_ID_PATH_VARIABLE, "1");
        when(orderRepository.findById("1")).thenReturn(Optional.of(order));
        when(order.getUserId()).thenReturn(orderOwnerId);
    }

    /**
     * Sets up the request with the named path variable as Spring does.
     * @param name the name of the path variable
     * @param value the value of the path variable
     */
    private void givenPathVariable(final String name, final String value) {
        final Map<String, String> uriPathVariables = singletonMap(name, value);
        when(request.getAttribute(URI_TEMPLATE_VARIABLES_ATTRIBUTE)).thenReturn(uriPathVariables);
    }

    /**
     * Sets up request with required header values to represent a signed in user.
     * @param userId the user ID in the request's <code>ERIC-Identity</code> header
     */
    private void givenRequestHasSignedInUser(final String userId) {
        when(request.getHeader(ERIC_IDENTITY_TYPE)).thenReturn(OAUTH2_IDENTITY_TYPE);
        when(request.getHeader(ERIC_IDENTITY)).thenReturn(userId);
    }

    /**
     * Sets up the request for an API client with an internal user role.
     */
    private void givenRequestHasInternalUserRole() {
        when(request.getHeader(ERIC_IDENTITY_TYPE)).thenReturn(API_KEY_IDENTITY_TYPE);
        when(request.getHeader(ERIC_AUTHORISED_KEY_ROLES)).thenReturn(INTERNAL_USER_ROLE);
    }

    /**
     * Sets up the request for an API client with no internal user role.
     */
    private void givenRequestDoesNotHaveInternalUserRole() {
        when(request.getHeader(ERIC_IDENTITY_TYPE)).thenReturn(API_KEY_IDENTITY_TYPE);
        when(request.getHeader(ERIC_AUTHORISED_KEY_ROLES)).thenReturn(null);
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

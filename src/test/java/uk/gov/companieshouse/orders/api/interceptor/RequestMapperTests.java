package uk.gov.companieshouse.orders.api.interceptor;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

import javax.servlet.http.HttpServletRequest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.*;
import static uk.gov.companieshouse.orders.api.interceptor.RequestMapper.*;

/**
 * Unit/integration tests the {@link RequestMapper} class.
 */
@DirtiesContext
@SpringBootTest
@EmbeddedKafka
public class RequestMapperTests {

    @Autowired
    private RequestMapper requestMapperUnderTest;

    @Mock
    private HttpServletRequest request;

    @Test
    @DisplayName("getRequestMappingInfo gets the add item request mapping")
    void getRequestMappingInfoGetsAddItem() {

        // Given
        givenRequest(POST, "/basket/items");

        // When and then
        assertThat(requestMapperUnderTest.getRequestMapping(request).getName(), is(ADD_ITEM));
    }

    @Test
    @DisplayName("getRequestMappingInfo gets the checkout basket request mapping")
    void getRequestMappingInfoGetsCheckoutBasket() {

        // Given
        givenRequest(POST, "/basket/checkouts");

        // When and then
        assertThat(requestMapperUnderTest.getRequestMapping(request).getName(), is(CHECKOUT_BASKET));
    }

    @Test
    @DisplayName("getRequestMappingInfo gets the get payment details request mapping")
    void getRequestMappingInfoGetsGetPaymentDetails() {

        // Given
        givenRequest(GET, "/basket/checkouts/{checkoutId}/payment");

        // When and then
        assertThat(requestMapperUnderTest.getRequestMapping(request).getName(), is(GET_PAYMENT_DETAILS));
    }

    @Test
    @DisplayName("getRequestMappingInfo gets the get basket request mapping")
    void getRequestMappingInfoGetsGetBasket() {

        // Given
        givenRequest(GET, "/basket");

        // When and then
        assertThat(requestMapperUnderTest.getRequestMapping(request).getName(), is(BASKET));
    }

    @Test
    @DisplayName("getRequestMappingInfo gets the patch basket request mapping")
    void getRequestMappingInfoGetsPatchBasket() {

        // Given
        givenRequest(PATCH, "/basket");

        // When and then
        assertThat(requestMapperUnderTest.getRequestMapping(request).getName(), is(BASKET));
    }

    @Test
    @DisplayName("getRequestMappingInfo gets the patch payment details request mapping")
    void getRequestMappingInfoGetsPatchPaymentDetails() {

        // Given
        givenRequest(PATCH, "/basket/checkouts/{id}/payment");

        // When and then
        assertThat(requestMapperUnderTest.getRequestMapping(request).getName(), is(PATCH_PAYMENT_DETAILS));
    }

    @Test
    @DisplayName("getRequestMappingInfo gets the get order request mapping")
    void getRequestMappingInfoGetsGetOrder() {

        // Given
        givenRequest(GET, "/orders/{id}");

        // When and then
        assertThat(requestMapperUnderTest.getRequestMapping(request).getName(), is(GET_ORDER));
    }

    @Test
    @DisplayName("getRequestMappingInfo returns null where no mapping found")
    void getRequestMappingInfoReturnsNullWhereNoMappingFound() {

        // Given
        givenRequest(DELETE, "/unknown/uri");

        // When and then
        assertThat(requestMapperUnderTest.getRequestMapping(request), is(nullValue()));
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

}

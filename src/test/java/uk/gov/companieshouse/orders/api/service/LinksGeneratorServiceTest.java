package uk.gov.companieshouse.orders.api.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.orders.api.model.CheckoutLinks;
import uk.gov.companieshouse.orders.api.model.OrderLinks;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class LinksGeneratorServiceTest {
    private static final String CHECKOUT_URI = "/basket/checkouts";
    private static final String ORDER_URI = "/order";
    private static final String CHECKOUT_ID = "1";
    private static final String ORDER_ID = "1";

    @Test
    @DisplayName("Generates links correctly with valid inputs")
    void generatesCheckoutLinksCorrectlyWithValidInputs() {
        final LinksGeneratorService generatorUnderTest = new LinksGeneratorService(CHECKOUT_URI, ORDER_URI);
        final CheckoutLinks links = generatorUnderTest.generateCheckoutLinks(CHECKOUT_ID);

        assertThat(links.getSelf(), is(CHECKOUT_URI + "/" + CHECKOUT_ID));
        assertThat(links.getPayment(), is(CHECKOUT_URI + "/" + CHECKOUT_ID+"/payment"));
    }

    @Test
    @DisplayName("Generates links correctly with valid inputs")
    void generatesOrderLinksCorrectlyWithValidInputs() {
        final LinksGeneratorService generatorUnderTest = new LinksGeneratorService(CHECKOUT_URI, ORDER_URI);
        final OrderLinks links = generatorUnderTest.generateOrderLinks(ORDER_ID);

        assertThat(links.getSelf(), is(ORDER_URI + "/" + ORDER_ID));
    }

    @Test
    @DisplayName("Unpopulated Checkout ID argument results in an IllegalArgumentException")
    void itemIdMustNotBeBlank() {
        final LinksGeneratorService generatorUnderTest = new LinksGeneratorService(CHECKOUT_URI, ORDER_URI);

        final IllegalArgumentException exception =
                Assertions.assertThrows(IllegalArgumentException.class,
                        () -> generatorUnderTest.generateCheckoutLinks(null));

        assertThat(exception.getMessage(), is("Checkout ID not populated!"));
    }

    @Test
    @DisplayName("Unpopulated Checkout URI results in an IllegalArgumentException")
    void selfPathMustNotBeBlank() {
        final IllegalArgumentException exception =
                Assertions.assertThrows(IllegalArgumentException.class,
                        () -> new LinksGeneratorService(null, null));

        assertThat(exception.getMessage(), is("Checkout URI not configured!"));
    }
}

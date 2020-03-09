package uk.gov.companieshouse.orders.api.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.orders.api.model.Links;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class LinksGeneratorServiceTest {
    private static final String CHECKOUT_URI = "/basket/checkouts";
    private static final String CHECKOUT_ID = "1";

    @Test
    @DisplayName("Generates links correctly with valid inputs")
    void generatesLinksCorrectlyWithValidInputs() {
        final LinksGeneratorService generatorUnderTest =new LinksGeneratorService(CHECKOUT_URI);
        final Links links = generatorUnderTest.generateCheckoutLinks(CHECKOUT_ID);

        assertThat(links.getSelf(), is(CHECKOUT_URI + "/" + CHECKOUT_ID));
        assertThat(links.getPayment(), is(CHECKOUT_URI + "/" + CHECKOUT_ID+"/payment"));
    }

    @Test
    @DisplayName("Unpopulated Checkout ID argument results in an IllegalArgumentException")
    void itemIdMustNotBeBlank() {
        final LinksGeneratorService generatorUnderTest = new LinksGeneratorService(CHECKOUT_URI);

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
                        () -> new LinksGeneratorService(null));

        assertThat(exception.getMessage(), is("Checkout URI not configured!"));
    }
}

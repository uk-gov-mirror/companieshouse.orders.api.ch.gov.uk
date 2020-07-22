package uk.gov.companieshouse.orders.api.listener;

import org.bson.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.mapping.event.AfterConvertEvent;
import uk.gov.companieshouse.orders.api.model.Checkout;
import uk.gov.companieshouse.orders.api.model.CheckoutData;
import uk.gov.companieshouse.orders.api.model.Item;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests the {@link MongoCheckoutListener} class.
 */
@ExtendWith(MockitoExtension.class)
class MongoCheckoutListenerTest {

    @InjectMocks
    private MongoCheckoutListener listenerUnderTest;

    @Mock
    private AfterConvertEvent<Checkout> event;

    @Mock
    private Document checkoutDocument;

    @Mock
    private Checkout checkout;

    @Mock
    private CheckoutData checkoutData;

    @Mock
    private List<Item> items;

    @Mock
    private OrderItemOptionsReader reader;


    @Test
    @DisplayName("onAfterConvert() delegates to reader")
    void onAfterConvertDelegatesToReader() {

        // Given
        givenValidEvent();

        // When
        listenerUnderTest.onAfterConvert(event);

        // Then
        verify(reader).readOrderItemsOptions(items, checkoutDocument, "checkout");
    }

    @Test
    @DisplayName("onAfterConvert() propagates reader IllegalStateException")
    void onAfterConvertPropagatesReaderIllegalStateException() {

        // Given
        givenValidEvent();
        doThrow(new IllegalStateException("Test exception"))
                .when(reader).readOrderItemsOptions(items, checkoutDocument, "checkout");

        // When and then
        final IllegalStateException exception =
                assertThrows(IllegalStateException.class, () -> listenerUnderTest.onAfterConvert(event));
        assertThat(exception.getMessage(), is("Test exception"));
    }

    @Test
    @DisplayName("onAfterConvert() propagates reader IllegalArgumentException")
    void onAfterConvertPropagatesReaderIllegalArgumentException() {
        // Given
        givenValidEvent();
        doThrow(new IllegalArgumentException("Test exception"))
                .when(reader).readOrderItemsOptions(items, checkoutDocument, "checkout");

        // When and then
        final IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> listenerUnderTest.onAfterConvert(event));
        assertThat(exception.getMessage(), is("Test exception"));
    }

    /**
     * Provides a valid event set up for testing {@link MongoCheckoutListener#onAfterConvert(AfterConvertEvent)}.
     */
    private void givenValidEvent() {
        when(event.getDocument()).thenReturn(checkoutDocument);
        when(event.getSource()).thenReturn(checkout);
        when(checkout.getData()).thenReturn(checkoutData);
        when(checkoutData.getItems()).thenReturn(items);
    }

}

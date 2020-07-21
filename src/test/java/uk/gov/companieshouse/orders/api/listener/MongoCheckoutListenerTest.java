package uk.gov.companieshouse.orders.api.listener;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.mapping.event.AfterConvertEvent;
import uk.gov.companieshouse.orders.api.model.CertificateItemOptions;
import uk.gov.companieshouse.orders.api.model.CertifiedCopyItemOptions;
import uk.gov.companieshouse.orders.api.model.Checkout;
import uk.gov.companieshouse.orders.api.model.CheckoutData;
import uk.gov.companieshouse.orders.api.model.Item;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.orders.api.util.TestConstants.CERTIFICATE_KIND;
import static uk.gov.companieshouse.orders.api.util.TestConstants.CERTIFIED_COPY_KIND;

/**
 * Unit tests the {@link MongoCheckoutListener} class.
 */
@ExtendWith(MockitoExtension.class)
class MongoCheckoutListenerTest {

    private static final String UNKNOWN_KIND = "item#unknown";

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
    private Item certificateItem;

    @Mock
    private Document checkoutDataDocument;

    @Mock
    private List<Document> itemDocuments;

    @Mock
    private Document itemDocument;

    @Mock
    private ObjectMapper mapper;

    @Mock
    private Document optionsDocument;

    @Mock
    private CertificateItemOptions certificateItemOptions;

    @Test
    @DisplayName("onAfterConvert() updates item options correctly")
    void onAfterConvertUpdatesCertificateItemOptionsCorrectly() throws IOException {

        // Given
        when(event.getDocument()).thenReturn(checkoutDocument);
        when(event.getSource()).thenReturn(checkout);
        when(checkout.getData()).thenReturn(checkoutData);
        when(checkoutData.getItems()).thenReturn(items);
        when(items.size()).thenReturn(1);
        when(items.get(0)).thenReturn(certificateItem);

        when(checkoutDocument.get("data", Document.class)).thenReturn(checkoutDataDocument);
        when(checkoutDataDocument.get("items", List.class)).thenReturn(itemDocuments);
        when(itemDocuments.get(0)).thenReturn(itemDocument);
        when(itemDocument.get("item_options", Document.class)).thenReturn(optionsDocument);
        when(optionsDocument.toJson()).thenReturn("{}");
        when(certificateItem.getKind()).thenReturn(CERTIFICATE_KIND);
        when(mapper.readValue("{}", CertificateItemOptions.class)).thenReturn(certificateItemOptions);

        // When
        listenerUnderTest.onAfterConvert(event);

        // Then
        verify(items).get(0);
        verify(certificateItem).getKind();
        verify(mapper).readValue("{}", CertificateItemOptions.class);
        verify(certificateItem).setItemOptions(certificateItemOptions);

    }

    @Test
    @DisplayName("onAfterConvert() copes with missing item options")
    void onAfterConvertCopesWithMissingItemOptions() throws IOException {

        // Given
        when(event.getDocument()).thenReturn(checkoutDocument);
        when(event.getSource()).thenReturn(checkout);
        when(checkout.getData()).thenReturn(checkoutData);
        when(checkoutData.getItems()).thenReturn(items);
        when(items.size()).thenReturn(1);
        when(items.get(0)).thenReturn(certificateItem);

        when(checkoutDocument.get("data", Document.class)).thenReturn(checkoutDataDocument);
        when(checkoutDataDocument.get("items", List.class)).thenReturn(itemDocuments);
        when(itemDocuments.get(0)).thenReturn(itemDocument);
        when(itemDocument.get("item_options", Document.class)).thenReturn(null);

        // When
        listenerUnderTest.onAfterConvert(event);

        // Then
        verify(items).get(0);
        verify(mapper, never()).readValue(anyString(), ArgumentMatchers.eq(CertificateItemOptions.class));
    }

    @Test
    @DisplayName("onAfterConvert() propagates mapper IOException as an IllegalStateException")
    void onAfterConvertPropagatesMapperIOExceptionAsIllegalStateException() throws IOException {

        // Given
        when(event.getDocument()).thenReturn(checkoutDocument);
        when(event.getSource()).thenReturn(checkout);
        when(checkout.getData()).thenReturn(checkoutData);
        when(checkoutData.getItems()).thenReturn(items);
        when(items.size()).thenReturn(1);
        when(items.get(0)).thenReturn(certificateItem);

        when(checkoutDocument.get("data", Document.class)).thenReturn(checkoutDataDocument);
        when(checkoutDataDocument.get("items", List.class)).thenReturn(itemDocuments);
        when(itemDocuments.get(0)).thenReturn(itemDocument);
        when(itemDocument.get("item_options", Document.class)).thenReturn(optionsDocument);
        when(optionsDocument.toJson()).thenReturn("{}");
        when(certificateItem.getKind()).thenReturn(CERTIFICATE_KIND);
        when(mapper.readValue("{}", CertificateItemOptions.class)).thenThrow(new IOException("Test message"));

        // When and then
        final IllegalStateException exception =
                assertThrows(IllegalStateException.class, () -> listenerUnderTest.onAfterConvert(event));
        assertThat(exception.getMessage(), is("Error parsing item options JSON: Test message"));

        // Then
        verify(items).get(0);
        verify(certificateItem).getKind();
        verify(mapper).readValue("{}", CertificateItemOptions.class);
        verify(certificateItem, never()).setItemOptions(certificateItemOptions);

    }

    // TODO GCI-984 Test certified copy item options too

    @Test
    @DisplayName("onAfterConvert() throws IllegalStateException if no checkout document found on event")
    void onAfterConvertThrowsIllegalStateExceptionIfNoCheckoutDocumentFoundOnEvent() {
        final IllegalStateException exception =
                assertThrows(IllegalStateException.class, () -> listenerUnderTest.onAfterConvert(event));
        assertThat(exception.getMessage(), is("No checkout document found on event."));
    }

    @Test
    @DisplayName("getType() infers item options class type correctly from kind")
    void getTypeInfersItemOptionsClassCorrectlyFromKind() {
        assertEquals(CertificateItemOptions.class, listenerUnderTest.getType(CERTIFICATE_KIND).getOptionsType());
        assertEquals(CertifiedCopyItemOptions.class, listenerUnderTest.getType(CERTIFIED_COPY_KIND).getOptionsType());
    }

    @Test
    @DisplayName("getType() throws IllegalArgumentException for unknown kind")
    void getTypeThrowsIllegalArgumentExceptionForUnknownKind() {
        final IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> listenerUnderTest.getType(UNKNOWN_KIND));
        assertEquals("'" + UNKNOWN_KIND + "' is not a known kind!", exception.getMessage());
    }

}
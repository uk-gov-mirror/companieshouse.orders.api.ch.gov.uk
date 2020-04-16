package uk.gov.companieshouse.orders.api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.orders.api.model.*;
import uk.gov.companieshouse.orders.api.repository.CheckoutRepository;
import uk.gov.companieshouse.orders.api.util.TimestampedEntityVerifier;

import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.orders.api.util.TestConstants.ERIC_AUTHORISED_USER_VALUE;
import static uk.gov.companieshouse.orders.api.util.TestConstants.ERIC_IDENTITY_VALUE;

@ExtendWith(MockitoExtension.class)
public class CheckoutServiceTest {

    private static final String COMPANY_NUMBER = "00006400";
    private static final String ETAG = "etag";
    private static final String LINKS_SELF = "links/self";
    private static final String LINKS_PAYMENT = "links/payment";
    private static final String KIND = "order";

    private static final String ADDRESS_LINE_1 = "address line 1";
    private static final String ADDRESS_LINE_2 = "address line 2";
    private static final String COUNTRY = "country";
    private static final String FORENAME = "forename";
    private static final String LOCALITY = "locality";
    private static final String PO_BOX = "po box";
    private static final String POSTAL_CODE = "postal code";
    private static final String PREMISES = "premises";
    private static final String REGION = "region";
    private static final String SURNAME = "surname";

    @InjectMocks
    CheckoutService serviceUnderTest;

    @Mock
    CheckoutRepository checkoutRepository;

    @Mock
    EtagGeneratorService etagGeneratorService;

    @Mock
    LinksGeneratorService linksGeneratorService;

    @Captor
    ArgumentCaptor<Checkout> checkoutCaptor;

    private TimestampedEntityVerifier timestamps;

    @BeforeEach
    void setUp() {
        timestamps = new TimestampedEntityVerifier();
    }

    @Test
    void createCheckoutPopulatesCreatedAndUpdated() {
        when(checkoutRepository.save(any(Checkout.class))).thenReturn(new Checkout());

        timestamps.start();

        serviceUnderTest.createCheckout(new Certificate(), ERIC_IDENTITY_VALUE,
                ERIC_AUTHORISED_USER_VALUE, new DeliveryDetails());
        verify(checkoutRepository).save(checkoutCaptor.capture());

        timestamps.end();

        timestamps.verifyCreationTimestampsWithinExecutionInterval(checkout());
    }

    @Test
    void createCheckoutPopulatesAndSavesItem() {
        Certificate certificate = new Certificate();
        certificate.setCompanyNumber(COMPANY_NUMBER);
        when(checkoutRepository.save(any(Checkout.class))).thenReturn(new Checkout());

        serviceUnderTest.createCheckout(certificate, ERIC_IDENTITY_VALUE,
                ERIC_AUTHORISED_USER_VALUE, new DeliveryDetails());
        verify(checkoutRepository).save(checkoutCaptor.capture());

        assertEquals(1, checkout().getData().getItems().size());
        assertEquals(ERIC_IDENTITY_VALUE, checkout().getUserId());
        assertEquals(COMPANY_NUMBER, checkout().getData().getItems().get(0).getCompanyNumber());
        assertEquals(checkout().getId(), checkout().getData().getReference());
        assertEquals(KIND, checkout().getData().getKind());
    }

    @Test
    void createCheckoutPopulatesAndSavesCheckedOutBy() {
        when(checkoutRepository.save(any(Checkout.class))).thenReturn(new Checkout());

        serviceUnderTest.createCheckout(new Certificate(), ERIC_IDENTITY_VALUE,
                ERIC_AUTHORISED_USER_VALUE, new DeliveryDetails());
        verify(checkoutRepository).save(checkoutCaptor.capture());

        assertThat(checkout().getData().getCheckedOutBy().getId(), is(ERIC_IDENTITY_VALUE));
        assertThat(checkout().getData().getCheckedOutBy().getEmail(), is(ERIC_AUTHORISED_USER_VALUE));
    }

    @Test
    void createCheckoutPopulatesAndSavesDeliveryDetails() {
        when(checkoutRepository.save(any(Checkout.class))).thenReturn(new Checkout());
        DeliveryDetails deliveryDetails = new DeliveryDetails();
        deliveryDetails.setAddressLine1(ADDRESS_LINE_1);
        deliveryDetails.setAddressLine2(ADDRESS_LINE_2);
        deliveryDetails.setCountry(COUNTRY);
        deliveryDetails.setForename(FORENAME);
        deliveryDetails.setLocality(LOCALITY);
        deliveryDetails.setPoBox(PO_BOX);
        deliveryDetails.setPostalCode(POSTAL_CODE);
        deliveryDetails.setPremises(PREMISES);
        deliveryDetails.setRegion(REGION);
        deliveryDetails.setSurname(SURNAME);

        serviceUnderTest.createCheckout(new Certificate(), ERIC_IDENTITY_VALUE,
                ERIC_AUTHORISED_USER_VALUE, deliveryDetails);
        verify(checkoutRepository).save(checkoutCaptor.capture());

        DeliveryDetails createdDeliveryDetails = checkout().getData().getDeliveryDetails();
        assertThat(createdDeliveryDetails.getAddressLine1(), is(ADDRESS_LINE_1));
        assertThat(createdDeliveryDetails.getAddressLine2(), is(ADDRESS_LINE_2));
        assertThat(createdDeliveryDetails.getCountry(), is(COUNTRY));
        assertThat(createdDeliveryDetails.getForename(), is(FORENAME));
        assertThat(createdDeliveryDetails.getLocality(), is(LOCALITY));
        assertThat(createdDeliveryDetails.getPoBox(), is(PO_BOX));
        assertThat(createdDeliveryDetails.getPostalCode(), is(POSTAL_CODE));
        assertThat(createdDeliveryDetails.getPremises(), is(PREMISES));
        assertThat(createdDeliveryDetails.getRegion(), is(REGION));
        assertThat(createdDeliveryDetails.getSurname(), is(SURNAME));
    }

    @Test
    void createCheckoutPopulatesAndSavesEtagAndLinks() {
        CheckoutLinks checkoutLinks = new CheckoutLinks();
        checkoutLinks.setSelf(LINKS_SELF);
        checkoutLinks.setPayment(LINKS_PAYMENT);

        when(checkoutRepository.save(any(Checkout.class))).thenReturn(new Checkout());
        when(etagGeneratorService.generateEtag()).thenReturn(ETAG);
        when(linksGeneratorService.generateCheckoutLinks(any(String.class))).thenReturn(checkoutLinks);

        serviceUnderTest.createCheckout(new Certificate(), ERIC_IDENTITY_VALUE,
                ERIC_AUTHORISED_USER_VALUE, new DeliveryDetails());
        verify(checkoutRepository).save(checkoutCaptor.capture());

        verify(etagGeneratorService, times(1)).generateEtag();
        verify(linksGeneratorService, times(1)).generateCheckoutLinks(any(String.class));
        assertEquals(LINKS_SELF, checkout().getData().getLinks().getSelf());
        assertEquals(LINKS_PAYMENT, checkout().getData().getLinks().getPayment());
        assertEquals(ETAG, checkout().getData().getEtag());
    }

    @Test
    @DisplayName("saveCheckout saves updated checkout correctly")
    void saveCheckoutSavesUpdatedCheckout() {

        // Given
        final Checkout checkout = new Checkout();
        checkout.setCreatedAt(LocalDateTime.now());
        checkout.getData().setStatus(PaymentStatus.PAID);
        when(etagGeneratorService.generateEtag()).thenReturn(ETAG);

        timestamps.start();

        // When
        serviceUnderTest.saveCheckout(checkout);

        timestamps.end();

        // Then
        verify(etagGeneratorService, times(1)).generateEtag();
        verify(checkoutRepository).save(checkoutCaptor.capture());
        assertThat(checkout().getData().getEtag(), is(ETAG));
        timestamps.verifyUpdatedAtTimestampWithinExecutionInterval(checkout());
    }

    /**
     * @return the captured {@link Checkout}.
     */
    private Checkout checkout() {
        return checkoutCaptor.getValue();
    }

}

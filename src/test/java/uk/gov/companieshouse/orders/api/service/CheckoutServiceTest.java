package uk.gov.companieshouse.orders.api.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.orders.api.model.Certificate;
import uk.gov.companieshouse.orders.api.model.Checkout;
import uk.gov.companieshouse.orders.api.model.CheckoutLinks;
import uk.gov.companieshouse.orders.api.repository.CheckoutRepository;

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

    @InjectMocks
    CheckoutService serviceUnderTest;

    @Mock
    CheckoutRepository checkoutRepository;

    @Mock
    EtagGeneratorService etagGeneratorService;

    @Mock
    LinksGeneratorService linksGeneratorService;

    @Captor
    ArgumentCaptor<Checkout> argCaptor;

    @Test
    void createCheckoutPopulatesCreatedAndUpdated() {
        when(checkoutRepository.save(any(Checkout.class))).thenReturn(new Checkout());

        final LocalDateTime intervalStart = LocalDateTime.now();

        serviceUnderTest.createCheckout(new Certificate(), ERIC_IDENTITY_VALUE, ERIC_AUTHORISED_USER_VALUE);
        verify(checkoutRepository).save(argCaptor.capture());

        final LocalDateTime intervalEnd = LocalDateTime.now();

        verifyCreationTimestampsWithinExecutionInterval(argCaptor.getValue(), intervalStart, intervalEnd);
    }

    @Test
    void createCheckoutPopulatesAndSavesItem() {
        Certificate certificate = new Certificate();
        certificate.setCompanyNumber(COMPANY_NUMBER);
        when(checkoutRepository.save(any(Checkout.class))).thenReturn(new Checkout());

        serviceUnderTest.createCheckout(certificate, ERIC_IDENTITY_VALUE, ERIC_AUTHORISED_USER_VALUE);
        verify(checkoutRepository).save(argCaptor.capture());

        assertEquals(1, argCaptor.getValue().getData().getItems().size());
        assertEquals(ERIC_IDENTITY_VALUE, argCaptor.getValue().getUserId());
        assertEquals(COMPANY_NUMBER, argCaptor.getValue().getData().getItems().get(0).getCompanyNumber());
        assertEquals(argCaptor.getValue().getId(), argCaptor.getValue().getData().getReference());
        assertEquals(KIND, argCaptor.getValue().getData().getKind());
    }

    @Test
    void createCheckoutPopulatesAndSavesCheckedOutBy() {
        when(checkoutRepository.save(any(Checkout.class))).thenReturn(new Checkout());

        serviceUnderTest.createCheckout(new Certificate(), ERIC_IDENTITY_VALUE, ERIC_AUTHORISED_USER_VALUE);
        verify(checkoutRepository).save(argCaptor.capture());

        assertEquals(argCaptor.getValue().getData().getCheckedOutBy().getId(), ERIC_IDENTITY_VALUE);
        assertEquals(argCaptor.getValue().getData().getCheckedOutBy().getEmail(), ERIC_AUTHORISED_USER_VALUE);
    }

    @Test
    void createCheckoutPopulatesAndSavesEtagAndLinks() {
        CheckoutLinks checkoutLinks = new CheckoutLinks();
        checkoutLinks.setSelf(LINKS_SELF);
        checkoutLinks.setPayment(LINKS_PAYMENT);

        when(checkoutRepository.save(any(Checkout.class))).thenReturn(new Checkout());
        when(etagGeneratorService.generateEtag()).thenReturn(ETAG);
        when(linksGeneratorService.generateCheckoutLinks(any(String.class))).thenReturn(checkoutLinks);

        serviceUnderTest.createCheckout(new Certificate(), ERIC_IDENTITY_VALUE, ERIC_AUTHORISED_USER_VALUE);
        verify(checkoutRepository).save(argCaptor.capture());

        verify(etagGeneratorService, times(1)).generateEtag();
        verify(linksGeneratorService, times(1)).generateCheckoutLinks(any(String.class));
        assertEquals(LINKS_SELF, argCaptor.getValue().getData().getLinks().getSelf());
        assertEquals(LINKS_PAYMENT, argCaptor.getValue().getData().getLinks().getPayment());
        assertEquals(ETAG, argCaptor.getValue().getData().getEtag());
    }

    private void verifyCreationTimestampsWithinExecutionInterval(final Checkout itemCreated,
                                                                 final LocalDateTime intervalStart,
                                                                 final LocalDateTime intervalEnd) {
        assertThat(itemCreated.getCreatedAt().isAfter(intervalStart) ||
                itemCreated.getCreatedAt().isEqual(intervalStart), is(true));
        assertThat(itemCreated.getCreatedAt().isBefore(intervalEnd) ||
                itemCreated.getCreatedAt().isEqual(intervalEnd), is(true));
        assertThat(itemCreated.getUpdatedAt().isAfter(intervalStart) ||
                itemCreated.getUpdatedAt().isEqual(intervalStart), is(true));
        assertThat(itemCreated.getUpdatedAt().isBefore(intervalEnd) ||
                itemCreated.getUpdatedAt().isEqual(intervalEnd), is(true));
    }
}

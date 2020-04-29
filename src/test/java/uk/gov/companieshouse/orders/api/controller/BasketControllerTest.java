package uk.gov.companieshouse.orders.api.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import uk.gov.companieshouse.api.model.payment.PaymentApi;
import uk.gov.companieshouse.orders.api.dto.BasketPaymentRequestDTO;
import uk.gov.companieshouse.orders.api.model.*;
import uk.gov.companieshouse.orders.api.service.ApiClientService;
import uk.gov.companieshouse.orders.api.service.BasketService;
import uk.gov.companieshouse.orders.api.service.CheckoutService;
import uk.gov.companieshouse.orders.api.service.OrderService;
import uk.gov.companieshouse.sdk.manager.ApiSdkManager;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Partially unit tests the {@link BasketController} class.
 */
@ExtendWith(MockitoExtension.class)
class BasketControllerTest {

    @InjectMocks
    private BasketController controllerUnderTest;

    @Mock
    private CheckoutService checkoutService;

    @Mock
    private OrderService orderService;

    @Mock
    private BasketService basketService;

    @Mock
    private Checkout checkout;

    @Mock
    private CheckoutData checkoutData;

    @Mock
    private ApiClientService apiClientService;

    @Test
    @DisplayName("Patch payment details PAID status update is saved to checkout")
    void patchPaymentDetailsPaidStatusUpdateIsSaved() throws IOException {
        patchPaymentDetailsStatusUpdateIsSaved(PaymentStatus.PAID);
    }

    @Test
    @DisplayName("Patch payment details FAILED status update is saved to checkout")
    void patchPaymentDetailsFailedStatusUpdateIsSaved() throws IOException {
        patchPaymentDetailsStatusUpdateIsSaved(PaymentStatus.FAILED);
    }

    @Test
    @DisplayName("Patch payment details PENDING status update is saved to checkout")
    void patchPaymentDetailsPendingStatusUpdateIsSaved() throws IOException {
        patchPaymentDetailsStatusUpdateIsSaved(PaymentStatus.PENDING);
    }

    @Test
    @DisplayName("Patch payment details EXPIRED status update is saved to checkout")
    void patchPaymentDetailsExpiredStatusUpdateIsSaved() throws IOException {
        patchPaymentDetailsStatusUpdateIsSaved(PaymentStatus.EXPIRED);
    }

    @Test
    @DisplayName("Patch payment details IN_PROGRESS status update is saved to checkout")
    void patchPaymentDetailsInProgressStatusUpdateIsSaved() throws IOException {
        patchPaymentDetailsStatusUpdateIsSaved(PaymentStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("Patch payment details NO_FUNDS status update is saved to checkout")
    void patchPaymentDetailsNoFundsStatusUpdateIsSaved() throws IOException {
        patchPaymentDetailsStatusUpdateIsSaved(PaymentStatus.NO_FUNDS);
    }

    /**
     * Verifies that the controller has requested the checkout service to save the updated status to the checkout,
     * and made any further updates required to the checkout for that updated status.
     * @param paymentOutcome the payment status updated value
     */
    private void patchPaymentDetailsStatusUpdateIsSaved(final PaymentStatus paymentOutcome) throws IOException {

        // Given
        final String checkout_id = "123456789";
        final String payment_id = "987654321";
        final String eric_header = "EricHeader";
        final LocalDateTime paidAt = LocalDateTime.now();

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(ApiSdkManager.getEricPassthroughTokenHeader(), eric_header);
        request.setRequestURI("/basket/checkouts/" + checkout_id + "/payment");

        final BasketPaymentRequestDTO paymentStatusUpdate = new BasketPaymentRequestDTO();
        paymentStatusUpdate.setStatus(paymentOutcome);
        paymentStatusUpdate.setPaidAt(paidAt);
        paymentStatusUpdate.setPaymentReference(payment_id);

        final PaymentApi paymentSummary = new PaymentApi();
        paymentSummary.setStatus("paid");
        paymentSummary.setAmount("211.03");
        paymentSummary.setLinks(new HashMap<String, String>() {{
            put("resource", "/basket/checkouts/" + checkout_id + "/payment");
        }});

        when(checkoutService.getCheckoutById(checkout_id)).thenReturn(Optional.of(checkout));
        when(checkout.getData()).thenReturn(checkoutData);
        if (paymentOutcome.equals(PaymentStatus.PAID)) {
            mockCheckoutDataItems();
            when(apiClientService.getPaymentSummary("EricHeader", payment_id)).thenReturn(paymentSummary);
        }

        // When
        controllerUnderTest.patchBasketPaymentDetails(paymentStatusUpdate, request, checkout_id, "requestId");

        // Then
        verify(checkoutData).setStatus(paymentOutcome);
        if (paymentOutcome.equals(PaymentStatus.PAID)) {
            verify(checkoutData).setPaidAt(paidAt);
            verify(checkoutData).setPaymentReference(payment_id);
        }
        verify(checkoutService).saveCheckout(checkout);
    }

    private void mockCheckoutDataItems() {
        ArrayList<Item> items = new ArrayList<>();

        Item mockItem1 = new Item();
        ItemCosts mockItemCosts1 = new ItemCosts();
        mockItemCosts1.setCalculatedCost("40.43");
        ItemCosts mockItemCosts2 = new ItemCosts();
        mockItemCosts2.setCalculatedCost("30.60");
        mockItem1.setItemCosts(Arrays.asList(mockItemCosts1, mockItemCosts2));

        Item mockItem2 = new Item();
        ItemCosts mockItemCosts3 = new ItemCosts();
        mockItemCosts3.setCalculatedCost("140");
        mockItem2.setItemCosts(Arrays.asList(mockItemCosts3));

        items.add(mockItem1);
        items.add(mockItem2);

        when(checkoutData.getItems()).thenReturn(items);
    }

}

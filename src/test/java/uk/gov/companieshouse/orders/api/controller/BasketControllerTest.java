package uk.gov.companieshouse.orders.api.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.orders.api.dto.BasketPaymentRequestDTO;
import uk.gov.companieshouse.orders.api.mapper.BasketMapper;
import uk.gov.companieshouse.orders.api.mapper.CheckoutToPaymentDetailsMapper;
import uk.gov.companieshouse.orders.api.mapper.DeliveryDetailsMapper;
import uk.gov.companieshouse.orders.api.model.Checkout;
import uk.gov.companieshouse.orders.api.model.CheckoutData;
import uk.gov.companieshouse.orders.api.model.PaymentStatus;
import uk.gov.companieshouse.orders.api.service.ApiClientService;
import uk.gov.companieshouse.orders.api.service.BasketService;
import uk.gov.companieshouse.orders.api.service.CheckoutService;
import uk.gov.companieshouse.orders.api.service.OrderService;
import uk.gov.companieshouse.orders.api.validator.CheckoutBasketValidator;
import uk.gov.companieshouse.orders.api.validator.DeliveryDetailsValidator;

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
    private BasketMapper basketMapper;

    @Mock
    private DeliveryDetailsMapper deliveryDetailsMapper;

    @Mock
    private CheckoutToPaymentDetailsMapper checkoutToPaymentDetailsMapper;

    @Mock
    private BasketService basketService;

    @Mock
    private CheckoutService checkoutService;

    @Mock
    private Checkout checkout;

    @Mock
    private CheckoutData checkoutData;

    @Mock
    private CheckoutBasketValidator checkoutBasketValidator;

    @Mock
    private DeliveryDetailsValidator deliveryDetailsValidator;

    @Mock
    private ApiClientService apiClientService;

    @Mock
    private OrderService orderService;

    @Mock
    private BasketPaymentRequestDTO paymentStatusUpdate;

    @Test
    @DisplayName("Patch payment details PAID status update is saved")
    void patchPaymentDetailsPaidStatusUpdateIsSaved() {
        patchPaymentDetailsStatusUpdateIsSaved(PaymentStatus.PAID);
    }

    @Test
    @DisplayName("Patch payment details FAILED status update is saved")
    void patchPaymentDetailsFailedStatusUpdateIsSaved() {
        patchPaymentDetailsStatusUpdateIsSaved(PaymentStatus.FAILED);
    }

    @Test
    @DisplayName("Patch payment details PENDING status update is saved")
    void patchPaymentDetailsPendingStatusUpdateIsSaved() {
        patchPaymentDetailsStatusUpdateIsSaved(PaymentStatus.PENDING);
    }

    @Test
    @DisplayName("Patch payment details EXPIRED status update is saved")
    void patchPaymentDetailsExpiredStatusUpdateIsSaved() {
        patchPaymentDetailsStatusUpdateIsSaved(PaymentStatus.EXPIRED);
    }

    @Test
    @DisplayName("Patch payment details IN_PROGRESS status update is saved")
    void patchPaymentDetailsInProgressStatusUpdateIsSaved() {
        patchPaymentDetailsStatusUpdateIsSaved(PaymentStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("Patch payment details NO_FUNDS status update is saved")
    void patchPaymentDetailsNoFundsStatusUpdateIsSaved() {
        patchPaymentDetailsStatusUpdateIsSaved(PaymentStatus.NO_FUNDS);
    }

    /**
     * Verifies that the controller has requested the checkout service to save the updated status to the checkout.
     * @param paymentOutcome the payment status updated value
     */
    private void patchPaymentDetailsStatusUpdateIsSaved(final PaymentStatus paymentOutcome) {

        // Given
        final BasketPaymentRequestDTO paymentStatusUpdate = new BasketPaymentRequestDTO();
        paymentStatusUpdate.setStatus(paymentOutcome);
        when(checkoutService.getCheckoutById("checkoutId")).thenReturn(Optional.of(checkout));
        when(checkout.getData()).thenReturn(checkoutData);

        // When
        controllerUnderTest.patchBasketPaymentDetails(paymentStatusUpdate, "checkoutId", "requestId");

        // Then
        verify(checkoutData).setStatus(paymentOutcome);
        verify(checkout).getData();
        verify(checkoutService).saveCheckout(checkout);
    }

}

package uk.gov.companieshouse.orders.api.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.companieshouse.api.model.payment.PaymentApi;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.orders.api.dto.*;
import uk.gov.companieshouse.orders.api.exception.ConflictException;
import uk.gov.companieshouse.orders.api.exception.ErrorType;
import uk.gov.companieshouse.orders.api.mapper.BasketMapper;
import uk.gov.companieshouse.orders.api.mapper.CheckoutToPaymentDetailsMapper;
import uk.gov.companieshouse.orders.api.mapper.DeliveryDetailsMapper;
import uk.gov.companieshouse.orders.api.mapper.ItemMapper;
import uk.gov.companieshouse.orders.api.model.*;
import uk.gov.companieshouse.orders.api.service.ApiClientService;
import uk.gov.companieshouse.orders.api.service.BasketService;
import uk.gov.companieshouse.orders.api.service.CheckoutService;
import uk.gov.companieshouse.orders.api.service.OrderService;
import uk.gov.companieshouse.orders.api.util.EricHeaderHelper;
import uk.gov.companieshouse.orders.api.validator.CheckoutBasketValidator;
import uk.gov.companieshouse.orders.api.validator.DeliveryDetailsValidator;
import uk.gov.companieshouse.sdk.manager.ApiSdkManager;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.http.HttpStatus.*;
import static uk.gov.companieshouse.orders.api.OrdersApiApplication.*;

@RestController
public class BasketController {
    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAMESPACE);

    public static final String CHECKOUT_ID_PATH_VARIABLE = "checkoutId";

    /** <code>${uk.gov.companieshouse.orders.api.basket.checkouts}/{checkoutId}/payment</code> */
    public static final String GET_PAYMENT_DETAILS_URI =
            "${uk.gov.companieshouse.orders.api.basket.checkouts}/{"
            + CHECKOUT_ID_PATH_VARIABLE + "}/payment";
    public static final String ADD_ITEM_URI =
            "${uk.gov.companieshouse.orders.api.basket.items}";
    public static final String BASKET_URI =
            "${uk.gov.companieshouse.orders.api.basket}";
    public static final String CHECKOUT_BASKET_URI =
            "${uk.gov.companieshouse.orders.api.basket.checkouts}";
    public static final String PATCH_PAYMENT_DETAILS_URI =
            "${uk.gov.companieshouse.orders.api.basket.checkouts}/{id}/payment";

    private static final String PAYMENT_REQUIRED_HEADER = "x-payment-required";
    @Value("${uk.gov.companieshouse.payments.api.payments}")
    private String costsLink;

    private final ItemMapper itemMapper;
    private final BasketMapper basketMapper;
    private final DeliveryDetailsMapper deliveryDetailsMapper;
    private final CheckoutToPaymentDetailsMapper checkoutToPaymentDetailsMapper;
    private final BasketService basketService;
    private final CheckoutService checkoutService;
    private final CheckoutBasketValidator checkoutBasketValidator;
    private final DeliveryDetailsValidator deliveryDetailsValidator;
    private final ApiClientService apiClientService;
    private final OrderService orderService;

    public BasketController(final ItemMapper itemMapper,
                            final BasketMapper basketMapper,
                            final DeliveryDetailsMapper deliveryDetailsMapper,
                            final CheckoutToPaymentDetailsMapper checkoutDataMapper,
                            final BasketService basketService,
                            final CheckoutService checkoutService,
                            final CheckoutBasketValidator checkoutBasketValidator,
                            final ApiClientService apiClientService,
                            final DeliveryDetailsValidator deliveryDetailsValidator,
                            final OrderService orderService){
        this.itemMapper = itemMapper;
        this.deliveryDetailsMapper = deliveryDetailsMapper;
        this.basketMapper = basketMapper;
        this.checkoutToPaymentDetailsMapper = checkoutDataMapper;
        this.basketService = basketService;
        this.checkoutService = checkoutService;
        this.checkoutBasketValidator = checkoutBasketValidator;
        this.deliveryDetailsValidator = deliveryDetailsValidator;
        this.apiClientService = apiClientService;
        this.orderService = orderService;
    }

    @GetMapping(GET_PAYMENT_DETAILS_URI)
    public ResponseEntity<Object> getPaymentDetails(final @PathVariable(CHECKOUT_ID_PATH_VARIABLE) String checkoutId,
                                                    final @RequestHeader(REQUEST_ID_HEADER_NAME) String requestId){
        trace("ENTERING getPaymentDetails(" + checkoutId + ")", requestId);

        final Checkout checkout = checkoutService.getCheckoutById(checkoutId)
                .orElseThrow(ResourceNotFoundException::new);
        CheckoutData checkoutData = checkout.getData();

        PaymentDetailsDTO paymentDetailsDTO = checkoutToPaymentDetailsMapper.checkoutToPaymentDetailsMapper(checkout);
        checkoutToPaymentDetailsMapper.updateDTOWithPaymentDetails(checkoutData, paymentDetailsDTO);
        trace("EXITING getPaymentDetails() with " + paymentDetailsDTO, requestId);

        return ResponseEntity.status(OK).body(paymentDetailsDTO);
    }

    @PostMapping(ADD_ITEM_URI)
    public ResponseEntity<?> addItemToBasket(final @Valid @RequestBody AddToBasketRequestDTO addToBasketRequestDTO,
                                                                  HttpServletRequest request,
                                                                  final @RequestHeader(REQUEST_ID_HEADER_NAME) String requestId){
        trace("ENTERING addItemToBasket(" + addToBasketRequestDTO + ")", requestId);
        String itemUri = addToBasketRequestDTO.getItemUri();
        Item item;
        try {
            item = apiClientService.getItem(itemUri);
        } catch (Exception exception) {
            LOGGER.error("Failed to get item " + itemUri, exception);
            return ResponseEntity.status(BAD_REQUEST).body(new ApiError(BAD_REQUEST, ErrorType.BASKET_ITEM_INVALID.getValue()));
        }

        final Optional<Basket> retrievedBasket = basketService.getBasketById(EricHeaderHelper.getIdentity(request));

        Basket mappedBasket = basketMapper.addToBasketRequestDTOToBasket(addToBasketRequestDTO);

        if(retrievedBasket.isPresent()) {
            retrievedBasket.get().getData().setItems(mappedBasket.getData().getItems());
            basketService.saveBasket(retrievedBasket.get());
        } else {
            mappedBasket.setId(EricHeaderHelper.getIdentity(request));
            basketService.saveBasket(mappedBasket);
        }

        BasketItemDTO basketItemDTO = itemMapper.itemToBasketItemDTO(item);

        trace("EXITING addItemToBasket() with " + addToBasketRequestDTO, requestId);
        return ResponseEntity.status(HttpStatus.OK).body(basketItemDTO);
    }

    @GetMapping(BASKET_URI)
    public ResponseEntity<BasketData> getBasket(HttpServletRequest request,
                                            final @RequestHeader(REQUEST_ID_HEADER_NAME) String requestId) {

        trace("ENTERING getBasket", requestId);

        final Optional<Basket> retrievedBasket = basketService.getBasketById(EricHeaderHelper.getIdentity(request));

        Basket basket;
        if(retrievedBasket.isPresent()) {
            basket = retrievedBasket.get();
        } else {
            Basket newBasket = new Basket();
            newBasket.setId(EricHeaderHelper.getIdentity((request)));
            basket = basketService.saveBasket(newBasket);
        }

        return ResponseEntity.status(HttpStatus.OK).body(basket.getData());
    }

    @PatchMapping(BASKET_URI)
    public ResponseEntity<?> addDeliveryDetailsToBasket(final @Valid @RequestBody AddDeliveryDetailsRequestDTO addDeliveryDetailsRequestDTO,
                                                                 HttpServletRequest request,
                                                                 final @RequestHeader(REQUEST_ID_HEADER_NAME) String requestId){
        trace("ENTERING addDeliveryDetailsToBasket(" + addDeliveryDetailsRequestDTO + ")", requestId);

        final Optional<Basket> retrievedBasket = basketService.getBasketById(EricHeaderHelper.getIdentity(request));

        DeliveryDetails mappedDeliveryDetails = deliveryDetailsMapper.addToDeliveryDetailsRequestDTOToDeliveryDetails(addDeliveryDetailsRequestDTO);

        final List<String> errors = deliveryDetailsValidator.getValidationErrors(addDeliveryDetailsRequestDTO);
        if (!errors.isEmpty()) {
            return ResponseEntity.status(BAD_REQUEST).body(new ApiError(BAD_REQUEST, errors));
        }

        Basket returnedBasket;
        if(retrievedBasket.isPresent()) {
            Basket basket = retrievedBasket.get();
            final List<String> basketErrors = checkoutBasketValidator.getValidationErrors(basket);
            if (!basketErrors.isEmpty() && basketErrors.contains(ErrorType.BASKET_ITEM_INVALID.getValue())){
                return ResponseEntity.status(BAD_REQUEST).body(new ApiError(BAD_REQUEST, basketErrors));
            }
            basket.getData().setDeliveryDetails(mappedDeliveryDetails);
            returnedBasket = basketService.saveBasket(retrievedBasket.get());
        } else {
            Basket basket = new Basket();
            basket.setId(EricHeaderHelper.getIdentity((request)));
            basket.getData().setDeliveryDetails(mappedDeliveryDetails);
            returnedBasket = basketService.saveBasket(basket);
        }
        trace("EXITING addDeliveryDetailToBasket() with " + addDeliveryDetailsRequestDTO, requestId);
        return ResponseEntity.status(HttpStatus.OK).body(returnedBasket.getData());
    }

    @PostMapping(CHECKOUT_BASKET_URI)
    public ResponseEntity<?> checkoutBasket(@RequestBody(required = false) String json,
                                            HttpServletRequest request,
                                            final @RequestHeader(REQUEST_ID_HEADER_NAME) String requestId) {
        trace("Entering checkoutBasket", requestId);

        if(json!=null) {
            return ResponseEntity.status(BAD_REQUEST).body(new ApiError(BAD_REQUEST, "The body must be empty"));
        }

        final Basket retrievedBasket = basketService.getBasketById(EricHeaderHelper.getIdentity(request))
                .orElseThrow(ConflictException::new);

        final List<String> errors = checkoutBasketValidator.getValidationErrors(retrievedBasket);
        if (!errors.isEmpty()){
            if (errors.contains(ErrorType.BASKET_ITEMS_MISSING.getValue())) {
                return ResponseEntity.status(CONFLICT).body(new ApiError(CONFLICT, errors));
            }
            else if (errors.contains(ErrorType.BASKET_ITEM_INVALID.getValue())){
                return ResponseEntity.status(BAD_REQUEST).body(new ApiError(BAD_REQUEST, errors));
            }
        }

        Item item;
        String itemUri = null;
        try {
            itemUri = retrievedBasket.getData().getItems().get(0).getItemUri();
            item = apiClientService.getItem(itemUri);
        } catch (Exception exception) {
            LOGGER.error("Failed to get item "+itemUri, exception);
            return ResponseEntity.status(BAD_REQUEST).body(new ApiError(BAD_REQUEST, "Failed to retrieve item"));
        }

        Checkout checkout = checkoutService.createCheckout(item,
                EricHeaderHelper.getIdentity(request),
                EricHeaderHelper.getAuthorisedUser(request),
                retrievedBasket.getData().getDeliveryDetails());
        trace("Successfully created checkout with id "+checkout.getId(), requestId);

        CheckoutData checkoutData = checkout.getData();
        HttpHeaders headers = new HttpHeaders();
        int totalOrderCost = Integer.parseInt(checkoutData.getTotalOrderCost());
        if (totalOrderCost > 0) {
            headers.add(PAYMENT_REQUIRED_HEADER, costsLink);
            return new ResponseEntity<>(checkoutData, headers, ACCEPTED);
        }
        else {
            return new ResponseEntity<>(checkoutData, OK);
        }
    }

    @PatchMapping(PATCH_PAYMENT_DETAILS_URI)
    public ResponseEntity<String> patchBasketPaymentDetails(final @RequestBody BasketPaymentRequestDTO basketPaymentRequestDTO,
                                                            HttpServletRequest request,
                                                            final @PathVariable String id,
                                                            final @RequestHeader(REQUEST_ID_HEADER_NAME) String requestId) {
        trace("ENTERING patchBasketPaymentDetails(" + basketPaymentRequestDTO + ", " + id + ", " + requestId + ")", requestId);

        // Return checkout that is attempting to be updated
        final Checkout checkout = checkoutService.getCheckoutById(id)
                .orElseThrow(ResourceNotFoundException::new);
        final CheckoutData checkoutData = checkout.getData();

        // Check if payment was successful
        if (basketPaymentRequestDTO.getStatus().equals(PaymentStatus.PAID)) {
            PaymentApi paymentSession;

            // Retrieve payment session from payments.api
            try {
                // Use header in request as header for request to payments.api
                String passthroughHeader = request.getHeader(ApiSdkManager.getEricPassthroughTokenHeader());
                paymentSession = apiClientService.getPaymentSummary(passthroughHeader, basketPaymentRequestDTO.getPaymentReference());
            } catch (Exception exception) {
                LOGGER.error("Failed to return payment " + basketPaymentRequestDTO.getPaymentReference() + " from payments api", exception);
                return ResponseEntity.status(NOT_FOUND).body("Failed to return payment " + basketPaymentRequestDTO.getPaymentReference() + " from payments api");
            }
            trace("Payment summary successfully returned for " + basketPaymentRequestDTO.getPaymentReference(), requestId);

            // Check payment is paid with payments API
            if (!paymentSession.getStatus().equals("paid")) {
                LOGGER.error("Payment is not set to paid in payments api for payment " + basketPaymentRequestDTO.getPaymentReference());
                return ResponseEntity.status(BAD_REQUEST).body("Payment is not set to paid in payment api for payment " + basketPaymentRequestDTO.getPaymentReference());
            }

            // Check the amount paid in the payment session and the amount expected in the order are the same
            if (Double.parseDouble(paymentSession.getAmount()) != calculateTotalAmountToBePaid(checkout)) {
                String errorMessage = "Total amount paid for with payment session " + basketPaymentRequestDTO.getPaymentReference() + ": " + paymentSession.getAmount()
                        + " does not match amount expected for order " + id + ": " + checkoutData.getTotalOrderCost();
                LOGGER.error(errorMessage);
                return ResponseEntity.status(BAD_REQUEST).body(errorMessage);
            }

            // Get the URI for the resource in the payments session
            String paymentsResourceUri = paymentSession.getLinks().get("resource")
                    .substring(paymentSession.getLinks().get("resource").lastIndexOf("/basket/checkouts/"));
            // Check that the URI that has been requested to mark as paid, matches URI from the payments session
            if (!paymentsResourceUri.equals(request.getRequestURI())) {
                String errorMessage = "The URI that is attempted to be closed " + request.getRequestURI()
                        + " does not match the URI that the payment session is created for " + paymentsResourceUri;
                LOGGER.error(errorMessage);
                return ResponseEntity.status(BAD_REQUEST).body(errorMessage);
            }

            trace("Payment confirmed as paid with payments API for payment session: " + basketPaymentRequestDTO.getPaymentReference()
                    + " and order: " + id, requestId);

            // Update the checkout of paid order
            final Checkout updatedCheckout = updateCheckout(checkout, basketPaymentRequestDTO);

            // Process successful payment
            processSuccessfulPayment(requestId, updatedCheckout);
        } else {

            // Update the checkout of non-paid order
            updateCheckout(checkout, basketPaymentRequestDTO);
        }

        trace("Order " + id + " has been marked as " + basketPaymentRequestDTO.getStatus() + " with payment id " + basketPaymentRequestDTO.getPaymentReference(), requestId);

        return ResponseEntity.status(NO_CONTENT).body(null);
    }

    /**
     * Updates the checkout identified with the payment status update provided.
     * @param checkout the checkout to be updated
     * @param update the payment status update
     * @return the updated checkout
     */
    private Checkout updateCheckout(final Checkout checkout, final BasketPaymentRequestDTO update) {
        final CheckoutData data = checkout.getData();
        data.setStatus(update.getStatus());
        if (update.getStatus() == PaymentStatus.PAID) {
            data.setPaidAt(update.getPaidAt());
            data.setPaymentReference(update.getPaymentReference());
        }
        checkoutService.saveCheckout(checkout);
        return checkout;
    }

    /**
     * Performs the actions required to process a successful payment.
     * @param requestId the request ID used for logging purposes
     * @param checkout the checkout required to process the payment
     */
    private void processSuccessfulPayment(final String requestId,
                                          final Checkout checkout) {
        final Order order = orderService.createOrder(checkout);
        trace("Created order: " + order, requestId);
        final Basket basket = basketService.clearBasket(checkout.getUserId());
        trace("Cleared basket: " + basket, requestId);
    }

    /**
     * Performs the calculation to work out the total to be paid for this checkout.
     * @param checkout the checkout required to calculate the total to be paid.
     * @return the total to be paid
     */
    private Double calculateTotalAmountToBePaid(Checkout checkout) {
        // total is type Double to compare with decimal value that is returned from payments.api 
        Double total = 0.00;
        for (Item item : checkout.getData().getItems()) {
            for (ItemCosts itemCosts : item.getItemCosts()) {
                total += Double.parseDouble(itemCosts.getCalculatedCost());
            }
        }

        return total;
    }

    /**
     * Utility method that logs each message with the request ID for log tracing/analysis.
     * @param message the message to log
     * @param requestId the request ID
     */
    private void trace(final String message, final String requestId) {
        final Map<String, Object> logData = new HashMap<>();
        logData.put(LOG_MESSAGE_DATA_KEY, message);
        LOGGER.traceContext(requestId, "X Request ID header", logData);
    }
}

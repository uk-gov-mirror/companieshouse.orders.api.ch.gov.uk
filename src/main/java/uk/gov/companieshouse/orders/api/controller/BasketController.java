package uk.gov.companieshouse.orders.api.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.api.model.payment.PaymentApi;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.orders.api.dto.AddDeliveryDetailsRequestDTO;
import uk.gov.companieshouse.orders.api.dto.AddToBasketRequestDTO;
import uk.gov.companieshouse.orders.api.dto.BasketItemDTO;
import uk.gov.companieshouse.orders.api.dto.BasketPaymentRequestDTO;
import uk.gov.companieshouse.orders.api.dto.PaymentDetailsDTO;
import uk.gov.companieshouse.orders.api.exception.ConflictException;
import uk.gov.companieshouse.orders.api.exception.ErrorType;
import uk.gov.companieshouse.orders.api.logging.LoggingUtils;
import uk.gov.companieshouse.orders.api.mapper.BasketMapper;
import uk.gov.companieshouse.orders.api.mapper.CheckoutToPaymentDetailsMapper;
import uk.gov.companieshouse.orders.api.mapper.DeliveryDetailsMapper;
import uk.gov.companieshouse.orders.api.mapper.ItemMapper;
import uk.gov.companieshouse.orders.api.model.ApiError;
import uk.gov.companieshouse.orders.api.model.Basket;
import uk.gov.companieshouse.orders.api.model.Checkout;
import uk.gov.companieshouse.orders.api.model.CheckoutData;
import uk.gov.companieshouse.orders.api.model.DeliveryDetails;
import uk.gov.companieshouse.orders.api.model.Item;
import uk.gov.companieshouse.orders.api.model.ItemCosts;
import uk.gov.companieshouse.orders.api.model.Order;
import uk.gov.companieshouse.orders.api.model.PaymentStatus;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.companieshouse.orders.api.logging.LoggingUtils.APPLICATION_NAMESPACE;
import static uk.gov.companieshouse.orders.api.OrdersApiApplication.REQUEST_ID_HEADER_NAME;

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
        Map<String, Object> logMap = LoggingUtils.createLogMapWithRequestId(requestId);
        logMap.put(LoggingUtils.CHECKOUT_ID, checkoutId);
        LOGGER.info("Getting payment details", logMap);

        final Checkout checkout = checkoutService.getCheckoutById(checkoutId)
                .orElseThrow(ResourceNotFoundException::new);
        CheckoutData checkoutData = checkout.getData();

        PaymentDetailsDTO paymentDetailsDTO = checkoutToPaymentDetailsMapper.checkoutToPaymentDetailsMapper(checkout);
        checkoutToPaymentDetailsMapper.updateDTOWithPaymentDetails(checkoutData, paymentDetailsDTO);
       
        logMap.put(LoggingUtils.STATUS, OK);
        LOGGER.info("Payment details returned", logMap);

        return ResponseEntity.status(OK).body(paymentDetailsDTO);
    }

    @PostMapping(ADD_ITEM_URI)
    public ResponseEntity<?> addItemToBasket(final @Valid @RequestBody AddToBasketRequestDTO addToBasketRequestDTO,
                                                                  HttpServletRequest request,
                                                                  final @RequestHeader(REQUEST_ID_HEADER_NAME) String requestId){
        Map<String, Object> logMap = LoggingUtils.createLogMapWithRequestId(requestId);
        LOGGER.infoRequest(request, "Adding item to basket", logMap);
        
        String itemUri = addToBasketRequestDTO.getItemUri();
        logMap.put(LoggingUtils.ITEM_URI, itemUri);
        Item item;
        try {
            item = apiClientService.getItem(itemUri);
        } catch (Exception exception) {
            logMap.put(LoggingUtils.EXCEPTION, exception);
            logMap.put(LoggingUtils.STATUS, BAD_REQUEST);
            logMap.put(LoggingUtils.ERROR_TYPE, ErrorType.BASKET_ITEM_INVALID.getValue());
            LOGGER.errorRequest(request, "Failed to get item from API", logMap);
            return ResponseEntity.status(BAD_REQUEST).body(new ApiError(BAD_REQUEST, ErrorType.BASKET_ITEM_INVALID.getValue()));
        }
        if(item != null) {            
            LoggingUtils.logIfNotNull(logMap, LoggingUtils.COMPANY_NUMBER, item.getCompanyNumber());
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

        logMap.put(LoggingUtils.BASKET_ID, mappedBasket.getId());
        logMap.put(LoggingUtils.STATUS, HttpStatus.OK);
        LOGGER.infoRequest(request, "Item added to basket", logMap);
        return ResponseEntity.status(HttpStatus.OK).body(basketItemDTO);
    }

    @GetMapping(BASKET_URI)
    public ResponseEntity<?> getBasket(HttpServletRequest request,
                                            final @RequestHeader(REQUEST_ID_HEADER_NAME) String requestId) {

        Map<String, Object> logMap = LoggingUtils.createLogMapWithRequestId(requestId);
        LOGGER.infoRequest(request, "Getting basket", logMap);

        final Optional<Basket> retrievedBasket = basketService.getBasketById(EricHeaderHelper.getIdentity(request));

        Basket basket;
        if(retrievedBasket.isPresent()) {
            LOGGER.infoRequest(request, "Basket present", logMap);
            basket = retrievedBasket.get();
        } else {
            logMap.put(LoggingUtils.STATUS, HttpStatus.OK);
            LOGGER.infoRequest(request, "No basket present, creating a basket", logMap);
            Basket newBasket = new Basket();
            newBasket.setId(EricHeaderHelper.getIdentity((request)));
            basket = basketService.saveBasket(newBasket);
            return ResponseEntity.status(HttpStatus.OK).body(basket.getData());
        }

        if(basket.getData().getItems().size() == 0) {
            logMap.put(LoggingUtils.STATUS, HttpStatus.OK);
            LOGGER.infoRequest(request, "Basket has 0 items", logMap);
            return ResponseEntity.status(HttpStatus.OK).body(basket.getData());
        }

        Item item;
        String itemUri = null;
        try {
            itemUri = basket.getData().getItems().get(0).getItemUri();
            item = apiClientService.getItem(itemUri);

            logMap.put(LoggingUtils.ITEM_URI, itemUri);
            if(item != null) {                
                LoggingUtils.logIfNotNull(logMap, LoggingUtils.COMPANY_NUMBER, item.getCompanyNumber());
            }
            List<Item> items = new ArrayList<>();
            items.add(item);

            basket.getData().setItems(items);
        } catch (Exception exception) {
            logMap.put(LoggingUtils.EXCEPTION, exception);
            logMap.put(LoggingUtils.STATUS, BAD_REQUEST);
            LOGGER.errorRequest(request, "Failed to get item ", logMap);
            return ResponseEntity.status(BAD_REQUEST).body(new ApiError(BAD_REQUEST, "Failed to retrieve item"));
        }

        logMap.put(LoggingUtils.STATUS, HttpStatus.OK);
        LOGGER.infoRequest(request, "Basket retrieved and returned", logMap);
        return ResponseEntity.status(HttpStatus.OK).body(basket.getData());
    }

    @PatchMapping(BASKET_URI)
    public ResponseEntity<?> addDeliveryDetailsToBasket(final @Valid @RequestBody AddDeliveryDetailsRequestDTO addDeliveryDetailsRequestDTO,
                                                                 HttpServletRequest request,
                                                                 final @RequestHeader(REQUEST_ID_HEADER_NAME) String requestId){
        Map<String, Object> logMap = LoggingUtils.createLogMapWithRequestId(requestId);
        LOGGER.infoRequest(request, "Adding delivery details to basket", logMap);

        final Optional<Basket> retrievedBasket = basketService.getBasketById(EricHeaderHelper.getIdentity(request));

        DeliveryDetails mappedDeliveryDetails = deliveryDetailsMapper.addToDeliveryDetailsRequestDTOToDeliveryDetails(addDeliveryDetailsRequestDTO);

        final List<String> errors = deliveryDetailsValidator.getValidationErrors(addDeliveryDetailsRequestDTO);
        if (!errors.isEmpty()) {
            logMap.put(LoggingUtils.VALIDATION_ERRORS, errors);
            logMap.put(LoggingUtils.STATUS, BAD_REQUEST);
            LOGGER.errorRequest(request, "Validation errors in delivery details", logMap);
            return ResponseEntity.status(BAD_REQUEST).body(new ApiError(BAD_REQUEST, errors));
        }

        Basket returnedBasket;
        if(retrievedBasket.isPresent()) {
            Basket basket = retrievedBasket.get();
            LoggingUtils.logIfNotNull(logMap, LoggingUtils.BASKET_ID, basket.getId());
            final List<String> basketErrors = checkoutBasketValidator.getValidationErrors(basket);
            if (!basketErrors.isEmpty() && basketErrors.contains(ErrorType.BASKET_ITEM_INVALID.getValue())){
                logMap.put(LoggingUtils.VALIDATION_ERRORS, basketErrors);
                logMap.put(LoggingUtils.ERROR_TYPE, ErrorType.BASKET_ITEM_INVALID.getValue());
                logMap.put(LoggingUtils.STATUS, BAD_REQUEST);
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
        logMap.put(LoggingUtils.STATUS, HttpStatus.OK);
        LOGGER.infoRequest(request, "Delivery details added to basket", logMap);
        return ResponseEntity.status(HttpStatus.OK).body(returnedBasket.getData());
    }

    @PostMapping(CHECKOUT_BASKET_URI)
    public ResponseEntity<?> checkoutBasket(@RequestBody(required = false) String json,
                                            HttpServletRequest request,
                                            final @RequestHeader(REQUEST_ID_HEADER_NAME) String requestId) {
        Map<String, Object> logMap = LoggingUtils.createLogMapWithRequestId(requestId);
        LOGGER.infoRequest(request, "Checkout basket request", logMap);

        if(json!=null) {
            logMap.put(LoggingUtils.STATUS, BAD_REQUEST);
            LOGGER.errorRequest(request, "The request body must be empty", logMap);
            return ResponseEntity.status(BAD_REQUEST).body(new ApiError(BAD_REQUEST, "The body must be empty"));
        }

        final Basket retrievedBasket = basketService.getBasketById(EricHeaderHelper.getIdentity(request))
                .orElseThrow(ConflictException::new);

        final List<String> errors = checkoutBasketValidator.getValidationErrors(retrievedBasket);
        if (!errors.isEmpty()){
            logMap.put(LoggingUtils.VALIDATION_ERRORS, errors);
            if (errors.contains(ErrorType.BASKET_ITEMS_MISSING.getValue())) {
                logMap.put(LoggingUtils.STATUS, CONFLICT);
                logMap.put(LoggingUtils.ERROR_TYPE, ErrorType.BASKET_ITEMS_MISSING.getValue());
                LOGGER.errorRequest(request, "Validation error - basket items missing", logMap);
                return ResponseEntity.status(CONFLICT).body(new ApiError(CONFLICT, errors));
            }
            else if (errors.contains(ErrorType.BASKET_ITEM_INVALID.getValue())){
                logMap.put(LoggingUtils.STATUS, BAD_REQUEST);
                logMap.put(LoggingUtils.ERROR_TYPE, ErrorType.BASKET_ITEM_INVALID.getValue());
                LOGGER.errorRequest(request, "Validation error - basket item invalid", logMap);
                return ResponseEntity.status(BAD_REQUEST).body(new ApiError(BAD_REQUEST, errors));
            }
        }

        Item item;
        String itemUri = null;
        try {
            itemUri = retrievedBasket.getData().getItems().get(0).getItemUri();
            LoggingUtils.logIfNotNull(logMap, LoggingUtils.ITEM_URI, itemUri);
            item = apiClientService.getItem(itemUri);
            if(item != null) {                
                LoggingUtils.logIfNotNull(logMap, LoggingUtils.COMPANY_NUMBER, item.getCompanyNumber());
            }
        } catch (Exception exception) {
            logMap.put(LoggingUtils.STATUS, BAD_REQUEST);
            logMap.put(LoggingUtils.EXCEPTION, exception);
            LOGGER.errorRequest(request, "Failed to retrieve item from api client", logMap);
            return ResponseEntity.status(BAD_REQUEST).body(new ApiError(BAD_REQUEST, "Failed to retrieve item"));
        }

        Checkout checkout = checkoutService.createCheckout(item,
                EricHeaderHelper.getIdentity(request),
                EricHeaderHelper.getAuthorisedUser(request),
                retrievedBasket.getData().getDeliveryDetails());
        logMap.put(LoggingUtils.CHECKOUT_ID, checkout.getId());
        LOGGER.infoRequest(request, "Checkout successfully created", logMap);

        CheckoutData checkoutData = checkout.getData();
        HttpHeaders headers = new HttpHeaders();
        int totalOrderCost = Integer.parseInt(checkoutData.getTotalOrderCost());
        if (totalOrderCost > 0) {
            logMap.put(LoggingUtils.STATUS, ACCEPTED);
            LOGGER.infoRequest(request, "Basket checkout completed requiring payment", logMap);
            headers.add(PAYMENT_REQUIRED_HEADER, costsLink);
            return new ResponseEntity<>(checkoutData, headers, ACCEPTED);
        }
        else {
            logMap.put(LoggingUtils.STATUS,  OK);
            LOGGER.infoRequest(request, "Basket checkout completed no payment required", logMap);
            return new ResponseEntity<>(checkoutData, OK);
        }
    }

    @PatchMapping(PATCH_PAYMENT_DETAILS_URI)
    public ResponseEntity<String> patchBasketPaymentDetails(final @RequestBody BasketPaymentRequestDTO basketPaymentRequestDTO,
                                                            HttpServletRequest request,
                                                            final @PathVariable String id,
                                                            final @RequestHeader(REQUEST_ID_HEADER_NAME) String requestId) {
        Map<String, Object> logMap = LoggingUtils.createLogMapWithRequestId(requestId);
        LoggingUtils.logIfNotNull(logMap, LoggingUtils.CHECKOUT_ID, id);
        LoggingUtils.logIfNotNull(logMap, LoggingUtils.ORDER_ID, id);
        LoggingUtils.logIfNotNull(logMap, LoggingUtils.PAYMENT_STATUS, basketPaymentRequestDTO.getStatus());
        LoggingUtils.logIfNotNull(logMap, LoggingUtils.PAYMENT_REF, basketPaymentRequestDTO.getPaymentReference());
        LOGGER.infoRequest(request, "Patching basket payment details", logMap);

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
                logMap.put(LoggingUtils.EXCEPTION, exception);
                logMap.put(LoggingUtils.STATUS, NOT_FOUND);
                LOGGER.errorRequest(request, "Failed to return payment from payments api", logMap);
                return ResponseEntity.status(NOT_FOUND).body("Failed to return payment " + basketPaymentRequestDTO.getPaymentReference() + " from payments api");
            }
            LOGGER.infoRequest(request, "Payment summary successfully returned", logMap);
            
            // Check payment is paid with payments API
            if (!paymentSession.getStatus().equals("paid")) {
                LOGGER.errorRequest(request, "Payment is not set to paid in payments api", logMap);
                return ResponseEntity.status(BAD_REQUEST).body("Payment is not set to paid in payment api for payment " + basketPaymentRequestDTO.getPaymentReference());
            }

            // Check the amount paid in the payment session and the amount expected in the order are the same
            if (Double.parseDouble(paymentSession.getAmount()) != calculateTotalAmountToBePaid(checkout)) {
                LoggingUtils.logIfNotNull(logMap, LoggingUtils.ORDER_TOTAL_COST, checkoutData.getTotalOrderCost());
                logMap.put(LoggingUtils.STATUS, BAD_REQUEST);
                LOGGER.errorRequest(request, "Total amount paid with payment session does not match amount expected", logMap);
                String errorMessage = "Total amount paid for with payment session " + basketPaymentRequestDTO.getPaymentReference() + ": " + paymentSession.getAmount()
                        + " does not match amount expected for order " + id + ": " + checkoutData.getTotalOrderCost();
                return ResponseEntity.status(BAD_REQUEST).body(errorMessage);
            }

            // Get the URI for the resource in the payments session
            String paymentsResourceUri = paymentSession.getLinks().get("resource")
                    .substring(paymentSession.getLinks().get("resource").lastIndexOf("/basket/checkouts/"));
            // Check that the URI that has been requested to mark as paid, matches URI from the payments session
            if (!paymentsResourceUri.equals(request.getRequestURI())) {
                logMap.put(LoggingUtils.PAYMENT_URI, paymentsResourceUri);
                logMap.put(LoggingUtils.STATUS, BAD_REQUEST);
                LOGGER.errorRequest(request, "URI attemped to be closed does not match payment session URI", logMap);
                String errorMessage = "The URI that is attempted to be closed " + request.getRequestURI()
                        + " does not match the URI that the payment session is created for " + paymentsResourceUri;
                return ResponseEntity.status(BAD_REQUEST).body(errorMessage);
            }

            LOGGER.infoRequest(request, "Payment confirmed as paid with payments api", logMap);

            // Update the checkout of paid order
            final Checkout updatedCheckout = updateCheckout(checkout, basketPaymentRequestDTO);

            // Process successful payment
            processSuccessfulPayment(requestId, updatedCheckout);
        } else {

            // Update the checkout of non-paid order
            updateCheckout(checkout, basketPaymentRequestDTO);
            LOGGER.infoRequest(request, "Checkout updated for order not requiring any payment", logMap);
        }

        logMap.put(LoggingUtils.STATUS, NO_CONTENT);
        LOGGER.infoRequest(request, "Basket payment details updated", logMap);
        
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
        Map<String, Object> logMap = LoggingUtils.createLogMapWithRequestId(requestId);
        LoggingUtils.logIfNotNull(logMap, LoggingUtils.USER_ID, checkout.getId());
        orderService.createOrder(checkout);
        basketService.clearBasket(checkout.getUserId());
        LOGGER.info("Process successful payment, order created and basket cleared", logMap);
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
}

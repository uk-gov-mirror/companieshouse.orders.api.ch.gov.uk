package uk.gov.companieshouse.orders.api.controller;

import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.orders.api.dto.*;
import uk.gov.companieshouse.orders.api.exception.ConflictException;
import uk.gov.companieshouse.orders.api.exception.ErrorType;
import uk.gov.companieshouse.orders.api.mapper.BasketMapper;
import uk.gov.companieshouse.orders.api.mapper.CheckoutToPaymentDetailsMapper;
import uk.gov.companieshouse.orders.api.mapper.DeliveryDetailsMapper;
import uk.gov.companieshouse.orders.api.model.*;
import uk.gov.companieshouse.orders.api.service.ApiClientService;
import uk.gov.companieshouse.orders.api.service.BasketService;
import uk.gov.companieshouse.orders.api.service.CheckoutService;
import uk.gov.companieshouse.orders.api.service.OrderService;
import uk.gov.companieshouse.orders.api.util.EricHeaderHelper;
import uk.gov.companieshouse.orders.api.validator.CheckoutBasketValidator;
import uk.gov.companieshouse.orders.api.validator.DeliveryDetailsValidator;

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
    public static final String PATCH_BASKET_URI =
            "${uk.gov.companieshouse.orders.api.basket}";
    public static final String CHECKOUT_BASKET_URI =
            "${uk.gov.companieshouse.orders.api.basket.checkouts}";
    public static final String PATCH_PAYMENT_DETAILS_URI =
            "${uk.gov.companieshouse.orders.api.basket.checkouts}/{id}/payment";

    private final BasketMapper basketMapper;
    private final DeliveryDetailsMapper deliveryDetailsMapper;
    private final CheckoutToPaymentDetailsMapper checkoutToPaymentDetailsMapper;
    private final BasketService basketService;
    private final CheckoutService checkoutService;
    private final CheckoutBasketValidator checkoutBasketValidator;
    private final DeliveryDetailsValidator deliveryDetailsValidator;
    private final ApiClientService apiClientService;
    private final OrderService orderService;

    public BasketController(final BasketMapper mapper,
                            final DeliveryDetailsMapper deliveryDetailsMapper,
                            final CheckoutToPaymentDetailsMapper checkoutDataMapper,
                            final BasketService basketService,
                            final CheckoutService checkoutService,
                            final CheckoutBasketValidator checkoutBasketValidator,
                            final ApiClientService apiClientService,
                            final DeliveryDetailsValidator deliveryDetailsValidator,
                            final OrderService orderService){
        this.deliveryDetailsMapper = deliveryDetailsMapper;
        this.basketMapper = mapper;
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
    public ResponseEntity<AddToBasketResponseDTO> addItemToBasket(final @Valid @RequestBody AddToBasketRequestDTO addToBasketRequestDTO,
                                                                  HttpServletRequest request,
                                                                  final @RequestHeader(REQUEST_ID_HEADER_NAME) String requestId){
        trace("ENTERING addItemToBasket(" + addToBasketRequestDTO + ")", requestId);

        final Optional<Basket> retrievedBasket = basketService.getBasketById(EricHeaderHelper.getIdentity(request));

        Basket mappedBasket = basketMapper.addToBasketRequestDTOToBasket(addToBasketRequestDTO);

        Basket returnedBasket;
        if(retrievedBasket.isPresent()) {
            retrievedBasket.get().getData().setItems(mappedBasket.getData().getItems());
            returnedBasket = basketService.saveBasket(retrievedBasket.get());
        } else {
            mappedBasket.setId(EricHeaderHelper.getIdentity(request));
            returnedBasket = basketService.saveBasket(mappedBasket);
        }

        final AddToBasketResponseDTO addToBasketResponseDTO = basketMapper.basketToAddToBasketDTO(returnedBasket);
        trace("EXITING addItemToBasket() with " + addToBasketRequestDTO, requestId);
        return ResponseEntity.status(HttpStatus.OK).body(addToBasketResponseDTO);
    }

    @PatchMapping(PATCH_BASKET_URI)
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
            if (!basketErrors.isEmpty() && basketErrors.contains(ErrorType.BASKET_ITEM_INVALID.value)){
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
        if (!errors.isEmpty() && errors.contains(ErrorType.BASKET_ITEMS_MISSING.value)){
            return ResponseEntity.status(CONFLICT).body(new ApiError(CONFLICT, errors));
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

        HttpStatus responseStatus = checkout.getData().getTotalOrderCost().equals("0") ? OK : ACCEPTED;

        return ResponseEntity.status(responseStatus).body(checkout.getData());
    }

    @PatchMapping(PATCH_PAYMENT_DETAILS_URI)
    public ResponseEntity<String> patchBasketPaymentDetails(final @RequestBody BasketPaymentRequestDTO basketPaymentRequestDTO,
                                                            final @PathVariable String id,
                                                            final @RequestHeader(REQUEST_ID_HEADER_NAME) String requestId) {
        trace("ENTERING patchBasketPaymentDetails(" + basketPaymentRequestDTO + ", " + id + ", " + requestId + ")", requestId);
        final Checkout updatedCheckout = updateCheckout(id, basketPaymentRequestDTO);
        if (basketPaymentRequestDTO.getStatus() == PaymentStatus.PAID) {
            processSuccessfulPayment(requestId, updatedCheckout);
        }
        return ResponseEntity.ok("");
    }

    /**
     * Updates the checkout identified with the payment status update provided.
     * @param checkoutId the id of the checkout to be updated
     * @param update the payment status update
     * @return the updated checkout
     */
    private Checkout updateCheckout(final String checkoutId, final BasketPaymentRequestDTO update) {
        final Checkout checkout = checkoutService.getCheckoutById(checkoutId)
                .orElseThrow(ResourceNotFoundException::new);
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

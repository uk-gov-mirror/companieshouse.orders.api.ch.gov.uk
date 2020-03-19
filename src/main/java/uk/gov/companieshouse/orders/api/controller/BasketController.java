package uk.gov.companieshouse.orders.api.controller;

import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.orders.api.dto.*;
import uk.gov.companieshouse.orders.api.exception.ConflictException;
import uk.gov.companieshouse.orders.api.mapper.BasketMapper;
import uk.gov.companieshouse.orders.api.mapper.CheckoutToPaymentDetailsMapper;
import uk.gov.companieshouse.orders.api.mapper.DeliveryDetailsMapper;
import uk.gov.companieshouse.orders.api.model.ApiError;
import uk.gov.companieshouse.orders.api.model.Basket;
import uk.gov.companieshouse.orders.api.model.Checkout;
import uk.gov.companieshouse.orders.api.model.DeliveryDetails;
import uk.gov.companieshouse.orders.api.model.Item;
import uk.gov.companieshouse.orders.api.model.Order;
import uk.gov.companieshouse.orders.api.model.PaymentStatus;
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
import java.util.*;

import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static uk.gov.companieshouse.orders.api.OrdersApiApplication.APPLICATION_NAMESPACE;
import static uk.gov.companieshouse.orders.api.OrdersApiApplication.LOG_MESSAGE_DATA_KEY;
import static uk.gov.companieshouse.orders.api.OrdersApiApplication.REQUEST_ID_HEADER_NAME;

@RestController
public class BasketController {
    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAMESPACE);

    private static final String REQUEST_ID_HEADER_NAME = "X-Request-ID";
    private static final String LOG_MESSAGE_DATA_KEY = "message";

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

    @GetMapping("${uk.gov.companieshouse.orders.api.basket.checkouts}/{checkoutId}/payment")
    public ResponseEntity<Object> getPaymentDetails(final @PathVariable String checkoutId,
                                      final @RequestHeader(REQUEST_ID_HEADER_NAME) String requestId){
        trace("Getting checkout item details for id: " + checkoutId, requestId);

        final Checkout checkout = checkoutService.getCheckoutById(checkoutId)
                .orElseThrow(ResourceNotFoundException::new);
        CheckoutData checkoutData = checkout.getData();

        PaymentDetailsDTO paymentDetailsDTO = new PaymentDetailsDTO();
        //checkoutToPaymentDetailsMapper.updateDTOWithPaymentDetails(checkoutData, paymentDetailsDTO);
        trace("Successfully returned payment details for checkoutId "+checkoutId, requestId);

        return ResponseEntity.status(OK).body(paymentDetailsDTO);
    }

    @PostMapping("${uk.gov.companieshouse.orders.api.basket.items}")
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

    @PatchMapping("${uk.gov.companieshouse.orders.api.basket}")
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
            retrievedBasket.get().getData().setDeliveryDetails(mappedDeliveryDetails);
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

    @PostMapping("${uk.gov.companieshouse.orders.api.basket.checkouts}")
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
        if (!errors.isEmpty()) {
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
                EricHeaderHelper.getAuthorisedUser(request));
        trace("Successfully created checkout with id "+checkout.getId(), requestId);

        return ResponseEntity.status(HttpStatus.OK).body(checkout.getData());
    }

    @PatchMapping("${uk.gov.companieshouse.orders.api.basket.checkouts}/{id}/payment")
    public ResponseEntity<String> patchBasketPaymentDetails(final @RequestBody BasketPaymentRequestDTO basketPaymentRequestDTO,
                                                            final @PathVariable String id,
                                                            HttpServletRequest request,
                                                            final @RequestHeader(REQUEST_ID_HEADER_NAME) String requestId) {
        trace("ENTERING patchBasketPaymentDetails(" + basketPaymentRequestDTO + ", " + id + ", " + requestId + ")", requestId);
        if(basketPaymentRequestDTO.getStatus().equals(PaymentStatus.PAID)) {
            processSuccessfulPayment(request, requestId, id);
        }
        return ResponseEntity.ok("");
    }

    /**
     * Performs the actions required to process a successful payment.
     * @param request the request
     * @param requestId the request ID
     * @param checkoutId the checkout ID
     */
    private void processSuccessfulPayment(final HttpServletRequest request,
                                          final String requestId,
                                          final String checkoutId) {
        final Checkout checkout = checkoutService.getCheckoutById(checkoutId)
                .orElseThrow(ResourceNotFoundException::new);
        final Order order = orderService.createOrder(checkout);
        trace("Created order: " + order, requestId);
        final Basket basket = basketService.clearBasket(EricHeaderHelper.getIdentity(request));
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

package uk.gov.companieshouse.orders.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.companieshouse.orders.api.dto.AddToBasketItemRequestDTO;
import uk.gov.companieshouse.orders.api.dto.AddToBasketItemResponseDTO;
import uk.gov.companieshouse.orders.api.mapper.BasketItemMapper;
import uk.gov.companieshouse.orders.api.model.BasketItem;

import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.orders.api.service.BasketItemService;
import uk.gov.companieshouse.orders.api.util.EricHeaderHelper;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static uk.gov.companieshouse.orders.api.OrdersApiApplication.APPLICATION_NAMESPACE;

@RestController
public class BasketController {
    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAMESPACE);

    private static final String REQUEST_ID_HEADER_NAME = "X-Request-ID";
    private static final String LOG_MESSAGE_DATA_KEY = "message";

    private BasketItemMapper mapper;
    private final BasketItemService basketItemservice;

    public BasketController(final BasketItemMapper mapper, final BasketItemService basketItemservice){
        this.mapper = mapper;
        this.basketItemservice = basketItemservice;
    }

    @PostMapping("${uk.gov.companieshouse.orders.api.basket.items}")
    public ResponseEntity<AddToBasketItemResponseDTO> addItemToBasket(final @Valid @RequestBody AddToBasketItemRequestDTO addToBasketItemRequestDTO,
                                                                      HttpServletRequest request,
                                                                      final @RequestHeader(REQUEST_ID_HEADER_NAME) String requestId){
        trace("ENTERING addItemToBasket(" + addToBasketItemRequestDTO + ")", requestId);

        final Optional<BasketItem> retrievedBasketItem = basketItemservice.getBasketById(EricHeaderHelper.getIdentity(request));

        BasketItem item = mapper.addBasketItemDTOToBasketItem(addToBasketItemRequestDTO);

        BasketItem returnedBasketItem;
        if(retrievedBasketItem.isPresent()) {
            retrievedBasketItem.get().getData().setItems(item.getData().getItems());
            returnedBasketItem = basketItemservice.saveBasketItem(retrievedBasketItem.get());
        } else {
            item.setId(EricHeaderHelper.getIdentity(request));
            returnedBasketItem = basketItemservice.saveBasketItem(item);
        }

        final AddToBasketItemResponseDTO basketItemDTO = mapper.basketItemToBasketItemDTO(returnedBasketItem);
        trace("EXITING addItemToBasket() with " + addToBasketItemRequestDTO, requestId);
        return ResponseEntity.status(HttpStatus.OK).body(basketItemDTO);
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

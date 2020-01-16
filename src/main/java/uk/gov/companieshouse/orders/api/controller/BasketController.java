package uk.gov.companieshouse.orders.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.companieshouse.orders.api.dto.AddToBasketItemDTO;
import uk.gov.companieshouse.orders.api.mapper.BasketItemMapper;
import uk.gov.companieshouse.orders.api.model.BasketData;
import uk.gov.companieshouse.orders.api.model.BasketItem;

import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.orders.api.model.Item;
import uk.gov.companieshouse.orders.api.service.BasketItemService;
import uk.gov.companieshouse.orders.api.util.EricHeaderHelper;

import javax.servlet.http.HttpServletRequest;
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
    public ResponseEntity<BasketItem> addItemToBasket(final @RequestBody AddToBasketItemDTO addToBasketItemDTO,
                                                      HttpServletRequest request,
                                                      final @RequestHeader(REQUEST_ID_HEADER_NAME) String requestId){
        trace("Post received", requestId);

        final Optional<BasketItem> basketItem = basketItemservice.getBasketById(EricHeaderHelper.getIdentity(request));
        System.out.println("INCONTROLER");
        System.out.println(addToBasketItemDTO.getItemUri());

        Item item = new Item();
        item.setItemUri(addToBasketItemDTO.getItemUri());
        if(basketItem.isPresent()) {
            if(basketItem.get().getData() != null) {
                basketItem.get().getData().setItems(new Item[]{item});
            } else {
                BasketData basketData = new BasketData();
                basketData.setItems(new Item[]{item});
                basketItem.get().setData(basketData);
            }
            basketItemservice.saveBasketItem(basketItem.get());
        } else {
            BasketItem newBasketItem = new BasketItem();
            newBasketItem.setId(EricHeaderHelper.getIdentity(request));
            BasketData basketData = new BasketData();
            basketData.setItems(new Item[]{item});
            newBasketItem.setData(basketData);
            basketItemservice.createBasketItem(newBasketItem);
        }

        BasketItem res = null;
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
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

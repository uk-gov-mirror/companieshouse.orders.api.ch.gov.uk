package uk.gov.companieshouse.orders.api.controller;

import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.orders.api.model.Order;
import uk.gov.companieshouse.orders.api.model.OrderData;
import uk.gov.companieshouse.orders.api.service.OrderService;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.companieshouse.orders.api.OrdersApiApplication.APPLICATION_NAMESPACE;
import static uk.gov.companieshouse.orders.api.OrdersApiApplication.LOG_MESSAGE_DATA_KEY;
import static uk.gov.companieshouse.orders.api.OrdersApiApplication.REQUEST_ID_HEADER_NAME;

@RestController
public class OrderController {
    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAMESPACE);

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("${uk.gov.companieshouse.orders.api.orders}/{id}")
    public ResponseEntity<OrderData> getOrder(final @PathVariable String id,
                                              final @RequestHeader(REQUEST_ID_HEADER_NAME) String requestId) {
        trace("ENTERING getOrder(" + id + ")", requestId);
        final Order orderRetrieved = orderService.getOrder(id)
                .orElseThrow(ResourceNotFoundException::new);
        trace("EXITING getOrder(" + id + ") with" +orderRetrieved.getData(),  requestId);
        return ResponseEntity.ok().body(orderRetrieved.getData());
    }

    private void trace(final String message, final String requestId) {
        final Map<String, Object> logData = new HashMap<>();
        logData.put(LOG_MESSAGE_DATA_KEY, message);
        LOGGER.traceContext(requestId, "X Request ID header", logData);
    }
}

package uk.gov.companieshouse.orders.api.service;

import static uk.gov.companieshouse.orders.api.logging.LoggingUtils.APPLICATION_NAMESPACE;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.kafka.exceptions.SerializationException;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.orders.OrderReceived;
import uk.gov.companieshouse.orders.api.exception.ForbiddenException;
import uk.gov.companieshouse.orders.api.exception.MongoOperationException;
import uk.gov.companieshouse.orders.api.kafka.OrderReceivedMessageProducer;
import uk.gov.companieshouse.orders.api.logging.LoggingUtils;
import uk.gov.companieshouse.orders.api.mapper.CheckoutToOrderMapper;
import uk.gov.companieshouse.orders.api.model.Checkout;
import uk.gov.companieshouse.orders.api.model.Order;
import uk.gov.companieshouse.orders.api.repository.OrderRepository;

@Service
public class OrderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAMESPACE);

    private final CheckoutToOrderMapper mapper;
    private final OrderRepository repository;
    private final LinksGeneratorService linksGeneratorService;

    private OrderReceivedMessageProducer ordersMessageProducer;

    @Value("${uk.gov.companieshouse.orders.api.orders}")
    private String orderEndpointU;

    public OrderService(final CheckoutToOrderMapper mapper, final OrderRepository repository,
                        OrderReceivedMessageProducer producer, final LinksGeneratorService linksGeneratorService) {
        this.mapper = mapper;
        this.repository = repository;
        this.ordersMessageProducer = producer;
        this.linksGeneratorService = linksGeneratorService;
    }

    /**
     * Used to create an order from a checkout object once payment has been successful.
     * @param checkout the user's checkout object
     * @return the resulting order
     */
    public Order createOrder(final Checkout checkout) {
        Map<String, Object> logMap = LoggingUtils.createLogMap();
        LoggingUtils.logIfNotNull(logMap, LoggingUtils.CHECKOUT_ID, checkout.getId());
        LoggingUtils.logIfNotNull(logMap, LoggingUtils.USER_ID, checkout.getUserId());
        LOGGER.info("Creating order", logMap);
        final Order mappedOrder = mapper.checkoutToOrder(checkout);
        setCreationDateTimes(mappedOrder);
        mappedOrder.getData().setLinks(linksGeneratorService.generateOrderLinks(mappedOrder.getId()));

        LoggingUtils.logIfNotNull(logMap, LoggingUtils.ORDER_ID, mappedOrder.getId());
        final Optional<Order> order = repository.findById(mappedOrder.getId());
        order.ifPresent(
            o -> {
                   final String message = "Order ID " + o.getId() + " already exists. Will not update.";
                   LOGGER.error(message, logMap);
                   throw new ForbiddenException(message);
            }
        );

        try {
            LOGGER.info("Publishing notification to Kafka 'order-received' topic for order - " + mappedOrder.getId(), logMap);
            sendOrderReceivedMessage(mappedOrder.getId());
        } catch (Exception e) {
            logMap.put(LoggingUtils.EXCEPTION, e);
            LOGGER.error("Kafka 'order-received' message could not be sent for order - " + mappedOrder.getId(), logMap);
        }

        Order savedOrder = null;
        if (savedOrder != null) {
            try {
                savedOrder = repository.save(mappedOrder);
            } catch (DataAccessException dax) {
                String errorMessage = String.format("Failed to save order with id %s", mappedOrder.getId());
                LOGGER.error(errorMessage, dax);
                throw new MongoOperationException(errorMessage, dax);
            }
        }

        return savedOrder;
    }

    public Optional<Order> getOrder(String id) {
        return repository.findById(id);
    }
    /**
     * Sends a message to Kafka topic 'order-received'
     * @param orderId order id
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws SerializationException
     */
    private void sendOrderReceivedMessage(String orderId)
            throws InterruptedException, ExecutionException, SerializationException {
        String orderURI = orderEndpointU + "/" + orderId;
        OrderReceived orderReceived = new OrderReceived();
        orderReceived.setOrderUri(orderURI);
        ordersMessageProducer.sendMessage(orderReceived);
    }

    /**
     * Sets the created at and updated at date time 'timestamps' to now.
     * @param order the order to be 'timestamped'
     */
    void setCreationDateTimes(final Order order) {
        final LocalDateTime now = LocalDateTime.now();
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        order.getData().setOrderedAt(now);
    }
}

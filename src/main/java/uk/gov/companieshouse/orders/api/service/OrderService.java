package uk.gov.companieshouse.orders.api.service;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.orders.api.exception.ForbiddenException;
import uk.gov.companieshouse.orders.api.kafka.OrdersAvroSerializer;
import uk.gov.companieshouse.orders.api.kafka.OrdersMessageProducer;
import uk.gov.companieshouse.orders.api.mapper.CheckoutToOrderMapper;
import uk.gov.companieshouse.orders.api.model.Checkout;
import uk.gov.companieshouse.orders.api.model.Order;
import uk.gov.companieshouse.orders.api.repository.OrderRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static uk.gov.companieshouse.orders.api.OrdersApiApplication.APPLICATION_NAMESPACE;

@Service
public class OrderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAMESPACE);

    private final CheckoutToOrderMapper mapper;
    private final OrderRepository repository;

    private OrdersAvroSerializer ordersAvroSerializer;
    private OrdersMessageProducer ordersMessageProducer;
    private static final String ORDER_RECEIVED_TOPIC = "order-received";

    public OrderService(final CheckoutToOrderMapper mapper, final OrderRepository repository,
                        OrdersAvroSerializer serializer, OrdersMessageProducer producer) {
        this.mapper = mapper;
        this.repository = repository;
        this.ordersAvroSerializer = serializer;
        this.ordersMessageProducer = producer;
    }

    public void sendOrderReceivedMessage(String orderId) throws Exception {
        byte[] message = ordersAvroSerializer.serialize(orderId);
        ordersMessageProducer.sendMessage(message, ORDER_RECEIVED_TOPIC);
    }

    /**
     * Used to create an order from a checkout object once payment has been successful.
     * @param checkout the user's checkout object
     * @return the resulting order
     */
    public Order createOrder(final Checkout checkout) {
        final Order mappedOrder = mapper.checkoutToOrder(checkout);
        setCreationDateTimes(mappedOrder);

        final Optional<Order> order = repository.findById(mappedOrder.getId());
        order.ifPresent(
            o -> {
                   final String message = "Order ID " + o.getId() + " already exists. Will not update.";
                   LOGGER.error(message);
                   throw new ForbiddenException(message);
            }
        );

        try {
            LOGGER.info("Publishing notification to Kafka 'order-received' topic for order - " + mappedOrder.getId());
            sendOrderReceivedMessage(mappedOrder.getId());
        } catch (Exception e) {
            LOGGER.error("Kafka 'order-received' message could not be sent for order - " + mappedOrder.getId());
        }

        return repository.save(mappedOrder);
    }

    /**
     * Sets the created at and updated at date time 'timestamps' to now.
     * @param order the order to be 'timestamped'
     */
    void setCreationDateTimes(final Order order) {
        final LocalDateTime now = LocalDateTime.now();
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
    }
}

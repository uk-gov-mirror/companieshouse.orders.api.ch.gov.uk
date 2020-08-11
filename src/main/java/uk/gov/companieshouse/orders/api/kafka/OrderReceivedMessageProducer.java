package uk.gov.companieshouse.orders.api.kafka;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.orders.OrderReceived;
import uk.gov.companieshouse.orders.api.exception.KafkaMessagingException;
import uk.gov.companieshouse.orders.api.logging.LoggingUtils;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.companieshouse.orders.api.logging.LoggingUtils.APPLICATION_NAMESPACE;

@Service
public class OrderReceivedMessageProducer {
    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAMESPACE);
    private final OrdersMessageFactory ordersAvroSerializer;
    private final OrdersKafkaProducer ordersKafkaProducer;

    public OrderReceivedMessageProducer(final OrdersMessageFactory avroSerializer, final OrdersKafkaProducer kafkaMessageProducer) {
        this.ordersAvroSerializer = avroSerializer;
        this.ordersKafkaProducer = kafkaMessageProducer;
    }

    /**
     * Sends order-received message to CHKafkaProducer
     * @param orderId order id
     * @param orderReceived order-received object
     */
    public void sendMessage(final String orderId, final OrderReceived orderReceived) {
        Map<String, Object> logMap = LoggingUtils.createLogMap();
        LoggingUtils.logIfNotNull(logMap, LoggingUtils.ORDER_ID, orderId);

        LOGGER.info("Sending message to kafka producer", logMap);
        try {
            Message message = ordersAvroSerializer.createMessage(orderReceived);
            ordersKafkaProducer.sendMessage(orderId, message, recordMetadata -> {
                long offset = recordMetadata.offset();
                String topic = message.getTopic();
                Map<String, Object> logMapCallback = new HashMap<>();
                logMapCallback.put(LoggingUtils.TOPIC, topic);
                logMapCallback.put(LoggingUtils.ORDER_ID, orderId);
                logMapCallback.put(LoggingUtils.OFFSET, offset);
                LoggerFactory.getLogger(APPLICATION_NAMESPACE).info("Message sent to Kafka topic", logMapCallback);
            });
        } catch (Exception e) {
            final String errorMessage
                    = String.format("Kafka 'order-received' message could not be sent for order - %s", orderId);
            logMap.put(LoggingUtils.EXCEPTION, e);
            LOGGER.error(errorMessage, logMap);
            throw new KafkaMessagingException(errorMessage, e);
        }
    }
}

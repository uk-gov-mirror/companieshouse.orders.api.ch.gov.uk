package uk.gov.companieshouse.orders.api.kafka;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.kafka.exceptions.SerializationException;
import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.orders.OrderReceived;
import uk.gov.companieshouse.orders.api.logging.LoggingUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

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
     * @param orderReceived order-received object
     * @throws SerializationException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public void sendMessage(final OrderReceived orderReceived) throws SerializationException {
        Message message = ordersAvroSerializer.createMessage(orderReceived);
        LOGGER.info("Sending message to kafka producer");
        ordersKafkaProducer.sendMessage(message, recordMetadata -> {
            long offset = recordMetadata.offset();
            String topic = message.getTopic();
            String orderUri = orderReceived.getOrderUri();
            Map<String, Object> logMap = new HashMap<>();
            logMap.put(LoggingUtils.TOPIC, topic);
            logMap.put(LoggingUtils.ORDER_ID, orderUri.substring(8));
            logMap.put(LoggingUtils.OFFSET, offset);
            LoggerFactory.getLogger(APPLICATION_NAMESPACE).info("Message sent to Kafka topic", logMap);
        });
    }
}

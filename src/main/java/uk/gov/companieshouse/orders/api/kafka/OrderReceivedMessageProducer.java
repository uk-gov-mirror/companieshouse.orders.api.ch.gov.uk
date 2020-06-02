package uk.gov.companieshouse.orders.api.kafka;

import static uk.gov.companieshouse.orders.api.logging.LoggingUtils.APPLICATION_NAMESPACE;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.kafka.exceptions.SerializationException;
import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.orders.OrderReceived;
import uk.gov.companieshouse.orders.api.logging.LoggingUtils;

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
    public void sendMessage(final OrderReceived orderReceived)
            throws SerializationException, ExecutionException, InterruptedException {
        Message message = ordersAvroSerializer.createMessage(orderReceived);
        Map<String, Object> logMap = LoggingUtils.createLogMap();
        LoggingUtils.logIfNotNull(logMap, LoggingUtils.TOPIC, message.getTopic());
        LoggingUtils.logIfNotNull(logMap, LoggingUtils.OFFSET, message.getOffset());
        LOGGER.info("Sending message to kafka producer", logMap);
        ordersKafkaProducer.sendMessage(message);
    }
}

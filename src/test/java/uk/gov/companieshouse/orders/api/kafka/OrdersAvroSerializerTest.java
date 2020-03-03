package uk.gov.companieshouse.orders.api.kafka;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.test.context.EmbeddedKafka;
import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.companieshouse.orders.api.OrdersApiApplication.APPLICATION_NAMESPACE;

@EmbeddedKafka
public class OrdersAvroSerializerTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAMESPACE);
    private static final String ORDER_RECEIVED_TOPIC = "order-received";
    private static final String ORDER_CONSUMER_GROUP = "orders-consumer";

    @Autowired
    private OrdersAvroSerializer ordersAvroSerializer;

    @Autowired
    private OrdersMessageProducer ordersMessageProducer;

    @Autowired
    private OrdersMessageConsumer ordersMessageConsumer;

    @Test
    void sendOrderReceivedMessageToKafkaTopic() throws Exception {
        // Given order is created
        String order_id = "ORDER-12345";

        // When order-received message is sent to kafka topic
        byte[] message = ordersAvroSerializer.serialize(order_id);
        ordersMessageProducer.sendMessage(message, ORDER_RECEIVED_TOPIC);

        // Then
        List<String> topics = new ArrayList<>();
        topics.add(ORDER_RECEIVED_TOPIC);
        ordersMessageConsumer.configureAndConnect(topics, ORDER_CONSUMER_GROUP);
        List<Message> messages = ordersMessageConsumer.pollConsumerGroup();
        assertThat(messages.isEmpty(), is(false));
    }
}

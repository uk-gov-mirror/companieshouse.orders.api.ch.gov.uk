package uk.gov.companieshouse.orders.api.kafka;

import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import uk.gov.companieshouse.kafka.message.Message;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@SpringBootTest
@EmbeddedKafka
@TestPropertySource(properties="uk.gov.companieshouse.orders.api.order=/order")
public class OrdersKafkaProducerConsumerIntegrationTest {
    private static final String ORDER_RECEIVED_TOPIC = "order-received";
    private static final String ORDER_CONSUMER_GROUP = "orders-consumer";

    @Autowired
    private OrdersAvroSerializer ordersAvroSerializer;

    @Autowired
    private OrdersMessageProducer ordersMessageProducer;

    @Autowired
    private OrdersMessageConsumer ordersMessageConsumer;

    @BeforeEach
    void setUp() throws Exception{
        List<String> topics = new ArrayList<>();
        topics.add(ORDER_RECEIVED_TOPIC);
        ordersMessageConsumer.configureAndConnect(topics, ORDER_CONSUMER_GROUP);
    }

    @Test
    void sendOrderReceivedMessageToKafkaTopic() throws Exception {
        // Given order is created
        String order_id = "ORDER-12345";

        // When order-received message is sent to kafka topic
        byte[] message = ordersAvroSerializer.serialize(order_id);
        ordersMessageProducer.sendMessage(message, ORDER_RECEIVED_TOPIC);

        List<Message> messages;
        int count = 0;
        // Then
        do {
            messages = ordersMessageConsumer.pollConsumerGroup();
            count++;
        } while(messages.isEmpty() && count < 10);
        assertThat(messages.isEmpty(), is(false));
    }
}

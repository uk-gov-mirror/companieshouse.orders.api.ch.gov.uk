package uk.gov.companieshouse.orders.api.kafka;

import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import uk.gov.companieshouse.kafka.message.Message;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@SpringBootTest
@DirtiesContext
@EmbeddedKafka
@TestPropertySource(properties="uk.gov.companieshouse.orders.api.order=/order")
public class OrdersMessageProducerTest {
    private static final String ORDER_RECEIVED_SCHEMA = "order-received";
    private static final String ORDER_RECEIVED_TOPIC = "order-received";
    private static final String MESSAGE_PROPERTY_ORDER_URI = "order_uri";
    private static final String ORDER_URI = "/order/ORDER-12345";
    private static final String SERIALIZED_ORDER_URI = "$/order/ORDER-12345";

    @Mock
    AvroSchemaHelper avroSchemaHelper;

    @Mock
    OrdersAvroSerializer ordersAvroSerializer;

    @Autowired
    OrdersMessageProducer ordersMessageProducerUnderTest;

    @Autowired
    private OrdersMessageConsumer testOrdersMessageConsumer;

    @Before
    public void init() throws IOException {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testSendOrderReceivedMessageToKafkaTopic() throws Exception {
        // Given an order is generated
        Mockito.when(ordersAvroSerializer.serialize(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(SERIALIZED_ORDER_URI.getBytes());

        // When order-received message is sent to kafka topic
        List<Message> messages = sendAndConsumeMessage();

        // Then it is available to be consumed by a kafka consumer
        assertThat(messages.isEmpty(), is(false));
    }

    private List<Message> sendAndConsumeMessage() throws Exception {
        List<Message> messages;
        testOrdersMessageConsumer.connect();
        int count = 0;
        do {
            messages = testOrdersMessageConsumer.pollConsumerGroup();
            byte[] message = ordersAvroSerializer.serialize(ORDER_RECEIVED_SCHEMA, MESSAGE_PROPERTY_ORDER_URI, ORDER_URI);
            ordersMessageProducerUnderTest.sendMessage(message, ORDER_RECEIVED_TOPIC);
            count++;
        } while(messages.isEmpty() && count < 15);

        return messages;
    }
}

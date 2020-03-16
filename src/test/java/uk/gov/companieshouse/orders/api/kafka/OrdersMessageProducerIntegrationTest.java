package uk.gov.companieshouse.orders.api.kafka;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.orders.OrderReceived;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@SpringBootTest
@DirtiesContext
@EmbeddedKafka
@TestPropertySource(properties="uk.gov.companieshouse.orders.api.order=/order")
public class OrdersMessageProducerIntegrationTest {
    private static final String ORDER_URI = "/order/ORDER-12345";

    @Autowired
    OrderReceivedMessageProducer ordersMessageProducerUnderTest;

    @Autowired
    OrdersMessageConsumer testOrdersMessageConsumer;

    @Test
    void testSendOrderReceivedMessageToKafkaTopic() throws Exception {
        // Given an order is generated
        OrderReceived orderReceived = new OrderReceived();
        orderReceived.setOrderUri(ORDER_URI);

        // When order-received message is sent to kafka topic
        List<Message> messages = sendAndConsumeMessage(orderReceived);

        // Then we have successfully consumed some messages.
        assertThat(messages.isEmpty(), is(false));
    }

    private List<Message> sendAndConsumeMessage(final OrderReceived orderReceived) throws Exception {
        List<Message> messages;
        testOrdersMessageConsumer.connect();
        int count = 0;
        do {
            messages = testOrdersMessageConsumer.pollConsumerGroup();
            ordersMessageProducerUnderTest.sendMessage(orderReceived);
            count++;
        } while(messages.isEmpty() && count < 15);

        return messages;
    }
}

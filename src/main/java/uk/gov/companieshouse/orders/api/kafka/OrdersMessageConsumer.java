package uk.gov.companieshouse.orders.api.kafka;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.kafka.consumer.CHKafkaConsumerGroup;
import uk.gov.companieshouse.kafka.consumer.ConsumerConfig;
import uk.gov.companieshouse.kafka.consumer.ConsumerConfigHelper;
import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.companieshouse.orders.api.OrdersApiApplication.APPLICATION_NAMESPACE;

@Service
public class OrdersMessageConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAMESPACE);
    private static CHKafkaConsumerGroup consumerGroup;

    public void configureAndConnect(List<String> consumerTopics, String groupName) {
        ConsumerConfig config = new ConsumerConfig();
        config.setTopics(consumerTopics);
        config.setGroupName(groupName);
        config.setResetOffset(false);
        ConsumerConfigHelper.assignBrokerAddresses(config);

        Map<String, Object> logData = new HashMap<>();
        logData.put("kafka_broker_addresses", Arrays.toString(config.getBrokerAddresses()));
        logData.put("topic", consumerTopics);
        logData.put("group name", groupName);
        LOGGER.trace("Connecting to Kafka consumer group", logData);

        consumerGroup = new CHKafkaConsumerGroup(config);
        consumerGroup.connect();
    }

    public List<Message> pollConsumerGroup() throws IOException, InterruptedException {
        return consumerGroup.consume();
    }

    public void closeConsumerGroup() {
        consumerGroup.close();
    }

    public void commit() {
        consumerGroup.commit();
    }

    public void reprocess(Message message) {
        consumerGroup.reprocess(message);
    }
}

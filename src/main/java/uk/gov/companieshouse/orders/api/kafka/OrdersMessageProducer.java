package uk.gov.companieshouse.orders.api.kafka;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.kafka.exceptions.ProducerConfigException;
import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.kafka.producer.Acks;
import uk.gov.companieshouse.kafka.producer.CHKafkaProducer;
import uk.gov.companieshouse.kafka.producer.ProducerConfig;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import java.util.Date;
import java.util.concurrent.ExecutionException;

import static uk.gov.companieshouse.orders.api.OrdersApiApplication.APPLICATION_NAMESPACE;

@Service
public class OrdersMessageProducer implements InitializingBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAMESPACE);
    private static String KAFKA_BROKER_ADDR = "KAFKA_BROKER_ADDR";
    private static CHKafkaProducer chKafkaProducer;
    @Value("${kafka.broker.addresses}")
    private String brokerAddresses;

    /**
     * Sends serialized message to kafka topic
     * @param data serialized message
     * @param topic kafka topic
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public void sendMessage(byte[] data, String topic) throws ExecutionException, InterruptedException {
        LOGGER.trace("Sending message to CH Kafka topic " + topic);
        Message message = new Message();
        message.setValue(data);
        message.setTopic(topic);
        message.setTimestamp((new Date()).getTime());
        chKafkaProducer.send(message);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        LOGGER.trace("Configuring CH Kafka producer");
        ProducerConfig config = new ProducerConfig();
        if (brokerAddresses != null && !brokerAddresses.isEmpty()) {
            config.setBrokerAddresses(brokerAddresses.split(","));
        } else {
            throw new ProducerConfigException("Broker addresses for kafka broker not supplied, use the environment variable KAFKA_BROKER_ADDR");
        }

        config.setRoundRobinPartitioner(true);
        config.setAcks(Acks.WAIT_FOR_ALL);
        config.setRetries(10);
        chKafkaProducer = new CHKafkaProducer(config);
    }
}

package uk.gov.companieshouse.orders.api.kafka;

import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.kafka.exceptions.ProducerConfigException;
import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.kafka.producer.Acks;
import uk.gov.companieshouse.kafka.producer.CHKafkaProducer;
import uk.gov.companieshouse.kafka.producer.ProducerConfig;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.orders.api.logging.LoggingUtils;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import static uk.gov.companieshouse.orders.api.logging.LoggingUtils.APPLICATION_NAMESPACE;

@Service
public class OrdersKafkaProducer implements InitializingBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAMESPACE);
    private CHKafkaProducer chKafkaProducer;
    @Value("${spring.kafka.producer.bootstrap-servers}")
    private String brokerAddresses;

    /**
     * Sends message to Kafka topic
     * @param message message
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Async
    public void sendMessage(final Message message, Consumer<RecordMetadata> asyncResponseLogger)
            throws ExecutionException, InterruptedException {
        LOGGER.info("Sending message to kafka topic");
        Future<RecordMetadata> recordMetadataFuture = chKafkaProducer.sendAndReturnFuture(message);
        asyncResponseLogger.accept(recordMetadataFuture.get());
    }

    @Override
    public void afterPropertiesSet() {
        LOGGER.trace("Configuring CH Kafka producer");
        ProducerConfig config = new ProducerConfig();
        if (brokerAddresses != null && !brokerAddresses.isEmpty()) {
            config.setBrokerAddresses(brokerAddresses.split(","));
        } else {
            throw new ProducerConfigException("Broker addresses for kafka broker missing, check if environment variable KAFKA_BROKER_ADDR is configured. " +
                    "[Hint: The property 'kafka.broker.addresses' uses the value of this environment variable in live environments " +
                    "and that of 'spring.embedded.kafka.brokers' property in test.]");
        }

        config.setRoundRobinPartitioner(true);
        config.setAcks(Acks.WAIT_FOR_ALL);
        config.setRetries(10);
        config.setMaxBlockMilliseconds(10000);
        chKafkaProducer = new CHKafkaProducer(config);
    }
}

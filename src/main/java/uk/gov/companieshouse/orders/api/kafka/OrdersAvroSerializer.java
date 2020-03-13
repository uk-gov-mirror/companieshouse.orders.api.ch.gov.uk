package uk.gov.companieshouse.orders.api.kafka;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import java.io.IOException;

import static uk.gov.companieshouse.orders.api.OrdersApiApplication.APPLICATION_NAMESPACE;

@Service
public class OrdersAvroSerializer extends AvroSerializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAMESPACE);
    private static final String CONTEXT_PATH = "/subjects/%s/versions/latest";
    private static final String REGISTRY_URL_ENV_VARIABLE = "SCHEMA_REGISTRY_URL";

    private AvroSchemaHelper avroSchemaHelper;

    public OrdersAvroSerializer(AvroSchemaHelper schemaHelper) {
        avroSchemaHelper = schemaHelper;
    }

    /**
     * Orders avro serializer
     * @param schemaName schema to use for serializing
     * @param key message property name
     * @param message message property value
     * @return serialized message
     * @throws IOException
     */
    public byte[] serialize(String schemaName, String key, String message) throws IOException {
        LOGGER.trace("Serializng message of type " + schemaName);
        Schema schema = getSchema(schemaName);
        GenericRecord orderReceivedData = new GenericData.Record(schema);
        orderReceivedData.put(key, message);

        return super.serialize(schema, orderReceivedData);
    }

    /**
     * Fetches schema by name from registry
     * @param schemaName schema name
     * @return schema
     * @throws IOException
     */
    public Schema getSchema(String schemaName) throws IOException {
        String host = System.getenv(REGISTRY_URL_ENV_VARIABLE);
        String url = host + String.format(CONTEXT_PATH, schemaName);
        Schema schema = avroSchemaHelper.getSchema(url);

        return schema;
    }
}

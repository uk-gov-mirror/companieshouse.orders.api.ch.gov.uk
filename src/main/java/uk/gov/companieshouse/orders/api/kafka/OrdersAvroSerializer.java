package uk.gov.companieshouse.orders.api.kafka;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.MalformedURLException;

@Service
public class OrdersAvroSerializer extends AvroSerializer {
    private static final String CONTEXT_PATH = "/subjects/%s/versions/latest";
    private static final String REGISTRY_URL_ENV_VARIABLE = "SCHEMA_REGISTRY_URL";
    private static final String ORDER_RECEIVED_SCHEMA = "order-received";
    @Value("${uk.gov.companieshouse.orders.api.order}")
    private static String ORDER_ENDPOINT_URL;

    public byte[] serialize(String orderId) throws IOException {
        Schema schema = getSchema(ORDER_RECEIVED_SCHEMA);
        GenericRecord orderReceivedData = new GenericData.Record(schema);
        orderReceivedData.put("order_uri", ORDER_ENDPOINT_URL + "/" + orderId);

        return super.serialize(schema, orderReceivedData);
    }

    private Schema getSchema(String schemaName) throws MalformedURLException, IOException {
        String host = System.getenv(REGISTRY_URL_ENV_VARIABLE);
        String url = host + String.format(CONTEXT_PATH, schemaName);
        Schema schema = AvroSchemaHelper.getSchema(url);

        return schema;
    }
}

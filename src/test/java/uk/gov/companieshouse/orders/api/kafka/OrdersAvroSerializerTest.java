package uk.gov.companieshouse.orders.api.kafka;

import org.apache.avro.Schema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class OrdersAvroSerializerTest {
    private static final String MESSAGE_PROPERTY_ORDER_URI = "order_uri";
    private static final String TEST_SCHEMA_FILE = "order-received.avsc";
    private static final String ORDERS_RECEIVED_SCHEMA_NAME = "order-received";
    private static final String ORDER_URI = "/order/ORDER-54321";
    private static final String EXPECTED_SERIALIZED_MESSAGE = "$/order/ORDER-54321";

    @Mock
    AvroSchemaHelper avroSchemaHelperMock;

    @Spy
    @InjectMocks
    OrdersAvroSerializer serializerUnderTest;

    @BeforeEach
    public void init() throws IOException {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testSerializeOrderReceived() throws IOException {
        when(serializerUnderTest.getSchema(ORDERS_RECEIVED_SCHEMA_NAME)).thenReturn(getTestSchema(TEST_SCHEMA_FILE));
        byte[] result = serializerUnderTest.serialize(ORDERS_RECEIVED_SCHEMA_NAME, MESSAGE_PROPERTY_ORDER_URI, ORDER_URI);

        assertEquals(EXPECTED_SERIALIZED_MESSAGE, new String(result));
    }

    private Schema getTestSchema(String schemaName) throws IOException {
        String avroSchemaPath = this.getClass().getClassLoader().getResource(schemaName).getFile();
        Schema.Parser parser = new Schema.Parser();
        return parser.parse(new File(avroSchemaPath));
    }
}

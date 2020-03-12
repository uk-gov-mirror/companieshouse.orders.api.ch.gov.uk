package uk.gov.companieshouse.orders.api.kafka;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.kafka.message.Message;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@Service
public class AvroDeserializer {
	@Autowired
	AvroSchemaHelper avroSchemaHelper;

	public GenericRecord deserialize(String schemaName, Message message) throws IOException {
		Schema schema = avroSchemaHelper.getSchema(schemaName);
		return deserialize(schema, message);
	}
	
	public GenericRecord deserialize(Schema schema, Message message) throws IOException {
		DatumReader<GenericRecord> reader = new GenericDatumReader<GenericRecord>(schema);
		ByteArrayInputStream in = new ByteArrayInputStream(message.getValue());
		Decoder decoder = DecoderFactory.get().binaryDecoder(in, null);
		
		try {
			GenericRecord datum = reader.read(null, decoder);
			return datum;
		}  finally {
			in.close();
		}
	}
}

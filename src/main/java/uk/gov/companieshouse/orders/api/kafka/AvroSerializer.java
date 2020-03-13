package uk.gov.companieshouse.orders.api.kafka;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;

@Service
public class AvroSerializer {
	/**
	 * Avro serializer to serialize kafka message
	 * @param schema avro schema
	 * @param documentData data to serialize
	 * @return serilaized object
	 * @throws IOException
	 */
	public byte[] serialize(Schema schema, GenericRecord documentData) throws IOException {
		DatumWriter<GenericRecord> datumWriter = new GenericDatumWriter<GenericRecord>(schema);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Encoder encoder = EncoderFactory.get().binaryEncoder(out, null);

		datumWriter.write(documentData, encoder);
		encoder.flush();
		byte[] serializedAvro = out.toByteArray();
		
		encoder.flush();
		out.close();
		
		return serializedAvro;
	}
}

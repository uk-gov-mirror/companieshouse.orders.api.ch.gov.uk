package uk.gov.companieshouse.orders.api.kafka;

import org.apache.avro.Schema;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

@Service
public class AvroSchemaHelper {
	
	/**
	 * Retrieves Avro schema from schema registry
	 * 
	 * @param url registry url
	 * @return schema requested avro schema
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public Schema getSchema(String url) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
		
		if(connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
			try(InputStream is = connection.getInputStream()) {
				try(BufferedReader in = new BufferedReader(new InputStreamReader(is))) {
					StringBuilder response = new StringBuilder();
					String line;
					while ((line = in.readLine()) != null) {
						response.append(line);
					}
					
					return convertResponseToSchema(response);
				}
			}
		}
		
		return null;
	}

	private Schema convertResponseToSchema(StringBuilder response) {
		if(response != null && response.length() > 0) {
			String schemaString = response.toString();
			JSONObject schemaJson = new JSONObject(schemaString);
			String schema = schemaJson.getString("schema");			
			
			Schema.Parser parser = new Schema.Parser();
			return parser.parse(schema);
		}
		
		return null;
	}
}

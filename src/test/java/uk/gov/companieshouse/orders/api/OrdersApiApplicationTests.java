package uk.gov.companieshouse.orders.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.web.reactive.server.WebTestClient;
import uk.gov.companieshouse.orders.api.dto.AddToBasketRequestDTO;

import static org.springframework.web.reactive.function.BodyInserters.fromObject;
import static uk.gov.companieshouse.orders.api.util.TestConstants.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka
class OrdersApiApplicationTests {

	private static final String ITEM_URI = "/orderable/certificates/12345678";

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void contextLoads() {
	}

	@Test
	@DisplayName("Add item accepts request with signed in user")
	void addItemAcceptsRequestWithSignedInUser() {

		// Given
		final AddToBasketRequestDTO addToBasketRequestDTO = new AddToBasketRequestDTO();
		addToBasketRequestDTO.setItemUri(ITEM_URI);

		// When and then
		webTestClient.post().uri("/basket/items")
				.contentType(MediaType.APPLICATION_JSON)
				.header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
				.header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_OAUTH2_TYPE_VALUE)
				.header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
				.header(ERIC_AUTHORISED_USER_HEADER_NAME, ERIC_AUTHORISED_USER_VALUE)
				.body(fromObject(addToBasketRequestDTO))
				.exchange()
				.expectStatus().isOk();
	}

	@Test
	@DisplayName("Add item rejects request without any authenticated client")
	void addItemRejectsRequestWithoutAnyAuthenticatedClient() {

		// Given
		final AddToBasketRequestDTO addToBasketRequestDTO = new AddToBasketRequestDTO();
		addToBasketRequestDTO.setItemUri(ITEM_URI);

		// When and then
		webTestClient.post().uri("/basket/items")
				.contentType(MediaType.APPLICATION_JSON)
				.header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
				.body(fromObject(addToBasketRequestDTO))
				.exchange()
				.expectStatus().isUnauthorized();
	}

	@Test
	@DisplayName("Add item rejects request with authenticated API client only")
	void addItemRejectsRequestWithoutWithAuthenticatedApiOnly() {

		// Given
		final AddToBasketRequestDTO addToBasketRequestDTO = new AddToBasketRequestDTO();
		addToBasketRequestDTO.setItemUri(ITEM_URI);

		// TODO GCI-332 Check understanding of what ends up in ERIC headers for 2 authenticated client types
		// When and then
		webTestClient.post().uri("/basket/items")
				.contentType(MediaType.APPLICATION_JSON)
				.header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
				.header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_API_KEY_TYPE_VALUE)
				.header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
				.body(fromObject(addToBasketRequestDTO))
				.exchange()
				.expectStatus().isUnauthorized();
	}

}

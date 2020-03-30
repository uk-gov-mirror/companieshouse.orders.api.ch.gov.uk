package uk.gov.companieshouse.orders.api;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.web.reactive.server.WebTestClient;
import uk.gov.companieshouse.orders.api.dto.AddDeliveryDetailsRequestDTO;
import uk.gov.companieshouse.orders.api.dto.AddToBasketRequestDTO;
import uk.gov.companieshouse.orders.api.dto.DeliveryDetailsDTO;
import uk.gov.companieshouse.orders.api.model.*;
import uk.gov.companieshouse.orders.api.repository.BasketRepository;
import uk.gov.companieshouse.orders.api.service.ApiClientService;
import uk.gov.companieshouse.orders.api.service.CheckoutService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.web.reactive.function.BodyInserters.fromObject;
import static uk.gov.companieshouse.orders.api.util.TestConstants.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka
class OrdersApiApplicationTests {

	private static final String ITEM_URI = "/orderable/certificates/12345678";
	private static final String COMPANY_NUMBER = "00006400";
	private static final String FORENAME = "forename";
	private static final String SURNAME = "surname";
	private static final String ADDRESS_LINE_1 = "address line 1";
	private static final String ADDRESS_LINE_2 = "address line 2";
	private static final String COUNTRY = "country";
	private static final String LOCALITY = "locality";
	private static final String PO_BOX = "po box";
	private static final String POSTAL_CODE = "postal code";
	private static final String PREMISES = "premises";
	private static final String REGION = "region";

	@Autowired
	private WebTestClient webTestClient;

	@Autowired
	private BasketRepository basketRepository;

	@MockBean
	private ApiClientService apiClientService;

	@MockBean
	private CheckoutService checkoutService;

	@MockBean
	private Checkout checkout;

	@AfterEach
	void tearDown() {
		basketRepository.findById(ERIC_IDENTITY_VALUE).ifPresent(basketRepository::delete);
		// TODO GCI-332 Are we avoiding using these repos? Could we be mocking out more so we are retesting less?
		// checkoutRepository.deleteAll();
		// orderRepository.deleteAll();
	}

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
	void addItemRejectsRequestWithAuthenticatedApiOnly() {

		// Given
		final AddToBasketRequestDTO addToBasketRequestDTO = new AddToBasketRequestDTO();
		addToBasketRequestDTO.setItemUri(ITEM_URI);

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

	@Test
	@DisplayName("Checkout basket accepts request with signed in user")
	void checkoutBasketAcceptsRequestWithSignedInUser() throws Exception {

		// Given
		final Basket basket = new Basket();
		basket.setId(ERIC_IDENTITY_VALUE);
		final BasketItem basketItem = new BasketItem();
		basketItem.setItemUri(ITEM_URI);
		basket.getData().getItems().add(basketItem);
		basketRepository.save(basket);

		final Certificate certificate = new Certificate();
		certificate.setCompanyNumber(COMPANY_NUMBER);
		final CertificateItemOptions options = new CertificateItemOptions();
		options.setForename(FORENAME);
		options.setSurname(SURNAME);
		certificate.setItemOptions(options);
		when(apiClientService.getItem(ITEM_URI)).thenReturn(certificate);

		when(checkoutService.createCheckout(any(Certificate.class), any(String.class), any(String.class)))
				.thenReturn(checkout);

		// When and then
		webTestClient.post().uri("/basket/checkouts")
				.contentType(MediaType.APPLICATION_JSON)
				.header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
				.header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_OAUTH2_TYPE_VALUE)
				.header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
				.header(ERIC_AUTHORISED_USER_HEADER_NAME, ERIC_AUTHORISED_USER_VALUE)
				.exchange()
				.expectStatus().isOk();
	}

	@Test
	@DisplayName("Checkout basket rejects request without any authenticated client")
	void checkoutBasketRejectsRequestWithoutAnyAuthenticatedClient() {

		// When and then
		webTestClient.post().uri("/basket/checkouts")
				.contentType(MediaType.APPLICATION_JSON)
				.header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
				.exchange()
				.expectStatus().isUnauthorized();
	}

	@Test
	@DisplayName("Checkout basket rejects request with authenticated API client only")
	void checkoutBasketRejectsRequestWithAuthenticatedApiOnly() {

		// When and then
		webTestClient.post().uri("/basket/checkouts")
				.contentType(MediaType.APPLICATION_JSON)
				.header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
				.header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_API_KEY_TYPE_VALUE)
				.header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
				.exchange()
				.expectStatus().isUnauthorized();
	}

	@Test
	@DisplayName("Patch basket accepts request with signed in user")
	void patchBasketAcceptsRequestWithSignedInUser() {

		// Given
		final Basket basket = new Basket();
		basketRepository.save(basket);

		final AddDeliveryDetailsRequestDTO addDeliveryDetailsRequestDTO = new AddDeliveryDetailsRequestDTO();
		DeliveryDetailsDTO deliveryDetailsDTO = new DeliveryDetailsDTO();
		deliveryDetailsDTO.setAddressLine1(ADDRESS_LINE_1);
		deliveryDetailsDTO.setAddressLine2(ADDRESS_LINE_2);
		deliveryDetailsDTO.setCountry(COUNTRY);
		deliveryDetailsDTO.setForename(FORENAME);
		deliveryDetailsDTO.setLocality(LOCALITY);
		deliveryDetailsDTO.setPoBox(PO_BOX);
		deliveryDetailsDTO.setPostalCode(POSTAL_CODE);
		deliveryDetailsDTO.setPremises(PREMISES);
		deliveryDetailsDTO.setRegion(REGION);
		deliveryDetailsDTO.setSurname(SURNAME);
		addDeliveryDetailsRequestDTO.setDeliveryDetails(deliveryDetailsDTO);

		// When and then
		webTestClient.patch().uri("/basket")
				.contentType(MediaType.APPLICATION_JSON)
				.header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
				.header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_OAUTH2_TYPE_VALUE)
				.header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
				.header(ERIC_AUTHORISED_USER_HEADER_NAME, ERIC_AUTHORISED_USER_VALUE)
				.body(fromObject(addDeliveryDetailsRequestDTO))
				.exchange()
				.expectStatus().isOk();
	}

	@Test
	@DisplayName("Patch basket rejects request without any authenticated client")
	void patchBasketRejectsRequestWithoutAnyAuthenticatedClient() {

		// Given
		final AddToBasketRequestDTO addToBasketRequestDTO = new AddToBasketRequestDTO();
		addToBasketRequestDTO.setItemUri(ITEM_URI);

		// When and then
		webTestClient.patch().uri("/basket")
				.contentType(MediaType.APPLICATION_JSON)
				.header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
				.body(fromObject(addToBasketRequestDTO))
				.exchange()
				.expectStatus().isUnauthorized();
	}

	@Test
	@DisplayName("Patch basket rejects request with authenticated API client only")
	void patchBasketRejectsRequestWithAuthenticatedApiOnly() {

		// Given
		final AddToBasketRequestDTO addToBasketRequestDTO = new AddToBasketRequestDTO();
		addToBasketRequestDTO.setItemUri(ITEM_URI);

		// When and then
		webTestClient.patch().uri("/basket")
				.contentType(MediaType.APPLICATION_JSON)
				.header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
				.header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_API_KEY_TYPE_VALUE)
				.header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
				.body(fromObject(addToBasketRequestDTO))
				.exchange()
				.expectStatus().isUnauthorized();
	}


}

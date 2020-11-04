package uk.gov.companieshouse.orders.api.interceptor;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;
import uk.gov.companieshouse.orders.api.dto.AddDeliveryDetailsRequestDTO;
import uk.gov.companieshouse.orders.api.dto.AddToBasketRequestDTO;
import uk.gov.companieshouse.orders.api.dto.DeliveryDetailsDTO;
import uk.gov.companieshouse.orders.api.model.Basket;
import uk.gov.companieshouse.orders.api.model.Certificate;
import uk.gov.companieshouse.orders.api.model.CertificateItemOptions;
import uk.gov.companieshouse.orders.api.model.Checkout;
import uk.gov.companieshouse.orders.api.model.CheckoutData;
import uk.gov.companieshouse.orders.api.model.DeliveryDetails;
import uk.gov.companieshouse.orders.api.model.Item;
import uk.gov.companieshouse.orders.api.model.ItemCosts;
import uk.gov.companieshouse.orders.api.service.ApiClientService;
import uk.gov.companieshouse.orders.api.service.BasketService;
import uk.gov.companieshouse.orders.api.service.CheckoutService;
import uk.gov.companieshouse.sdk.manager.ApiSdkManager;

import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.web.reactive.function.BodyInserters.fromObject;
import static uk.gov.companieshouse.orders.api.model.ProductType.CERTIFICATE_ADDITIONAL_COPY;
import static uk.gov.companieshouse.orders.api.model.ProductType.CERTIFICATE_SAME_DAY;
import static uk.gov.companieshouse.orders.api.util.TestConstants.ERIC_ACCESS_TOKEN;
import static uk.gov.companieshouse.orders.api.util.TestConstants.ERIC_AUTHORISED_USER_HEADER_NAME;
import static uk.gov.companieshouse.orders.api.util.TestConstants.ERIC_AUTHORISED_USER_VALUE;
import static uk.gov.companieshouse.orders.api.util.TestConstants.ERIC_IDENTITY_API_KEY_TYPE_VALUE;
import static uk.gov.companieshouse.orders.api.util.TestConstants.ERIC_IDENTITY_HEADER_NAME;
import static uk.gov.companieshouse.orders.api.util.TestConstants.ERIC_IDENTITY_OAUTH2_TYPE_VALUE;
import static uk.gov.companieshouse.orders.api.util.TestConstants.ERIC_IDENTITY_TYPE_HEADER_NAME;
import static uk.gov.companieshouse.orders.api.util.TestConstants.ERIC_IDENTITY_VALUE;
import static uk.gov.companieshouse.orders.api.util.TestConstants.REQUEST_ID_HEADER_NAME;
import static uk.gov.companieshouse.orders.api.util.TestConstants.TOKEN_REQUEST_ID_VALUE;

@DirtiesContext
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka
class OrdersApiAuthenticationTests {

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
	private static final String REGION = "region";
	private static final List<ItemCosts> ITEM_COSTS =
			asList(new ItemCosts( "0", "50", "50", CERTIFICATE_SAME_DAY),
					new ItemCosts("40", "50", "10", CERTIFICATE_ADDITIONAL_COPY),
					new ItemCosts("40", "50", "10", CERTIFICATE_ADDITIONAL_COPY));
	private static final String POSTAGE_COST = "0";
	private static final String TOTAL_ITEM_COST = "70";

	@Autowired
	private WebTestClient webTestClient;

	@MockBean
	private BasketService basketService;

	@MockBean
	private ApiClientService apiClientService;

	@MockBean
	private CheckoutService checkoutService;

	@MockBean
	private Checkout checkout;

	@MockBean
	private CheckoutData checkoutData;

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
		final Item basketItem = new Item();
		basketItem.setItemUri(ITEM_URI);
		basketItem.setPostalDelivery(false);
		basket.getData().getItems().add(basketItem);
		basket.getData().setDeliveryDetails(new DeliveryDetails());

		final Certificate certificate = new Certificate();
		certificate.setCompanyNumber(COMPANY_NUMBER);
		certificate.setItemCosts(ITEM_COSTS);
		certificate.setPostageCost(POSTAGE_COST);
		certificate.setTotalItemCost(TOTAL_ITEM_COST);
		certificate.setPostalDelivery(false);
		final CertificateItemOptions options = new CertificateItemOptions();
		options.setForename(FORENAME);
		options.setSurname(SURNAME);
		certificate.setItemOptions(options);
		when(apiClientService.getItem(ERIC_ACCESS_TOKEN, ITEM_URI)).thenReturn(certificate);

		when(basketService.getBasketById(anyString())).thenReturn(Optional.of(basket));
		when(checkoutService.createCheckout(
				any(Certificate.class), any(String.class), any(String.class), any(DeliveryDetails.class)))
				.thenReturn(checkout);

		CheckoutData checkoutDataResp = new CheckoutData();
		checkoutDataResp.setTotalOrderCost("20");
		when(checkout.getData()).thenReturn(checkoutDataResp);

		// When and then
		webTestClient.post().uri("/basket/checkouts")
				.contentType(MediaType.APPLICATION_JSON)
				.header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
				.header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_OAUTH2_TYPE_VALUE)
				.header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
				.header(ERIC_AUTHORISED_USER_HEADER_NAME, ERIC_AUTHORISED_USER_VALUE)
                .header(ApiSdkManager.getEricPassthroughTokenHeader(), ERIC_ACCESS_TOKEN)
				.exchange()
				.expectStatus().isAccepted()
				.expectBody(Checkout.class);
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
		final AddDeliveryDetailsRequestDTO addDeliveryDetailsRequestDTO = new AddDeliveryDetailsRequestDTO();
		DeliveryDetailsDTO deliveryDetailsDTO = new DeliveryDetailsDTO();
		deliveryDetailsDTO.setAddressLine1(ADDRESS_LINE_1);
		deliveryDetailsDTO.setAddressLine2(ADDRESS_LINE_2);
		deliveryDetailsDTO.setCountry(COUNTRY);
		deliveryDetailsDTO.setForename(FORENAME);
		deliveryDetailsDTO.setLocality(LOCALITY);
		deliveryDetailsDTO.setPoBox(PO_BOX);
		deliveryDetailsDTO.setPostalCode(POSTAL_CODE);
		deliveryDetailsDTO.setRegion(REGION);
		deliveryDetailsDTO.setSurname(SURNAME);
		addDeliveryDetailsRequestDTO.setDeliveryDetails(deliveryDetailsDTO);

		final Basket basket = new Basket();
		when(basketService.getBasketById(anyString())).thenReturn(Optional.of(basket));
		when(basketService.saveBasket(basket)).thenReturn(basket);

		// When and then
		webTestClient.patch().uri("/basket")
				.contentType(MediaType.APPLICATION_JSON)
				.header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
				.header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_OAUTH2_TYPE_VALUE)
				.header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
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

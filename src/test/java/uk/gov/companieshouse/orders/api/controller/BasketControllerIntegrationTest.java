package uk.gov.companieshouse.orders.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.model.payment.PaymentApi;
import uk.gov.companieshouse.orders.api.dto.AddDeliveryDetailsRequestDTO;
import uk.gov.companieshouse.orders.api.dto.AddToBasketRequestDTO;
import uk.gov.companieshouse.orders.api.dto.BasketItemDTO;
import uk.gov.companieshouse.orders.api.dto.BasketPaymentRequestDTO;
import uk.gov.companieshouse.orders.api.dto.DeliveryDetailsDTO;
import uk.gov.companieshouse.orders.api.dto.ItemDTO;
import uk.gov.companieshouse.orders.api.dto.PaymentDetailsDTO;
import uk.gov.companieshouse.orders.api.dto.PaymentLinksDTO;
import uk.gov.companieshouse.orders.api.exception.ErrorType;
import uk.gov.companieshouse.orders.api.model.ApiError;
import uk.gov.companieshouse.orders.api.model.Basket;
import uk.gov.companieshouse.orders.api.model.BasketData;
import uk.gov.companieshouse.orders.api.model.Certificate;
import uk.gov.companieshouse.orders.api.model.CertificateItemOptions;
import uk.gov.companieshouse.orders.api.model.CertifiedCopy;
import uk.gov.companieshouse.orders.api.model.CertifiedCopyItemOptions;
import uk.gov.companieshouse.orders.api.model.Checkout;
import uk.gov.companieshouse.orders.api.model.CheckoutData;
import uk.gov.companieshouse.orders.api.model.DeliveryDetails;
import uk.gov.companieshouse.orders.api.model.Item;
import uk.gov.companieshouse.orders.api.model.ItemCosts;
import uk.gov.companieshouse.orders.api.model.ItemOptions;
import uk.gov.companieshouse.orders.api.model.Order;
import uk.gov.companieshouse.orders.api.model.PaymentStatus;
import uk.gov.companieshouse.orders.api.repository.BasketRepository;
import uk.gov.companieshouse.orders.api.repository.CheckoutRepository;
import uk.gov.companieshouse.orders.api.repository.OrderRepository;
import uk.gov.companieshouse.orders.api.service.ApiClientService;
import uk.gov.companieshouse.orders.api.service.CheckoutService;
import uk.gov.companieshouse.orders.api.service.EtagGeneratorService;
import uk.gov.companieshouse.orders.api.util.TimestampedEntityVerifier;
import uk.gov.companieshouse.orders.api.validator.DeliveryDetailsValidator;
import uk.gov.companieshouse.sdk.manager.ApiSdkManager;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.companieshouse.api.util.security.EricConstants.ERIC_AUTHORISED_KEY_ROLES;
import static uk.gov.companieshouse.api.util.security.SecurityConstants.INTERNAL_USER_ROLE;
import static uk.gov.companieshouse.orders.api.model.CertificateType.INCORPORATION_WITH_ALL_NAME_CHANGES;
import static uk.gov.companieshouse.orders.api.model.ProductType.CERTIFICATE_ADDITIONAL_COPY;
import static uk.gov.companieshouse.orders.api.model.ProductType.CERTIFICATE_SAME_DAY;
import static uk.gov.companieshouse.orders.api.util.TestConstants.CERTIFICATE_KIND;
import static uk.gov.companieshouse.orders.api.util.TestConstants.DOCUMENT;
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
import static uk.gov.companieshouse.orders.api.util.TestConstants.VALID_CERTIFICATE_URI;
import static uk.gov.companieshouse.orders.api.util.TestConstants.VALID_CERTIFIED_COPY_URI;

@DirtiesContext
@AutoConfigureMockMvc
@SpringBootTest
@EmbeddedKafka
class BasketControllerIntegrationTest {

    private static final String OLD_CERTIFICATE_URI = "/orderable/certificates/11111111";

    private static final String COMPANY_NAME = "companyName";
    private static final String COMPANY_NUMBER = "00000000";
    private static final String ADDRESS_LINE_1 = "address line 1";
    private static final String ADDRESS_LINE_2 = "address line 2";
    private static final String COUNTRY = "country";
    private static final String FORENAME = "forename";
    private static final String LOCALITY = "locality";
    private static final String PO_BOX = "po box";
    private static final String POSTAL_CODE = "postal code";
    private static final String REGION = "region";
    private static final String SURNAME = "surname";
    private static final String PAYMENT_ID = "4321";

    private static final String CUSTOMER_REFERENCE  ="customerReference";
    private static final String DESCRIPTION = "description";
    private static final String DESCRIPTION_IDENTIFIER = "descriptionIdentifier";
    private static final Map<String, String> DESCRIPTION_VALUES = new HashMap<String, String>() {{
        put("descriptionKey1", "descriptionValue1");
        put("descriptionKey2", "descriptionValue2");
    }};
    private static final String ETAG = "etag";
    private static final String KIND = "kind";
    private static final Boolean POSTAL_DELIVERY = true;
    private static final Integer QUANTITY = 1;
    private static final LocalDateTime SATISFIED_AT = LocalDateTime.of(2020, Month.JANUARY, 1, 0, 0);

    private static final String EXPECTED_TOTAL_ORDER_COST = "15";
    private static final String DISCOUNT_APPLIED_1 = "0";
    private static final String ITEM_COST_1 = "5";
    private static final String CALCULATED_COST_1 = "5";
    private static final String DISCOUNT_APPLIED_2 = "10";
    private static final String ITEM_COST_2 = "5";
    private static final String CALCULATED_COST_2 = "5";
    private static final String DISCOUNT_APPLIED_3 = "0";
    private static final String ITEM_COST_3 = "5";
    private static final String CALCULATED_COST_3 = "5";
    private static final String INVALID_ITEM_URI = "invalid_uri";

    private static final String PAYMENT_REQUIRED_HEADER = "x-payment-required";
    private static final String COSTS_LINK = "payments.service/payments";

    private static final List<ItemCosts> ITEM_COSTS =
             asList(new ItemCosts( "0", "50", "50", CERTIFICATE_SAME_DAY),
                    new ItemCosts("40", "50", "10", CERTIFICATE_ADDITIONAL_COPY),
                    new ItemCosts("40", "50", "10", CERTIFICATE_ADDITIONAL_COPY));
    private static final String POSTAGE_COST = "0";
    private static final String TOTAL_ITEM_COST = "70";
    private static final List<ItemCosts> ITEM_COSTS_ZERO =
            asList(new ItemCosts( "0", "0", "0", CERTIFICATE_SAME_DAY));
    private static final String TOTAL_ITEM_COST_ZERO = "0";
    private static final String PAYMENT_KIND = "payment-details#payment-details";
    private static final String UPDATED_ETAG = "dc3b9657a32453c6f79d5f3981bfa9af0a8b5478";
    private static final LocalDateTime PAID_AT_DATE = LocalDateTime.of(2020, Month.JANUARY, 1, 0, 0);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private BasketRepository basketRepository;

    @Autowired
    private CheckoutRepository checkoutRepository;

    @Autowired
    private OrderRepository orderRepository;

    @MockBean
    private ApiClientService apiClientService;

    @SpyBean
    private CheckoutService checkoutService;

    @MockBean
    private EtagGeneratorService etagGenerator;

    @Autowired
    private DeliveryDetailsValidator deliveryDetailsValidator;

    private TimestampedEntityVerifier timestamps;

    @Mock
    private ApiErrorResponseException apiErrorResponseException;

    @BeforeEach
    void setUp() {
        timestamps = new TimestampedEntityVerifier();
    }

    @AfterEach
    void tearDown() {
        basketRepository.findById(ERIC_IDENTITY_VALUE).ifPresent(basketRepository::delete);
        checkoutRepository.deleteAll();
        orderRepository.deleteAll();
    }

    @Test
    @DisplayName("Add Item successfully adds an item to the basket and returns item if the basket does not exist")
    void addItemSuccessfullyAddsItemToBasketIfBasketDoesNotExist() throws Exception {
        AddToBasketRequestDTO addToBasketRequestDTO = new AddToBasketRequestDTO();
        addToBasketRequestDTO.setItemUri(VALID_CERTIFICATE_URI);

        Certificate certificate = new Certificate();
        certificate.setCompanyNumber(COMPANY_NUMBER);
        certificate.setItemCosts(createItemCosts());
        certificate.setPostageCost(POSTAGE_COST);
        when(apiClientService.getItem(VALID_CERTIFICATE_URI)).thenReturn(certificate);

        ResultActions resultActions = mockMvc.perform(post("/basket/items")
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_OAUTH2_TYPE_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(addToBasketRequestDTO)))
                .andExpect(status().isOk());

        MvcResult result = resultActions.andReturn();
        MockHttpServletResponse response = result.getResponse();
        String contentAsString = response.getContentAsString();
        BasketItemDTO basketItemDTOResp = mapper.readValue(contentAsString, BasketItemDTO.class);

        final Optional<Basket> retrievedBasket = basketRepository.findById(ERIC_IDENTITY_VALUE);
        assertEquals(VALID_CERTIFICATE_URI, retrievedBasket.get().getData().getItems().get(0).getItemUri());
        assertThat(basketItemDTOResp.getCompanyNumber(), is(COMPANY_NUMBER));
        assertThat(basketItemDTOResp.getItemCosts().get(0).getDiscountApplied(), is(DISCOUNT_APPLIED_1));
        assertThat(basketItemDTOResp.getItemCosts().get(0).getItemCost(), is(ITEM_COST_1));
        assertThat(basketItemDTOResp.getItemCosts().get(0).getCalculatedCost(), is(CALCULATED_COST_1));
        assertThat(basketItemDTOResp.getPostageCost(), is(POSTAGE_COST));
        assertEquals(1, retrievedBasket.get().getData().getItems().size());
    }

    @Test
    @DisplayName("Add item successfully adds an item to the basket and returns item if the basket exists")
    void addItemSuccessfullyAddsAnItemToBasketIfBasketAlreadyExists() throws Exception {
        Basket basket = new Basket();
        basketRepository.save(basket);

        AddToBasketRequestDTO addToBasketRequestDTO = new AddToBasketRequestDTO();
        addToBasketRequestDTO.setItemUri(VALID_CERTIFICATE_URI);

        Certificate certificate = new Certificate();
        certificate.setCompanyNumber(COMPANY_NUMBER);
        certificate.setItemCosts(createItemCosts());
        certificate.setPostageCost(POSTAGE_COST);
        when(apiClientService.getItem(VALID_CERTIFICATE_URI)).thenReturn(certificate);

        ResultActions resultActions = mockMvc.perform(post("/basket/items")
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_OAUTH2_TYPE_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(addToBasketRequestDTO)))
                .andExpect(status().isOk());

        MvcResult result = resultActions.andReturn();
        MockHttpServletResponse response = result.getResponse();
        String contentAsString = response.getContentAsString();
        BasketItemDTO basketItemDTOResp = mapper.readValue(contentAsString, BasketItemDTO.class);

        final Optional<Basket> retrievedBasket = basketRepository.findById(ERIC_IDENTITY_VALUE);
        assertThat(basketItemDTOResp.getCompanyNumber(), is(COMPANY_NUMBER));
        assertThat(basketItemDTOResp.getItemCosts().get(0).getDiscountApplied(), is(DISCOUNT_APPLIED_1));
        assertThat(basketItemDTOResp.getItemCosts().get(0).getItemCost(), is(ITEM_COST_1));
        assertThat(basketItemDTOResp.getItemCosts().get(0).getCalculatedCost(), is(CALCULATED_COST_1));
        assertThat(basketItemDTOResp.getPostageCost(), is(POSTAGE_COST));
        assertEquals(VALID_CERTIFICATE_URI, retrievedBasket.get().getData().getItems().get(0).getItemUri());
    }

    @Test
    @DisplayName("Add certificate to basket responds with correctly populated certificate item options")
    void addCertificateReturnsCorrectlyPopulatedOptions() throws Exception {
        final AddToBasketRequestDTO addToBasketRequestDTO = new AddToBasketRequestDTO();
        addToBasketRequestDTO.setItemUri(VALID_CERTIFICATE_URI);

        final Certificate certificate = new Certificate();
        final CertificateItemOptions options = new CertificateItemOptions();
        options.setCertificateType(INCORPORATION_WITH_ALL_NAME_CHANGES);
        certificate.setItemOptions(options);
        when(apiClientService.getItem(VALID_CERTIFICATE_URI)).thenReturn(certificate);

        mockMvc.perform(post("/basket/items")
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_OAUTH2_TYPE_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(addToBasketRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.item_options.certificate_type",
                        is(INCORPORATION_WITH_ALL_NAME_CHANGES.getJsonName())));

        final Optional<Basket> retrievedBasket = basketRepository.findById(ERIC_IDENTITY_VALUE);
        assertThat(retrievedBasket.get().getData().getItems().get(0).getItemUri(), is(VALID_CERTIFICATE_URI));
    }

    @Test
    @DisplayName("Add certified copy to basket responds with correctly populated certified copy item options")
    void addCertifiedCopyReturnsCorrectlyPopulatedOptions() throws Exception {
        final AddToBasketRequestDTO addToBasketRequestDTO = new AddToBasketRequestDTO();
        addToBasketRequestDTO.setItemUri(VALID_CERTIFIED_COPY_URI);

        final CertifiedCopy copy = new CertifiedCopy();
        final CertifiedCopyItemOptions options = new CertifiedCopyItemOptions();
        options.setFilingHistoryDocuments(singletonList(DOCUMENT));
        copy.setItemOptions(options);
        when(apiClientService.getItem(VALID_CERTIFIED_COPY_URI)).thenReturn(copy);

        mockMvc.perform(post("/basket/items")
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_OAUTH2_TYPE_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(addToBasketRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.item_options.filing_history_documents[0].filing_history_date",
                        is(DOCUMENT.getFilingHistoryDate())))
                .andExpect(jsonPath("$.item_options.filing_history_documents[0].filing_history_description",
                        is(DOCUMENT.getFilingHistoryDescription())))
                .andExpect(jsonPath("$.item_options.filing_history_documents[0].filing_history_id",
                        is(DOCUMENT.getFilingHistoryId())))
                .andExpect(jsonPath("$.item_options.filing_history_documents[0].filing_history_type",
                        is(DOCUMENT.getFilingHistoryType())));

        final Optional<Basket> retrievedBasket = basketRepository.findById(ERIC_IDENTITY_VALUE);
        assertThat(retrievedBasket.get().getData().getItems().get(0).getItemUri(), is(VALID_CERTIFIED_COPY_URI));
    }


    @Test
    @DisplayName("Add item returns 400 when invalid item passed in request")
    void addItemReturns400WhenRequestedItemIsInvalid() throws Exception {
        Basket basket = new Basket();
        basket.setId(ERIC_IDENTITY_VALUE);
        Item basketItem = new Item();
        basketItem.setItemUri(INVALID_ITEM_URI);
        basket.getData().setItems(Collections.singletonList(basketItem));
        basketRepository.save(basket);

        AddToBasketRequestDTO addToBasketRequestDTO = new AddToBasketRequestDTO();
        addToBasketRequestDTO.setItemUri(INVALID_ITEM_URI);
        when(apiClientService.getItem(anyString())).thenThrow(apiErrorResponseException);

        final ApiError expectedValidationError =
                new ApiError(BAD_REQUEST,
                        asList(ErrorType.BASKET_ITEM_INVALID.getValue()));

        mockMvc.perform(post("/basket/items")
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_OAUTH2_TYPE_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(addToBasketRequestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(mapper.writeValueAsString(expectedValidationError)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @DisplayName("Add item successfully replaces an item in the basket")
    void addItemSuccessfullyReplacesAnItemInTheBasket() throws Exception {
        Item item = new Item();
        item.setItemUri(OLD_CERTIFICATE_URI);
        BasketData basketData = new BasketData();
        basketData.setItems(Arrays.asList(item));
        Basket basket = new Basket();
        basket.setData(basketData);
        basketRepository.save(basket);

        AddToBasketRequestDTO addToBasketRequestDTO = new AddToBasketRequestDTO();
        addToBasketRequestDTO.setItemUri(VALID_CERTIFICATE_URI);

        mockMvc.perform(post("/basket/items")
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_OAUTH2_TYPE_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(addToBasketRequestDTO)))
                .andExpect(status().isOk());

        final Optional<Basket> retrievedBasket = basketRepository.findById(ERIC_IDENTITY_VALUE);
        assertEquals(VALID_CERTIFICATE_URI, retrievedBasket.get().getData().getItems().get(0).getItemUri());

    }

    @Test
    @DisplayName("Add item fails to add item to basket that fails validation")
    void addItemFailsToAddItemToBasketIfFailsValidation() throws Exception {
        mockMvc.perform(post("/basket/items")
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_OAUTH2_TYPE_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"gibberish\":\"gibberish\"}"))
                .andExpect(status().isBadRequest());

        final Optional<Basket> retrievedBasket = basketRepository.findById(ERIC_IDENTITY_VALUE);
        assertFalse(retrievedBasket.isPresent());
    }

    @Test
    @DisplayName("Checkout basket successfully creates checkout, when basket contains a valid certificate uri")
    void checkoutBasketSuccessfullyCreatesCheckoutWhenBasketIsValid() throws Exception {
        basketRepository.save(getBasket(false));

        Certificate certificate = new Certificate();
        certificate.setCompanyNumber(COMPANY_NUMBER);
        certificate.setKind(CERTIFICATE_KIND);
        final CertificateItemOptions options = new CertificateItemOptions();
        options.setCertificateType(INCORPORATION_WITH_ALL_NAME_CHANGES);
        options.setForename(FORENAME);
        options.setSurname(SURNAME);
        certificate.setItemOptions(options);
        certificate.setItemCosts(createItemCosts());
        certificate.setPostageCost(POSTAGE_COST);
        certificate.setPostalDelivery(false);
        when(apiClientService.getItem(VALID_CERTIFICATE_URI)).thenReturn(certificate);

        ResultActions resultActions = mockMvc.perform(post("/basket/checkouts")
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_OAUTH2_TYPE_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                .header(ERIC_AUTHORISED_USER_HEADER_NAME, ERIC_AUTHORISED_USER_VALUE))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.items[0].item_options.certificate_type",
                        is(INCORPORATION_WITH_ALL_NAME_CHANGES.getJsonName())));

        MvcResult result = resultActions.andReturn();
        MockHttpServletResponse response = result.getResponse();
        assertThat(response.getHeader(PAYMENT_REQUIRED_HEADER), is(COSTS_LINK));
        String contentAsString = response.getContentAsString();
        CheckoutData responseCheckoutData = mapper.readValue(contentAsString, CheckoutData.class);

        final Optional<Checkout> retrievedCheckout = checkoutRepository.findById(responseCheckoutData.getReference());
        assertTrue(retrievedCheckout.isPresent());
        assertEquals(ERIC_IDENTITY_VALUE, retrievedCheckout.get().getUserId());
        final CheckoutData checkoutData = retrievedCheckout.get().getData();
        final Item item = checkoutData.getItems().get(0);
        assertEquals(COMPANY_NUMBER, item.getCompanyNumber());
        final ItemOptions retrievedOptions = item.getItemOptions();
        assertEquals(FORENAME, retrievedOptions.getForename());
        assertEquals(SURNAME, retrievedOptions.getSurname());
        assertEquals(EXPECTED_TOTAL_ORDER_COST, checkoutData.getTotalOrderCost());
    }

    private Basket getBasket(boolean isPostalDelivery) {
        Basket basket = new Basket();
        basket.setId(ERIC_IDENTITY_VALUE);
        BasketData basketData = new BasketData();
        if (isPostalDelivery) {
            DeliveryDetails deliveryDetails = new DeliveryDetails();
            deliveryDetails.setAddressLine1(ADDRESS_LINE_1);
            deliveryDetails.setForename(FORENAME);
            deliveryDetails.setSurname(SURNAME);
            deliveryDetails.setCountry(COUNTRY);
            deliveryDetails.setLocality(LOCALITY);
            deliveryDetails.setRegion(REGION);
            basketData.setDeliveryDetails(deliveryDetails);
        }

        Item basketItem = new Item();
        basketItem.setItemUri(VALID_CERTIFICATE_URI);

        basketItem.setPostalDelivery(isPostalDelivery);
        basketData.setItems(Collections.singletonList(basketItem));
        basket.setData(basketData);
        return basket;
    }

    @Test
    @DisplayName("Checkout basket returns 200 when total order cost is zero")
    void checkoutBasketReturns200WhenTotalOrderCostIsZero() throws Exception {
        basketRepository.save(getBasket(false));

        Certificate certificate = new Certificate();
        certificate.setKind(CERTIFICATE_KIND);
        certificate.setItemOptions(new ItemOptions());
        certificate.setItemCosts(ITEM_COSTS_ZERO);
        certificate.setPostageCost(POSTAGE_COST);
        certificate.setTotalItemCost(TOTAL_ITEM_COST_ZERO);
        certificate.setPostalDelivery(false);
        when(apiClientService.getItem(VALID_CERTIFICATE_URI)).thenReturn(certificate);

        ResultActions resultActions = mockMvc.perform(post("/basket/checkouts")
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_OAUTH2_TYPE_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                .header(ERIC_AUTHORISED_USER_HEADER_NAME, ERIC_AUTHORISED_USER_VALUE))
                .andExpect(status().isOk());

        MvcResult result = resultActions.andReturn();
        MockHttpServletResponse response = result.getResponse();
        assertThat(response.getHeader(PAYMENT_REQUIRED_HEADER), isEmptyOrNullString());
        String contentAsString = response.getContentAsString();
        CheckoutData responseCheckoutData = mapper.readValue(contentAsString, CheckoutData.class);

        assertEquals(1, checkoutRepository.count());
        final Optional<Checkout> retrievedCheckout = checkoutRepository.findById(responseCheckoutData.getReference());
        assertTrue(retrievedCheckout.isPresent());
        final Item retrievedItem = retrievedCheckout.get().getData().getItems().get(0);
        assertThat(retrievedItem.getItemCosts(), is(ITEM_COSTS_ZERO));
        assertThat(retrievedItem.getPostageCost(), is(POSTAGE_COST));
        assertThat(retrievedItem.getTotalItemCost(), is(TOTAL_ITEM_COST_ZERO));
    }

    @Test
    @DisplayName("Checkout basket returns 202 when total order cost is non-zero")
    void checkoutBasketReturns202WhenTotalOrderCostIsNonZero() throws Exception {
        basketRepository.save(getBasket(true));

        final Certificate certificate = new Certificate();
        certificate.setKind(CERTIFICATE_KIND);
        certificate.setItemOptions(new ItemOptions());
        certificate.setPostalDelivery(true);
        certificate.setItemCosts(ITEM_COSTS);
        certificate.setPostageCost(POSTAGE_COST);
        certificate.setTotalItemCost(TOTAL_ITEM_COST);
        when(apiClientService.getItem(VALID_CERTIFICATE_URI)).thenReturn(certificate);

        ResultActions resultActions = mockMvc.perform(post("/basket/checkouts")
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_OAUTH2_TYPE_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                .header(ERIC_AUTHORISED_USER_HEADER_NAME, ERIC_AUTHORISED_USER_VALUE))
                .andExpect(status().isAccepted());

        MvcResult result = resultActions.andReturn();
        String contentAsString = result.getResponse().getContentAsString();
        CheckoutData response = mapper.readValue(contentAsString, CheckoutData.class);

        assertEquals(1, checkoutRepository.count());
        final Optional<Checkout> retrievedCheckout = checkoutRepository.findById(response.getReference());
        assertTrue(retrievedCheckout.isPresent());
        final Item retrievedItem = retrievedCheckout.get().getData().getItems().get(0);
        assertThat(retrievedItem.getItemCosts(), is(ITEM_COSTS));
        assertThat(retrievedItem.getPostageCost(), is(POSTAGE_COST));
        assertThat(retrievedItem.getTotalItemCost(), is(TOTAL_ITEM_COST));
    }

    @Test
    @DisplayName("Checkout basket fails to create checkout and returns 409 conflict, when basket is empty")
    void checkoutBasketfFailsToCreateCheckoutIfBasketIsEmpty() throws Exception {
        Basket basket = new Basket();
        basket.setId(ERIC_IDENTITY_VALUE);
        basketRepository.save(basket);

        mockMvc.perform(post("/basket/checkouts")
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_OAUTH2_TYPE_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE))
                .andExpect(status().isConflict());

        assertEquals(0, checkoutRepository.count());
    }

    @Test
    @DisplayName("Checkout Basket fails to create checkout and returns 409, when basket does not exist")
    void checkoutBasketFailsToCreateCheckoutIfBasketDoesNotExist() throws Exception {

        mockMvc.perform(post("/basket/checkouts")
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_OAUTH2_TYPE_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE))
                .andExpect(status().isConflict());

        assertEquals(0, checkoutRepository.count());
    }

    @Test
    @DisplayName("Checkout Basket fails to create checkout and returns 400, when there is a failure getting the item")
    void checkoutBasketFailsToCreateCheckoutWhenItFailsToGetAnItem() throws Exception {
        basketRepository.save(getBasket(false));

        when(apiClientService.getItem(VALID_CERTIFICATE_URI)).thenThrow(apiErrorResponseException);

        mockMvc.perform(post("/basket/checkouts")
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_OAUTH2_TYPE_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE))
                .andExpect(status().isBadRequest());

        assertEquals(0, checkoutRepository.count());
    }

    @Test
    @DisplayName("Check out basket returns 403 if body is present")
    void checkoutBasketReturnsBadRequestIfBodyIsPresent() throws Exception {

        mockMvc.perform(post("/basket/checkouts")
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_OAUTH2_TYPE_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"gibberish\":\"gibberish\"}"))
                .andExpect(status().isBadRequest());

        assertEquals(0, checkoutRepository.count());
    }

    @Test
    @DisplayName("Checkout basket successfully creates checkout with costs from Certificates API")
    void checkoutBasketCheckoutContainsCosts() throws Exception {
        basketRepository.save(getBasket(false));

        final Certificate certificate = new Certificate();
        certificate.setKind(CERTIFICATE_KIND);
        certificate.setItemOptions(new ItemOptions());
        certificate.setItemCosts(ITEM_COSTS);
        certificate.setPostageCost(POSTAGE_COST);
        certificate.setTotalItemCost(TOTAL_ITEM_COST);
        certificate.setPostalDelivery(false);
        when(apiClientService.getItem(VALID_CERTIFICATE_URI)).thenReturn(certificate);

        final CheckoutData expectedResponseBody = new CheckoutData();
        final Item item = new Item();
        item.setItemCosts(ITEM_COSTS);
        item.setPostageCost(POSTAGE_COST);
        item.setTotalItemCost(TOTAL_ITEM_COST);
        expectedResponseBody.setItems(singletonList(item));

        ResultActions resultActions = mockMvc.perform(post("/basket/checkouts")
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_OAUTH2_TYPE_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                .header(ERIC_AUTHORISED_USER_HEADER_NAME, ERIC_AUTHORISED_USER_VALUE))
                .andExpect(status().isAccepted())
                .andExpect(content().json(mapper.writeValueAsString(expectedResponseBody)));

        MvcResult result = resultActions.andReturn();
        String contentAsString = result.getResponse().getContentAsString();
        CheckoutData response = mapper.readValue(contentAsString, CheckoutData.class);

        final Optional<Checkout> retrievedCheckout = checkoutRepository.findById(response.getReference());
        assertTrue(retrievedCheckout.isPresent());
        final Item retrievedItem = retrievedCheckout.get().getData().getItems().get(0);
        assertThat(retrievedItem.getItemCosts(), is(ITEM_COSTS));
        assertThat(retrievedItem.getPostageCost(), is(POSTAGE_COST));
        assertThat(retrievedItem.getTotalItemCost(), is(TOTAL_ITEM_COST));

    }

    @Test
    @DisplayName("Get basket successfully returns a basket populated with a certificate")
    void getBasketReturnsBasketPopulatedWithCertificate() throws Exception {
        final LocalDateTime start = timestamps.start();
        Basket basket = createBasket(start);

        basketRepository.save(basket);

        final Certificate certificate = new Certificate();
        certificate.setItemUri(VALID_CERTIFICATE_URI);
        certificate.setCompanyNumber(COMPANY_NUMBER);
        certificate.setCompanyName(COMPANY_NAME);
        certificate.setCustomerReference(CUSTOMER_REFERENCE);
        certificate.setDescription(DESCRIPTION);
        certificate.setDescriptionIdentifier(DESCRIPTION_IDENTIFIER);
        certificate.setDescriptionValues(DESCRIPTION_VALUES);
        certificate.setItemCosts(ITEM_COSTS);
        certificate.setEtag(ETAG);
        certificate.setKind(KIND);
        certificate.setPostalDelivery(POSTAL_DELIVERY);
        certificate.setQuantity(QUANTITY);
        certificate.setSatisfiedAt(SATISFIED_AT);
        certificate.setPostageCost(POSTAGE_COST);
        certificate.setTotalItemCost(TOTAL_ITEM_COST);
        final CertificateItemOptions options = new CertificateItemOptions();
        options.setCertificateType(INCORPORATION_WITH_ALL_NAME_CHANGES);
        certificate.setItemOptions(options);
        when(apiClientService.getItem(VALID_CERTIFICATE_URI)).thenReturn(certificate);

        final String jsonResponse = mockMvc.perform(get("/basket")
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_OAUTH2_TYPE_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].item_options.certificate_type",
                        is(INCORPORATION_WITH_ALL_NAME_CHANGES.getJsonName())))
                .andReturn().getResponse().getContentAsString();

        final BasketData response = mapper.readValue(jsonResponse, BasketData.class);

        final DeliveryDetails getDeliveryDetails = response.getDeliveryDetails();
        final Item item = response.getItems().get(0);
        assertEquals(ADDRESS_LINE_1, getDeliveryDetails.getAddressLine1());
        assertEquals(ADDRESS_LINE_2, getDeliveryDetails.getAddressLine2());
        assertEquals(COUNTRY, getDeliveryDetails.getCountry());
        assertEquals(FORENAME, getDeliveryDetails.getForename());
        assertEquals(LOCALITY, getDeliveryDetails.getLocality());
        assertEquals(SURNAME, getDeliveryDetails.getSurname());

        assertEquals(VALID_CERTIFICATE_URI, item.getItemUri());
        assertEquals(COMPANY_NAME, item.getCompanyName());
        assertEquals(COMPANY_NUMBER, item.getCompanyNumber());
        assertEquals(CUSTOMER_REFERENCE, item.getCustomerReference());
        assertEquals(DESCRIPTION, item.getDescription());
        assertEquals(DESCRIPTION_IDENTIFIER, item.getDescriptionIdentifier());
        assertEquals(DESCRIPTION_VALUES, item.getDescriptionValues());
        assertEquals(ITEM_COSTS, item.getItemCosts());
        assertEquals(ETAG, item.getEtag());
        assertEquals(KIND, item.getKind());
        assertEquals(POSTAL_DELIVERY, item.isPostalDelivery());
        assertEquals(QUANTITY, item.getQuantity());
        assertEquals(SATISFIED_AT, item.getSatisfiedAt());
        assertEquals(POSTAGE_COST, item.getPostageCost());
        assertEquals(TOTAL_ITEM_COST, item.getTotalItemCost());

        verifyBasketIsUnchanged(start, basket.getData().getDeliveryDetails(), VALID_CERTIFICATE_URI);
    }

    @Test
    @DisplayName("Get basket successfully returns a basket populated with a certified copy")
    void getBasketReturnsBasketPopulatedWithCertifiedCopy() throws Exception {
        final LocalDateTime start = timestamps.start();
        final Basket basket = createBasket(start, VALID_CERTIFIED_COPY_URI);

        basketRepository.save(basket);

        final CertifiedCopy copy = new CertifiedCopy();
        final CertifiedCopyItemOptions options = new CertifiedCopyItemOptions();
        options.setFilingHistoryDocuments(singletonList(DOCUMENT));
        copy.setItemOptions(options);
        copy.setItemUri(VALID_CERTIFIED_COPY_URI);
        copy.setCompanyNumber(COMPANY_NUMBER);
        copy.setCompanyName(COMPANY_NAME);
        copy.setCustomerReference(CUSTOMER_REFERENCE);
        copy.setDescription(DESCRIPTION);
        copy.setDescriptionIdentifier(DESCRIPTION_IDENTIFIER);
        copy.setDescriptionValues(DESCRIPTION_VALUES);
        copy.setItemCosts(ITEM_COSTS);
        copy.setEtag(ETAG);
        copy.setKind(KIND);
        copy.setPostalDelivery(POSTAL_DELIVERY);
        copy.setQuantity(QUANTITY);
        copy.setSatisfiedAt(SATISFIED_AT);
        copy.setPostageCost(POSTAGE_COST);
        copy.setTotalItemCost(TOTAL_ITEM_COST);
        when(apiClientService.getItem(VALID_CERTIFIED_COPY_URI)).thenReturn(copy);

        final String jsonResponse = mockMvc.perform(get("/basket")
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_OAUTH2_TYPE_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].item_options.filing_history_documents[0].filing_history_date",
                        is(DOCUMENT.getFilingHistoryDate())))
                .andExpect(jsonPath("$.items[0].item_options.filing_history_documents[0].filing_history_description",
                        is(DOCUMENT.getFilingHistoryDescription())))
                .andExpect(jsonPath("$.items[0].item_options.filing_history_documents[0].filing_history_id",
                        is(DOCUMENT.getFilingHistoryId())))
                .andExpect(jsonPath("$.items[0].item_options.filing_history_documents[0].filing_history_type",
                        is(DOCUMENT.getFilingHistoryType())))
                .andReturn().getResponse().getContentAsString();

        final BasketData response = mapper.readValue(jsonResponse, BasketData.class);

        final DeliveryDetails getDeliveryDetails = response.getDeliveryDetails();
        final Item item = response.getItems().get(0);
        assertEquals(ADDRESS_LINE_1, getDeliveryDetails.getAddressLine1());
        assertEquals(ADDRESS_LINE_2, getDeliveryDetails.getAddressLine2());
        assertEquals(COUNTRY, getDeliveryDetails.getCountry());
        assertEquals(FORENAME, getDeliveryDetails.getForename());
        assertEquals(LOCALITY, getDeliveryDetails.getLocality());
        assertEquals(SURNAME, getDeliveryDetails.getSurname());

        assertEquals(VALID_CERTIFIED_COPY_URI, item.getItemUri());
        assertEquals(COMPANY_NAME, item.getCompanyName());
        assertEquals(COMPANY_NUMBER, item.getCompanyNumber());
        assertEquals(CUSTOMER_REFERENCE, item.getCustomerReference());
        assertEquals(DESCRIPTION, item.getDescription());
        assertEquals(DESCRIPTION_IDENTIFIER, item.getDescriptionIdentifier());
        assertEquals(DESCRIPTION_VALUES, item.getDescriptionValues());
        assertEquals(ITEM_COSTS, item.getItemCosts());
        assertEquals(ETAG, item.getEtag());
        assertEquals(KIND, item.getKind());
        assertEquals(POSTAL_DELIVERY, item.isPostalDelivery());
        assertEquals(QUANTITY, item.getQuantity());
        assertEquals(SATISFIED_AT, item.getSatisfiedAt());
        assertEquals(POSTAGE_COST, item.getPostageCost());
        assertEquals(TOTAL_ITEM_COST, item.getTotalItemCost());

        verifyBasketIsUnchanged(start, basket.getData().getDeliveryDetails(), VALID_CERTIFIED_COPY_URI);
    }

    @Test
    @DisplayName("Create new basket when GET basket returns no basket")
    void createNewBasketOnNoBasketReturned() throws Exception {

        Certificate certificate = new Certificate();
        certificate.setCompanyNumber(COMPANY_NUMBER);
        certificate.setItemCosts(createItemCosts());
        certificate.setPostageCost(POSTAGE_COST);

        mockMvc.perform(get("/basket")
            .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
            .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_OAUTH2_TYPE_VALUE)
            .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        final Optional<Basket> retrievedBasket = basketRepository.findById(ERIC_IDENTITY_VALUE);
        assertNotNull(retrievedBasket);
    }

    @Test
    @DisplayName("Add delivery details to the basket, if the basket exists")
    void addDeliveryDetailsToBasketIfTheBasketExists() throws Exception {
        Basket basket = new Basket();
        basketRepository.save(basket);

        AddDeliveryDetailsRequestDTO addDeliveryDetailsRequestDTO = new AddDeliveryDetailsRequestDTO();
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

        mockMvc.perform(patch("/basket")
            .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
            .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_OAUTH2_TYPE_VALUE)
            .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(addDeliveryDetailsRequestDTO)))
            .andExpect(status().isOk());

        final Optional<Basket> retrievedBasket = basketRepository.findById(ERIC_IDENTITY_VALUE);
        final DeliveryDetails getDeliveryDetails = retrievedBasket.get().getData().getDeliveryDetails();
        assertEquals(ADDRESS_LINE_1, getDeliveryDetails.getAddressLine1());
        assertEquals(ADDRESS_LINE_2, getDeliveryDetails.getAddressLine2());
        assertEquals(COUNTRY, getDeliveryDetails.getCountry());
        assertEquals(FORENAME, getDeliveryDetails.getForename());
        assertEquals(LOCALITY, getDeliveryDetails.getLocality());
        assertEquals(PO_BOX, getDeliveryDetails.getPoBox());
        assertEquals(POSTAL_CODE, getDeliveryDetails.getPostalCode());
        assertEquals(REGION, getDeliveryDetails.getRegion());
        assertEquals(SURNAME, getDeliveryDetails.getSurname());
    }

    @Test
    @DisplayName("Add delivery details to the basket, if the basket does not exist")
    void addDeliveryDetailsToBasketIfTheBasketDoesNotExist() throws Exception {

        AddDeliveryDetailsRequestDTO addDeliveryDetailsRequestDTO = new AddDeliveryDetailsRequestDTO();
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

        mockMvc.perform(patch("/basket")
            .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_OAUTH2_TYPE_VALUE)
            .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(addDeliveryDetailsRequestDTO)))
            .andExpect(status().isOk());

        final Optional<Basket> retrievedBasket = basketRepository.findById(ERIC_IDENTITY_VALUE);
        final DeliveryDetails getDeliveryDetails = retrievedBasket.get().getData().getDeliveryDetails();
        assertEquals(ADDRESS_LINE_1, getDeliveryDetails.getAddressLine1());
        assertEquals(ADDRESS_LINE_2, getDeliveryDetails.getAddressLine2());
        assertEquals(COUNTRY, getDeliveryDetails.getCountry());
        assertEquals(FORENAME, getDeliveryDetails.getForename());
        assertEquals(LOCALITY, getDeliveryDetails.getLocality());
        assertEquals(PO_BOX, getDeliveryDetails.getPoBox());
        assertEquals(POSTAL_CODE, getDeliveryDetails.getPostalCode());
        assertEquals(REGION, getDeliveryDetails.getRegion());
        assertEquals(SURNAME, getDeliveryDetails.getSurname());
    }

    @Test
    @DisplayName("Add delivery details fails due to failed validation")
    void addDeliveryDetailsFailsDueToFailedValidation() throws Exception {
        AddDeliveryDetailsRequestDTO addDeliveryDetailsRequestDTO = new AddDeliveryDetailsRequestDTO();
        DeliveryDetailsDTO deliveryDetailsDTO = new DeliveryDetailsDTO();
        deliveryDetailsDTO.setAddressLine1("");
        deliveryDetailsDTO.setAddressLine2(ADDRESS_LINE_2);
        deliveryDetailsDTO.setCountry(COUNTRY);
        deliveryDetailsDTO.setSurname(SURNAME);
        deliveryDetailsDTO.setForename(FORENAME);
        deliveryDetailsDTO.setLocality(LOCALITY);
        addDeliveryDetailsRequestDTO.setDeliveryDetails(deliveryDetailsDTO);

        final ApiError expectedValidationError =
                new ApiError(BAD_REQUEST,
                        asList("delivery_details.address_line_1: must not be blank"));

        mockMvc.perform(patch("/basket")
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_OAUTH2_TYPE_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(addDeliveryDetailsRequestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(mapper.writeValueAsString(expectedValidationError)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @DisplayName("Patch basket returns 400 when item uri is invalid")
    void patchBasketReturnsBadRequestItemUriInvalid() throws Exception {
        Basket basket = new Basket();
        basket.setId(ERIC_IDENTITY_VALUE);
        Item basketItem = new Item();
        basketItem.setItemUri(INVALID_ITEM_URI);
        basket.getData().setItems(Collections.singletonList(basketItem));
        basketRepository.save(basket);

        AddDeliveryDetailsRequestDTO addDeliveryDetailsRequestDTO = new AddDeliveryDetailsRequestDTO();
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

        when(apiClientService.getItem(anyString())).thenThrow(apiErrorResponseException);

        final ApiError expectedValidationError =
                new ApiError(BAD_REQUEST,
                        asList(ErrorType.BASKET_ITEM_INVALID.getValue()));

        mockMvc.perform(patch("/basket")
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_OAUTH2_TYPE_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(addDeliveryDetailsRequestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(mapper.writeValueAsString(expectedValidationError)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @DisplayName("Checkout basket fails to create checkout and returns 409 conflict, when basket items are missing")
    void checkoutBasketfFailsToCreateCheckoutIfBasketHasNoItems() throws Exception {
        Basket basket = new Basket();
        basket.setId(ERIC_IDENTITY_VALUE);
        basketRepository.save(basket);

        when(apiClientService.getItem(null)).thenThrow(apiErrorResponseException);
        final ApiError expectedValidationError =
                new ApiError(CONFLICT,
                        asList(ErrorType.BASKET_ITEMS_MISSING.getValue()));

        mockMvc.perform(post("/basket/checkouts")
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_OAUTH2_TYPE_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE))
                .andExpect(status().isConflict())
                .andExpect(content().json(mapper.writeValueAsString(expectedValidationError)));

        assertEquals(0, checkoutRepository.count());
    }

    @Test
    @DisplayName("Checkout basket fails to create checkout and returns 409 conflict, " +
            "when delivery details are missing and postal delivery true")
    void checkoutBasketfFailsToCreateCheckoutWhenDeliverDetailsMissing() throws Exception {
        Basket basket = new Basket();
        basket.setId(ERIC_IDENTITY_VALUE);
        BasketData basketData = new BasketData();
        Item basketItem = new Item();
        basketItem.setPostalDelivery(true);
        basketItem.setItemUri(VALID_CERTIFICATE_URI);
        basketData.setItems(Collections.singletonList(basketItem));
        basket.setData(basketData);
        basketRepository.save(basket);

        Certificate certificate = new Certificate();
        certificate.setPostalDelivery(true);
        certificate.setCompanyNumber(COMPANY_NUMBER);
        certificate.setItemCosts(createItemCosts());
        certificate.setPostageCost(POSTAGE_COST);
        when(apiClientService.getItem(VALID_CERTIFICATE_URI)).thenReturn(certificate);

        mockMvc.perform(post("/basket/checkouts")
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_OAUTH2_TYPE_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE))
                .andExpect(status().isConflict());

        assertEquals(0, checkoutRepository.count());
    }

    @Test
    @DisplayName("Add item to basket returns 400 when item uri is invalid")
    void checkoutBasketReturnsBadRequestWhenItemUriInvalid() throws Exception {
        Basket basket = new Basket();
        basket.setId(ERIC_IDENTITY_VALUE);
        Item basketItem = new Item();
        basketItem.setItemUri(INVALID_ITEM_URI);
        basket.getData().setItems(Collections.singletonList(basketItem));
        basketRepository.save(basket);

        when(apiClientService.getItem(anyString())).thenThrow(apiErrorResponseException);

        final ApiError expectedValidationError =
                new ApiError(BAD_REQUEST,
                        asList(ErrorType.BASKET_ITEM_INVALID.getValue()));

        mockMvc.perform(post("/basket/checkouts")
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_OAUTH2_TYPE_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                .header(ERIC_AUTHORISED_USER_HEADER_NAME, ERIC_AUTHORISED_USER_VALUE))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(mapper.writeValueAsString(expectedValidationError)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @DisplayName("Patch payment-details endpoint success path for paid payments session")
    void patchBasketPaymentDetailsSuccessPaid() throws Exception {
        final LocalDateTime start = timestamps.start();

        BasketPaymentRequestDTO basketPaymentRequest = createBasketPaymentRequest(PaymentStatus.PAID);
        createBasket(start);
        Checkout checkout = createCheckout();
        PaymentApi paymentSession = createPaymentSession(checkout.getId(), "paid", "70.00");

        when(apiClientService.getPaymentSummary(ERIC_ACCESS_TOKEN, PAYMENT_ID)).thenReturn(paymentSession);
        when(etagGenerator.generateEtag()).thenReturn(UPDATED_ETAG);

        mockMvc.perform(patch("/basket/checkouts/" + checkout.getId() + "/payment")
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_API_KEY_TYPE_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                .header(ERIC_AUTHORISED_KEY_ROLES, INTERNAL_USER_ROLE)
                .header(ApiSdkManager.getEricPassthroughTokenHeader(), ERIC_ACCESS_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(basketPaymentRequest)))
                .andExpect(status().isNoContent());

        timestamps.end();

        // Check basket is empty
        final Optional<Basket> retrievedBasket = basketRepository.findById(ERIC_IDENTITY_VALUE);
        timestamps.verifyUpdatedAtTimestampWithinExecutionInterval(retrievedBasket.get());
        assertTrue(retrievedBasket.get().getData().getItems().isEmpty());
        assertNotNull(retrievedBasket.get().getData().getDeliveryDetails());

        // Check checkout is correctly updated
        final Optional<Checkout> retrievedCheckout = checkoutRepository.findById(checkout.getId());
        timestamps.verifyUpdatedAtTimestampWithinExecutionInterval(retrievedCheckout.get());
        assertThat(retrievedCheckout.isPresent(), is(true));
        assertThat(retrievedCheckout.get().getData(), is(notNullValue()));
        final CheckoutData data = retrievedCheckout.get().getData();
        assertThat(data.getStatus(), is(PaymentStatus.PAID));
        assertThat(data.getEtag(), is(UPDATED_ETAG));
        assertThat(data.getItems(), is(notNullValue()));
        assertThat(data.getItems().isEmpty(), is(false));
        assertThat(data.getItems().get(0), is(notNullValue()));

        final Item checkoutItem = data.getItems().get(0);
        verifyCertificateItemOptionsAreCorrect(checkoutItem);

        // Assert order is created with correct information
        final Order orderRetrieved = assertOrderCreatedCorrectly(checkout.getId(), timestamps);
        final Item retrievedItem = orderRetrieved.getData().getItems().get(0);

        assertThat(retrievedItem.getItemCosts(), is(ITEM_COSTS));
        assertThat(retrievedItem.getPostageCost(), is(POSTAGE_COST));
        assertThat(retrievedItem.getTotalItemCost(), is(TOTAL_ITEM_COST));

        verifyCertificateItemOptionsAreCorrect(retrievedItem);
    }

    @Test
    @DisplayName("Patch payment-details endpoint success path for failed payments session")
    void patchBasketPaymentDetailsSuccessFailed() throws Exception {
        final LocalDateTime start = timestamps.start();

        BasketPaymentRequestDTO basketPaymentRequest = createBasketPaymentRequest(PaymentStatus.FAILED);
        createBasket(start);
        Checkout checkout = createCheckout();

        mockMvc.perform(patch("/basket/checkouts/" + checkout.getId() + "/payment")
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_API_KEY_TYPE_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                .header(ERIC_AUTHORISED_KEY_ROLES, INTERNAL_USER_ROLE)
                .header(ApiSdkManager.getEricPassthroughTokenHeader(), ERIC_ACCESS_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(basketPaymentRequest)))
                .andExpect(status().isNoContent());

        timestamps.end();

        checkPatchHasNotUpdated(checkout.getId(), PaymentStatus.FAILED);
    }

    @Test
    @DisplayName("Patch payment-details endpoint success path for cancelled payments session")
    void patchBasketPaymentDetailsSuccessCancelled() throws Exception {
        final LocalDateTime start = timestamps.start();

        BasketPaymentRequestDTO basketPaymentRequest = createBasketPaymentRequest(PaymentStatus.CANCELLED);
        createBasket(start);
        Checkout checkout = createCheckout();

        mockMvc.perform(patch("/basket/checkouts/" + checkout.getId() + "/payment")
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_API_KEY_TYPE_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                .header(ERIC_AUTHORISED_KEY_ROLES, INTERNAL_USER_ROLE)
                .header(ApiSdkManager.getEricPassthroughTokenHeader(), ERIC_ACCESS_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(basketPaymentRequest)))
                .andExpect(status().isNoContent());

        timestamps.end();

        checkPatchHasNotUpdated(checkout.getId(), PaymentStatus.CANCELLED);
    }

    @Test
    @DisplayName("Patch payment-details endpoint success path for no-funds payments session")
    void patchBasketPaymentDetailsSuccessNoFunds() throws Exception {
        final LocalDateTime start = timestamps.start();

        BasketPaymentRequestDTO basketPaymentRequest = createBasketPaymentRequest(PaymentStatus.NO_FUNDS);
        createBasket(start);
        Checkout checkout = createCheckout();

        mockMvc.perform(patch("/basket/checkouts/" + checkout.getId() + "/payment")
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_API_KEY_TYPE_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                .header(ERIC_AUTHORISED_KEY_ROLES, INTERNAL_USER_ROLE)
                .header(ApiSdkManager.getEricPassthroughTokenHeader(), ERIC_ACCESS_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(basketPaymentRequest)))
                .andExpect(status().isNoContent());

        timestamps.end();

        checkPatchHasNotUpdated(checkout.getId(), PaymentStatus.NO_FUNDS);
    }

    @Test
    @DisplayName("Patch payment-details endpoint fails if it doesn't return payment session")
    void patchBasketPaymentDetailsFailureReturningPaymentSession() throws Exception {
        final LocalDateTime start = timestamps.start();

        BasketPaymentRequestDTO basketPaymentRequest = createBasketPaymentRequest(PaymentStatus.PAID);
        createBasket(start);
        Checkout checkout = createCheckout();

        when(apiClientService.getPaymentSummary(ERIC_ACCESS_TOKEN, PAYMENT_ID)).thenThrow(new IOException());

        mockMvc.perform(patch("/basket/checkouts/" + checkout.getId() + "/payment")
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_API_KEY_TYPE_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                .header(ERIC_AUTHORISED_KEY_ROLES, INTERNAL_USER_ROLE)
                .header(ApiSdkManager.getEricPassthroughTokenHeader(), ERIC_ACCESS_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(basketPaymentRequest)))
                .andExpect(status().isNotFound());

        timestamps.end();

        checkPatchHasNotUpdated(checkout.getId(), PaymentStatus.PENDING);
    }

    @Test
    @DisplayName("Patch payment details endpoints fails if status on payments service is not paid")
    void patchBasketPaymentDetailsFailureCheckingPaymentStatus() throws Exception {
        final LocalDateTime start = timestamps.start();

        BasketPaymentRequestDTO basketPaymentRequest = createBasketPaymentRequest(PaymentStatus.PAID);
        createBasket(start);
        Checkout checkout = createCheckout();
        PaymentApi paymentSession = createPaymentSession(checkout.getId(), "pending", "70.00");

        when(apiClientService.getPaymentSummary(ERIC_ACCESS_TOKEN, PAYMENT_ID)).thenReturn(paymentSession);

        mockMvc.perform(patch("/basket/checkouts/" + checkout.getId() + "/payment")
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_API_KEY_TYPE_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                .header(ERIC_AUTHORISED_KEY_ROLES, INTERNAL_USER_ROLE)
                .header(ApiSdkManager.getEricPassthroughTokenHeader(), ERIC_ACCESS_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(basketPaymentRequest)))
                .andExpect(status().isBadRequest());

        timestamps.end();

        checkPatchHasNotUpdated(checkout.getId(), PaymentStatus.PENDING);
    }

    @Test
    @DisplayName("Patch payment details endpoints fails if payment total on payments service is different")
    void patchBasketPaymentDetailsFailureCheckingPaymentTotal() throws Exception {
        final LocalDateTime start = timestamps.start();

        BasketPaymentRequestDTO basketPaymentRequest = createBasketPaymentRequest(PaymentStatus.PAID);
        createBasket(start);
        Checkout checkout = createCheckout();
        PaymentApi paymentSession = createPaymentSession(checkout.getId(), "paid", "70.50");

        when(apiClientService.getPaymentSummary(ERIC_ACCESS_TOKEN, PAYMENT_ID)).thenReturn(paymentSession);

        mockMvc.perform(patch("/basket/checkouts/" + checkout.getId() + "/payment")
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_API_KEY_TYPE_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                .header(ERIC_AUTHORISED_KEY_ROLES, INTERNAL_USER_ROLE)
                .header(ApiSdkManager.getEricPassthroughTokenHeader(), ERIC_ACCESS_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(basketPaymentRequest)))
                .andExpect(status().isBadRequest());

        timestamps.end();

        checkPatchHasNotUpdated(checkout.getId(), PaymentStatus.PENDING);
    }

    @Test
    @DisplayName("Patch payment details endpoints fails if checkout resource is different to the one on payments service")
    void patchBasketPaymentDetailsFailureCheckingResourceUpdated() throws Exception {
        final LocalDateTime start = timestamps.start();

        BasketPaymentRequestDTO basketPaymentRequest = createBasketPaymentRequest(PaymentStatus.PAID);
        createBasket(start);
        Checkout checkout = createCheckout();
        PaymentApi paymentSession = createPaymentSession("invalid", "paid", "70.00");

        when(apiClientService.getPaymentSummary(ERIC_ACCESS_TOKEN, PAYMENT_ID)).thenReturn(paymentSession);

        mockMvc.perform(patch("/basket/checkouts/" + checkout.getId() + "/payment")
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_API_KEY_TYPE_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                .header(ERIC_AUTHORISED_KEY_ROLES, INTERNAL_USER_ROLE)
                .header(ApiSdkManager.getEricPassthroughTokenHeader(), ERIC_ACCESS_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(basketPaymentRequest)))
                .andExpect(status().isBadRequest());

        timestamps.end();

        checkPatchHasNotUpdated(checkout.getId(), PaymentStatus.PENDING);
    }

    @Test
    @DisplayName("Get payment details endpoint fails if the checkout id does not exist")
    void getPaymentDetailsReturnsNotFound() throws Exception {

        mockMvc.perform(get("/basket/checkouts/doesnotexist/payment")
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_OAUTH2_TYPE_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @DisplayName("Get payment details endpoint successfully gets payment details")
    void getPaymentDetailsSuccessfully() throws Exception {
        // When item(s) checked out
        final Checkout checkout = createCheckout();

        final PaymentDetailsDTO paymentDetailsDTO = createPaymentDetailsDTO(PaymentStatus.PENDING);
        final PaymentLinksDTO paymentLinksDTO = createPaymentLinksDTO(checkout.getId());

        // Then expect payment details
        mockMvc.perform(get("/basket/checkouts/" + checkout.getId() + "/payment")
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_OAUTH2_TYPE_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(paymentDetailsDTO)))
                .andExpect(jsonPath("$.payment_reference", is(checkout.getId())))
                .andExpect(jsonPath("$.kind", is("payment-details#payment-details")))
                .andExpect(jsonPath("$.status", is("pending")))
                .andExpect(jsonPath("$.links.self", is(mapper.convertValue(paymentLinksDTO.getSelf(), String.class))))
                .andExpect(jsonPath("$.links.resource", is(mapper.convertValue(paymentLinksDTO.getResource(), String.class))))
                .andDo(MockMvcResultHandlers.print());

    }

    @Test
    @DisplayName("Get payment details endpoint successfully gets paid payment details for reconciliation")
    void getsPaidPaymentDetailsSuccessfully() throws Exception {

        // Given item(s) checked out and paid for
        final Checkout checkout = createCheckout();
        payForOrder(checkout);

        final PaymentDetailsDTO paymentDetailsDTO = createPaymentDetailsDTO(PaymentStatus.PAID);
        final PaymentLinksDTO paymentLinksDTO = createPaymentLinksDTO(checkout.getId());

        // Then expect payment details
        mockMvc.perform(get("/basket/checkouts/" + checkout.getId() + "/payment")
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_OAUTH2_TYPE_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(paymentDetailsDTO)))
                .andExpect(jsonPath("$.payment_reference", is(checkout.getId())))
                .andExpect(jsonPath("$.kind", is("payment-details#payment-details")))
                .andExpect(jsonPath("$.status", is("paid")))
                .andExpect(jsonPath("$.company_number", is(COMPANY_NUMBER)))
                .andExpect(jsonPath("$.paid_at", is(paymentsApiParsableDateTime(PAID_AT_DATE))))
                .andExpect(jsonPath("$.links.self", is(mapper.convertValue(paymentLinksDTO.getSelf(), String.class))))
                .andExpect(jsonPath("$.links.resource", is(mapper.convertValue(paymentLinksDTO.getResource(), String.class))))
                .andDo(MockMvcResultHandlers.print());

    }

    /**
     * Verifies that the certificate item's options are of the right type and have the expected field
     * correctly populated.
     * @param certificate the {@link Item} to check
     */
    private void verifyCertificateItemOptionsAreCorrect(final Item certificate) {
        assertThat(certificate.getItemOptions() instanceof CertificateItemOptions, is(true));
        final CertificateItemOptions options = (CertificateItemOptions) certificate.getItemOptions();
        assertThat(options.getCertificateType(), is(INCORPORATION_WITH_ALL_NAME_CHANGES));
    }

    private List<ItemCosts> createItemCosts(){
        List<ItemCosts> itemCosts = new ArrayList<>();
        ItemCosts itemCosts1 = new ItemCosts();
        itemCosts1.setDiscountApplied(DISCOUNT_APPLIED_1);
        itemCosts1.setItemCost(ITEM_COST_1);
        itemCosts1.setCalculatedCost(CALCULATED_COST_1);
        itemCosts.add(itemCosts1);
        ItemCosts itemCosts2 = new ItemCosts();
        itemCosts2.setDiscountApplied(DISCOUNT_APPLIED_2);
        itemCosts2.setItemCost(ITEM_COST_2);
        itemCosts2.setCalculatedCost(CALCULATED_COST_2);
        itemCosts.add(itemCosts2);
        ItemCosts itemCosts3 = new ItemCosts();
        itemCosts3.setDiscountApplied(DISCOUNT_APPLIED_3);
        itemCosts3.setItemCost(ITEM_COST_3);
        itemCosts3.setCalculatedCost(CALCULATED_COST_3);
        itemCosts.add(itemCosts3);

        return itemCosts;
    }

    private BasketPaymentRequestDTO createBasketPaymentRequest(PaymentStatus paymentStatus) {
        final BasketPaymentRequestDTO basketPaymentRequestDTO = new BasketPaymentRequestDTO();
        basketPaymentRequestDTO.setPaidAt(LocalDateTime.now());
        basketPaymentRequestDTO.setPaymentReference(PAYMENT_ID);
        basketPaymentRequestDTO.setStatus(paymentStatus);

        return basketPaymentRequestDTO;
    }

    private Checkout createCheckout() {
        final Item item = new Item();
        item.setItemCosts(ITEM_COSTS);
        item.setPostageCost(POSTAGE_COST);
        item.setTotalItemCost(TOTAL_ITEM_COST);
        item.setCompanyNumber(COMPANY_NUMBER);
        item.setKind(CERTIFICATE_KIND);
        final CertificateItemOptions options = new CertificateItemOptions();
        options.setCertificateType(INCORPORATION_WITH_ALL_NAME_CHANGES);
        item.setItemOptions(options);

        return checkoutService.createCheckout(item, ERIC_IDENTITY_VALUE, ERIC_AUTHORISED_USER_VALUE, new DeliveryDetails());
    }

    /**
     * Verifies that the basket is as it was when created by {@link #createBasket(LocalDateTime, String)}.
     * @param basketCreationTime the time the basket was created
     * @param deliveryDetailsAdded the delivery details added to the basket
     * @param itemUri the URI of the item stored in the basket
     */
    private void verifyBasketIsUnchanged(final LocalDateTime basketCreationTime,
                                         final DeliveryDetails deliveryDetailsAdded,
                                         final String itemUri) {
        final Optional<Basket> retrievedBasket = basketRepository.findById(ERIC_IDENTITY_VALUE);
        assertThat(retrievedBasket.isPresent(), is(true));
        final Basket basket = retrievedBasket.get();
        assertThat(basket.getCreatedAt(), is(basketCreationTime));
        assertThat(basket.getUpdatedAt(), is(basketCreationTime));
        assertThat(basket.getId(), is(ERIC_IDENTITY_VALUE));
        assertThat(basket.getItems().size(), is(1));
        assertThat(basket.getItems().get(0).getItemUri(), is(itemUri));
        org.assertj.core.api.Assertions.assertThat(basket.getData().getDeliveryDetails())
                .isEqualToComparingFieldByField(deliveryDetailsAdded);
    }

    /**
     * Creates a basket containing an item with just its item URI member populated, and some delivery details.
     * In this way, it creates a basket in the database that is similar to what results when
     * {@link BasketController#addItemToBasket(AddToBasketRequestDTO, HttpServletRequest, String)} and
     * {@link BasketController#addDeliveryDetailsToBasket(AddDeliveryDetailsRequestDTO, HttpServletRequest, String)}
     * have been called.
     * @param start the creation/update time of the basket
     * @return the {@link Basket} as persisted in the database
     */
    private Basket createBasket(final LocalDateTime start) {
        return createBasket(start, VALID_CERTIFICATE_URI);
    }

    /**
     * Creates a basket containing an item with just its item URI member populated, and some delivery details.
     * In this way, it creates a basket in the database that is similar to what results when
     * {@link BasketController#addItemToBasket(AddToBasketRequestDTO, HttpServletRequest, String)} and
     * {@link BasketController#addDeliveryDetailsToBasket(AddDeliveryDetailsRequestDTO, HttpServletRequest, String)}
     * have been called.
     * @param start the creation/update time of the basket
     * @param itemUri the URI of the item stored in the basket
     * @return the {@link Basket} as persisted in the database
     */
    private Basket createBasket(final LocalDateTime start, final String itemUri) {
        final Basket basket = new Basket();
        basket.setCreatedAt(start);
        basket.setUpdatedAt(start);
        basket.setId(ERIC_IDENTITY_VALUE);
        Item basketItem = new Item();
        basketItem.setItemUri(itemUri);
        basket.getData().getItems().add(basketItem);

        DeliveryDetails deliveryDetails = new DeliveryDetails();
        deliveryDetails.setAddressLine1(ADDRESS_LINE_1);
        deliveryDetails.setAddressLine2(ADDRESS_LINE_2);
        deliveryDetails.setCountry(COUNTRY);
        deliveryDetails.setForename(FORENAME);
        deliveryDetails.setSurname(SURNAME);
        deliveryDetails.setLocality(LOCALITY);

        basket.getData().setDeliveryDetails(deliveryDetails);

        return basketRepository.save(basket);
    }

    private PaymentApi createPaymentSession(String checkoutId, String status, String total) {
        final PaymentApi paymentSummary = new PaymentApi();
        paymentSummary.setStatus(status);
        paymentSummary.setAmount(total);
        paymentSummary.setLinks(new HashMap<String, String>() {{
            put("resource", "/basket/checkouts/" + checkoutId + "/payment");
        }});

        return paymentSummary;
    }

    /**
     * Verifies that the order assumed to have been created by a PAID patch payment details request can be retrieved
     * from the database using its expected ID value.
     * @param expectedOrderId the expected ID of the newly created order
     * @param timestamps the timestamp verifier
     * @return the order retrieved from the database for further assertions
     */
    private Order assertOrderCreatedCorrectly(final String expectedOrderId,
                                              final TimestampedEntityVerifier timestamps) {
        final Optional<Order> retrievedOrder = orderRepository.findById(expectedOrderId);
        assertThat(retrievedOrder.isPresent(), is(true));
        final Order order = retrievedOrder.get();
        assertThat(order.getId(), is(expectedOrderId));
        timestamps.verifyCreationTimestampsWithinExecutionInterval(order);
        return order;
    }

    /**
     * Verifies that the patch request has not updated the basket, checkout, or created an order.
     * @param checkoutId the ID for the checkout that is attempted to be updated
     */
    private void checkPatchHasNotUpdated(final String checkoutId, final PaymentStatus paymentStatus) {
        // Check basket has not been emptied
        final Optional<Basket> retrievedBasket = basketRepository.findById(ERIC_IDENTITY_VALUE);
        timestamps.verifyUpdatedAtTimestampWithinExecutionInterval(retrievedBasket.get());
        assertFalse(retrievedBasket.get().getData().getItems().isEmpty());

        // Check checkout has not been updated
        final Optional<Checkout> retrievedCheckout = checkoutRepository.findById(checkoutId);
        timestamps.verifyUpdatedAtTimestampWithinExecutionInterval(retrievedCheckout.get());
        assertThat(retrievedCheckout.isPresent(), is(true));
        assertThat(retrievedCheckout.get().getData(), is(notNullValue()));
        assertThat(retrievedCheckout.get().getData().getStatus(), is(paymentStatus));

        // Assert order has not been created
        assertEquals(0, orderRepository.count());
    }

    private PaymentLinksDTO createPaymentLinksDTO(String checkoutId) {
        PaymentLinksDTO paymentLinksDTO = new PaymentLinksDTO();
        paymentLinksDTO.setSelf("/basket/checkouts/" + checkoutId + "/payment");
        paymentLinksDTO.setResource("/basket/checkouts/" + checkoutId);

        return paymentLinksDTO;
    }

    private PaymentDetailsDTO createPaymentDetailsDTO(final PaymentStatus status) {
        PaymentDetailsDTO paymentDetailsDTO = new PaymentDetailsDTO();
        paymentDetailsDTO.setStatus(status);
        paymentDetailsDTO.setKind(PAYMENT_KIND);
        List<ItemDTO> itemDTOs = new ArrayList<>();
        ItemDTO itemDTO1 = new ItemDTO();
        itemDTO1.setAmount("50");
        itemDTO1.setAvailablePaymentMethods(Collections.singletonList("credit-card"));
        itemDTO1.setClassOfPayment(Collections.singletonList("orderable-item"));
        itemDTO1.setKind("cost#cost");
        itemDTO1.setProductType("certificate-same-day");
        itemDTOs.add(itemDTO1);
        ItemDTO itemDTO2 = new ItemDTO();
        itemDTO2.setAmount("10");
        itemDTO2.setAvailablePaymentMethods(Collections.singletonList("credit-card"));
        itemDTO2.setClassOfPayment(Collections.singletonList("orderable-item"));
        itemDTO2.setKind("cost#cost");
        itemDTO2.setProductType("certificate-additional-copy");
        itemDTOs.add(itemDTO2);
        ItemDTO itemDTO3 = new ItemDTO();
        itemDTO3.setAmount("10");
        itemDTO3.setAvailablePaymentMethods(Collections.singletonList("credit-card"));
        itemDTO3.setClassOfPayment(Collections.singletonList("orderable-item"));
        itemDTO3.setKind("cost#cost");
        itemDTO3.setProductType("certificate-additional-copy");
        itemDTOs.add(itemDTO3);
        paymentDetailsDTO.setItems(itemDTOs);

        paymentDetailsDTO.setCompanyNumber(COMPANY_NUMBER);

        return paymentDetailsDTO;
    }

    /**
     * Emulates part of the impact of successful payment on the checkout state.
     * @param checkout the checkout to be updated to reflect successful payment
     */
    private void payForOrder(final Checkout checkout) {
        final CheckoutData data = checkout.getData();
        data.setStatus(PaymentStatus.PAID);
        data.setPaidAt(PAID_AT_DATE);
        checkoutService.saveCheckout(checkout);
    }

    /**
     * Renders the date/time as a String formatted in a way that should be parsable by the Payments API.
     * @param dateTime the date/time to be rendered
     * @return the date/time as a String
     */
    private String paymentsApiParsableDateTime(final LocalDateTime dateTime) {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").format(dateTime);
    }
}

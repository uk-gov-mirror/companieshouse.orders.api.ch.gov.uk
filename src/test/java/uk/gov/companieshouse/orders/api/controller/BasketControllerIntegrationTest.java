package uk.gov.companieshouse.orders.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import uk.gov.companieshouse.orders.api.dto.*;
import uk.gov.companieshouse.orders.api.model.*;
import uk.gov.companieshouse.orders.api.repository.BasketRepository;
import uk.gov.companieshouse.orders.api.repository.CheckoutRepository;
import uk.gov.companieshouse.orders.api.repository.OrderRepository;
import uk.gov.companieshouse.orders.api.service.ApiClientService;
import uk.gov.companieshouse.orders.api.service.CheckoutService;
import uk.gov.companieshouse.orders.api.util.ResultCaptor;

import java.time.LocalDateTime;
import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static uk.gov.companieshouse.orders.api.model.ProductType.CERTIFICATE_ADDITIONAL_COPY;
import static uk.gov.companieshouse.orders.api.model.ProductType.CERTIFICATE_SAME_DAY;
import static uk.gov.companieshouse.orders.api.util.TestConstants.*;

@AutoConfigureMockMvc
@SpringBootTest
@EmbeddedKafka
class BasketControllerIntegrationTest {

    private static final String ITEM_URI = "/orderable/certificates/12345678";
    private static final String ITEM_URI_OLD = "/orderable/certificates/11111111";

    private static final String COMPANY_NUMBER = "00006400";
    private static final String ADDRESS_LINE_1 = "address line 1";
    private static final String ADDRESS_LINE_2 = "address line 2";
    private static final String COUNTRY = "country";
    private static final String FORENAME = "forename";
    private static final String LOCALITY = "locality";
    private static final String PO_BOX = "po box";
    private static final String POSTAL_CODE = "postal code";
    private static final String PREMISES = "premises";
    private static final String REGION = "region";
    private static final String SURNAME = "surname";
    private static final String CHECKOUT_ID = "1234";
    private static final String UNKNOWN_CHECKOUT_ID = "5555";

    private static final List<ItemCosts> ITEM_COSTS =
             asList(new ItemCosts( "0", "50", "50", CERTIFICATE_SAME_DAY),
                    new ItemCosts("40", "50", "10", CERTIFICATE_ADDITIONAL_COPY),
                    new ItemCosts("40", "50", "10", CERTIFICATE_ADDITIONAL_COPY));
    private static final String POSTAGE_COST = "0";
    private static final String TOTAL_ITEM_COST = "70";
    static final String PAYMENT_KIND = "payment-details#payment-details";

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

    @AfterEach
    void tearDown() {
        basketRepository.findById(ERIC_IDENTITY_VALUE).ifPresent(basketRepository::delete);
        checkoutRepository.deleteAll();
        orderRepository.deleteAll();
    }

    @Test
    @DisplayName("Add Item successfully adds an item to the basket, if the basket does not exist")
    public void addItemSuccessfullyAddsItemToBasketIfBasketDoesNotExist() throws Exception {
        AddToBasketRequestDTO addToBasketRequestDTO = new AddToBasketRequestDTO();
        addToBasketRequestDTO.setItemUri(ITEM_URI);

        mockMvc.perform(post("/basket/items")
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_OAUTH2_TYPE_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(addToBasketRequestDTO)))
                .andExpect(status().isOk());

        final Optional<Basket> retrievedBasket = basketRepository.findById(ERIC_IDENTITY_VALUE);
        assertEquals(ITEM_URI, retrievedBasket.get().getData().getItems().get(0).getItemUri());
        assertEquals(1, retrievedBasket.get().getData().getItems().size());
    }

    @Test
    @DisplayName("Add item successfully adds an item to the basket, if the basket exists")
    public void addItemSuccessfullyAddsAnItemToBasketIfBasketAlreadyExists() throws Exception {
        Basket basket = new Basket();
        basketRepository.save(basket);

        AddToBasketRequestDTO addToBasketRequestDTO = new AddToBasketRequestDTO();
        addToBasketRequestDTO.setItemUri(ITEM_URI);

        mockMvc.perform(post("/basket/items")
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_OAUTH2_TYPE_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(addToBasketRequestDTO)))
                .andExpect(status().isOk());

        final Optional<Basket> retrievedBasket = basketRepository.findById(ERIC_IDENTITY_VALUE);
        assertEquals(ITEM_URI, retrievedBasket.get().getData().getItems().get(0).getItemUri());

    }

    @Test
    @DisplayName("Add item successfully replaces an item in the basket")
    public void addItemSuccessfullyReplacesAnItemInTheBasket() throws Exception {
        BasketItem item = new BasketItem();
        item.setItemUri(ITEM_URI_OLD);
        BasketData basketData = new BasketData();
        basketData.setItems(Arrays.asList(item));
        Basket basket = new Basket();
        basket.setData(basketData);
        basketRepository.save(basket);

        AddToBasketRequestDTO addToBasketRequestDTO = new AddToBasketRequestDTO();
        addToBasketRequestDTO.setItemUri(ITEM_URI);

        mockMvc.perform(post("/basket/items")
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_OAUTH2_TYPE_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(addToBasketRequestDTO)))
                .andExpect(status().isOk());

        final Optional<Basket> retrievedBasket = basketRepository.findById(ERIC_IDENTITY_VALUE);
        assertEquals(ITEM_URI, retrievedBasket.get().getData().getItems().get(0).getItemUri());

    }

    @Test
    @DisplayName("Add item fails to add item to basket that fails validation")
    public void addItemFailsToAddItemToBasketIfFailsValidation() throws Exception {
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
    public void checkoutBasketSuccessfullyCreatesCheckoutWhenBasketIsValid() throws Exception {
        Basket basket = new Basket();
        basket.setId(ERIC_IDENTITY_VALUE);
        BasketItem basketItem = new BasketItem();
        basketItem.setItemUri(ITEM_URI);
        basket.getData().getItems().add(basketItem);
        basketRepository.save(basket);

        Certificate certificate = new Certificate();
        certificate.setCompanyNumber(COMPANY_NUMBER);
        final CertificateItemOptions options = new CertificateItemOptions();
        options.setForename(FORENAME);
        options.setSurname(SURNAME);
        certificate.setItemOptions(options);
        when(apiClientService.getItem(ITEM_URI)).thenReturn(certificate);

        ResultCaptor<Checkout> resultCaptor = new ResultCaptor<>();
        doAnswer(resultCaptor).when(checkoutService).createCheckout(any(Certificate.class), any(String.class), any(String.class));

        mockMvc.perform(post("/basket/checkouts")
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_OAUTH2_TYPE_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                .header(ERIC_AUTHORISED_USER_HEADER_NAME, ERIC_AUTHORISED_USER_VALUE))
                .andExpect(status().isOk());

        final Optional<Checkout> retrievedCheckout = checkoutRepository.findById(resultCaptor.getResult().getId());
        assertTrue(retrievedCheckout.isPresent());
        assertEquals(ERIC_IDENTITY_VALUE, retrievedCheckout.get().getUserId());
        final Item item = retrievedCheckout.get().getData().getItems().get(0);
        assertEquals(COMPANY_NUMBER, item.getCompanyNumber());
        final CertificateItemOptions retrievedOptions = item.getItemOptions();
        assertEquals(FORENAME, retrievedOptions.getForename());
        assertEquals(SURNAME, retrievedOptions.getSurname());
    }

    @Test
    @DisplayName("Checkout basket fails to create checkout and returns 409 conflict, when basket is empty")
    public void checkoutBasketfFailsToCreateCheckoutIfBasketIsEmpty() throws Exception {
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
    public void checkoutBasketFailsToCreateCheckoutIfBasketDoesNotExist() throws Exception {

        mockMvc.perform(post("/basket/checkouts")
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_OAUTH2_TYPE_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE))
                .andExpect(status().isConflict());

        assertEquals(0, checkoutRepository.count());
    }

    @Test
    @DisplayName("Checkout Basket fails to create checkout and returns 400, when there is a failure getting the item")
    public void checkoutBasketFailsToCreateCheckoutWhenItFailsToGetAnItem() throws Exception {
        Basket basket = new Basket();
        basket.setId(ERIC_IDENTITY_VALUE);
        BasketItem basketItem = new BasketItem();
        basketItem.setItemUri(ITEM_URI);
        basket.getData().getItems().add(basketItem);
        basketRepository.save(basket);

        when(apiClientService.getItem(ITEM_URI)).thenThrow(new Exception());

        mockMvc.perform(post("/basket/checkouts")
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_OAUTH2_TYPE_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE))
                .andExpect(status().isBadRequest());

        assertEquals(0, checkoutRepository.count());
    }

    @Test
    @DisplayName("Check out basket returns 403 if body is present")
    public void checkoutBasketReturnsBadRequestIfBodyIsPresent() throws Exception {

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
    public void checkoutBasketCheckoutContainsCosts() throws Exception {
        final Basket basket = new Basket();
        basket.setId(ERIC_IDENTITY_VALUE);
        BasketItem basketItem = new BasketItem();
        basketItem.setItemUri(ITEM_URI);
        basket.getData().getItems().add(basketItem);
        basketRepository.save(basket);

        final Certificate certificate = new Certificate();
        certificate.setItemCosts(ITEM_COSTS);
        certificate.setPostageCost(POSTAGE_COST);
        certificate.setTotalItemCost(TOTAL_ITEM_COST);
        when(apiClientService.getItem(ITEM_URI)).thenReturn(certificate);

        final CheckoutData expectedResponseBody = new CheckoutData();
        final Item item = new Item();
        item.setItemCosts(ITEM_COSTS);
        item.setPostageCost(POSTAGE_COST);
        item.setTotalItemCost(TOTAL_ITEM_COST);
        expectedResponseBody.setItems(singletonList(item));

        final ResultCaptor<Checkout> resultCaptor = new ResultCaptor<>();
        doAnswer(resultCaptor)
                .when(checkoutService)
                .createCheckout(any(Certificate.class), any(String.class), any(String.class));

        mockMvc.perform(post("/basket/checkouts")
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_OAUTH2_TYPE_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                .header(ERIC_AUTHORISED_USER_HEADER_NAME, ERIC_AUTHORISED_USER_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(expectedResponseBody)));

        final Optional<Checkout> retrievedCheckout = checkoutRepository.findById(resultCaptor.getResult().getId());
        assertTrue(retrievedCheckout.isPresent());
        final Item retrievedItem = retrievedCheckout.get().getData().getItems().get(0);
        assertThat(retrievedItem.getItemCosts(), is(ITEM_COSTS));
        assertThat(retrievedItem.getPostageCost(), is(POSTAGE_COST));
        assertThat(retrievedItem.getTotalItemCost(), is(TOTAL_ITEM_COST));

    }

    @Test
    @DisplayName("Add delivery details to the basket, if the basket exists")
    public void addDeliveryDetailsToBasketIfTheBasketExists() throws Exception {
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
        deliveryDetailsDTO.setPremises(PREMISES);
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
        assertEquals(PREMISES, getDeliveryDetails.getPremises());
        assertEquals(REGION, getDeliveryDetails.getRegion());
        assertEquals(SURNAME, getDeliveryDetails.getSurname());
    }

    @Test
    @DisplayName("Add delivery details to the basket, if the basket does not exist")
    public void addDeliveryDetailsToBasketIfTheBasketDoesNotExist() throws Exception {

        AddDeliveryDetailsRequestDTO addDeliveryDetailsRequestDTO = new AddDeliveryDetailsRequestDTO();
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
        assertEquals(PREMISES, getDeliveryDetails.getPremises());
        assertEquals(REGION, getDeliveryDetails.getRegion());
        assertEquals(SURNAME, getDeliveryDetails.getSurname());
    }

    @Test
    @DisplayName("Add delivery details fails due to failed validation")
    public void addDeliveryDetailsFailsDueToFailedValidation() throws Exception {
        AddDeliveryDetailsRequestDTO addDeliveryDetailsRequestDTO = new AddDeliveryDetailsRequestDTO();
        DeliveryDetailsDTO deliveryDetailsDTO = new DeliveryDetailsDTO();
        deliveryDetailsDTO.setAddressLine1("");
        deliveryDetailsDTO.setAddressLine2(ADDRESS_LINE_2);
        deliveryDetailsDTO.setCountry(COUNTRY);
        deliveryDetailsDTO.setPremises(PREMISES);
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
    @DisplayName("Patch basket payment details returns OK")
    public void patchBasketPaymentDetailsReturnsOK() throws Exception {
        final Checkout checkout = new Checkout();
        checkout.setId(CHECKOUT_ID);
        checkoutRepository.save(checkout);

        BasketPaymentRequestDTO basketPaymentRequestDTO = new BasketPaymentRequestDTO();
        basketPaymentRequestDTO.setPaidAt("paid-at");
        basketPaymentRequestDTO.setPaymentReference("reference");
        basketPaymentRequestDTO.setStatus(PaymentStatus.PAID);

        mockMvc.perform(patch("/basket/checkouts/" + CHECKOUT_ID + "/payment")
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_API_KEY_TYPE_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(basketPaymentRequestDTO)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Patch basket payment details clears basket is status is paid")
    public void patchBasketPaymentDetailsClearsBasketStatusPaid() throws Exception {
        Basket basket = new Basket();
        basket.setId(ERIC_IDENTITY_VALUE);
        BasketItem basketItem = new BasketItem();
        basketItem.setItemUri(ITEM_URI);
        basket.getData().getItems().add(basketItem);
        basketRepository.save(basket);

        final Checkout checkout = new Checkout();
        checkout.setId(CHECKOUT_ID);
        checkoutRepository.save(checkout);

        BasketPaymentRequestDTO basketPaymentRequestDTO = new BasketPaymentRequestDTO();
        basketPaymentRequestDTO.setPaidAt("paid-at");
        basketPaymentRequestDTO.setPaymentReference("reference");
        basketPaymentRequestDTO.setStatus(PaymentStatus.PAID);

        mockMvc.perform(patch("/basket/checkouts/" + CHECKOUT_ID + "/payment")
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_API_KEY_TYPE_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(basketPaymentRequestDTO)))
                .andExpect(status().isOk());

        final Optional<Basket> retrievedBasket = basketRepository.findById(ERIC_IDENTITY_VALUE);
        assertTrue(retrievedBasket.get().getData().getItems().isEmpty());
    }

    @Test
    @DisplayName("Patch basket payment details does not clear basket is status is not paid")
    public void patchBasketPaymentDetailsDoesNotClearBasketStatusNotPaid() throws Exception {
        Basket basket = new Basket();
        basket.setId(ERIC_IDENTITY_VALUE);
        BasketItem basketItem = new BasketItem();
        basketItem.setItemUri(ITEM_URI);
        basket.getData().getItems().add(basketItem);
        basketRepository.save(basket);

        BasketPaymentRequestDTO basketPaymentRequestDTO = new BasketPaymentRequestDTO();
        basketPaymentRequestDTO.setPaidAt("paid-at");
        basketPaymentRequestDTO.setPaymentReference("reference");
        basketPaymentRequestDTO.setStatus(PaymentStatus.FAILED);

        mockMvc.perform(patch("/basket/checkouts/" + CHECKOUT_ID + "/payment")
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_API_KEY_TYPE_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(basketPaymentRequestDTO)))
                .andExpect(status().isOk());

        final Optional<Basket> retrievedBasket = basketRepository.findById(ERIC_IDENTITY_VALUE);
        assertFalse(retrievedBasket.get().getData().getItems().isEmpty());
    }

    @Test
    @DisplayName("Patch basket payment details updates updated_at field")
    public void patchBasketPaymentDetailsUpdatesUpdatedAt() throws Exception {
        final LocalDateTime intervalStart = LocalDateTime.now();

        Basket basket = new Basket();
        basket.setId(ERIC_IDENTITY_VALUE);
        basket.setCreatedAt(intervalStart);
        basket.setUpdatedAt(intervalStart);
        BasketItem basketItem = new BasketItem();
        basketItem.setItemUri(ITEM_URI);
        basket.getData().getItems().add(basketItem);
        basketRepository.save(basket);

        final Checkout checkout = new Checkout();
        checkout.setId(CHECKOUT_ID);
        checkoutRepository.save(checkout);

        BasketPaymentRequestDTO basketPaymentRequestDTO = new BasketPaymentRequestDTO();
        basketPaymentRequestDTO.setPaidAt("paid-at");
        basketPaymentRequestDTO.setPaymentReference("reference");
        basketPaymentRequestDTO.setStatus(PaymentStatus.PAID);

        mockMvc.perform(patch("/basket/checkouts/" + CHECKOUT_ID + "/payment")
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_API_KEY_TYPE_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(basketPaymentRequestDTO)))
                .andExpect(status().isOk());

        final LocalDateTime intervalEnd = LocalDateTime.now();

        final Optional<Basket> retrievedBasket = basketRepository.findById(ERIC_IDENTITY_VALUE);
        verifyUpdatedAtTimestampWithinExecutionInterval(retrievedBasket.get(), intervalStart, intervalEnd);
    }

    @DisplayName("PAID patch basket payment request creates order")
    public void paidPatchBasketPaymentDetailsCreatesOrder() throws Exception {
        final Checkout checkout = new Checkout();
        checkout.setId(CHECKOUT_ID);
        checkoutRepository.save(checkout);

        BasketPaymentRequestDTO basketPaymentRequestDTO = new BasketPaymentRequestDTO();
        basketPaymentRequestDTO.setPaidAt("paid-at");
        basketPaymentRequestDTO.setPaymentReference("reference");
        basketPaymentRequestDTO.setStatus(PaymentStatus.PAID);

        final LocalDateTime intervalStart = LocalDateTime.now();

        mockMvc.perform(patch("/basket/checkouts/" + CHECKOUT_ID + "/payment")
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(basketPaymentRequestDTO)))
                .andExpect(status().isOk());

        final LocalDateTime intervalEnd = LocalDateTime.now();

        assertOrderCreatedCorrectly(CHECKOUT_ID, intervalStart, intervalEnd);
    }

    @Test
    @DisplayName("PAID patch basket payment request creates order with costs from the checkout")
    public void paidPatchBasketPaymentDetailsOrderContainsCosts() throws Exception {
        final Checkout checkout = new Checkout();
        checkout.setId(CHECKOUT_ID);
        final Item item = new Item();
        item.setItemCosts(ITEM_COSTS);
        item.setPostageCost(POSTAGE_COST);
        item.setTotalItemCost(TOTAL_ITEM_COST);
        checkout.getData().setItems(singletonList(item));
        checkoutRepository.save(checkout);

        BasketPaymentRequestDTO basketPaymentRequestDTO = new BasketPaymentRequestDTO();
        basketPaymentRequestDTO.setPaidAt("paid-at");
        basketPaymentRequestDTO.setPaymentReference("reference");
        basketPaymentRequestDTO.setStatus(PaymentStatus.PAID);

        final LocalDateTime intervalStart = LocalDateTime.now();

        mockMvc.perform(patch("/basket/checkouts/" + CHECKOUT_ID + "/payment")
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_API_KEY_TYPE_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(basketPaymentRequestDTO)))
                .andExpect(status().isOk());

        final LocalDateTime intervalEnd = LocalDateTime.now();

        final Order orderRetrieved = assertOrderCreatedCorrectly(CHECKOUT_ID, intervalStart, intervalEnd);
        final Item retrievedItem = orderRetrieved.getData().getItems().get(0);
        assertThat(retrievedItem.getItemCosts(), is(ITEM_COSTS));
        assertThat(retrievedItem.getPostageCost(), is(POSTAGE_COST));
        assertThat(retrievedItem.getTotalItemCost(), is(TOTAL_ITEM_COST));
    }

    @Test
    @DisplayName("FAILED patch basket payment request does not create an order")
    public void failedPatchBasketPaymentDetailsCreatesNoOrder() throws Exception {
        final Checkout checkout = new Checkout();
        checkout.setId(CHECKOUT_ID);
        checkoutRepository.save(checkout);

        BasketPaymentRequestDTO basketPaymentRequestDTO = new BasketPaymentRequestDTO();
        basketPaymentRequestDTO.setPaidAt("paid-at");
        basketPaymentRequestDTO.setPaymentReference("reference");
        basketPaymentRequestDTO.setStatus(PaymentStatus.FAILED);

        mockMvc.perform(patch("/basket/checkouts/" + CHECKOUT_ID + "/payment")
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_API_KEY_TYPE_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(basketPaymentRequestDTO)))
                .andExpect(status().isOk());

        assertOrderNotCreated(CHECKOUT_ID);
    }

    @Test
    @DisplayName("PAID patch basket payment request does not create order for unknown checkout ID")
    public void paidPatchBasketPaymentDetailsDoesNotCreateUnknownOrder() throws Exception {
        final Checkout checkout = new Checkout();
        checkout.setId(CHECKOUT_ID);
        checkoutRepository.save(checkout);

        BasketPaymentRequestDTO basketPaymentRequestDTO = new BasketPaymentRequestDTO();
        basketPaymentRequestDTO.setPaidAt("paid-at");
        basketPaymentRequestDTO.setPaymentReference("reference");
        basketPaymentRequestDTO.setStatus(PaymentStatus.PAID);

        mockMvc.perform(patch("/basket/checkouts/" + UNKNOWN_CHECKOUT_ID + "/payment")
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_API_KEY_TYPE_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(basketPaymentRequestDTO)))
                .andExpect(status().isNotFound());

        assertOrderNotCreated(CHECKOUT_ID);
        assertOrderNotCreated(UNKNOWN_CHECKOUT_ID);
    }

    @Test
    @DisplayName("Duplicate PAID patch basket payment request does NOT recreate order")
    public void duplicatePaidPatchBasketPaymentDetailsDoesNotRecreateOrder() throws Exception {
        final Checkout checkout = new Checkout();
        checkout.setId(CHECKOUT_ID);
        checkoutRepository.save(checkout);

        final LocalDateTime preexistingOrderCreationTime = LocalDateTime.now();
        final Order preexistingOrder = new Order();
        preexistingOrder.setId(CHECKOUT_ID);
        preexistingOrder.setCreatedAt(preexistingOrderCreationTime);
        preexistingOrder.setUpdatedAt(preexistingOrderCreationTime);
        orderRepository.save(preexistingOrder);

        BasketPaymentRequestDTO basketPaymentRequestDTO = new BasketPaymentRequestDTO();
        basketPaymentRequestDTO.setPaidAt("paid-at");
        basketPaymentRequestDTO.setPaymentReference("reference");
        basketPaymentRequestDTO.setStatus(PaymentStatus.PAID);

        mockMvc.perform(patch("/basket/checkouts/" + CHECKOUT_ID + "/payment")
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_API_KEY_TYPE_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(basketPaymentRequestDTO)))
                .andExpect(status().isForbidden());

        assertOrderCreatedCorrectly(CHECKOUT_ID, preexistingOrderCreationTime, preexistingOrderCreationTime);
    }

    @Test
    @DisplayName("Return not found when payment details do not exist")
    void getPaymentDetailsReturnsNotFound() throws Exception {
        // When and then
        mockMvc.perform(get("/basket/checkouts/doesnotexist/payment")
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_OAUTH2_TYPE_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @DisplayName("Successfully gets payment details")
    void getPaymentDetailsSuccessfully() throws Exception {
        // When item(s) checked out
        Checkout checkout = createCheckout();

        PaymentDetailsDTO paymentDetailsDTO = createPaymentDetailsDTO();
        PaymentLinksDTO paymentLinksDTO = createPaymentLinksDTO(checkout.getId());

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

    private PaymentLinksDTO createPaymentLinksDTO(String checkoutId) {
        PaymentLinksDTO paymentLinksDTO = new PaymentLinksDTO();
        paymentLinksDTO.setSelf("/basket/checkouts/" + checkoutId + "/payment");
        paymentLinksDTO.setResource("/basket/checkouts/" + checkoutId);

        return paymentLinksDTO;
    }

    private PaymentDetailsDTO createPaymentDetailsDTO(){
        PaymentDetailsDTO paymentDetailsDTO = new PaymentDetailsDTO();
        paymentDetailsDTO.setStatus(PaymentStatus.PENDING);
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

        return paymentDetailsDTO;
    }

    private Checkout createCheckout() {
        final Item item = new Item();
        item.setItemCosts(ITEM_COSTS);
        item.setPostageCost(POSTAGE_COST);
        item.setTotalItemCost(TOTAL_ITEM_COST);

        return checkoutService.createCheckout(item, ERIC_IDENTITY_VALUE, ERIC_AUTHORISED_USER_VALUE);
    }

    private void verifyUpdatedAtTimestampWithinExecutionInterval(final Basket itemUpdated,
                                                                 final LocalDateTime intervalStart,
                                                                 final LocalDateTime intervalEnd) {

        assertThat(itemUpdated.getUpdatedAt().isAfter(itemUpdated.getCreatedAt()) ||
                itemUpdated.getUpdatedAt().isEqual(itemUpdated.getCreatedAt()), is(true));

        assertThat(itemUpdated.getUpdatedAt().isAfter(intervalStart) ||
                itemUpdated.getUpdatedAt().isEqual(intervalStart), is(true));
        assertThat(itemUpdated.getUpdatedAt().isBefore(intervalEnd) ||
                itemUpdated.getUpdatedAt().isEqual(intervalEnd), is(true));
    }

    /**
     * Verifies that the order created at and updated at timestamps are within the expected interval
     * for item creation.
     * @param orderCreated the order created
     * @param intervalStart roughly the start of the test
     * @param intervalEnd roughly the end of the test
     */
    private void verifyCreationTimestampsWithinExecutionInterval(final Order orderCreated,
                                                                 final LocalDateTime intervalStart,
                                                                 final LocalDateTime intervalEnd) {
        assertThat(orderCreated.getCreatedAt().isAfter(intervalStart) ||
                orderCreated.getCreatedAt().isEqual(intervalStart), is(true));
        assertThat(orderCreated.getCreatedAt().isBefore(intervalEnd) ||
                orderCreated.getCreatedAt().isEqual(intervalEnd), is(true));
        assertThat(orderCreated.getUpdatedAt().isAfter(intervalStart) ||
                orderCreated.getUpdatedAt().isEqual(intervalStart), is(true));
        assertThat(orderCreated.getUpdatedAt().isBefore(intervalEnd) ||
                orderCreated.getUpdatedAt().isEqual(intervalEnd), is(true));
    }

    /**
     * Verifies that the order assumed to have been created by a PAID patch payment details request can be retrieved
     * from the database using its expected ID value.
     * @param expectedOrderId the expected ID of the newly created order
     * @param intervalStart roughly the start of the test
     * @param intervalEnd roughly the end of the test
     * @return the order retrieved from the database for further assertions
     */
    private Order assertOrderCreatedCorrectly(final String expectedOrderId,
                                              final LocalDateTime intervalStart,
                                              final LocalDateTime intervalEnd) {
        final Optional<Order> retrievedOrder = orderRepository.findById(expectedOrderId);
        assertThat(retrievedOrder.isPresent(), is(true));
        final Order order = retrievedOrder.get();
        assertThat(order.getId(), is(expectedOrderId));
        verifyCreationTimestampsWithinExecutionInterval(order, intervalStart, intervalEnd);
        return order;
    }

    /**
     * Verifies that the item that could have been created by the create item POST request cannot in fact be retrieved
     * from the database.
     * @param expectedOrderId the expected ID of the newly created order
     */
    private void assertOrderNotCreated(final String expectedOrderId) {
        final Optional<Order> retrievedOrder = orderRepository.findById(expectedOrderId);
        assertThat(retrievedOrder.isPresent(), is(false));
    }

}

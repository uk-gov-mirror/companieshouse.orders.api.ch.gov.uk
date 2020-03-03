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
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.companieshouse.orders.api.dto.AddDeliveryDetailsRequestDTO;
import uk.gov.companieshouse.orders.api.dto.AddToBasketRequestDTO;
import uk.gov.companieshouse.orders.api.dto.BasketPaymentRequestDTO;
import uk.gov.companieshouse.orders.api.dto.DeliveryDetailsDTO;
import uk.gov.companieshouse.orders.api.model.Basket;
import uk.gov.companieshouse.orders.api.model.BasketData;
import uk.gov.companieshouse.orders.api.model.BasketItem;
import uk.gov.companieshouse.orders.api.model.Certificate;
import uk.gov.companieshouse.orders.api.model.Checkout;
import uk.gov.companieshouse.orders.api.model.DeliveryDetails;
import uk.gov.companieshouse.orders.api.model.Item;
import uk.gov.companieshouse.orders.api.model.PaymentStatus;
import uk.gov.companieshouse.orders.api.repository.BasketRepository;
import uk.gov.companieshouse.orders.api.repository.CheckoutRepository;
import uk.gov.companieshouse.orders.api.service.ApiClientService;
import uk.gov.companieshouse.orders.api.service.CheckoutService;
import uk.gov.companieshouse.orders.api.util.ResultCaptor;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.companieshouse.orders.api.util.TestConstants.ERIC_IDENTITY_HEADER_NAME;
import static uk.gov.companieshouse.orders.api.util.TestConstants.ERIC_IDENTITY_VALUE;
import static uk.gov.companieshouse.orders.api.util.TestConstants.REQUEST_ID_HEADER_NAME;
import static uk.gov.companieshouse.orders.api.util.TestConstants.TOKEN_REQUEST_ID_VALUE;

@AutoConfigureMockMvc
@SpringBootTest
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

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private BasketRepository basketRepository;

    @Autowired
    private CheckoutRepository checkoutRepository;

    @MockBean
    private ApiClientService apiClientService;

    @SpyBean
    private CheckoutService checkoutService;

    @AfterEach
    void tearDown() {
        basketRepository.findById(ERIC_IDENTITY_VALUE).ifPresent(basketRepository::delete);
        checkoutRepository.deleteAll();
    }

    @Test
    @DisplayName("Add Item successfully adds an item to the basket, if the basket does not exist")
    public void addItemSuccessfullyAddsItemToBasketIfBasketDoesNotExist() throws Exception {
        AddToBasketRequestDTO addToBasketRequestDTO = new AddToBasketRequestDTO();
        addToBasketRequestDTO.setItemUri(ITEM_URI);

        mockMvc.perform(post("/basket/items")
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
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
        when(apiClientService.getItem(ITEM_URI)).thenReturn(certificate);

        ResultCaptor<Checkout> resultCaptor = new ResultCaptor<>();
        doAnswer(resultCaptor).when(checkoutService).createCheckout(any(Certificate.class), any(String.class));

        mockMvc.perform(post("/basket/checkout")
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE))
                .andExpect(status().isOk());

        final Optional<Checkout> retrievedCheckout = checkoutRepository.findById(resultCaptor.getResult().getId());
        assertTrue(retrievedCheckout.isPresent());
        assertEquals(ERIC_IDENTITY_VALUE, retrievedCheckout.get().getUserId());
        final Item item = retrievedCheckout.get().getData().getItems().get(0);
        assertEquals(COMPANY_NUMBER, item.getCompanyNumber());
    }

    @Test
    @DisplayName("Checkout basket fails to create checkout and returns 409 conflict, when basket is empty")
    public void checkoutBasketfFailsToCreateCheckoutIfBasketIsEmpty() throws Exception {
        Basket basket = new Basket();
        basket.setId(ERIC_IDENTITY_VALUE);
        basketRepository.save(basket);

        mockMvc.perform(post("/basket/checkout")
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE))
                .andExpect(status().isConflict());

        assertEquals(0, checkoutRepository.count());
    }

    @Test
    @DisplayName("Checkout Basket fails to create checkout and returns 409, when basket does not exist")
    public void checkoutBasketFailsToCreateCheckoutIfBasketDoesNotExist() throws Exception {

        mockMvc.perform(post("/basket/checkout")
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
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

        mockMvc.perform(post("/basket/checkout")
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE))
                .andExpect(status().isBadRequest());

        assertEquals(0, checkoutRepository.count());
    }

    @Test
    @DisplayName("Check out basket returns 403 if body is present")
    public void checkoutBasketReturnsBadRequestIfBodyIsPresent() throws Exception {

        mockMvc.perform(post("/basket/checkout")
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"gibberish\":\"gibberish\"}"))
                .andExpect(status().isBadRequest());

        assertEquals(0, checkoutRepository.count());
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
        deliveryDetailsDTO.setCountry("");
        deliveryDetailsDTO.setForename(FORENAME);
        addDeliveryDetailsRequestDTO.setDeliveryDetails(deliveryDetailsDTO);

        mockMvc.perform(patch("/basket")
            .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
            .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(addDeliveryDetailsRequestDTO)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Patch basket payment details returns OK")
    public void patchBasketPaymentDetailsReturnsOK() throws Exception {
        BasketPaymentRequestDTO basketPaymentRequestDTO = new BasketPaymentRequestDTO();
        basketPaymentRequestDTO.setPaidAt("paid-at");
        basketPaymentRequestDTO.setPaymentReference("reference");
        basketPaymentRequestDTO.setStatus(PaymentStatus.PAID);

        mockMvc.perform(patch("/basket/payment/1234")
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
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

        BasketPaymentRequestDTO basketPaymentRequestDTO = new BasketPaymentRequestDTO();
        basketPaymentRequestDTO.setPaidAt("paid-at");
        basketPaymentRequestDTO.setPaymentReference("reference");
        basketPaymentRequestDTO.setStatus(PaymentStatus.PAID);

        mockMvc.perform(patch("/basket/payment/1234")
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
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

        mockMvc.perform(patch("/basket/payment/1234")
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
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

        BasketPaymentRequestDTO basketPaymentRequestDTO = new BasketPaymentRequestDTO();
        basketPaymentRequestDTO.setPaidAt("paid-at");
        basketPaymentRequestDTO.setPaymentReference("reference");
        basketPaymentRequestDTO.setStatus(PaymentStatus.PAID);

        mockMvc.perform(patch("/basket/payment/1234")
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(basketPaymentRequestDTO)))
                .andExpect(status().isOk());

        final LocalDateTime intervalEnd = LocalDateTime.now();

        final Optional<Basket> retrievedBasket = basketRepository.findById(ERIC_IDENTITY_VALUE);
        verifyUpdatedAtTimestampWithinExecutionInterval(retrievedBasket.get(), intervalStart, intervalEnd);
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
}
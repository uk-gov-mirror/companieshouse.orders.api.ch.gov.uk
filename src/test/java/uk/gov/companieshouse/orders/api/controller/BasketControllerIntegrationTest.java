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
import uk.gov.companieshouse.orders.api.dto.AddToBasketRequestDTO;
import uk.gov.companieshouse.orders.api.model.Basket;
import uk.gov.companieshouse.orders.api.model.BasketData;
import uk.gov.companieshouse.orders.api.model.BasketItem;
import uk.gov.companieshouse.orders.api.model.Certificate;
import uk.gov.companieshouse.orders.api.model.Checkout;
import uk.gov.companieshouse.orders.api.model.Item;
import uk.gov.companieshouse.orders.api.repository.BasketRepository;
import uk.gov.companieshouse.orders.api.repository.CheckoutRepository;
import uk.gov.companieshouse.orders.api.service.ApiClientService;
import uk.gov.companieshouse.orders.api.service.CheckoutService;
import uk.gov.companieshouse.orders.api.util.ResultCaptor;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.companieshouse.orders.api.util.TestConstants.*;

@AutoConfigureMockMvc
@SpringBootTest
class BasketControllerIntegrationTest {

    private static final String ITEM_URI = "/orderable/certificates/12345678";
    private static final String ITEM_URI_OLD = "/orderable/certificates/11111111";
    private static final String COMPANY_NUMBER = "00006400";

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
    @DisplayName("Successfully adds an item to the basket if it does not exist")
    public void successfullyAddsItemToBasketIfItDoesNotExists() throws Exception {
        AddToBasketRequestDTO addToBasketRequestDTO = new AddToBasketRequestDTO();
        addToBasketRequestDTO.setItemUri(ITEM_URI);

        mockMvc.perform(post("/basket/items")
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(addToBasketRequestDTO)))
                .andExpect(status().isOk());

        final Optional<Basket> retrievedBasket = basketRepository.findById(ERIC_IDENTITY_VALUE);
        assertEquals(retrievedBasket.get().getData().getItems().get(0).getItemUri(), ITEM_URI);
        assertEquals(retrievedBasket.get().getData().getItems().size(), 1);
    }

    @Test
    @DisplayName("Successfully adds an item to the basket if it exists")
    public void successfullyAddsAnItemToBasketIfItAlreadyExists() throws Exception {
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
        assertEquals(retrievedBasket.get().getData().getItems().get(0).getItemUri(), ITEM_URI);

    }

    @Test
    @DisplayName("Successfully replaces an item in the basket")
    public void successfullyReplacesAnItemInTheBasket() throws Exception {
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
        assertEquals(retrievedBasket.get().getData().getItems().get(0).getItemUri(), ITEM_URI);

    }

    @Test
    @DisplayName("Fails to add item to basket that fails validation")
    public void failsToAddItemToBasketIfFailsValidation() throws Exception {
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
    @DisplayName("Successfully creates checkout when basket contains a valid certificate uri")
    public void successfullyCreatesCheckoutWhenBasketIsValid() throws Exception {
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
        doAnswer(resultCaptor).when(checkoutService).createCheckout(any(Certificate.class));

        mockMvc.perform(post("/basket/checkout")
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE))
                .andExpect(status().isOk());

        final Optional<Checkout> retrievedCheckout = checkoutRepository.findById(resultCaptor.getResult().getId());
        assertTrue(retrievedCheckout.isPresent());
        final Item item = retrievedCheckout.get().getData().getItems().get(0);
        assertEquals(COMPANY_NUMBER, item.getCompanyNumber());
    }

    @Test
    @DisplayName("Fails to create checkout and returns 409 conflict, when basket is empty")
    public void failsToCreateCheckoutIfBasketIsEmpty() throws Exception {
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
    @DisplayName("Fails to create checkout and returns 409, when basket does not exist")
    public void failsToCreateCheckoutIfBasketDoesNotExist() throws Exception {

        mockMvc.perform(post("/basket/checkout")
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE))
                .andExpect(status().isConflict());

        assertEquals(0, checkoutRepository.count());
    }

    @Test
    @DisplayName("Fails to create checkout and returns 400, when there is a failure getting the item")
    public void failsToCreateCheckoutWhenItFailsToGetAnItem() throws Exception {
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
}


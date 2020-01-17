package uk.gov.companieshouse.orders.api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.companieshouse.orders.api.dto.AddToBasketItemDTO;
import uk.gov.companieshouse.orders.api.model.BasketData;
import uk.gov.companieshouse.orders.api.model.BasketItem;
import uk.gov.companieshouse.orders.api.model.Item;
import uk.gov.companieshouse.orders.api.repository.BasketItemRepository;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static uk.gov.companieshouse.orders.api.util.TestConstants.*;



@AutoConfigureMockMvc
@SpringBootTest
class BasketControllerIntegrationTest {

    private static final String ITEM_URI = "/orderable/certificate/12345678";
    private static final String ITEM_URI_OLD = "/orderable/certificate/11111111";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private BasketItemRepository repository;

    @AfterEach
    void tearDown() {
        repository.findById(ERIC_IDENTITY_VALUE).ifPresent(repository::delete);
    }

    @Test
    @DisplayName("Successfully adds an item to the basket if it does not exist")
    public void successfullyAddsItemToBasketIfItDoesNotExists() throws Exception {
        AddToBasketItemDTO addToBasketItemDTO = new AddToBasketItemDTO();
        addToBasketItemDTO.setItemUri(ITEM_URI);

        mockMvc.perform(post("/basket/items")
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(addToBasketItemDTO)))
                .andExpect(status().isCreated());

        final Optional<BasketItem> retrievedBasketItem = repository.findById(ERIC_IDENTITY_VALUE);
        assertEquals(retrievedBasketItem.get().getData().getItems().get(0).getItemUri(), ITEM_URI);
    }

    @Test
    @DisplayName("Successfully adds an item to the basket if it exists")
    public void successfullyAddsAnItemToBasketIfItAlreadyExists() throws Exception {
        BasketItem newItem = new BasketItem();
        repository.save(newItem);

        AddToBasketItemDTO addToBasketItemDTO = new AddToBasketItemDTO();
        addToBasketItemDTO.setItemUri(ITEM_URI);

        mockMvc.perform(post("/basket/items")
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(addToBasketItemDTO)))
                .andExpect(status().isCreated());

        final Optional<BasketItem> retrievedBasketItem = repository.findById(ERIC_IDENTITY_VALUE);
        assertEquals(retrievedBasketItem.get().getData().getItems().get(0).getItemUri(), ITEM_URI);

    }

    @Test
    @DisplayName("Successfully replaces an item in the basket")
    public void successfullyReplacesAnItemInTheBasket() throws Exception {
        Item item = new Item();
        item.setItemUri(ITEM_URI_OLD);
        BasketData basketData = new BasketData();
        basketData.setItems(Arrays.asList(item));
        BasketItem newItem = new BasketItem();
        newItem.setData(basketData);
        repository.save(newItem);

        AddToBasketItemDTO addToBasketItemDTO = new AddToBasketItemDTO();
        addToBasketItemDTO.setItemUri(ITEM_URI);

        mockMvc.perform(post("/basket/items")
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(addToBasketItemDTO)))
                .andExpect(status().isCreated());

        final Optional<BasketItem> retrievedBasketItem = repository.findById(ERIC_IDENTITY_VALUE);
        assertEquals(retrievedBasketItem.get().getData().getItems().get(0).getItemUri(), ITEM_URI);

    }

    @Test
    @DisplayName("Fails to add item to basket that fails validation")
    public void failsToAddItemToBasketIfFailsValidation() throws Exception {
        mockMvc.perform(post("/basket/items")
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}


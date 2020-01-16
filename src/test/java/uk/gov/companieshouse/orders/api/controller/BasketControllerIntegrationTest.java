package uk.gov.companieshouse.orders.api.controller;

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
import uk.gov.companieshouse.orders.api.model.BasketItem;
import uk.gov.companieshouse.orders.api.repository.BasketItemRepository;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.companieshouse.orders.api.util.TestConstants.*;



@AutoConfigureMockMvc
@SpringBootTest
class BasketControllerIntegrationTest {

    private static final String ITEM_URI = "/orderable/certificate/12345678";

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
    @DisplayName("Successfully adds an item to the basket")
    public void successfullyAddsItemToBasket() throws Exception {
        AddToBasketItemDTO addToBasketItemDTO = new AddToBasketItemDTO();
        addToBasketItemDTO.setItemUri(ITEM_URI);

        mockMvc.perform(post("/basket/items")
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(addToBasketItemDTO)))
                .andExpect(status().isCreated());

        final Optional<BasketItem> retrievedBasketItem = repository.findById(ERIC_IDENTITY_VALUE);
        assertEquals(retrievedBasketItem.get().getData().getItems()[0].getItemUri(), ITEM_URI);

    }
}


package uk.gov.companieshouse.orders.api.mapper;

import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.companieshouse.orders.api.dto.AddToBasketRequestDTO;
import uk.gov.companieshouse.orders.api.dto.AddToBasketResponseDTO;
import uk.gov.companieshouse.orders.api.model.Basket;
import uk.gov.companieshouse.orders.api.model.Item;

import java.util.Arrays;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@ExtendWith(SpringExtension.class)
@SpringJUnitConfig(BasketMapperTest.Config.class)
@RunWith(SpringRunner.class)
public class BasketMapperTest {

    private static final String ITEM_URI = "/orderable/certificate/12345678";

    @Configuration
    @ComponentScan(basePackageClasses = BasketMapperTest.class)
    static class Config {}

    @Autowired
    private BasketMapper basketMapper;

    @Test
    public void testAddToBasketRequestDTOToBasket(){
        final AddToBasketRequestDTO dto = new AddToBasketRequestDTO();
        dto.setItemUri(ITEM_URI);

        Basket item = basketMapper.addToBasketRequestDTOToBasket(dto);

        assertThat(item.getData(), is(notNullValue()));
        assertEquals(ITEM_URI, item.getData().getItems().get(0).getItemUri());
    }

    @Test
    public void testBasketToAddToBasketResponseDTO() {
        Basket basket = new Basket();
        Item item = new Item();
        item.setItemUri(ITEM_URI);
        basket.getData().setItems(Arrays.asList(item));

        AddToBasketResponseDTO addToBasketResponseDTO = basketMapper.basketToAddToBasketDTO(basket);

        assertEquals(ITEM_URI, addToBasketResponseDTO.getItemUri());
    }
}

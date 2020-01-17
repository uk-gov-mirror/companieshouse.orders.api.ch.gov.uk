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
import uk.gov.companieshouse.orders.api.dto.AddToBasketItemRequestDTO;
import uk.gov.companieshouse.orders.api.dto.AddToBasketItemResponseDTO;
import uk.gov.companieshouse.orders.api.model.BasketItem;
import uk.gov.companieshouse.orders.api.model.Item;

import java.util.Arrays;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@ExtendWith(SpringExtension.class)
@SpringJUnitConfig(BasketItemMapperTest.Config.class)
@RunWith(SpringRunner.class)
public class BasketItemMapperTest {

    private static final String ITEM_URI = "/orderable/certificate/12345678";

    @Configuration
    @ComponentScan(basePackageClasses = BasketItemMapperTest.class)
    static class Config {}

    @Autowired
    private BasketItemMapper basketItemMapper;

    @Test
    public void testAddToBasketItemDTOToBasket(){
        final AddToBasketItemRequestDTO dto = new AddToBasketItemRequestDTO();
        dto.setItemUri(ITEM_URI);

        BasketItem item = basketItemMapper.addBasketItemDTOToBasketItem(dto);

        assertThat(item.getData(), is(notNullValue()));
        assertEquals(ITEM_URI, item.getData().getItems().get(0).getItemUri());
    }

    @Test
    public void testBasketItemToBasketItemDTO() {
        BasketItem basketItem = new BasketItem();
        Item item = new Item();
        item.setItemUri(ITEM_URI);
        basketItem.getData().setItems(Arrays.asList(item));

        AddToBasketItemResponseDTO basketItemDTO = basketItemMapper.basketItemToBasketItemDTO(basketItem);

        assertEquals(ITEM_URI, basketItemDTO.getItemUri());
    }
}

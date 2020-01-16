package uk.gov.companieshouse.orders.api.mapper;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.junit4.SpringRunner;

//@ExtendWith(SpringExtension.class)
//@SpringJUnitConfig(BasketItemMapperTest.Config.class)
//@RunWith(SpringRunner.class)
public class BasketItemMapperTest {

    private static final String ID = "Y2VkZWVlMzhlZWFjY2M4MzQ3MT";
    private static final String ITEM_URI = "/orderable/certificate/12345678";

    @Configuration
    @ComponentScan(basePackageClasses = BasketItemMapperTest.class)
    static class Config {}

    //@Autowired
    //private BasketItemMapper basketItemMapper;

//    @Test
//    public void testAddToBasketItemDTOToBasket(){
//        final AddToBasketItemDTO dto = new AddToBasketItemDTO();
//        dto.setItemUri(ITEM_URI);
//
//        BasketItem item = basketItemMapper.addBasketItemDTOToBasketItem(dto);
//
//        assertThat(item.getData(), is(notNullValue()));
//    }
}

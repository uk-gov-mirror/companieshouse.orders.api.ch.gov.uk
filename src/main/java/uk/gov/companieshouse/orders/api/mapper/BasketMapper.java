package uk.gov.companieshouse.orders.api.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import uk.gov.companieshouse.orders.api.dto.AddToBasketRequestDTO;
import uk.gov.companieshouse.orders.api.model.Basket;
import uk.gov.companieshouse.orders.api.model.BasketItem;

import java.util.Arrays;

@Mapper(componentModel = "spring")
public interface BasketMapper {
    Basket addToBasketRequestDTOToBasket(AddToBasketRequestDTO addToBasketRequestDTO);

    @AfterMapping
    default void fillBasket(AddToBasketRequestDTO addToBasketRequestDTO, @MappingTarget Basket basket) {
        BasketItem item = new BasketItem();
        item.setItemUri(addToBasketRequestDTO.getItemUri());
        basket.getData().setItems(Arrays.asList(item));
    }
}

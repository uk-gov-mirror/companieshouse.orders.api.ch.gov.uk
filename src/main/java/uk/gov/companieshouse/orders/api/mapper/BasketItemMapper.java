package uk.gov.companieshouse.orders.api.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import uk.gov.companieshouse.orders.api.dto.AddToBasketItemDTO;
import uk.gov.companieshouse.orders.api.model.BasketItem;
import uk.gov.companieshouse.orders.api.model.Item;

import java.util.Arrays;

@Mapper(componentModel = "spring")
public interface BasketItemMapper {
    BasketItem addBasketItemDTOToBasketItem(AddToBasketItemDTO addToBasketItemDTO);

    @AfterMapping
    default void fillBasket(AddToBasketItemDTO addToBasketItemDTO, @MappingTarget BasketItem basketItem) {
        Item item = new Item();
        item.setItemUri(addToBasketItemDTO.getItemUri());
        basketItem.getData().setItems(Arrays.asList(item));
    }
}

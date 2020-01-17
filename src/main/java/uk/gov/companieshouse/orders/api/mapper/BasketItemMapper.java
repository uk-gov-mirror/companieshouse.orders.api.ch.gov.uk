package uk.gov.companieshouse.orders.api.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import uk.gov.companieshouse.orders.api.dto.AddToBasketItemRequestDTO;
import uk.gov.companieshouse.orders.api.dto.AddToBasketItemResponseDTO;
import uk.gov.companieshouse.orders.api.model.BasketItem;
import uk.gov.companieshouse.orders.api.model.Item;

import java.util.Arrays;

@Mapper(componentModel = "spring")
public interface BasketItemMapper {
    BasketItem addBasketItemDTOToBasketItem(AddToBasketItemRequestDTO addToBasketItemRequestDTO);

    @Mapping(target = "itemUri", expression = "java(basketItem.getData().getItems().get(0).getItemUri())")
    AddToBasketItemResponseDTO basketItemToBasketItemDTO(BasketItem basketItem);

    @AfterMapping
    default void fillBasket(AddToBasketItemRequestDTO addToBasketItemRequestDTO, @MappingTarget BasketItem basketItem) {
        Item item = new Item();
        item.setItemUri(addToBasketItemRequestDTO.getItemUri());
        basketItem.getData().setItems(Arrays.asList(item));
    }
}

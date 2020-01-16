package uk.gov.companieshouse.orders.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.companieshouse.orders.api.dto.AddToBasketItemDTO;
import uk.gov.companieshouse.orders.api.model.BasketItem;

@Mapper(componentModel = "spring")
public interface BasketItemMapper {
    //@Mapping(target = "data.items[0].itemUri", source="itemUri")
    BasketItem addBasketItemDTOToBasketItem(AddToBasketItemDTO addToBasketItemDTO);
}

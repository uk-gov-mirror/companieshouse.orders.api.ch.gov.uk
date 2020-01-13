package uk.gov.companieshouse.orders.api.mapper;

import org.mapstruct.Mapper;
import uk.gov.companieshouse.orders.api.dto.BasketItemDTO;
import uk.gov.companieshouse.orders.api.model.BasketItem;

@Mapper(componentModel = "spring")
public interface BasketItemMapper {
    BasketItem basketItemDTOtoBasketItem(BasketItemDTO basketItemDTO);
    BasketItemDTO basketItemtoBasketItemDTO(BasketItem basketItemDTO);
}

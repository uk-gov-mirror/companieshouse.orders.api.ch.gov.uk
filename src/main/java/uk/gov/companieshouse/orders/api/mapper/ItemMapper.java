package uk.gov.companieshouse.orders.api.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import uk.gov.companieshouse.orders.api.dto.BasketItemDTO;
import uk.gov.companieshouse.orders.api.dto.ItemDTO;
import uk.gov.companieshouse.orders.api.model.Item;

@Mapper(componentModel = "spring")
public interface ItemMapper {
    ItemDTO itemToItemDTO(Item item);
    Item itemDTOToItem(ItemDTO itemDTO);
    BasketItemDTO itemToBasketItemDTO(Item item);

    @AfterMapping
    default void setDescription(Item item, @MappingTarget BasketItemDTO basketItemDTO){
        basketItemDTO.setId(item.getId());
        basketItemDTO.setDescription(item.getDescription());
        basketItemDTO.setDescriptionIdentifier(item.getDescriptionIdentifier());
        basketItemDTO.setDescriptionValues(item.getDescriptionValues());
    }
}

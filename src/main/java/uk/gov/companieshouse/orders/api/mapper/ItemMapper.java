package uk.gov.companieshouse.orders.api.mapper;

import org.mapstruct.Mapper;
import uk.gov.companieshouse.orders.api.dto.ItemDTO;
import uk.gov.companieshouse.orders.api.model.Item;

@Mapper(componentModel = "spring")
public interface ItemMapper {
    ItemDTO itemToItemDTO(Item item);
    Item itemDTOToItem(ItemDTO itemDTO);
}

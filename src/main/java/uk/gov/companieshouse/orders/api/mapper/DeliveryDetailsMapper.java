package uk.gov.companieshouse.orders.api.mapper;

import org.mapstruct.Mapper;
import uk.gov.companieshouse.orders.api.dto.AddDeliveryDetailsRequestDTO;
import uk.gov.companieshouse.orders.api.dto.AddDeliveryDetailsResponseDTO;
import uk.gov.companieshouse.orders.api.model.DeliveryDetails;

@Mapper(componentModel = "spring")
public interface DeliveryDetailsMapper {
    DeliveryDetails addToDeliveryDetailsRequestDTOToDeliveryDetails(AddDeliveryDetailsRequestDTO addDeliveryDetailsRequestDTO);

    AddDeliveryDetailsResponseDTO deliveryDetailsToAddToDeliveryDetailsDTO(DeliveryDetails deliveryDetails);
}

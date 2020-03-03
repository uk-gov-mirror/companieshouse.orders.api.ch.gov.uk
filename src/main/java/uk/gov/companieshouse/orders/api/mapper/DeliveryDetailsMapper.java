package uk.gov.companieshouse.orders.api.mapper;

import org.mapstruct.Mapper;
import uk.gov.companieshouse.orders.api.dto.AddDeliveryDetailsRequestDTO;
import uk.gov.companieshouse.orders.api.dto.AddDeliveryDetailsResponseDTO;
import uk.gov.companieshouse.orders.api.dto.DeliveryDetailsDTO;
import uk.gov.companieshouse.orders.api.model.DeliveryDetails;

@Mapper(componentModel = "spring")
public interface DeliveryDetailsMapper {

    default DeliveryDetails addToDeliveryDetailsRequestDTOToDeliveryDetails(AddDeliveryDetailsRequestDTO source) {
        return addToDeliveryDetailsRequestDTOToDeliveryDetails(source.getDeliveryDetails());
    }

    DeliveryDetails addToDeliveryDetailsRequestDTOToDeliveryDetails(DeliveryDetailsDTO addDeliveryDetailsRequestDTO);

    AddDeliveryDetailsResponseDTO deliveryDetailsToAddToDeliveryDetailsDTO(DeliveryDetails deliveryDetails);
}

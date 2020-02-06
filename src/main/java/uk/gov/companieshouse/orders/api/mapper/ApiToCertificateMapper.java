package uk.gov.companieshouse.orders.api.mapper;

import org.mapstruct.Mapper;
import uk.gov.companieshouse.api.model.order.item.CertificateApi;
import uk.gov.companieshouse.orders.api.model.Certificate;

@Mapper(componentModel = "spring")
public interface ApiToCertificateMapper {
    Certificate apiToCertificate(CertificateApi certificateApi);
}

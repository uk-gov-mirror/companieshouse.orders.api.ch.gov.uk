package uk.gov.companieshouse.orders.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.companieshouse.api.model.order.item.*;
import uk.gov.companieshouse.orders.api.model.*;

// TODO GCI-1242 Rename class?
@Mapper(componentModel = "spring")
public interface ApiToCertificateMapper {
    @Mapping(source = "links.self", target="itemUri")
    @Mapping(target = "satisfiedAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    Certificate apiToCertificate(CertificateApi certificateApi);

    @Mapping(source = "links.self", target="itemUri")
    @Mapping(target = "satisfiedAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    CertifiedCopy apiToCertifiedCopy(CertifiedCopyApi certificateApi);

    CertificateItemOptions apiOptionsToCertificateOptions(CertificateItemOptionsApi certificateOptionsApi);
    CertifiedCopyItemOptions apiOptionsToCertificateOptions(CertifiedCopyItemOptionsApi certifiedCopyOptionsApi);
}

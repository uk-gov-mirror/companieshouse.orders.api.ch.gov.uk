package uk.gov.companieshouse.orders.api.mapper;

import org.mapstruct.Mapper;
import uk.gov.companieshouse.api.model.order.item.BaseItemApi;
import uk.gov.companieshouse.api.model.order.item.CertificateApi;
import uk.gov.companieshouse.api.model.order.item.CertificateItemOptionsApi;
import uk.gov.companieshouse.api.model.order.item.CertifiedCopyApi;
import uk.gov.companieshouse.api.model.order.item.CertifiedCopyItemOptionsApi;
import uk.gov.companieshouse.orders.api.model.Certificate;
import uk.gov.companieshouse.orders.api.model.CertificateItemOptions;
import uk.gov.companieshouse.orders.api.model.CertifiedCopy;
import uk.gov.companieshouse.orders.api.model.CertifiedCopyItemOptions;
import uk.gov.companieshouse.orders.api.model.Item;

@Mapper(componentModel = "spring")
public interface ItemtoApiMapper {
    default BaseItemApi itemToApi(Item item) {
        if (item instanceof Certificate) {
            return itemToCertificateApi((Certificate) item);
        }
        else {
            return itemToCertifiedCopy((CertifiedCopy) item);
        }
    }

    CertificateApi itemToCertificateApi(Certificate certificate);
    CertifiedCopyApi itemToCertifiedCopy(CertifiedCopy certifiedCopy);

    CertificateItemOptionsApi certificateItemOptionsToCertificateItemOptionsApi(CertificateItemOptions certificateItemOptions);
    CertifiedCopyItemOptionsApi certifiedCopyItemOptionsToCertifiedCopyItemOptionsApi(CertifiedCopyItemOptions certifiedCopyItemOptions);
}

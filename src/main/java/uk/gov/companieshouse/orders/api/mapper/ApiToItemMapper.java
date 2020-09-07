package uk.gov.companieshouse.orders.api.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import uk.gov.companieshouse.api.model.order.item.BaseItemApi;
import uk.gov.companieshouse.api.model.order.item.BaseItemOptionsApi;
import uk.gov.companieshouse.api.model.order.item.CertificateItemOptionsApi;
import uk.gov.companieshouse.api.model.order.item.CertifiedCopyItemOptionsApi;
import uk.gov.companieshouse.orders.api.model.CertificateItemOptions;
import uk.gov.companieshouse.orders.api.model.CertifiedCopyItemOptions;
import uk.gov.companieshouse.orders.api.model.Item;
import uk.gov.companieshouse.orders.api.model.ItemOptions;
import uk.gov.companieshouse.orders.api.model.ItemType;

@Mapper(componentModel = "spring")
public interface ApiToItemMapper {

    @Mapping(source = "links.self", target="itemUri")
    @Mapping(target = "satisfiedAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    Item apiToItem(BaseItemApi baseItemApi);

    /**
     * Maps item's `item_options` based on its `kind` correctly to
     * {@link CertificateItemOptions} or {@link CertifiedCopyItemOptions}
     * @param baseItemApi item object received via api call
     * @param item item object to be mapped from received api item
     */
    @AfterMapping
    default void apiToItemOptions(BaseItemApi baseItemApi, @MappingTarget Item item) {
        final String itemKind = baseItemApi.getKind();
        final BaseItemOptionsApi baseItemOptionsApi = baseItemApi.getItemOptions();
        if (itemKind.equals(ItemType.CERTIFICATE.getKind())) {
            item.setItemOptions(apiToCertificateItemOptions((CertificateItemOptionsApi) baseItemOptionsApi));
        }
        else {
            item.setItemOptions(apiToCertifiedCopyItemOptions((CertifiedCopyItemOptionsApi) baseItemOptionsApi));
        }
    }

    CertificateItemOptions apiToCertificateItemOptions(CertificateItemOptionsApi certificateOptionsApi);
    CertifiedCopyItemOptions apiToCertifiedCopyItemOptions(CertifiedCopyItemOptionsApi certifiedCopyOptionsApi);
}

package uk.gov.companieshouse.orders.api.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import uk.gov.companieshouse.api.model.order.item.BaseItemApi;
import uk.gov.companieshouse.api.model.order.item.BaseItemOptionsApi;
import uk.gov.companieshouse.api.model.order.item.CertificateItemOptionsApi;
import uk.gov.companieshouse.api.model.order.item.CertifiedCopyItemOptionsApi;
import uk.gov.companieshouse.api.model.order.item.MissingImageDeliveryItemOptionsApi;
import uk.gov.companieshouse.orders.api.model.CertificateItemOptions;
import uk.gov.companieshouse.orders.api.model.CertifiedCopyItemOptions;
import uk.gov.companieshouse.orders.api.model.Item;
import uk.gov.companieshouse.orders.api.model.ItemOptions;
import uk.gov.companieshouse.orders.api.model.ItemType;
import uk.gov.companieshouse.orders.api.model.MissingImageDeliveryItemOptions;

@Mapper(componentModel = "spring")
public interface ApiToItemMapper {

    @Mapping(source = "links.self", target="itemUri")
    @Mapping(target = "satisfiedAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "itemOptions", qualifiedByName = "preApiToItemOptions")
    Item apiToItem(BaseItemApi baseItemApi);

    @Named("preApiToItemOptions")
    default ItemOptions preApiToItemOptions(BaseItemOptionsApi object) {
        return null;
    }

    /**
     * Maps item's `item_options` based on its `kind` correctly to
     * {@link CertificateItemOptions} or {@link CertifiedCopyItemOptions}
     * or {@link MissingImageDeliveryItemOptions}
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
        else if (itemKind.equals(ItemType.CERTIFIED_COPY.getKind())) {
            item.setItemOptions(apiToCertifiedCopyItemOptions((CertifiedCopyItemOptionsApi) baseItemOptionsApi));
        } else {
            item.setItemOptions(apiToMissingImageDeliveryItemOptions((MissingImageDeliveryItemOptionsApi) baseItemOptionsApi));
        }
    }

    CertificateItemOptions apiToCertificateItemOptions(CertificateItemOptionsApi certificateOptionsApi);
    CertifiedCopyItemOptions apiToCertifiedCopyItemOptions(CertifiedCopyItemOptionsApi certifiedCopyOptionsApi);
    MissingImageDeliveryItemOptions apiToMissingImageDeliveryItemOptions(MissingImageDeliveryItemOptionsApi missingImageDeliveryItemOptionsApi);
}

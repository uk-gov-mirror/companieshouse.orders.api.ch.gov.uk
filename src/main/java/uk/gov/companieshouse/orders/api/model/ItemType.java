package uk.gov.companieshouse.orders.api.model;

public enum ItemType {
    CERTIFICATE("item#certificate", CertificateItemOptions.class),
    CERTIFIED_COPY("item#certified-copy", CertifiedCopyItemOptions.class),
    MISSING_IMAGE_DELIVERY("item#missing-image-delivery", MissingImageDeliveryItemOptions.class);

    ItemType(final String kind, final Class<? extends ItemOptions> optionsType) {
        this.kind = kind;
        this.optionsType = optionsType;
    }

    private String kind;
    private Class<? extends ItemOptions> optionsType;

    public String getKind(){
        return this.kind;
    }

    public Class<? extends ItemOptions> getOptionsType() {
        return optionsType;
    }
}

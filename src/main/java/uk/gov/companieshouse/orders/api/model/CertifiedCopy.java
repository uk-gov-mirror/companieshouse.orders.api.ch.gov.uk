package uk.gov.companieshouse.orders.api.model;


public class CertifiedCopy extends Item {

    private CertifiedCopyItemOptions itemOptions;

    public CertifiedCopyItemOptions getItemOptions() {
        return itemOptions;
    }

    public void setItemOptions(CertifiedCopyItemOptions itemOptions) {
        this.itemOptions = itemOptions;
    }
}

package uk.gov.companieshouse.orders.api.model;


public class Certificate extends Item {

    private CertificateItemOptions itemOptions;

    public CertificateItemOptions getItemOptions() {
        return itemOptions;
    }

    public void setItemOptions(CertificateItemOptions itemOptions) {
        this.itemOptions = itemOptions;
    }
}

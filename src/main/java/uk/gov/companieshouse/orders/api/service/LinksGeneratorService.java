package uk.gov.companieshouse.orders.api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.orders.api.model.Links;

import static org.apache.commons.lang.StringUtils.isBlank;

@Service
public class LinksGeneratorService {

    private final String checkoutUri;

    public LinksGeneratorService(final @Value("${uk.gov.companieshouse.orders.api.basket.checkouts}") String checkoutUri) {
        if (isBlank(checkoutUri)) {
            throw new IllegalArgumentException("Checkout URI not configured!");
        }
        this.checkoutUri = checkoutUri;
    }

    public Links generateCheckoutLinks(String checkoutId) {
        if (isBlank(checkoutId)) {
            throw new IllegalArgumentException("Checkout ID not populated!");
        }
        Links links = new Links();
        links.setSelf(checkoutUri+"/"+checkoutId);
        links.setPayment(checkoutUri+"/"+checkoutId+"/payment");
        return links;
    }

}

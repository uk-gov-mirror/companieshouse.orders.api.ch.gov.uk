package uk.gov.companieshouse.orders.api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.orders.api.model.CheckoutLinks;
import uk.gov.companieshouse.orders.api.model.OrderLinks;

import static org.apache.commons.lang.StringUtils.isBlank;

@Service
public class LinksGeneratorService {

    private final String checkoutUri;
    private final String orderUri;

    public LinksGeneratorService(final @Value("${uk.gov.companieshouse.orders.api.basket.checkouts}") String checkoutUri,
        final @Value("${uk.gov.companieshouse.orders.api.orders}") String orderUri) {
        if (isBlank(checkoutUri)) {
            throw new IllegalArgumentException("Checkout URI not configured!");
        }
        this.checkoutUri = checkoutUri;
        this.orderUri = orderUri;
    }

    public CheckoutLinks generateCheckoutLinks(String checkoutId) {
        if (isBlank(checkoutId)) {
            throw new IllegalArgumentException("Checkout ID not populated!");
        }
        CheckoutLinks links = new CheckoutLinks();
        links.setSelf(checkoutUri+"/"+checkoutId);
        links.setPayment(checkoutUri+"/"+checkoutId+"/payment");
        return links;
    }

    public OrderLinks generateOrderLinks(String orderId) {
        if (isBlank(orderId)) {
            throw new IllegalArgumentException("Order ID not populated!");
        }
        OrderLinks links = new OrderLinks();
        links.setSelf(orderUri+"/"+orderId);
        return links;
    }

}

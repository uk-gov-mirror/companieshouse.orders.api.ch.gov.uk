package uk.gov.companieshouse.orders.api.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PaymentDetails {
    private String description;

    private String etag;

    private LocalDateTime paidAt;

    private PaymentStatus status;

    private PaymentLinks links;

    private String paymentReference;

    private List<Item> items = new ArrayList<>();

    private String kind;

}

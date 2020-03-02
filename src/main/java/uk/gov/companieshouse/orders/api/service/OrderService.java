package uk.gov.companieshouse.orders.api.service;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.orders.api.mapper.CheckoutToOrderMapper;
import uk.gov.companieshouse.orders.api.model.Checkout;
import uk.gov.companieshouse.orders.api.model.Order;
import uk.gov.companieshouse.orders.api.repository.OrderRepository;

import java.time.LocalDateTime;

@Service
public class OrderService {

    private final CheckoutToOrderMapper mapper;
    private final OrderRepository repository;

    public OrderService(final CheckoutToOrderMapper mapper, final OrderRepository repository) {
        this.mapper = mapper;
        this.repository = repository;
    }

    /**
     * Used to create an order from a checkout object once payment has been successful.
     * @param checkout the user's checkout object
     * @return the resulting order
     */
    public Order createOrder(final Checkout checkout) {
        final Order mappedOrder = mapper.checkoutToOrder(checkout);
        setCreationDateTimes(mappedOrder);
        return repository.save(mappedOrder);
    }

    /**
     * Sets the created at and updated at date time 'timestamps' to now.
     * @param order the order to be 'timestamped'
     */
    void setCreationDateTimes(final Order order) {
        final LocalDateTime now = LocalDateTime.now();
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
    }

}

package uk.gov.companieshouse.orders.api.service;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.orders.api.model.BasketItem;
import uk.gov.companieshouse.orders.api.repository.BasketItemRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class BasketItemService {

    private final BasketItemRepository repository;

    public BasketItemService(BasketItemRepository repository) {
        this.repository = repository;
    }

    public BasketItem addItemToBasket(BasketItem basketItem) {
        return repository.save(basketItem);
    }

    public BasketItem createBasketItem(BasketItem basketItem) {
        final LocalDateTime now = LocalDateTime.now();
        basketItem.setUpdatedAt(now);
        basketItem.setCreatedAt(now);
        return repository.save(basketItem);
    }

    public BasketItem saveBasketItem(BasketItem basketItem) {
        final LocalDateTime now = LocalDateTime.now();
        basketItem.setUpdatedAt(now);
        return repository.save(basketItem);
    }

    public Optional<BasketItem> getBasketById(String id) {
        return repository.findById(id);
    }
}

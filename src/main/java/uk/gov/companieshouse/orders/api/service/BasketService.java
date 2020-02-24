package uk.gov.companieshouse.orders.api.service;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.orders.api.model.Basket;
import uk.gov.companieshouse.orders.api.repository.BasketRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class BasketService {

    private final BasketRepository repository;

    public BasketService(BasketRepository repository) {
        this.repository = repository;
    }

    public Basket saveBasket(Basket basket) {
        final LocalDateTime now = LocalDateTime.now();
        if(basket.getId() == null) {
            throw new IllegalArgumentException("ID Must be present");
        }
        if(basket.getCreatedAt() == null) {
            basket.setCreatedAt(now);
        }
        basket.setUpdatedAt(now);
        return repository.save(basket);
    }

    public Optional<Basket> getBasketById(String id) {
        return repository.findById(id);
    }

    public Basket clearBasket(String id) {
        return repository.clearBasketDataById(id);
    }
}

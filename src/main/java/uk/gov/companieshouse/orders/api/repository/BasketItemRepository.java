package uk.gov.companieshouse.orders.api.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import uk.gov.companieshouse.orders.api.model.BasketItem;

@RepositoryRestResource
public interface BasketItemRepository extends MongoRepository<BasketItem, String> { }

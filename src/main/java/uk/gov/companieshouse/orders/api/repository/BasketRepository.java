package uk.gov.companieshouse.orders.api.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import uk.gov.companieshouse.orders.api.model.Basket;

@Repository
public interface BasketRepository extends MongoRepository<Basket, String>, BasketRepositoryCustom { }

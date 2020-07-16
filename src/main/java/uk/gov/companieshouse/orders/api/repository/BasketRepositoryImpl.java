package uk.gov.companieshouse.orders.api.repository;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import uk.gov.companieshouse.orders.api.model.Basket;
import uk.gov.companieshouse.orders.api.model.BasketData;
import uk.gov.companieshouse.orders.api.model.DeliveryDetails;

import java.time.LocalDateTime;

import static org.springframework.data.mongodb.core.query.Criteria.where;


public class BasketRepositoryImpl implements BasketRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    public BasketRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Basket clearBasketDataById(String id) {
        Query query = new Query().addCriteria(where("_id").is(id));

        Basket basket = mongoTemplate.findOne(query, Basket.class);
        BasketData basketData = basket.getData();
        DeliveryDetails deliveryDetails = basketData.getDeliveryDetails();
        BasketData newBasketData = new BasketData();
        newBasketData.setDeliveryDetails(deliveryDetails);

        Update update = new Update();
        update.set("data", newBasketData);
        update.set("updated_at", LocalDateTime.now());

        return mongoTemplate.findAndModify(query, update, Basket.class);
    }
}

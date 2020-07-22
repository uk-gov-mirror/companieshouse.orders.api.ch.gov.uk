package uk.gov.companieshouse.orders.api.listener;

import org.bson.Document;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterConvertEvent;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.orders.api.model.Item;
import uk.gov.companieshouse.orders.api.model.Order;

import java.util.List;

@Component
public class MongoOrderListener extends AbstractMongoEventListener<Order> {

    private static final String ORDER_ORDER_TYPE_NAME = "order";

    private final OrderItemOptionsReader reader;

    public MongoOrderListener(final OrderItemOptionsReader reader) {
        this.reader = reader;
    }

    /**
     * Overridden here to intervene in the reading of an {@link Order} from the database to make sure the
     * item options, if present, are read correctly.
     * @param event the {@link AfterConvertEvent} presenting both the mapped {@link Order} and its {@link Document}
     */
    @Override
    public void onAfterConvert(final AfterConvertEvent<Order> event) {
        final Document orderDocument = event.getDocument();
        final List<Item> items = event.getSource().getData().getItems();
        reader.readOrderItemsOptions(items, orderDocument, ORDER_ORDER_TYPE_NAME);
    }

}

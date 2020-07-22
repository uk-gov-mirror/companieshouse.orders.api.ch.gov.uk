package uk.gov.companieshouse.orders.api.listener;

import org.bson.Document;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterConvertEvent;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.orders.api.model.Checkout;
import uk.gov.companieshouse.orders.api.model.Item;

import java.util.List;

@Component
public class MongoCheckoutListener extends AbstractMongoEventListener<Checkout> {

    private static final String CHECKOUT_ORDER_TYPE_NAME = "checkout";

    private final OrderItemOptionsReader reader;

    MongoCheckoutListener(final OrderItemOptionsReader reader) {
        this.reader = reader;
    }

    /**
     * Overridden here to intervene in the reading of a {@link Checkout} from the database to make sure the
     * item options, if present, are read correctly.
     * @param event the {@link AfterConvertEvent} presenting both the mapped {@link Checkout} and its {@link Document}
     */
    @Override
    public void onAfterConvert(final AfterConvertEvent<Checkout> event) {
        final Document checkoutDocument = event.getDocument();
        final List<Item> items = event.getSource().getData().getItems();
        reader.readOrderItemsOptions(items, checkoutDocument, CHECKOUT_ORDER_TYPE_NAME);
    }

}

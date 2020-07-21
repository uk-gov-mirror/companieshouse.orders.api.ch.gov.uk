package uk.gov.companieshouse.orders.api.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterConvertEvent;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.orders.api.model.CertificateItemOptions;
import uk.gov.companieshouse.orders.api.model.CertifiedCopyItemOptions;
import uk.gov.companieshouse.orders.api.model.Checkout;
import uk.gov.companieshouse.orders.api.model.Item;
import uk.gov.companieshouse.orders.api.model.ItemOptions;

import java.io.IOException;
import java.util.List;

import static java.util.Arrays.stream;

@Component
public class MongoCheckoutListener extends AbstractMongoEventListener<Checkout> {

    @Autowired
    private ObjectMapper mapper;

    /** Values of this represent different item types (aka kinds). */
    enum ItemType {
        CERTIFICATE("item#certificate", CertificateItemOptions.class),
        CERTIFIED_COPY("item#certified-copy", CertifiedCopyItemOptions.class);

        ItemType(final String kind, final Class<? extends ItemOptions> optionsType) {
            this.kind = kind;
            this.optionsType = optionsType;
        }

        Class<? extends ItemOptions> getOptionsType() {
            return optionsType;
        }

        private String kind;
        private Class<? extends ItemOptions> optionsType;
    }

    /**
     * Overridden here to intervene in the reading of a {@link Checkout} from the database to make sure the
     * item options, if present, are read correctly.
     * @param event the {@link AfterConvertEvent} presenting both the mapped {@link Checkout} and its {@link Document}
     */
    @Override
    public void onAfterConvert(final AfterConvertEvent<Checkout> event) {

        final Document checkoutDocument = event.getDocument();
        if (checkoutDocument == null) {
            throw new IllegalStateException("No checkout document found on event.");
        }
        final List<Item> items = event.getSource().getData().getItems();

        for (int index = 0; index < items.size(); index++) {
            try {
                readCheckoutItemsOptions(index, items, checkoutDocument);
            } catch (IOException ioe) {
                throw new IllegalStateException("Error parsing item options JSON: " + ioe.getMessage());
            }
        }

    }

    /**
     * Reads the item options for each item correctly. How to read these options correctly from the DB is determined
     * from the kind of the item. Updates the item options assigned to each item.
     * @param itemIndex the item index identifying both the item within the items collection and its {@link Document}
     * @param items the items held by the checkout
     * @param checkoutDocument the checkout {@link Document} from the DB
     * @throws IOException should there be an issue parsing the item options JSON from the DB
     */
    void readCheckoutItemsOptions(final int itemIndex, final List<Item> items, final Document checkoutDocument)
            throws IOException {
        final Item item = items.get(itemIndex);
        final Document optionsDocument = getItemOptionsDocument(checkoutDocument, itemIndex);
        if (optionsDocument == null) {
            // No item options to read.
            return;
        }
        final ItemOptions options = readItemOptions(optionsDocument, item.getKind());
        item.setItemOptions(options);
    }

    /**
     * Gets the document representing the item's item options.
     * @param checkoutDocument the {@link Document} representing the checkout
     * @param itemIndex the index of the item within the collection of items held within the checkout
     * @return the {@link Document} representing the item options for the item identified by the index
     */
    Document getItemOptionsDocument(final Document checkoutDocument, final int itemIndex) {
        @SuppressWarnings("unchecked") // Java language limitation (type erasure)
        final Document itemDocument =
                ((List<Document>) checkoutDocument.get("data", Document.class).get("items", List.class)).get(itemIndex);
        return itemDocument.get("item_options", Document.class);
    }

    /**
     * Reads (deserialises) the options document into the correct type of item options object.
     * @param optionsDocument the {@link Document} from the DB representing the item options
     * @param kind the item kind used to determine the correct item options class for the object to read the
     *             document into
     * @return the deserialised item options object, either a {@link CertificateItemOptions}, or a
     * {@link CertifiedCopyItemOptions} as appropriate for the kind
     * @throws IOException should there be an issue parsing the item options JSON from the DB
     */
    ItemOptions readItemOptions(final Document optionsDocument, final String kind) throws IOException {
        return mapper.readValue(optionsDocument.toJson(), getType(kind).optionsType);
    }

    /**
     * Gets the {@link ItemType} corresponding to the kind presented.
     * @param kind the kind
     * @return the corresponding {@link ItemType}
     */
    ItemType getType(final String kind) {
        return stream(ItemType.values())
                .filter(value -> value.kind.equals(kind))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("'" + kind + "' is not a known kind!"));
    }

}

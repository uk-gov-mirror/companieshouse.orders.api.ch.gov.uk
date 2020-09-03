package uk.gov.companieshouse.orders.api.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.Document;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.orders.api.model.CertificateItemOptions;
import uk.gov.companieshouse.orders.api.model.CertifiedCopyItemOptions;
import uk.gov.companieshouse.orders.api.model.Item;
import uk.gov.companieshouse.orders.api.model.ItemOptions;

import java.io.IOException;
import java.util.List;

import static java.util.Arrays.stream;

/**
 * Reads (deserialises) the item options for each item on an order read from the DB.
 */
@Component
class OrderItemOptionsReader {

    private final ObjectMapper mapper;
    private static final String CERTIFICATE_ITEM_OPTIONS_TYPE = "CertificateItemOptions";
    private static final String CERTIFIEDCOPY_ITEM_OPTIONS_TYPE = "CertifiedCopyItemOptions";

    OrderItemOptionsReader(final ObjectMapper mapper) {
        this.mapper = mapper;
    }

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
     * Reads the item options for each item on the order in the DB as represented by the order document.
     * @param items {@link List List&lt;Item&gt;} of the items read from the order
     * @param orderDocument the order document representing either a
     * {@link uk.gov.companieshouse.orders.api.model.Checkout} or an
     * {@link uk.gov.companieshouse.orders.api.model.Order}
     * @param orderType a human-readable name for the type of order, i.e., "checkout" or "order"
     */
    void readOrderItemsOptions(final List<Item> items, final Document orderDocument, final String orderType) {
        if (orderDocument == null) {
            throw new IllegalStateException("No " + orderType + " document found on event.");
        }
        for (int itemIndex = 0; itemIndex < items.size(); itemIndex++) {
            try {
                readItemOptions(itemIndex, items, orderDocument);
            } catch (IOException ioe) {
                throw new IllegalStateException("Error parsing item options JSON: " + ioe.getMessage());
            }
        }
    }

    /**
     * Reads the item options for the item indicated by the index. How to read these options correctly from the DB
     * is determined from the kind of the item. Updates the item options assigned to the item.
     * @param itemIndex the item index identifying both the item within the items collection and its {@link Document}
     * @param items the items held by the order
     * @param orderDocument the order {@link Document} from the DB
     * @throws IOException should there be an issue parsing the item options JSON from the DB
     */
    void readItemOptions(final int itemIndex, final List<Item> items, final Document orderDocument)
            throws IOException {
        final Item item = items.get(itemIndex);
        final Document optionsDocument = getItemOptionsDocument(orderDocument, itemIndex);
        if (optionsDocument == null) {
            // No item options to read.
            return;
        }
        ItemOptions options = readItemOptions(optionsDocument, item.getKind());
        if (item.getKind().equals(ItemType.CERTIFICATE.kind)){
            options.setType(CERTIFICATE_ITEM_OPTIONS_TYPE);
        }
        else {
            options.setType(CERTIFIEDCOPY_ITEM_OPTIONS_TYPE);
        }
        item.setItemOptions(options);
    }

    /**
     * Gets the document representing the item's item options.
     * @param orderDocument the {@link Document} representing the order
     * @param itemIndex the index of the item within the collection of items held within the order
     * @return the {@link Document} representing the item options for the item identified by the index
     */
    Document getItemOptionsDocument(final Document orderDocument, final int itemIndex) {
        @SuppressWarnings("unchecked") // Java language limitation (type erasure)
        final Document itemDocument =
                ((List<Document>) orderDocument.get("data", Document.class).get("items", List.class)).get(itemIndex);
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

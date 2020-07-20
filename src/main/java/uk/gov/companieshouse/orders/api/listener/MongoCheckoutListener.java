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

    private enum ItemType {
        CERTIFICATE("item#certificate", CertificateItemOptions.class),
        CERTIFIED_COPY("item#certified-copy", CertifiedCopyItemOptions.class);

        ItemType(final String kind, final Class<? extends ItemOptions> optionsType) {
            this.kind = kind;
            this.optionsType = optionsType;
        }

        private String kind;
        private Class<? extends ItemOptions> optionsType;
    }

    @Override
    public void onAfterConvert(final AfterConvertEvent<Checkout> event) {

        final Document checkoutDocument = event.getDocument();
        if (checkoutDocument == null) {
            // TODO GCI-1022 This seems unlikely?
            throw new IllegalStateException("No checkout document found on event.");
        }
        final List<Item> items = event.getSource().getData().getItems();

        for (int index = 0; index < items.size(); index++) {
            try {
                readItemOptions(index, items, checkoutDocument);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    // TODO GCI-1022 Tidy up
    void readItemOptions(final int index, final List<Item> items, final Document checkoutDocument) throws IOException {
        final Item item = items.get(index);
        @SuppressWarnings("unchecked") // Java language limitation (type erasure)
        final Document itemDocument =
                ((List<Document>) checkoutDocument.get("data", Document.class).get("items", List.class)).get(index);
        final Document optionsDocument = itemDocument.get("item_options", Document.class);
        if (optionsDocument == null) {
            // No item options to read.
            return;
        }
        final ItemOptions options = mapper.readValue(optionsDocument.toJson(), getType(item.getKind()).optionsType);
        item.setItemOptions(options);
    }

    ItemType getType(final String kind) {
        return stream(ItemType.values())
                .filter(value -> value.kind.equals(kind))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("'" + kind + "' is not a known kind!"));
    }

}

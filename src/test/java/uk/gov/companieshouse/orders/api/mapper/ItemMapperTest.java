package uk.gov.companieshouse.orders.api.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import uk.gov.companieshouse.orders.api.dto.BasketItemDTO;
import uk.gov.companieshouse.orders.api.model.Certificate;
import uk.gov.companieshouse.orders.api.model.Item;
import uk.gov.companieshouse.orders.api.model.ItemCosts;

import java.util.ArrayList;
import java.util.List;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@ExtendWith(SpringExtension.class)
@SpringJUnitConfig(ItemMapperTest.Config.class)
public class ItemMapperTest {

    private static final String ID = "ID00000001";
    private static final String COMPANY_NUMBER = "00006400";
    private static final String COMPANY_NAME = "Test Test Ltd";
    private static final String CUSTOMER_REFERENCE = "Testing Reference";
    private static final String DESCRIPTION = "Test Test Desc";
    private static final String DESCRIPTION_IDENTIFIER = "Test Test Desc Id";
    private static final String ETAG = "Test etag";
    private static final String ITEM_URI = "/item-uri";
    private static final String KIND = "test kind";
    private static final boolean POSTAL_DELIVERY = true;

    private static final String TOTAL_ITEM_COST = "15";
    private static final String DISCOUNT_APPLIED_1 = "0";
    private static final String ITEM_COST_1 = "5";
    private static final String CALCULATED_COST_1 = "5";
    private static final String DISCOUNT_APPLIED_2 = "10";
    private static final String ITEM_COST_2 = "5";
    private static final String CALCULATED_COST_2 = "5";
    private static final String DISCOUNT_APPLIED_3 = "0";
    private static final String ITEM_COST_3 = "5";
    private static final String CALCULATED_COST_3 = "5";
    private static final String INVALID_ITEM_URI = "invalid_uri";
    private static final String POSTAGE_COST = "0";

    @Configuration
    @ComponentScan(basePackageClasses = ItemMapperTest.class)
    static class Config {}

    @Autowired
    ItemMapper itemMapperUnderTest;

    private Item createCertificate(){
        Certificate certificate = new Certificate();
        certificate.setId(ID);
        certificate.setCompanyNumber(COMPANY_NUMBER);
        certificate.setCompanyName(COMPANY_NAME);
        certificate.setCustomerReference(CUSTOMER_REFERENCE);
        certificate.setDescription(DESCRIPTION);
        certificate.setDescriptionIdentifier(DESCRIPTION_IDENTIFIER);
        certificate.setEtag(ETAG);
        certificate.setItemUri(ITEM_URI);
        certificate.setKind(KIND);
        certificate.setPostalDelivery(POSTAL_DELIVERY);
        certificate.setTotalItemCost(TOTAL_ITEM_COST);
        certificate.setPostageCost(POSTAGE_COST);
        certificate.setItemCosts(createItemCosts());

        return certificate;
    }

    private List<ItemCosts> createItemCosts(){
        List<ItemCosts> itemCosts = new ArrayList<>();
        ItemCosts itemCosts1 = new ItemCosts();
        itemCosts1.setDiscountApplied(DISCOUNT_APPLIED_1);
        itemCosts1.setItemCost(ITEM_COST_1);
        itemCosts1.setCalculatedCost(CALCULATED_COST_1);
        itemCosts.add(itemCosts1);
        ItemCosts itemCosts2 = new ItemCosts();
        itemCosts2.setDiscountApplied(DISCOUNT_APPLIED_2);
        itemCosts2.setItemCost(ITEM_COST_2);
        itemCosts2.setCalculatedCost(CALCULATED_COST_2);
        itemCosts.add(itemCosts2);
        ItemCosts itemCosts3 = new ItemCosts();
        itemCosts3.setDiscountApplied(DISCOUNT_APPLIED_3);
        itemCosts3.setItemCost(ITEM_COST_3);
        itemCosts3.setCalculatedCost(CALCULATED_COST_3);
        itemCosts.add(itemCosts3);

        return itemCosts;
    }

    @Test
    public void testItemToBasketItemDTO(){
        Item itemToConvert = createCertificate();

        BasketItemDTO basketItemDTO = itemMapperUnderTest.itemToBasketItemDTO(itemToConvert);
        List<ItemCosts> itemCosts = basketItemDTO.getItemCosts();

        assertThat(ID, is(basketItemDTO.getId()));
        assertThat(COMPANY_NAME, is(basketItemDTO.getCompanyName()));
        assertThat(COMPANY_NUMBER, is(basketItemDTO.getCompanyNumber()));
        assertThat(CUSTOMER_REFERENCE, is(basketItemDTO.getCustomerReference()));
        assertThat(DESCRIPTION, is(basketItemDTO.getDescription()));
        assertThat(DESCRIPTION_IDENTIFIER, is(basketItemDTO.getDescriptionIdentifier()));
        assertThat(ETAG, is(basketItemDTO.getEtag()));
        assertThat(ITEM_URI, is(basketItemDTO.getItemUri()));
        assertThat(KIND, is(basketItemDTO.getKind()));
        assertThat(POSTAL_DELIVERY, is(basketItemDTO.getPostalDelivery()));
        assertThat(TOTAL_ITEM_COST, is(basketItemDTO.getTotalItemCost()));
        assertThat(POSTAGE_COST, is(basketItemDTO.getPostageCost()));

        assertThat(DISCOUNT_APPLIED_1, is(itemCosts.get(0).getDiscountApplied()));
        assertThat(ITEM_COST_1, is(itemCosts.get(0).getItemCost()));
        assertThat(CALCULATED_COST_1, is(itemCosts.get(0).getCalculatedCost()));
        assertThat(DISCOUNT_APPLIED_2, is(itemCosts.get(1).getDiscountApplied()));
        assertThat(ITEM_COST_2, is(itemCosts.get(1).getItemCost()));
        assertThat(CALCULATED_COST_2, is(itemCosts.get(1).getCalculatedCost()));
        assertThat(DISCOUNT_APPLIED_3, is(itemCosts.get(2).getDiscountApplied()));
        assertThat(ITEM_COST_3, is(itemCosts.get(2).getItemCost()));
        assertThat(CALCULATED_COST_3, is(itemCosts.get(2).getCalculatedCost()));
    }
}

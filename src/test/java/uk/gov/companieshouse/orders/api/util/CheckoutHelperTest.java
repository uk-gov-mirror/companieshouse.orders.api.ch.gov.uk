package uk.gov.companieshouse.orders.api.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.companieshouse.orders.api.model.Checkout;
import uk.gov.companieshouse.orders.api.model.CheckoutData;
import uk.gov.companieshouse.orders.api.model.Item;
import uk.gov.companieshouse.orders.api.model.ItemCosts;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@ExtendWith(SpringExtension.class)
public class CheckoutHelperTest {
    private static final int EXPECTED_TOTAL_ORDER_COST_SUCCESS = 20;
    private static final int EXPECTED_TOTAL_ORDER_COST_NO_ITEMCOSTS = 0;
    private static final String POSTAGE_COST = "5";
    private static final String DISCOUNT_APPLIED_1 = "0";
    private static final String ITEM_COST_1 = "5";
    private static final String CALCULATED_COST_1 = "5";
    private static final String DISCOUNT_APPLIED_2 = "10";
    private static final String ITEM_COST_2 = "5";
    private static final String CALCULATED_COST_2 = "5";
    private static final String DISCOUNT_APPLIED_3 = "0";
    private static final String ITEM_COST_3 = "5";
    private static final String CALCULATED_COST_3 = "5";

    private final CheckoutHelper checkoutHelper = new CheckoutHelper();

    @Test
    @DisplayName("calculateTotalOrderCostForCheckout sums `total order cost` correctly")
    void calculateTotalOrderCostForCheckout(){
        // Given
        Checkout checkout = new Checkout();
        checkout.setData(createCheckoutData());

        // When
        int actualTotalOrderCost = checkoutHelper.calculateTotalOrderCostForCheckout(checkout);

        // Then
        assertThat(actualTotalOrderCost, is(EXPECTED_TOTAL_ORDER_COST_SUCCESS));
    }

    @Test
    @DisplayName("calculateTotalOrderCostForCheckout returns zero `total order cost` when no itemcosts in item")
    void calculateTotalOrderCostForCheckoutNoItemCosts(){
        // Given
        Checkout checkout = new Checkout();
        checkout.setData(new CheckoutData());

        // When
        int actualTotalOrderCost = checkoutHelper.calculateTotalOrderCostForCheckout(checkout);

        // Then
        assertThat(actualTotalOrderCost, is(EXPECTED_TOTAL_ORDER_COST_NO_ITEMCOSTS));
    }

    private CheckoutData createCheckoutData(){
        List<Item> items = new ArrayList<>();
        items.add(createCertificateItem());
        CheckoutData checkoutData = new CheckoutData();
        checkoutData.setItems(items);

        return checkoutData;
    }

    private Item createCertificateItem(){
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

        Item item = new Item();
        item.setPostageCost(POSTAGE_COST);
        item.setItemCosts(itemCosts);

        return item;
    }
}

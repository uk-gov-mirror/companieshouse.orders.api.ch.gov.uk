package uk.gov.companieshouse.orders.api.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import uk.gov.companieshouse.orders.api.model.Basket;
import uk.gov.companieshouse.orders.api.model.BasketData;
import uk.gov.companieshouse.orders.api.model.DeliveryDetails;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BasketRespositoryImplTest {

    @InjectMocks
    private BasketRepositoryImpl repositoryUnderTest;

    @Mock
    MongoTemplate mongoTemplate;

    @Mock
    Basket basket;

    @Mock
    BasketData basketData;

    @Mock
    DeliveryDetails deliveryDetails;

    @Test
    public void clearBasketDataByIdVerifyFindAndModifyCalledOnce() {
        when(mongoTemplate.findOne(any(), any())).thenReturn(basket);
        when(basket.getData()).thenReturn(basketData);
        when(basketData.getDeliveryDetails()).thenReturn(deliveryDetails);
        repositoryUnderTest.clearBasketDataById("ID");
        verify(mongoTemplate, times(1)).findOne(any(Query.class), eq(Basket.class));
        verify(mongoTemplate, times(1)).findAndModify(any(Query.class), any(Update.class), eq(Basket.class));
    }

}

package uk.gov.companieshouse.orders.api.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import uk.gov.companieshouse.orders.api.model.Basket;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class BasketRespositoryImplTest {

    @InjectMocks
    private BasketRepositoryImpl repositoryUnderTest;

    @Mock
    MongoTemplate mongoTemplate;

    @Test
    public void clearBasketDataByIdVerifyFindAndModifyCalledOnce() {
        repositoryUnderTest.clearBasketDataById("ID");
        verify(mongoTemplate, times(1)).findAndModify(any(Query.class), any(Update.class), eq(Basket.class));
    }

}

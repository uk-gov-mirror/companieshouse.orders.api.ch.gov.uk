package uk.gov.companieshouse.orders.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.environment.exception.EnvironmentVariableException;
import uk.gov.companieshouse.environment.impl.EnvironmentReaderImpl;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.orders.api.environment.RequiredEnvironmentVariables;
import uk.gov.companieshouse.orders.api.logging.LoggingUtils;

@SpringBootApplication
public class OrdersApiApplication {
    private static Logger LOGGER = LoggingUtils.getLogger();

    public static final String REQUEST_ID_HEADER_NAME = "X-Request-ID";

    public static void main(String[] args) {
        if(checkEnvironmentVariables()) {
            SpringApplication.run(OrdersApiApplication.class, args);
        }
    }


    /**
     * Method to check if all of the required configuration variables
     * defined in EnvironmentVariables enum have been set to a value
     */
    public static boolean checkEnvironmentVariables() {
        EnvironmentReader environmentReader = new EnvironmentReaderImpl();
        boolean allVariablesPresent = true;
        LOGGER.info("Checking all environment variables present");
        for(RequiredEnvironmentVariables param : RequiredEnvironmentVariables.values()) {
            try{
                environmentReader.getMandatoryString(param.getName());
            } catch (EnvironmentVariableException eve) {
                allVariablesPresent = false;
                LOGGER.error(String.format("Required config item %s missing", param.getName()));
            }
        }

        return allVariablesPresent;
    }
}

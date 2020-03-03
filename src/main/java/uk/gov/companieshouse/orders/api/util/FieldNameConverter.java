package uk.gov.companieshouse.orders.api.util;

import org.springframework.stereotype.Component;

@Component
public class FieldNameConverter {

    /**
     * Converts the field name provided to its corresponding snake case representation.
     * It takes into account letters and numbers
     * @param fieldName the name of the field to be converted (typically camel case)
     * @return the field name's snake case representation minus any <code>is_</code>
     * string, assumed to be a prefix.
     */
    public String toSnakeCase(final String fieldName) {
        String[] splitString = fieldName.split("(?=[A-Z0-9']+)");
        return String.join("_", splitString).toLowerCase().replace("is_", "");
    }

}
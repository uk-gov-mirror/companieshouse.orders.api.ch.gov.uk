package uk.gov.companieshouse.orders.api.converter;

import static uk.gov.companieshouse.orders.api.converter.LocalDateTimeConverter.convertStringToLocalDateTime;
import java.time.LocalDateTime;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class StringToLocalDateTimeConverterFactory  implements ConverterFactory<String, LocalDateTime> {
    
    @Override
    public <T extends LocalDateTime> Converter<String, T> getConverter(Class<T> targetType) {
        return new StringToLocalDateTimeConverter(targetType);
    }
    
    private class StringToLocalDateTimeConverter<T extends String> implements Converter<String, LocalDateTime> {
                
        private Class<T> string;
        
        public StringToLocalDateTimeConverter(Class<T> string) {
            this.string = string;
        }

        @Override
        public LocalDateTime convert(String source) {
            return convertStringToLocalDateTime(source);
        }
    }

}

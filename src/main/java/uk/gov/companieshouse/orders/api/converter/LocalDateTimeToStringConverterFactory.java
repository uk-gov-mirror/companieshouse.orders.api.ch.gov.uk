package uk.gov.companieshouse.orders.api.converter;

import static uk.gov.companieshouse.orders.api.converter.LocalDateTimeConverter.convertLocalDateTimeToString;
import java.time.LocalDateTime;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class LocalDateTimeToStringConverterFactory implements ConverterFactory<LocalDateTime, String>{
    

    @Override
    public <T extends String> Converter<LocalDateTime, T> getConverter(Class<T> targetType) {
        return new LocalDateTimeToStringConverter(targetType);
    }
    
    private class LocalDateTimeToStringConverter<T extends LocalDateTime> implements Converter<LocalDateTime, String> {
                
        private Class<T> localDatetime;
        
        public LocalDateTimeToStringConverter(Class<T> localDateTime) {
            this.localDatetime = localDateTime;
        }

        @Override
        public String convert(LocalDateTime localDateTime) {
            return convertLocalDateTimeToString(localDateTime);
        }
    }

}

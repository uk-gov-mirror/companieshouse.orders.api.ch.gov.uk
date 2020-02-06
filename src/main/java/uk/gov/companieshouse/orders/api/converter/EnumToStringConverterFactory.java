package uk.gov.companieshouse.orders.api.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public final class EnumToStringConverterFactory implements ConverterFactory<Enum, String> {

    @Override
    public <T extends String> Converter<Enum, T> getConverter(Class<T> targetType) {
        return new EnumToStringConverter(targetType);
    }

    private final class EnumToStringConverter<T extends Enum> implements Converter<T, String> {

        private Class<T> enumType;

        public EnumToStringConverter(Class<T> enumType) {
            this.enumType = enumType;
        }

        public String convert(T source) {
            return source.name().toLowerCase();
        }
    }
}
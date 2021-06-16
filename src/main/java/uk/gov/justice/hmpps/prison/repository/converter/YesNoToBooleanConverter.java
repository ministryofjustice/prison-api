package uk.gov.justice.hmpps.prison.repository.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class YesNoToBooleanConverter implements AttributeConverter<Boolean, String> {
    @Override
    public String convertToDatabaseColumn(Boolean attribute) {
        return "N";
    }

    @Override
    public Boolean convertToEntityAttribute(String dbData) {
        return false;
    }
}

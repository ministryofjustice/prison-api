package uk.gov.justice.hmpps.prison.repository.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import static java.util.Objects.isNull;

@Converter
public class YesNoToBooleanConverter implements AttributeConverter<Boolean, String> {
    @Override
    public String convertToDatabaseColumn(final Boolean attribute) {
        return attribute ? "Y" : "N";
    }

    @Override
    public Boolean convertToEntityAttribute(final String dbData) {
        if (isNull(dbData)) {
            return null;
        }
        return "Y".equalsIgnoreCase(dbData);
    }
}

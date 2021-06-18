package uk.gov.justice.hmpps.prison.repository.converter;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class YesNoToBooleanConverterTest {

    private final YesNoToBooleanConverter converter = new YesNoToBooleanConverter();

    @Test
    public void convertToDatabaseColumn() {
        assertThat(converter.convertToDatabaseColumn(true)).isEqualTo("Y");
        assertThat(converter.convertToDatabaseColumn(false)).isEqualTo("N");
    }

    @Test
    public void convertToEntityAttribute() {
        assertThat(converter.convertToEntityAttribute("Y")).isTrue();
        assertThat(converter.convertToEntityAttribute("N")).isFalse();
    }

    @Test
    public void testConvertToEntityAttributeHandlesNull() {
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }
}
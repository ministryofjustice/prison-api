package uk.gov.justice.hmpps.nomis.prison.datacompliance.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.justice.hmpps.nomis.prison.datacompliance.service.IdentifierValidation.getValidCroComponents;
import static uk.gov.justice.hmpps.nomis.prison.datacompliance.service.IdentifierValidation.getValidPncComponents;

class IdentifierValidationTest {

    @Test
    void validPncNumber() {

        assertThat(getValidPncComponents("99/123456X")).isPresent();
        assertThat(getValidPncComponents("1999/0123456X")).isPresent();
        assertThat(getValidPncComponents("1999/1234567B")).isPresent();
        assertThat(getValidPncComponents("20/9N")).isPresent();
        assertThat(getValidPncComponents("2020/00009N")).isPresent();
        assertThat(getValidPncComponents("21/9999M")).isPresent();

        assertThat(getValidPncComponents("1999/1234567A")).isEmpty();
        assertThat(getValidPncComponents("1999/1234567")).isEmpty();
        assertThat(getValidPncComponents("1999/1234567BB")).isEmpty();
        assertThat(getValidPncComponents("19991234567B")).isEmpty();
        assertThat(getValidPncComponents("1999/ABCDEFGH")).isEmpty();
        assertThat(getValidPncComponents("1999/0001234567B")).isEmpty();
        assertThat(getValidPncComponents("20/9Z")).isEmpty();
        assertThat(getValidPncComponents("20001/9999M")).isEmpty();
        assertThat(getValidPncComponents("NONE")).isEmpty();
        assertThat(getValidPncComponents("N/A")).isEmpty();
        assertThat(getValidPncComponents("invalid")).isEmpty();
        assertThat(getValidPncComponents("?")).isEmpty();
    }

    @Test
    void validCroNumber() {

        assertThat(getValidCroComponents("123456/99L")).isPresent();
        assertThat(getValidCroComponents("001234/88P")).isPresent();
        assertThat(getValidCroComponents("1234/88P")).isPresent();
        assertThat(getValidCroComponents("1/11X")).isPresent();

        assertThat(getValidCroComponents("SF99/12345M")).isPresent();
        assertThat(getValidCroComponents("SF88/01234H")).isPresent();
        assertThat(getValidCroComponents("SF88/1234H")).isPresent();
        assertThat(getValidCroComponents("SF11/1C")).isPresent();

        assertThat(getValidCroComponents("123456/99Z")).isEmpty();
        assertThat(getValidCroComponents("1234567/99L")).isEmpty();
        assertThat(getValidCroComponents("123456/99LL")).isEmpty();
        assertThat(getValidCroComponents("SF99/123456Z")).isEmpty();
        assertThat(getValidCroComponents("SF99/1234567L")).isEmpty();
        assertThat(getValidCroComponents("NONE")).isEmpty();
        assertThat(getValidCroComponents("N/A")).isEmpty();
        assertThat(getValidCroComponents("invalid")).isEmpty();
        assertThat(getValidCroComponents("?")).isEmpty();
    }
}

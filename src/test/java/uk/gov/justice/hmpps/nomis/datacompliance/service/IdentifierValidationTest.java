package uk.gov.justice.hmpps.nomis.datacompliance.service;

import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static uk.gov.justice.hmpps.nomis.datacompliance.service.IdentifierValidation.isValidCroNumber;
import static uk.gov.justice.hmpps.nomis.datacompliance.service.IdentifierValidation.isValidPncNumber;

class IdentifierValidationTest {

    @Test
    void validPncNumber() {

        assertTrue(isValidPncNumber("99/123456X"));
        assertTrue(isValidPncNumber("1999/0123456X"));
        assertTrue(isValidPncNumber("1999/1234567B"));
        assertTrue(isValidPncNumber("20/9N"));
        assertTrue(isValidPncNumber("2020/00009N"));
        assertTrue(isValidPncNumber("21/9999M"));

        assertFalse(isValidPncNumber("1999/1234567A"));
        assertFalse(isValidPncNumber("1999/1234567"));
        assertFalse(isValidPncNumber("1999/1234567BB"));
        assertFalse(isValidPncNumber("19991234567B"));
        assertFalse(isValidPncNumber("1999/ABCDEFGH"));
        assertFalse(isValidPncNumber("1999/0001234567B"));
        assertFalse(isValidPncNumber("20/9Z"));
        assertFalse(isValidPncNumber("20001/9999M"));
        assertFalse(isValidPncNumber("NONE"));
        assertFalse(isValidPncNumber("N/A"));
        assertFalse(isValidPncNumber("invalid"));
        assertFalse(isValidPncNumber("?"));
    }

    @Test
    void validCroNumber() {

        assertTrue(isValidCroNumber("123456/99L"));
        assertTrue(isValidCroNumber("001234/88P"));
        assertTrue(isValidCroNumber("1234/88P"));
        assertTrue(isValidCroNumber("1/11X"));

        assertTrue(isValidCroNumber("SF99/123456L"));
        assertTrue(isValidCroNumber("SF88/001234P"));
        assertTrue(isValidCroNumber("SF88/1234P"));
        assertTrue(isValidCroNumber("SF11/1X"));

        assertFalse(isValidCroNumber("123456/99Z"));
        assertFalse(isValidCroNumber("1234567/99L"));
        assertFalse(isValidCroNumber("123456/99LL"));
        assertFalse(isValidCroNumber("SF99/123456Z"));
        assertFalse(isValidCroNumber("SF99/1234567L"));
        assertFalse(isValidCroNumber("NONE"));
        assertFalse(isValidCroNumber("N/A"));
        assertFalse(isValidCroNumber("invalid"));
        assertFalse(isValidCroNumber("?"));
    }
}

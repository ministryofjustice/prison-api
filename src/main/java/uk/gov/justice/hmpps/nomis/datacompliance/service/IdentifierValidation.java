package uk.gov.justice.hmpps.nomis.datacompliance.service;

import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static lombok.AccessLevel.PRIVATE;

/**
 * Following the definitions provided here:
 * https://assets.publishing.service.gov.uk/government/uploads/system/uploads/attachment_data/file/862971/cjs-data-standards-catalogue-6.pdf
 */
@NoArgsConstructor(access = PRIVATE)
public class IdentifierValidation {

    private static final Pattern VALID_PNC_FORMAT = Pattern.compile("^[0-9]{0,2}([0-9]{2})/([0-9]{1,7})([A-Z])$");
    private static final Pattern VALID_CRO_FORMAT = Pattern.compile("^([0-9]{1,6})/([0-9]{2})([A-Z])$");
    private static final Pattern VALID_CRO_SF_FORMAT = Pattern.compile("^SF([0-9]{2})/([0-9]{1,6})([A-Z])$");

    // All letters from the alphabet excluding 'I', 'O' and 'S', with 'Z' at the 0th index:
    private static final char[] VALID_CHECKSUM_CHARACTERS = {'Z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'L', 'M', 'N', 'P', 'Q', 'R', 'T', 'U', 'V', 'W', 'X', 'Y'};

    public static boolean isValidPncNumber(final String pncNumber) {

        final var pncMatcher = VALID_PNC_FORMAT.matcher(pncNumber);

        if (!pncMatcher.matches()) {
            return false;
        }

        return isChecksumAMatch(
                ChecksumComponents.builder()
                        .year(pncMatcher.group(1))
                        .serial(pncMatcher.group(2))
                        .serialFormat("%07d")
                        .checksum(pncMatcher.group(3))
                        .build());
    }

    public static boolean isValidCroNumber(final String croNumber) {

        final var croMatcher = VALID_CRO_FORMAT.matcher(croNumber);
        final var croSfMatcher = VALID_CRO_SF_FORMAT.matcher(croNumber);

        if (croMatcher.matches()) {
            return isChecksumAMatch(
                    ChecksumComponents.builder()
                            .year(croMatcher.group(2))
                            .serial(croMatcher.group(1))
                            .serialFormat("%06d")
                            .checksum(croMatcher.group(3))
                            .build());
        }

        if (croSfMatcher.matches()) {
            return isChecksumAMatch(
                    ChecksumComponents.builder()
                            .year(croSfMatcher.group(1))
                            .serial(croSfMatcher.group(2))
                            .serialFormat("%06d")
                            .checksum(croSfMatcher.group(3))
                            .build());
        }

        return false;
    }

    private static boolean isChecksumAMatch(final ChecksumComponents components) {
        return generateChecksumChar(components.getDerivedValue()) == components.getChecksumChar();
    }

    private static char generateChecksumChar(final int digits) {
        return VALID_CHECKSUM_CHARACTERS[digits % VALID_CHECKSUM_CHARACTERS.length];
    }

    @Builder
    private static class ChecksumComponents {
        private final String year;
        private final String serial;
        private final String serialFormat;
        private final String checksum;

        private int getDerivedValue() {
            return parseInt(year + format(serialFormat, parseInt(serial)));
        }

        private char getChecksumChar() {
            return checksum.charAt(0);
        }
    }
}

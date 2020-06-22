package uk.gov.justice.hmpps.nomis.datacompliance.service;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.regex.Matcher;
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
    private static final Pattern VALID_CRO_SF_FORMAT = Pattern.compile("^SF([0-9]{2})/([0-9]{1,5})([A-Z])$");

    // All letters from the alphabet excluding 'I', 'O' and 'S', with 'Z' at the 0th index:
    private static final char[] VALID_CHECKSUM_CHARACTERS = {'Z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'L', 'M', 'N', 'P', 'Q', 'R', 'T', 'U', 'V', 'W', 'X', 'Y'};

    public static Optional<ChecksumComponents> getValidPncComponents(final String pncNumber) {
        return matching(pncNumber, VALID_PNC_FORMAT)
                .map(match -> ChecksumComponents.builder()
                        .year(match.group(1))
                        .serial(match.group(2))
                        .serialFormat("%07d")
                        .checksum(match.group(3))
                        .build())
                .filter(IdentifierValidation::isChecksumAMatch);
    }

    public static Optional<ChecksumComponents> getValidCroComponents(final String croNumber) {
        return getValidStandardCroComponents(croNumber)
                .or(() -> getValidCroSfComponents(croNumber));
    }

    private static Optional<ChecksumComponents> getValidStandardCroComponents(final String croNumber) {
        return matching(croNumber, VALID_CRO_FORMAT)
                .map(match -> ChecksumComponents.builder()
                        .year(match.group(2))
                        .serial(match.group(1))
                        .serialFormat("%06d")
                        .checksum(match.group(3))
                        .build())
                .filter(IdentifierValidation::isChecksumAMatch);
    }

    private static Optional<ChecksumComponents> getValidCroSfComponents(final String croNumber) {
        return matching(croNumber, VALID_CRO_SF_FORMAT)
                .map(match -> ChecksumComponents.builder()
                        .year(match.group(1))
                        .serial(match.group(2))
                        .serialFormat("%05d")
                        .checksum(match.group(3))
                        .build())
                .filter(IdentifierValidation::isChecksumAMatch);
    }

    private static Optional<Matcher> matching(final String identity, final Pattern pattern) {
        return Optional.of(pattern.matcher(identity)).filter(Matcher::matches);
    }

    private static boolean isChecksumAMatch(final ChecksumComponents components) {
        return generateChecksumChar(components.getDerivedValue()) == components.getChecksumChar();
    }

    private static char generateChecksumChar(final int digits) {
        return VALID_CHECKSUM_CHARACTERS[digits % VALID_CHECKSUM_CHARACTERS.length];
    }

    @Getter
    @Builder
    public static class ChecksumComponents {
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

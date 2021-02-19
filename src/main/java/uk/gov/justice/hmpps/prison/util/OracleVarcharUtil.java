package uk.gov.justice.hmpps.prison.util;

import com.google.common.base.Utf8;

import java.nio.charset.StandardCharsets;

public class OracleVarcharUtil {

    private static int MAX_VARCHAR_SIZE_CHECK_THRESHOLD = 3950;
    private static int MAX_VARCHAR_SIZE = 4000;

    /**
     * Ensures the given string does not exceed the limit on characters in an Oracle DB (otherwise we get ORA-01461 errors).
     * This is required in addition to the 4000 character validation as it may exceed that when unicode encoding is applied.
     */
    public static String enforceMaximumTextSize(final String inputText) {
        String truncatedText = inputText;
        if (inputText.length() > MAX_VARCHAR_SIZE_CHECK_THRESHOLD) {
            if (Utf8.encodedLength(inputText) > MAX_VARCHAR_SIZE) {
                truncatedText = truncateUtf8String(inputText, MAX_VARCHAR_SIZE);
            }
        }
        return truncatedText;
    }

    private static String truncateUtf8String(final String inputText, final int maximumOutputSize) {
        // The simplest and most performant solution is to convert the characters from unicode to ASCII
        byte[] originalBytes = inputText.getBytes(StandardCharsets.US_ASCII);
        return new String(originalBytes);
    }
}

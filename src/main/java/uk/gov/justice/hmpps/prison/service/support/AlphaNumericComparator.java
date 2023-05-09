package uk.gov.justice.hmpps.prison.service.support;

import java.util.Comparator;
import java.util.regex.Pattern;

public class AlphaNumericComparator implements Comparator<String> {

    private static final Pattern JUST_LETTERS = Pattern.compile("[^a-zA-Z]");
    private static final Pattern JUST_NUMBERS = Pattern.compile("[^\\d]");

    @Override
    public int compare(final String leftValue, final String rightValue) {

        final var left = (leftValue != null) ? leftValue : "";
        final var right = (rightValue != null) ? rightValue : "";

        if (shouldApplyAlphaNumericSorting(left, right)) {

            final var sortIndex = compareAlpha(left, right);
            if (sortIndex != 0)
                return sortIndex;

            return compareAlphaNumbers(left, right);
        }

        return left.compareToIgnoreCase(right);
    }

    private Boolean shouldApplyAlphaNumericSorting(final String left, final String right) {
        return lastValueIsANumber(left) && lastValueIsANumber(right);
    }

    private Boolean lastValueIsANumber(final String value) {

        if (value.isEmpty())
            return false;

        final var data = value.toCharArray();

        return Character.isDigit(data[data.length - 1]);
    }

    private int compareAlpha(final String left, final String right) {
        return stripNumbers(left).compareToIgnoreCase(stripNumbers(right));
    }

    private int compareAlphaNumbers(final String left, final String right) {
        final var leftNumber = Integer.parseInt(stripLetters(left));
        final var rightNumber = Integer.parseInt(stripLetters(right));

        return leftNumber - rightNumber;
    }

    private String stripNumbers(final String value) {
        return JUST_LETTERS.matcher(value).replaceAll("").trim();
    }

    private String stripLetters(final String value) {
        return JUST_NUMBERS.matcher(value).replaceAll("").trim();
    }
}

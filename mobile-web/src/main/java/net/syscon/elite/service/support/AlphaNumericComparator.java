package net.syscon.elite.service.support;

import java.util.Comparator;

public class AlphaNumericComparator implements Comparator<String> {

    @Override
    public int compare(String leftValue, String rightValue) {

        String left = (leftValue != null) ? leftValue : "";
        String right = (rightValue != null) ? rightValue : "";

        if (shouldApplyAlphaNumericSorting(left, right)) {

            int sortIndex = compareAlpha(left, right);
            if(sortIndex != 0)
                return sortIndex;

            return compareAlphaNumbers(left, right);
        }

        return left.compareToIgnoreCase(right);
    }

    private Boolean shouldApplyAlphaNumericSorting(String left, String right) {
        return lastValueIsANumber(left) && lastValueIsANumber(right);
    }

    private Boolean lastValueIsANumber(String value) {

        if(value.isEmpty())
            return false;

        char[] data = value.toCharArray();

        return !Character.isAlphabetic(data[data.length-1]);
    }

    private int compareAlpha(String left, String right) {
        return stripNumbers(left).compareToIgnoreCase(stripNumbers(right));
    }

    private int compareAlphaNumbers(String left, String right) {
        int leftNumber = Integer.parseInt(stripLetters(left));
        int rightNumber = Integer.parseInt(stripLetters(right));

        return leftNumber - rightNumber;
    }

    private String stripNumbers(String value) {
        String justLetters = "[^a-zA-Z]";

        return value.replaceAll(justLetters, "").trim();
    }

    private String stripLetters(String value){
        String justNumbers = "[^\\d]";

        return value.replaceAll(justNumbers, "").trim();
    }
}
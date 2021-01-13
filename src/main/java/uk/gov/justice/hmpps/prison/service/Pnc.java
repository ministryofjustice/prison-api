package uk.gov.justice.hmpps.prison.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

import static java.lang.String.format;

@Getter
@AllArgsConstructor
public class Pnc {

    private final static Pattern LONG_PNC = Pattern.compile("^([0-9]{4})/[0-9]+[a-zA-Z]$");
    private final static Pattern SHORT_PNC = Pattern.compile("^([0-9]{2})/[0-9]+[a-zA-Z]$");

    private final String year;
    private final int serialNumber;
    private final char checksum;

    public String toString() {
        return format("%s/%d%c", year, serialNumber, checksum);
    }

    public Pnc(final String pncStr) {
        final var splitPnc = StringUtils.split(pncStr, "/");
        year = splitPnc[0];
        final var serial = splitPnc[1];
        serialNumber = Integer.parseInt(serial.substring(0, serial.length() - 1));
        checksum = StringUtils.substring(pncStr, pncStr.length() - 1).charAt(0);
    }


    public static boolean isPNCNumberLong(String pncStr) {
        return LONG_PNC.matcher(pncStr).find();
    }

    public static boolean isPNCNumberShort(String pncStr) {
        return SHORT_PNC.matcher(pncStr).find();
    }

    public static String addCenturyToYear(final String year) {
        final var yearAsInt = Integer.parseInt(year);
        if (yearAsInt >= 39 && yearAsInt <= 99) {
            return "19" + year;
        }
        return "20" + year;
    }
}

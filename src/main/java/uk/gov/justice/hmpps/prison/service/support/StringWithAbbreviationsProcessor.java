package uk.gov.justice.hmpps.prison.service.support;

import org.apache.commons.text.WordUtils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class containing methods for processing of strings which contain abbreviations.
 */
public class StringWithAbbreviationsProcessor {
    /**
     *
     * @param string string to convert
     * @return new location with correct titlecase
     *
     */
    public static String format(final String string) {
        // Handle the possibility of the userDescription being empty
        if (string == null) {
            return null;
        }
        var description = WordUtils.capitalizeFully(string);
        // Using word boundaries to find the right string ensures we catch the strings
        // wherever they appear in the description, while also avoiding replacing
        // the letter sequence should it appear in the middle of a word
        // e.g. this will not match 'mosaic' even though AIC is one of the abbreviations
        Matcher matcher = pattern.matcher(description);

        // There could be more than one abbreviation in a string,
        // e.g. HMP Moorland VCC Room 1
        // By using the string buffer and the appendReplacement method
        // we ensure that all the matching groups are replaced accordingly
        StringBuilder stringBuilder = new StringBuilder();
        while (matcher.find()) {
            var matched = matcher.group(1);
            matcher.appendReplacement(stringBuilder, matched.toUpperCase());
        }
        matcher.appendTail(stringBuilder);
        return stringBuilder.toString();
    }

    /**
     * List of abbreviations
     */
    public static final List<String> ABBREVIATIONS = List.of(
            "AAA",
            "ADTP",
            "AIC",
            "AM",
            "ATB",
            "BBV",
            "BHU",
            "BICS",
            "CAD",
            "CASU",
            "CES",
            "CGL",
            "CIT",
            "CSC",
            "CSCP",
            "CSU",
            "CTTLS",
            "CV",
            "DART",
            "DDU",
            "DHL",
            "DRU",
            "ETS",
            "ETSP",
            "ESOL",
            "FT",
            "GP",
            "GFSL",
            "HB\\d+",
            "HCC",
            "HDC",
            "HMP",
            "HMPYOI",
            "HR",
            "IAG",
            "ICT",
            "IDTS",
            "IMB",
            "IPD",
            "IPSO",
            "ISMS",
            "IT",
            "ITQ",
            "JAC",
            "LB\\d+",
            "LRC",
            "L&S",
            "MBU",
            "MCASU",
            "MDT",
            "MOD",
            "MPU",
            "NVQ",
            "NUJ",
            "OBP",
            "OMU",
            "OU",
            "PACT",
            "PASRO",
            "PCVL",
            "PE",
            "PICTA",
            "PIPE",
            "PM",
            "PT",
            "PTTLS",
            "RAM",
            "RAPT",
            "ROTL",
            "RSU",
            "SDP",
            "SIU",
            "SMS",
            "SOTP",
            "SPU",
            "STC",
            "TLC",
            "TSP",
            "TV",
            "UK",
            "VCC",
            "VDT",
            "VP",
            "VPU",
            "VTC",
            "WFC",
            "YPSMS",
            "YOI"
    );

    private static final Pattern pattern = Pattern.compile("\\b(" + String.join("|", ABBREVIATIONS) + ")\\b", Pattern.CASE_INSENSITIVE);
}

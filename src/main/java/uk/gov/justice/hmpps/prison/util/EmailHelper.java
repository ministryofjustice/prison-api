package uk.gov.justice.hmpps.prison.util;

import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class EmailHelper {
    public static String format(final String emailInput) {
        return StringUtils.replaceChars(StringUtils.lowerCase(StringUtils.trim(emailInput)), '’', '\'');
    }
}

package uk.gov.justice.hmpps.prison.api.model.v1;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

@Getter
public class OffenderIdentifier {
    // regular expression for validation integer
    private static final String INT_REGEXP = "[0-9]+";

    // regular expression for validating NOMS id
    private static final String NOMSID_REGEXP = "[a-zA-Z][0-9]{4}[a-zA-Z]{2}";

    private final String nomsId;
    private final Long rootOffenderId;
    private final String singleOffenderId;

    public OffenderIdentifier(final String offenderIdentifier) throws OffenderIdentifierInvalidException {
        if (StringUtils.isEmpty(offenderIdentifier)) {
            rootOffenderId = null;
            singleOffenderId = null;
            nomsId = null;
            // identify the type of offender_identifier passed in.
            // If it's an integer we assume its a root_offender_id
            // If it matches the noms number format then it's a noms id
            // otherwise assume single offender_id
        } else if (offenderIdentifier.matches(INT_REGEXP)) {
            rootOffenderId = Long.parseLong(offenderIdentifier);
            singleOffenderId = null;
            nomsId = null;
        } else if (offenderIdentifier.matches(NOMSID_REGEXP)) {
            nomsId = offenderIdentifier;
            singleOffenderId = null;
            rootOffenderId = null;
        } else if (offenderIdentifier.length() == 36) {
            singleOffenderId = offenderIdentifier;
            rootOffenderId = null;
            nomsId = null;
        } else {
            throw new OffenderIdentifierInvalidException("Invalid offender id " + offenderIdentifier);
        }
    }

    private static class OffenderIdentifierInvalidException extends HttpClientErrorException {
        OffenderIdentifierInvalidException(final String message) {
            super(HttpStatus.BAD_REQUEST, message);
        }
    }
}

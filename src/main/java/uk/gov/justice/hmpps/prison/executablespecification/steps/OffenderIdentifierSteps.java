package uk.gov.justice.hmpps.prison.executablespecification.steps;

import net.thucydides.core.annotations.Step;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import uk.gov.justice.hmpps.prison.api.model.OffenderIdentifier;

import java.util.List;

public class OffenderIdentifierSteps extends CommonSteps {
    private List<OffenderIdentifier> actualIdentifiers;

    public void requestOffenderIdentifiers(final String type, final String value) {
        final var response = restTemplate.exchange(
                API_PREFIX + "/identifiers/{type}/{value}",
                HttpMethod.GET,
                createEntity(),
                new ParameterizedTypeReference<List<OffenderIdentifier>>() {
                },
                type, value
        );
        actualIdentifiers = response.getBody();
    }

    @Step("Verify identifier booking Ids")
    public void verifyIdentifierBookingIds(final String bookingIds) {
        verifyLongValues(actualIdentifiers, OffenderIdentifier::getBookingId, bookingIds);
    }

    @Step("Verify identifier offender Nos")
    public void verifyIdentifierOffenderNos(final String offenderNos) {
        verifyPropertyValues(actualIdentifiers, OffenderIdentifier::getOffenderNo, offenderNos);
    }

}

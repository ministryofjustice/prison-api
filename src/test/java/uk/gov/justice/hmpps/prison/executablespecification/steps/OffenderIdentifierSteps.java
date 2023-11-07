package uk.gov.justice.hmpps.prison.executablespecification.steps;

import net.serenitybdd.annotations.Step;
import uk.gov.justice.hmpps.prison.api.model.OffenderIdentifier;

import java.util.List;

public class OffenderIdentifierSteps extends CommonSteps {
    private List<OffenderIdentifier> actualIdentifiers;

    @Step("Verify identifier booking Ids")
    public void verifyIdentifierBookingIds(final String bookingIds) {
        verifyLongValues(actualIdentifiers, OffenderIdentifier::getBookingId, bookingIds);
    }

    @Step("Verify identifier offender Nos")
    public void verifyIdentifierOffenderNos(final String offenderNos) {
        verifyPropertyValues(actualIdentifiers, OffenderIdentifier::getOffenderNo, offenderNos);
    }
}

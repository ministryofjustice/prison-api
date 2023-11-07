package uk.gov.justice.hmpps.prison.executablespecification.steps;

import net.serenitybdd.annotations.Step;
import org.springframework.http.HttpMethod;
import uk.gov.justice.hmpps.prison.api.model.Contact;
import uk.gov.justice.hmpps.prison.api.model.ContactDetail;
import uk.gov.justice.hmpps.prison.test.PrisonApiClientException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ContactSteps extends CommonSteps {
    private static final String BOOKING_CONTACTS_API_URL = API_PREFIX + "bookings/{bookingId}/contacts";
    private ContactDetail details;
    private int index;

    @Step("Get offender contact details")
    public void getContacts(final Long bookingId) {
        doSingleResultApiCall(bookingId);
    }

    @Step("Verify value of next of kin field in details")
    public void verifyNextOfKinField(final String field, final String value) throws ReflectiveOperationException {
        verifyField(details.getNextOfKin().get(index), field, value);
    }

    @Step("Verify value of other contacts field in details")
    public void verifyOtherContactsField(final String field, final String value) throws ReflectiveOperationException {
        verifyField(details.getOtherContacts().get(index), field, value);
    }

    private void doSingleResultApiCall(final long bookingId) {
        init();
        try {
            final var response = restTemplate.exchange(BOOKING_CONTACTS_API_URL, HttpMethod.GET,
                    createEntity(), ContactDetail.class, bookingId);
            details = response.getBody();
        } catch (final PrisonApiClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    protected void init() {
        super.init();
        details = null;
    }

    public void verifyNoNextOfKin() {
        assertThat(details.getNextOfKin().isEmpty()).as("There is " + details.getNextOfKin().size() + " next of kin").isTrue();
    }

    public void verifyNextOfKinList(final List<Contact> expected) {
        final var expectedIterator = expected.iterator();
        final var awardsIterator = details.getNextOfKin().iterator();
        while (expectedIterator.hasNext()) {
            final var expectedThis = expectedIterator.next();
            final var actualThis = awardsIterator.next();
            assertThat(actualThis.getLastName()).isEqualTo(expectedThis.getLastName());
            assertThat(actualThis.getFirstName()).isEqualTo(expectedThis.getFirstName());
            assertEqualsBlankIsNull(expectedThis.getMiddleName(), actualThis.getMiddleName());
            assertEqualsBlankIsNull(expectedThis.getContactType(), actualThis.getContactType());
            assertEqualsBlankIsNull(expectedThis.getContactTypeDescription(), actualThis.getContactTypeDescription());
            assertEqualsBlankIsNull(expectedThis.getRelationship(), actualThis.getRelationship());
            assertEqualsBlankIsNull(expectedThis.getRelationshipDescription(), actualThis.getRelationshipDescription());
            assertThat(actualThis.isEmergencyContact()).isEqualTo(expectedThis.isEmergencyContact());
        }
        assertThat(awardsIterator.hasNext()).as("Too many actual results").isFalse();
    }
}

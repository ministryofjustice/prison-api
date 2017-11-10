package net.syscon.elite.executablespecification.steps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import net.syscon.elite.api.model.ContactDetail;
import net.syscon.elite.test.EliteClientException;
import net.thucydides.core.annotations.Step;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

public class ContactSteps extends CommonSteps {
    private static final String BOOKING_CONTACTS_API_URL = API_PREFIX + "bookings/{bookingId}/contacts";
    private ContactDetail details;
    private int index;

    @Step("Get offender contact details")
    public void getContacts(Long bookingId) {
        doSingleResultApiCall(bookingId);
    }

    @Step("Verify value of next of kin field in details")
    public void verifyNextOfKinField(String field, String value) throws ReflectiveOperationException {
        verifyField(details.getNextOfKin().get(index), field, value);
    }

    private void doSingleResultApiCall(long bookingId) {
        init();
        try {
            ResponseEntity<ContactDetail> response = restTemplate.exchange(BOOKING_CONTACTS_API_URL, HttpMethod.GET,
                    createEntity(), ContactDetail.class, bookingId);
            details = response.getBody();
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    protected void init() {
        super.init();
        details = null;
    }

    public void verifyNoNextOfKin() {
        assertTrue("There is " + details.getNextOfKin().size() + " next of kin", details.getNextOfKin().isEmpty());
    }

    public void setNextOfKinIndex(int i) {
        index = i;
    }

    public void verifyNextOfKinNumber(int n) {
        assertEquals(n, details.getNextOfKin().size());
    }
}

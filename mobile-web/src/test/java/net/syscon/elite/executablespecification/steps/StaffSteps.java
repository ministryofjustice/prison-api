package net.syscon.elite.executablespecification.steps;

import net.syscon.elite.api.model.StaffDetail;
import net.syscon.elite.test.EliteClientException;
import net.thucydides.core.annotations.Step;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BDD step implementations for Staff domain.
 */
public class StaffSteps extends CommonSteps {
    private static final String API_STAFF_DETAIL_REQUEST_URL = API_PREFIX + "/staff/{staffId}";

    private StaffDetail staffDetail;

    @Override
    protected void init() {
        super.init();

        staffDetail = null;
    }

    @Step("Find staff details")
    public void findStaffDetails(Long staffId) {
        try {
            ResponseEntity<StaffDetail> response = restTemplate.exchange(
                    API_STAFF_DETAIL_REQUEST_URL,
                            HttpMethod.GET,
                            createEntity(),
                            StaffDetail.class,
                            staffId);

            staffDetail = response.getBody();
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    @Step("Verify staff details - first name")
    public void verifyStaffFirstName(String firstName) {
        assertThat(staffDetail.getFirstName()).isEqualTo(firstName);
    }

    @Step("Verify staff details - last name")
    public void verifyStaffLastName(String lastName) {
        assertThat(staffDetail.getLastName()).isEqualTo(lastName);
    }

    @Step("Verify staff details - email")
    public void verifyStaffEmail(String email) {
        assertThat(staffDetail.getEmail()).isEqualTo(email);
    }
}

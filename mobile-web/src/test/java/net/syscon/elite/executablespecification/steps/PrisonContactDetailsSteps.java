package net.syscon.elite.executablespecification.steps;

import net.syscon.elite.api.model.PrisonContactDetails;
import net.syscon.elite.test.EliteClientException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class PrisonContactDetailsSteps extends CommonSteps {
    private static final String PRISON_CONTACT_DETAILS_LIST_URL = API_PREFIX + "agencies/prison";
    private static final String PRISON_CONTACT_DETAILS_URL = API_PREFIX + "agencies/prison/{agencyId}";

    private PrisonContactDetails details;
    private List<PrisonContactDetails> detailsList;

    public void getPrisonContactDetails() {
        doListApiCall();
    }

    public void getPrisonContactDetails(String agencyId) {
        doSingleResultApiCall(agencyId);
    }

    public void verifyAListOfPrisonContactDetailsIsReturned() {
        assertThat(detailsList).extracting("agencyId")
                .containsExactly(
                        "BXI",
                        "BMI"
                );
    }

    private void doListApiCall() {
        init();

        try {
            ResponseEntity<List<PrisonContactDetails>> response =
                    restTemplate.exchange(
                            PRISON_CONTACT_DETAILS_LIST_URL,
                            HttpMethod.GET,
                            createEntity(),
                            new ParameterizedTypeReference<List<PrisonContactDetails>>() {});

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            detailsList = response.getBody();

           // buildResourceData(response);
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

   private void doSingleResultApiCall(String agencyId) {
        init();
        try {
            String url = PRISON_CONTACT_DETAILS_URL;
            ResponseEntity<PrisonContactDetails> response = restTemplate.exchange(url, HttpMethod.GET, createEntity(),
                    PrisonContactDetails.class, agencyId);
            details = response.getBody();
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    protected void init() {
        super.init();
        details = null;
        detailsList = null;
    }

    public void verifyPrisonContactDetails() {
        assertThat(details.getAgencyId()).isEqualTo("BMI");
    }
}

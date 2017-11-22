package net.syscon.elite.executablespecification.steps;

import net.syscon.elite.api.model.PrisonerCustodyStatus;
import net.syscon.elite.test.EliteClientException;
import net.thucydides.core.annotations.Step;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BDD step implementations for Custody Status Records feature.
 */
public class CustodyStatusSteps extends CommonSteps {
    private static final String API_REQUEST_BASE_URL = API_PREFIX + "custody-statuses";
    private static final String API_REQUEST_CUSTODY_STATUS_URL = API_PREFIX + "custody-statuses/{offenderNo}";

    private List<PrisonerCustodyStatus> custodyStatuses;
    private PrisonerCustodyStatus custodyStatus;


    @Step("Retrieve all custody status records")
    public void retrieveAllCustodyStatusRecords() { doListApiCall(); }

    @Step("Verify a list of records are returned")
    public void verifyAListOfRecordsIsReturned() {
        assertThat(custodyStatuses)
                .hasOnlyElementsOfType(PrisonerCustodyStatus.class)
                .size().isGreaterThan(0);
    }

    @Step("Retrieve a specific custody status record")
    public void retrieveASpecificCustodyStatusRecord() { doSingleResultApiCall("Z0017ZZ"); }

    @Step("Verify a single record is returned")
    public void verifyASingleRecordIsReturned() {
        assertThat(custodyStatus).isNotNull();
    }

    private void doListApiCall() {
        init();

        try {
            ResponseEntity<List<PrisonerCustodyStatus>> response =
                    restTemplate.exchange(
                            API_REQUEST_BASE_URL,
                            HttpMethod.GET,
                            createEntity(),
                            new ParameterizedTypeReference<List<PrisonerCustodyStatus>>() {}
                            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            custodyStatuses = response.getBody();

            buildResourceData(response);
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    private void doSingleResultApiCall(String offenderNo) {
        init();

        try {
            ResponseEntity<PrisonerCustodyStatus> response =
                    restTemplate.exchange(
                            API_REQUEST_CUSTODY_STATUS_URL,
                            HttpMethod.GET,
                            createEntity(),
                            new ParameterizedTypeReference<PrisonerCustodyStatus>() {},
                            offenderNo);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            custodyStatus = response.getBody();
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    protected void init() {
        super.init();

        custodyStatuses = null;
        custodyStatus = null;
    }

}

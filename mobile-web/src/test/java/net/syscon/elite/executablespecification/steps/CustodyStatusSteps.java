package net.syscon.elite.executablespecification.steps;

import net.syscon.elite.api.model.PrisonerCustodyStatus;
import net.syscon.elite.api.model.RollCount;
import net.syscon.elite.test.EliteClientException;
import net.thucydides.core.annotations.Step;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

/**
 * BDD step implementations for Custody Status Records feature.
 */
public class CustodyStatusSteps extends CommonSteps {
    private static final String API_REQUEST_BASE_URL = API_PREFIX + "custody-statuses?fromDateTime=%s&movementDate=%s";
    private static final String API_REQUEST_ROLLCOUNT_URL = API_PREFIX + "movements/rollcount/{agencyId}";

    private List<PrisonerCustodyStatus> movements;
    private List<RollCount> rollCounts;

    @Override
    protected void init() {
        super.init();

        movements = null;
    }

    @Step("Retrieve all custody status records")
    public void retrieveAllCustodyStatusRecords(String fromDateTime, String movementDate) {
        doPrisonerCustodyStatusListApiCall(fromDateTime, movementDate);
    }

    @Step("Verify a list of records are returned")
    public void verifyListOfRecords() {
        verifyNoError();
        assertThat(movements).hasOnlyElementsOfType(PrisonerCustodyStatus.class).size().isEqualTo(1);
        assertThat(movements).asList()
                .extracting("offenderNo", "createDateTime", "fromAgency", "toAgency", "movementType", "directionCode")
                .contains(
                        tuple("Z0021ZZ", LocalDateTime.of(2017, Month.FEBRUARY, 21, 0, 0),
                                "LEI", "OUT", "REL", "OUT"));
    }

    @Step("Retrieve all rollcount records")
    public void retrieveRollCounts(String agencyId) {
        doRollCountListApiCall(agencyId);
    }

    @Step("Verify a list of rollcounts are returned")
    public void verifyListOfRollCounts() {
        verifyNoError();
        assertThat(rollCounts).hasOnlyElementsOfType(RollCount.class).size().isEqualTo(2);
        assertThat(rollCounts).asList()
                .extracting("livingUnitDesc")
                .contains("LEI-A", "LEI-H");
    }

    private void doPrisonerCustodyStatusListApiCall(String fromDateTime, String movementDate) {
        init();

        try {
            ResponseEntity<List<PrisonerCustodyStatus>> response = restTemplate.exchange(
                    String.format(API_REQUEST_BASE_URL, fromDateTime, movementDate),
                    HttpMethod.GET, createEntity(),
                    new ParameterizedTypeReference<List<PrisonerCustodyStatus>>() {
                    });

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            movements = response.getBody();

            buildResourceData(response);
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    private void doRollCountListApiCall(String agencyId) {
        init();

        try {
            ResponseEntity<List<RollCount>> response = restTemplate.exchange(
                    API_REQUEST_ROLLCOUNT_URL,
                    HttpMethod.GET, createEntity(),
                    new ParameterizedTypeReference<List<RollCount>>() {
                    }, agencyId);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            rollCounts = response.getBody();

            buildResourceData(response);
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }
}

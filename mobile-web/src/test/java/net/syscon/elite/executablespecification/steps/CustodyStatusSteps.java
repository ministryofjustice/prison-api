package net.syscon.elite.executablespecification.steps;

import net.syscon.elite.api.model.MovementCount;
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
    private static final String API_REQUEST_ROLLCOUNT_URL = API_PREFIX + "movements/rollcount/{agencyId}?unassigned={unassigned}";
    private static final String API_REQUEST_MOVEMENT_COUNT_URL = API_PREFIX + "movements/rollcount/{agencyId}/movements?movementDate={date}";

    private List<PrisonerCustodyStatus> movements;
    private List<RollCount> rollCounts;
    private MovementCount movementCount;

    @Override
    protected void init() {
        super.init();

        movements = null;
        rollCounts = null;
        movementCount = null;
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
        doRollCountListApiCall(agencyId, false);
    }

    @Step("Retrieve all unassigned rollcount records")
    public void retrieveUnassignedRollCounts(String agencyId) {
        doRollCountListApiCall(agencyId, true);
    }

    @Step("Verify a list of rollcounts are returned")
    public void verifyListOfRollCounts() {
        verifyNoError();
        assertThat(rollCounts).hasOnlyElementsOfType(RollCount.class).size().isEqualTo(2);
        assertThat(rollCounts).asList()
                .extracting("livingUnitDesc")
                .contains("LEI-A", "LEI-H");
    }

    @Step("Verify a list of unassigned rollcounts are returned")
    public void verifyListOfUnassignedRollCounts() {
        verifyNoError();
        assertThat(rollCounts).hasOnlyElementsOfType(RollCount.class).size().isEqualTo(1);
        assertThat(rollCounts).asList()
                .extracting("livingUnitDesc")
                .contains("LEI-CHAP");
    }

    public void retrieveMovementCounts(String agencyId, String date) {
        doMovementCountApiCall(agencyId, date);
    }

    public void verifyMovementCounts() {
        assertThat(movementCount.getIn()).isEqualTo(0);
        assertThat(movementCount.getOut()).isEqualTo(2);
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

    private void doRollCountListApiCall(String agencyId, boolean unassigned) {
        init();

        try {
            ResponseEntity<List<RollCount>> response = restTemplate.exchange(
                    API_REQUEST_ROLLCOUNT_URL,
                    HttpMethod.GET, createEntity(),
                    new ParameterizedTypeReference<List<RollCount>>() {
                    }, agencyId, unassigned);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            rollCounts = response.getBody();

            buildResourceData(response);
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    private void doMovementCountApiCall(String agencyId, String date) {
        init();

        try {
            ResponseEntity<MovementCount> response = restTemplate.exchange(
                    API_REQUEST_MOVEMENT_COUNT_URL,
                    HttpMethod.GET, createEntity(),
                    new ParameterizedTypeReference<MovementCount>() {
                    }, agencyId, date);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            movementCount = response.getBody();

        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }
}

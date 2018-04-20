package net.syscon.elite.executablespecification.steps;

import net.syscon.elite.api.model.PrisonerCustodyStatus;
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
    private static final String API_REQUEST_BASE_URL = API_PREFIX + "custody-statuses";

    private List<PrisonerCustodyStatus> movements;

    @Step("Retrieve all custody status records")
    public void retrieveAllCustodyStatusRecords() {
        doListApiCall();
    }

    @Step("Verify a list of records are returned")
    public void verifyListOfRecords() {
        assertThat(movements).hasOnlyElementsOfType(PrisonerCustodyStatus.class).size().isEqualTo(2);

        assertThat(movements).asList().extracting("offenderNo", "createDateTime").contains(
                tuple("Z0024ZZ", LocalDateTime.of(2017, Month.FEBRUARY, 24, 0, 0)),
                tuple("Z0021ZZ", LocalDateTime.of(2017, Month.FEBRUARY, 21, 0, 0)));
    }

    private void doListApiCall() {
        init();

        try {
            ResponseEntity<List<PrisonerCustodyStatus>> response = restTemplate.exchange(
                    API_REQUEST_BASE_URL + "?fromDateTime=2017-02-20T13:56:00", HttpMethod.GET, createEntity(),
                    new ParameterizedTypeReference<List<PrisonerCustodyStatus>>() {
                    });

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            movements = response.getBody();

            buildResourceData(response);
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    @Override
    protected void init() {
        super.init();

        movements = null;
    }
}

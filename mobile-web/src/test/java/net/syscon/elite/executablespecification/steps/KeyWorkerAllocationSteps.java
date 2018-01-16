package net.syscon.elite.executablespecification.steps;

import net.syscon.elite.api.model.OffenderSummary;
import net.syscon.elite.test.EliteClientException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

/**
 * BDD step implementations for Key worker allocation feature.
 */
public class KeyWorkerAllocationSteps extends CommonSteps {
    private static final String KEY_WORKER_API_URL = API_PREFIX + "key-worker/offenders/unallocated";
    private static final String KEY_WORKER_API_URL_WITH_AGENCY_PARAM = KEY_WORKER_API_URL + "?agencyId=%s";

    private List<OffenderSummary> offenderSummaryList;

    public void getUnallocatedOffendersList(String agencyId) {
        doListApiCall(agencyId);
    }

    public void verifyAListOfUnallocatedOffendersIsReturned(int count) {
        assertThat(offenderSummaryList).hasSize(count);
    }

    private void doListApiCall(String agencyId) {
        init();

        String queryUrl = StringUtils.isNotBlank(agencyId) ? String.format(KEY_WORKER_API_URL_WITH_AGENCY_PARAM, agencyId) : KEY_WORKER_API_URL;

        try {
            ResponseEntity<List<OffenderSummary>> response =
                    restTemplate.exchange(
                            queryUrl,
                            HttpMethod.GET,
                            createEntity(null, addPaginationHeaders()),
                            new ParameterizedTypeReference<List<OffenderSummary>>() {});

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            offenderSummaryList = response.getBody();

        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    protected void init() {
        super.init();
        offenderSummaryList = null;
    }


    public void verifyListIsSortedByLastNameAsc() {
        assertThat(offenderSummaryList).extracting("lastName").isSorted();
    }

}

package net.syscon.elite.executablespecification.steps;

import net.syscon.elite.api.model.KeyWorkerAllocationDetail;
import net.syscon.elite.test.EliteClientException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

/**
 * BDD step implementations for Key worker allocation feature.
 */
public class KeyWorkerAllocatedOffendersSteps extends CommonSteps {
    private static final String KEY_WORKER_API_URL = API_PREFIX + "key-worker/{agencyId}/allocations";

    private List<KeyWorkerAllocationDetail> allocationList;

    private KeyWorkerAllocationDetail allocation;

    public void getAllocations(final String agencyId, final Map<String, String> queryParam) {
        doListApiCall(agencyId, queryParam);
    }

    public void verifyAllocations(final int count) {
        assertThat(allocationList).hasSize(count);
    }

    private void doListApiCall(final String agencyId, final Map<String, String> queryParams) {
        init();

        final var query = buildQuery(queryParams);

        try {
            final var response =
                    restTemplate.exchange(
                            query,
                            HttpMethod.GET,
                            createEntity(null, addPaginationHeaders()),
                            new ParameterizedTypeReference<List<KeyWorkerAllocationDetail>>() {},
                            agencyId);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            allocationList = response.getBody();

        } catch (final EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    private String buildQuery(final Map<String, String> queryParams) {
        final var params = new StringBuilder("?");
        queryParams.forEach((key, value) -> params.append(String.format("%s=%s&", key, value)));

        return KEY_WORKER_API_URL + (queryParams.isEmpty() ? "" : params.substring(0, params.length() - 1));
    }

    protected void init() {
        super.init();
        allocationList = null;
    }


    public void verifyListIsSortedByLastnameAsc() {
        assertThat(allocationList).extracting("lastName").isSorted();
    }

    public void putARowFromListInContext(final int i) {
        allocation = allocationList.get(i);
    }

    public void verifyAllocationFirstName(final String name) {
        assertThat(allocation.getFirstName()).isEqualTo(name);
    }

    public void verifyAllocationLastName(final String name) {
        assertThat(allocation.getLastName()).isEqualTo(name);
    }

    public void verifyAllocationAgencyId(final String value) {
        assertThat(allocation.getAgencyId()).isEqualTo(value);
    }


    public void verifyAllocationInternalLocationDesc(final String value) {
        assertThat(allocation.getInternalLocationDesc()).isEqualTo(value);
    }

    public void verifyAllocationAssignedDate(final String value) {
        verifyLocalDateTime(allocation.getAssigned(), value);
    }
}

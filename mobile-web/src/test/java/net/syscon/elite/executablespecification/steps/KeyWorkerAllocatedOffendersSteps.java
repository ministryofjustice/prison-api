package net.syscon.elite.executablespecification.steps;

import net.syscon.elite.api.model.KeyWorkerAllocationDetail;
import net.syscon.elite.test.EliteClientException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

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

    public void getAllocations(String agencyId, Map<String, String> queryParam) {
        doListApiCall(agencyId, queryParam);
    }

    public void verifyAllocations(int count) {
        assertThat(allocationList).hasSize(count);
    }

    private void doListApiCall(String agencyId, Map<String, String> queryParams) {
        init();

        final String query = buildQuery(queryParams);

        try {
            ResponseEntity<List<KeyWorkerAllocationDetail>> response =
                    restTemplate.exchange(
                            query,
                            HttpMethod.GET,
                            createEntity(null, addPaginationHeaders()),
                            new ParameterizedTypeReference<List<KeyWorkerAllocationDetail>>() {},
                            agencyId);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            allocationList = response.getBody();

        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    private String buildQuery(Map<String, String> queryParams) {
        StringBuilder params = new StringBuilder("?");
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

    public void putARowFromListInContext(int i) {
        allocation = allocationList.get(i);
    }

    public void verifyAllocationFirstName(String name) {
        assertThat(allocation.getFirstName()).isEqualTo(name);
    }

    public void verifyAllocationLastName(String name) {
        assertThat(allocation.getLastName()).isEqualTo(name);
    }

    public void verifyAllocationAgencyId(String value) {
        assertThat(allocation.getAgencyId()).isEqualTo(value);
    }


    public void verifyAllocationInternalLocationDesc(String value) {
        assertThat(allocation.getInternalLocationDesc()).isEqualTo(value);
    }

    public void verifyAllocationAssignedDate(String value) {
        verifyLocalDateTime(allocation.getAssigned(), value);
    }
}

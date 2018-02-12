package net.syscon.elite.executablespecification.steps;

import net.syscon.elite.api.model.NewAllocation;
import net.syscon.elite.test.EliteClientException;
import net.thucydides.core.annotations.Step;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BDD step implementations for Key worker allocation feature.
 */
public class KeyWorkerAllocateSteps extends CommonSteps {
    private static final String KEY_WORKER_API_URL = API_PREFIX + "key-worker/allocate";
    private static final String KEY_WORKER_AUTO_ALLOCATION_URL = API_PREFIX + "key-worker/{agencyId}/allocate/start";

    private String autoAllocCount;

    @Override
    protected void init() {
        super.init();

        autoAllocCount = StringUtils.EMPTY;
    }

    public void offenderIsAllocated(Long bookingId, Long staffId, String reason, String type) {
        final NewAllocation newAllocation = NewAllocation.builder().bookingId(bookingId).staffId(staffId).reason(reason).type(type).build();
        doPostApiCall(newAllocation);
    }

    @Step("Initialise auto-allocation process")
    public void initAutoAllocation(String agencyId) {
        requestAutoAllocation(agencyId);
    }

    @Step("Verify count of auto-allocation process allocations")
    public void verifyAutoAllocationsProcessed(String expectedAutoAllocCount) {
        assertThat(autoAllocCount).isEqualTo(expectedAutoAllocCount);
    }

    public void allocationIsSuccessfullyCreated() {
       verifyNoError();
    }

    private void doPostApiCall(NewAllocation newAllocation) {
        init();

        try {
            ResponseEntity<NewAllocation> response = restTemplate.exchange(KEY_WORKER_API_URL, HttpMethod.POST,
                    createEntity(newAllocation), NewAllocation.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    private void requestAutoAllocation(String agencyId) {
        init();

        try {
            ResponseEntity<String> response =
                    restTemplate.exchange(
                            KEY_WORKER_AUTO_ALLOCATION_URL,
                            HttpMethod.POST,
                            createEntity(),
                            String.class,
                            agencyId);

            autoAllocCount = response.getBody();
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }
}
